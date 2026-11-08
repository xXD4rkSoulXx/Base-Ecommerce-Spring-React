package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminAssignRoleRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminTeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditAction;
import com.d4rksoul.base.ecommerce.spring.react.enums.AuditEntityType;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.InvalidTeamChangeException;
import com.d4rksoul.base.ecommerce.spring.react.exception.UserNotFoundException;
import com.d4rksoul.base.ecommerce.spring.react.mapper.UserMapper;
import com.d4rksoul.base.ecommerce.spring.react.repository.RefreshTokenRepository;
import com.d4rksoul.base.ecommerce.spring.react.repository.UserRepository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTeamService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminAuditLogService adminAuditLogService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<AdminTeamMemberResponse> getTeam() {
        List<User> members = userRepository.findByRoleNotOrderByRoleAscEmailAsc(UserRole.CUSTOMER);

        return userMapper.toTeamMemberDtos(members);
    }

    @Transactional
    public AdminTeamMemberResponse assignRole(String actorEmail, AdminAssignRoleRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getEmail().equals(actorEmail)) {
            throw new InvalidTeamChangeException("You cannot change your own role");
        }

        if (user.getRole() == request.role()) {
            throw new InvalidTeamChangeException("User already has this role");
        }

        UserRole previousRole = user.getRole();

        user.setRole(request.role());
        refreshTokenRepository.deleteAllByUser(user);

        adminAuditLogService.register(
                AuditAction.ROLE_CHANGED,
                AuditEntityType.TEAM_MEMBER,
                user.getId(),
                user.getEmail(),
                previousRole + " → " + request.role()
        );

        return userMapper.toTeamMemberDto(user);
    }
}
