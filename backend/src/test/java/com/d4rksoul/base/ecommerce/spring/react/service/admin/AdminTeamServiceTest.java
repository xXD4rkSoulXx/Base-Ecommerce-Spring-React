package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminAssignRoleRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminTeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidTeamChangeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.admin.AdminTeamMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;
import com.d4rksoul.base.ecommerce.spring.react.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTeamServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private AdminAuditLogService adminAuditLogService;
    @Mock
    private AdminTeamMapper adminTeamMapper;

    @InjectMocks
    private AdminTeamService service;

    private final User admin = User.builder()
        .id(1L)
        .email("admin@mail.com")
        .role(UserRole.SUPPORT)
        .build();

    private final User member = User.builder()
        .id(2L)
        .email("ana@mail.com")
        .role(UserRole.CUSTOMER)
        .build();

    @Test
    @DisplayName("get team: lists the staff members, without customers")
    void getTeamListsStaffWithoutCustomers() {
        User first = User.builder()
            .id(3L)
            .role(UserRole.FULFILLMENT)
            .build();

        User second = User.builder()
            .id(4L)
            .role(UserRole.SUPPORT)
            .build();

        AdminTeamMemberResponse firstResponse = mock(AdminTeamMemberResponse.class);
        AdminTeamMemberResponse secondResponse = mock(AdminTeamMemberResponse.class);

        when(userRepository.findByRoleNotOrderByRoleAscEmailAsc(UserRole.CUSTOMER))
            .thenReturn(List.of(first, second));
        when(adminTeamMapper.toDto(first)).thenReturn(firstResponse);
        when(adminTeamMapper.toDto(second)).thenReturn(secondResponse);

        List<AdminTeamMemberResponse> team = service.getTeam();

        assertThat(team).containsExactly(firstResponse, secondResponse);
    }

    @Test
    @DisplayName("assign role: changes the role, logs the user out and registers it in the audit log")
    void assignRoleChangesRoleAndRevokesSessions() {
        AdminAssignRoleRequest request =
            new AdminAssignRoleRequest(" Ana@Mail.COM ", UserRole.SUPPORT);

        when(userService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(member));

        service.assignRole(request);

        assertThat(member.getRole()).isEqualTo(UserRole.SUPPORT);

        verify(refreshTokenRepository).deleteAllByUser(member);
        verify(adminAuditLogService).register(
            AuditAction.ROLE_CHANGED,
            AuditEntityType.TEAM_MEMBER,
            2L,
            "ana@mail.com",
            "CUSTOMER → SUPPORT"
        );
    }

    @Test
    @DisplayName("assign role: rejects an email that does not belong to any user")
    void assignRoleRejectsUnknownUser() {
        AdminAssignRoleRequest request =
            new AdminAssignRoleRequest("nobody@mail.com", UserRole.SUPPORT);

        when(userService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findByEmail("nobody@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(request))
            .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(refreshTokenRepository, adminAuditLogService);
    }

    @Test
    @DisplayName("assign role: nobody can change their own role")
    void assignRoleRejectsChangingOwnRole() {
        AdminAssignRoleRequest request =
            new AdminAssignRoleRequest("admin@mail.com", UserRole.FULFILLMENT);

        when(userService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.assignRole(request))
            .isInstanceOf(InvalidTeamChangeException.class)
            .hasMessageContaining("own role");
        assertThat(admin.getRole()).isEqualTo(UserRole.SUPPORT);

        verify(refreshTokenRepository, never()).deleteAllByUser(any(User.class));
    }

    @Test
    @DisplayName("assign role: rejects giving a user the role they already have")
    void assignRoleRejectsSameRole() {
        AdminAssignRoleRequest request =
            new AdminAssignRoleRequest("ana@mail.com", UserRole.CUSTOMER);

        when(userService.getCurrentUser()).thenReturn(admin);
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.assignRole(request))
            .isInstanceOf(InvalidTeamChangeException.class)
            .hasMessageContaining("already has this role");

        verify(refreshTokenRepository, never()).deleteAllByUser(any(User.class));
        verify(adminAuditLogService, never()).register(any(), any(), any(), any(), any());
    }
}
