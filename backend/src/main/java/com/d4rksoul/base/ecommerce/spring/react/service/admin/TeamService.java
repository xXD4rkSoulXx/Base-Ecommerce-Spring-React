package com.d4rksoul.base.ecommerce.spring.react.service.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AssignRoleRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.TeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.User;
import com.d4rksoul.base.ecommerce.spring.react.enums.UserRole;
import com.d4rksoul.base.ecommerce.spring.react.exception.admin.InvalidTeamChangeException;
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
public class TeamService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeam() {
        List<User> members = userRepository.findByRoleNotOrderByRoleAscEmailAsc(UserRole.CUSTOMER);

        return userMapper.toTeamMemberDtos(members);
    }

    @Transactional
    public TeamMemberResponse assignRole(String actorEmail, AssignRoleRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getEmail().equals(actorEmail)) {
            throw new InvalidTeamChangeException("You cannot change your own role");
        }

        if (user.getRole() == request.role()) {
            throw new InvalidTeamChangeException("User already has this role");
        }

        user.setRole(request.role());
        refreshTokenRepository.deleteAllByUser(user);

        return userMapper.toTeamMemberDto(user);
    }
}
