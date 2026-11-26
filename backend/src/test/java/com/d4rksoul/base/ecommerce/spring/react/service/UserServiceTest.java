package com.d4rksoul.base.ecommerce.spring.react.service;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.UpdateProfileRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.ProfileResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.UserResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.OrderStatus;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotAuthenticatedException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.UserMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.OrderRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "ana@example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService service;

    private final User user = User.builder()
        .id(1L)
        .email(EMAIL)
        .firstName("Old")
        .lastName("Name")
        .build();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("current user: rejects a request with no authentication")
    void currentUserRejectsMissingAuthentication() {
        assertThatThrownBy(() -> service.getCurrentUser())
            .isInstanceOf(UserNotAuthenticatedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("current user: rejects an authentication that is not a JWT")
    void currentUserRejectsPrincipalThatIsNotAJwt() {
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken("anonymous", "none")
        );

        assertThatThrownBy(() -> service.getCurrentUser())
            .isInstanceOf(UserNotAuthenticatedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("current user: rejects a valid token whose user no longer exists")
    void currentUserRejectsUnknownUser() {
        authenticateAsUser();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser())
            .isInstanceOf(UserNotAuthenticatedException.class);
    }

    @Test
    @DisplayName("current user: finds the user by the email in the token subject")
    void currentUserIsFoundByTokenSubject() {
        authenticateAsUser();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User result = service.getCurrentUser();

        assertThat(result).isSameAs(user);
    }

    @Test
    @DisplayName("profile: includes the number of orders in the visible statuses")
    void profileCountsVisibleOrders() {
        ProfileResponse response = mock(ProfileResponse.class);

        authenticateAsUser();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(orderRepository.countByUserIdAndStatusIn(1L, OrderStatus.VISIBLE_STATUSES))
            .thenReturn(4L);
        when(userMapper.toProfileDto(user, 4L)).thenReturn(response);

        ProfileResponse result = service.getProfile();

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("update profile: changes the first and last name of the current user")
    void updateProfileChangesNames() {
        UserResponse response = mock(UserResponse.class);

        authenticateAsUser();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(response);

        UserResponse result = service.updateProfile(new UpdateProfileRequest("  Ana ", " Silva  "));

        assertThat(result).isSameAs(response);
        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(user.getLastName()).isEqualTo("Silva");
    }

    private void authenticateAsUser() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject(EMAIL).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
