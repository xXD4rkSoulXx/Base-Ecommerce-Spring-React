package com.d4rksoul.base.ecommerce.spring.react.controller;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.LoginResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidRefreshTokenException;
import com.d4rksoul.base.ecommerce.spring.react.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest extends ControllerSecurityTestBase {

    private static final String XHR_HEADER = "X-Requested-With";
    private static final String XHR_VALUE = "XMLHttpRequest";
    private static final String REFRESH_COOKIE = "refreshToken";

    private static final String VALID_REGISTER = """
        {"email":"  User@Example.com ","password":"Password1","firstName":"Ana","lastName":"Silva"}
    """;

    @Autowired
    private AuthenticationService authenticationService;

    private static LoginResult loginResult() {
        UserResponse user = UserResponse.builder()
            .email("user@example.com")
            .firstName("Ana")
            .lastName("Silva")
            .role(UserRole.CUSTOMER)
            .build();

        return LoginResult.builder()
            .user(user)
            .accessToken("access-token")
            .refreshToken("new-refresh-token")
            .build();
    }

    private static String setCookieOf(MvcResult result) {
        String cookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertThat(cookie).isNotNull();

        return cookie;
    }

    private static void assertSessionCleared(String setCookie) {
        assertThat(setCookie)
            .startsWith(REFRESH_COOKIE + "=;")
            .contains("Max-Age=0")
            .contains("Path=/api/auth")
            .contains("HttpOnly");
    }


    @Test
    @DisplayName("register: a valid request answers 201 and does not need a csrf token")
    void registerAnswers201() throws Exception {
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REGISTER)
        ).andExpect(status().isCreated());

        verify(authenticationService).register(any());
    }

    @ParameterizedTest(name = "register: password \"{0}\" -> 400 \"{1}\"")
    @CsvSource(delimiter = '|', value = {
        "Ab1|Password must be between 8 and 72 characters long.",
        "abcdefgh1|Password must contain uppercase, lowercase and number.",
        "ABCDEFGH1|Password must contain uppercase, lowercase and number.",
        "Abcdefgh|Password must contain uppercase, lowercase and number."
    })
    void registerRejectsWeakPassword(String password, String message) throws Exception {
        String body = """
            {"email":"user@example.com","password":"%s","firstName":"Ana","lastName":"Silva"}
            """.formatted(password);

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(message));

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("register: an invalid email answers 400")
    void registerRejectsInvalidEmail() throws Exception {
        String body = """
            {"email":"not-an-email","password":"Password1","firstName":"Ana","lastName":"Silva"}
        """;

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid email format"));
    }

    @Test
    @DisplayName("register: a missing first name answers 400")
    void registerRejectsMissingFirstName() throws Exception {
        String body = """
            {"email":"user@example.com","password":"Password1","lastName":"Silva"}
        """;

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("First name is required"));
    }

    @Test
    @DisplayName("register: an empty body answers 400 and never reaches the service")
    void registerRejectsEmptyBody() throws Exception {
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        ).andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("login: answers the access token in the body and the refresh token in an HttpOnly cookie")
    void loginSetsRefreshCookie() throws Exception {
        given(authenticationService.login(any(LoginRequest.class))).willReturn(loginResult());

        String body = """
            {"email":"user@example.com","password":"Password1"}
        """;

        MvcResult result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.user.email").value("user@example.com"))
        .andExpect(jsonPath("$.refreshToken").doesNotExist())
        .andReturn();

        assertThat(setCookieOf(result))
            .startsWith(REFRESH_COOKIE + "=new-refresh-token;")
            .contains("HttpOnly")
            .contains("Path=/api/auth")
            .contains("Max-Age=2592000")
            .contains("SameSite=Lax")
            .doesNotContain("Secure");
    }

    @Test
    @DisplayName("login: a missing password answers 400")
    void loginRejectsMissingPassword() throws Exception {
        String body = """
            {"email":"user@example.com"}
        """;

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Password is required"));

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("refresh: without the X-Requested-With header the request is forbidden")
    void refreshRequiresXhrHeader() throws Exception {
        mockMvc.perform(
            post("/api/auth/refresh")
                .servletPath("/api/auth/refresh")
                .cookie(new Cookie(REFRESH_COOKIE, "old-token"))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("refresh: without the cookie it answers 401 \"No active session\"")
    void refreshWithoutCookieAnswers401() throws Exception {
        mockMvc.perform(
            post("/api/auth/refresh")
                .header(XHR_HEADER, XHR_VALUE)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("No active session"));

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("refresh: with the cookie it rotates the token and sets the new cookie")
    void refreshRotatesCookie() throws Exception {
        given(authenticationService.refresh("old-token")).willReturn(loginResult());

        MvcResult result = mockMvc.perform(
            post("/api/auth/refresh")
                .header(XHR_HEADER, XHR_VALUE)
                .cookie(new Cookie(REFRESH_COOKIE, "old-token"))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andReturn();

        assertThat(setCookieOf(result)).startsWith(REFRESH_COOKIE + "=new-refresh-token;");
    }

    @Test
    @DisplayName("refresh: an invalid token answers 401 with the exception message")
    void refreshWithInvalidTokenAnswers401() throws Exception {
        given(authenticationService.refresh("bad-token"))
            .willThrow(new InvalidRefreshTokenException("Invalid refresh token"));

        mockMvc.perform(
            post("/api/auth/refresh")
                .header(XHR_HEADER, XHR_VALUE)
                .cookie(new Cookie(REFRESH_COOKIE, "bad-token"))
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    @DisplayName("logout: with the cookie it revokes the token and clears the cookie")
    void logoutRevokesAndClearsCookie() throws Exception {
        MvcResult result = mockMvc.perform(
            delete("/api/auth/logout")
                .with(csrf())
                .cookie(new Cookie(REFRESH_COOKIE, "old-token"))
        )
        .andExpect(status().isOk())
        .andReturn();

        verify(authenticationService).logout("old-token");

        assertSessionCleared(setCookieOf(result));
    }

    @Test
    @DisplayName("logout: without the cookie it still clears the cookie and does not call the service")
    void logoutWithoutCookieStillClears() throws Exception {
        MvcResult result = mockMvc.perform(
            delete("/api/auth/logout")
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andReturn();

        verify(authenticationService, never()).logout(any());

        assertSessionCleared(setCookieOf(result));
    }

    @Test
    @DisplayName("logout: without a csrf token the request is forbidden")
    void logoutRequiresCsrf() throws Exception {
        mockMvc.perform(
            delete("/api/auth/logout")
                .cookie(new Cookie(REFRESH_COOKIE, "old-token"))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("change password: success clears the refresh cookie")
    void changePasswordClearsCookie() throws Exception {
        String body = """
            {"currentPassword":"OldPassword1","newPassword":"NewPassword1"}
        """;

        MvcResult result = mockMvc.perform(
            put("/api/auth/password")
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isOk())
        .andReturn();

        verify(authenticationService).changePassword(any());

        assertSessionCleared(setCookieOf(result));
    }

    @Test
    @DisplayName("change password: a weak new password answers 400")
    void changePasswordRejectsWeakPassword() throws Exception {
        String body = """
            {"currentPassword":"OldPassword1","newPassword":"abcdefgh1"}
        """;

        mockMvc.perform(
            put("/api/auth/password")
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message")
                .value("Password must contain uppercase, lowercase and number.")
        );

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("change password: a blank current password answers 400")
    void changePasswordRejectsBlankCurrentPassword() throws Exception {
        String body = """
            {"currentPassword":"","newPassword":"NewPassword1"}
        """;

        mockMvc.perform(
            put("/api/auth/password")
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Current password is required"));
    }

    @Test
    @DisplayName("change password: without a token it answers 401")
    void changePasswordRequiresAuthentication() throws Exception {
        String body = """
            {"currentPassword":"OldPassword1","newPassword":"NewPassword1"}
        """;

        mockMvc.perform(
            put("/api/auth/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("logout all: with a token it revokes every session and clears the cookie")
    void logoutAllClearsCookie() throws Exception {
        MvcResult result = mockMvc.perform(
            delete("/api/auth/logout-all")
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, bearer())
        )
        .andExpect(status().isOk())
        .andReturn();

        verify(authenticationService).logoutAllByUser();

        assertSessionCleared(setCookieOf(result));
    }

    @Test
    @DisplayName("logout all: without a token it answers 401")
    void logoutAllRequiresAuthentication() throws Exception {
        mockMvc.perform(
            delete("/api/auth/logout-all")
                .with(csrf())
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("prod profile: the refresh cookie is Secure and SameSite=None")
    void prodProfileCookieIsSecureAndSameSiteNone() {
        MockEnvironment prodEnvironment = new MockEnvironment();
        prodEnvironment.setActiveProfiles("prod");

        AuthenticationController controller =
            new AuthenticationController(prodEnvironment, authenticationService);

        given(authenticationService.login(any(LoginRequest.class))).willReturn(loginResult());

        ResponseEntity<LoginResponse> response =
            controller.login(new LoginRequest("user@example.com", "Password1"));

        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
            .contains("Secure")
            .contains("SameSite=None")
            .contains("HttpOnly");
    }
}
