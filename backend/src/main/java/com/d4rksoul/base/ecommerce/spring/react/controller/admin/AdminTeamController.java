package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AssignRoleRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.TeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/team")
@RequiredArgsConstructor
public class AdminTeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAuthority('TEAM_READ')")
    public ResponseEntity<List<TeamMemberResponse>> getTeam() {
        List<TeamMemberResponse> team = teamService.getTeam();

        return ResponseEntity.ok(team);
    }

    @PutMapping("/role")
    @PreAuthorize("hasAuthority('TEAM_WRITE')")
    public ResponseEntity<TeamMemberResponse> assignRole(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        TeamMemberResponse member = teamService.assignRole(jwt.getSubject(), request);

        return ResponseEntity.ok(member);
    }
}
