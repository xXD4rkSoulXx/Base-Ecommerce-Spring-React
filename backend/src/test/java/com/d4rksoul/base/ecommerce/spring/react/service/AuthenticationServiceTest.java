package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.ChangePasswordRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.LoginRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.RegisterRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.LoginResult;
import com.d4rksoul.base.ecommerce.spring.react.dto.result.RotateRefreshTokenResult;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.EmailAlreadyExistsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidCredentialsException;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidPasswordChangeException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.AuthenticationMapper;
import com.d4rksoul.base.ecommerce.spring.react.mapper.UserMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String DUMMY_HASH = "dummy-hash";

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Spy
    private AuthenticationMapper authenticationMapper = new AuthenticationMapper();
    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private AuthenticationService service;

    private final User user = User.builder()
        .id(1L)
        .email("ana@mail.com")
        .password("stored-hash")
        .firstName("Ana")
        .lastName("Silva")
        .role(UserRole.CUSTOMER)
        .build();

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn(DUMMY_HASH);

        service.init();
    }

    @Test
    @DisplayName("register: rejects an email that is already registered")
    void registerRejectsEmailAlreadyRegistered() {
        RegisterRequest request = registerRequest();

        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
            .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register: saves a customer with a normalized email and an encoded password")
    void registerSavesCustomerWithEncodedPassword() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ngPass1")).thenReturn("encoded-password");

        service.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("ana@mail.com");
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getFirstName()).isEqualTo("Ana");
        assertThat(saved.getLastName()).isEqualTo("Silva");
        assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("login: rejects an unknown email but still compares against a dummy hash")
    void loginRejectsUnknownEmail() {
        LoginRequest request = new LoginRequest("ana@mail.com", "Secret1");

        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password");

        verify(passwordEncoder).matches("Secret1", DUMMY_HASH);
        verifyNoInteractions(tokenService, refreshTokenService);
    }

    @Test
    @DisplayName("login: rejects a wrong password with the same message as an unknown email")
    void loginRejectsWrongPassword() {
        LoginRequest request = new LoginRequest("ana@mail.com", "Wrong1");

        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Wrong1", "stored-hash"))
            .thenReturn(false);

        assertThatThrownBy(() -> service.login(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password");

        verifyNoInteractions(tokenService, refreshTokenService);
    }

    @Test
    @DisplayName("login: returns the tokens and stores the refresh token")
    void loginReturnsTokensAndStoresRefreshToken() {
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret1", "stored-hash"))
            .thenReturn(true);
        when(tokenService.generateAccessToken(user)).thenReturn("access-token");
        when(tokenService.generateRefreshToken()).thenReturn("refresh-token");

        LoginResult result = service.login(new LoginRequest("ana@mail.com", "Secret1"));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().email()).isEqualTo("ana@mail.com");
        assertThat(result.user().role()).isEqualTo(UserRole.CUSTOMER);

        verify(refreshTokenService).create(user, "refresh-token");
    }

    @Test
    @DisplayName("refresh: returns a new access token and the rotated refresh token")
    void refreshReturnsNewTokens() {
        when(refreshTokenService.rotate("old-token"))
            .thenReturn(new RotateRefreshTokenResult(user, "new-token"));
        when(tokenService.generateAccessToken(user)).thenReturn("access-token");

        LoginResult result = service.refresh("old-token");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("new-token");
        assertThat(result.user().email()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("change password: rejects a wrong current password")
    void changePasswordRejectsWrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(
            "Wrong1", "NewPass1"
        );

        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("Wrong1", "stored-hash"))
            .thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(request))
            .isInstanceOf(InvalidPasswordChangeException.class)
            .hasMessageContaining("incorrect");
        assertThat(user.getPassword()).isEqualTo("stored-hash");

        verify(refreshTokenRepository, never()).deleteAllByUser(any(User.class));
    }

    @Test
    @DisplayName("change password: rejects a new password equal to the current one")
    void changePasswordRejectsSamePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(
            "SamePass1", "SamePass1"
        );

        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("SamePass1", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(request))
            .isInstanceOf(InvalidPasswordChangeException.class)
            .hasMessageContaining("different");
        assertThat(user.getPassword()).isEqualTo("stored-hash");

        verify(refreshTokenRepository, never()).deleteAllByUser(any(User.class));
    }

    @Test
    @DisplayName("change password: stores the new hash and logs out every session")
    void changePasswordUpdatesPasswordAndRevokesSessions() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("OldPass1", "stored-hash"))
            .thenReturn(true);
        when(passwordEncoder.matches("NewPass1", "stored-hash"))
            .thenReturn(false);
        when(passwordEncoder.encode("NewPass1")).thenReturn("new-hash");

        service.changePassword(new ChangePasswordRequest("OldPass1", "NewPass1"));

        assertThat(user.getPassword()).isEqualTo("new-hash");

        verify(refreshTokenRepository).deleteAllByUser(user);
    }

    @Test
    @DisplayName("logout: deletes the given refresh token")
    void logoutDeletesRefreshToken() {
        service.logout("raw-token");

        verify(refreshTokenService).delete("raw-token");
    }

    @Test
    @DisplayName("logout all: deletes every refresh token of the current user")
    void logoutAllDeletesEveryRefreshTokenOfTheUser() {
        when(userService.getCurrentUser()).thenReturn(user);

        service.logoutAllByUser();

        verify(refreshTokenRepository).deleteAllByUser(user);
    }

    private static RegisterRequest registerRequest() {
        return new RegisterRequest(
            " Ana@Mail.COM ", "Str0ngPass1", " Ana ", " Silva "
        );
    }
}
