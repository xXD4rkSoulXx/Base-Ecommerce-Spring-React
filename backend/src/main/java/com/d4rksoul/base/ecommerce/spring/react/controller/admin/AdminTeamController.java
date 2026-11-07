package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminAssignRoleRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminTeamMemberResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminTeamService;
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

    private final AdminTeamService adminTeamService;

    @GetMapping
    @PreAuthorize("hasAuthority('TEAM_READ')")
    public ResponseEntity<List<AdminTeamMemberResponse>> getTeam() {
        List<AdminTeamMemberResponse> team = adminTeamService.getTeam();

        return ResponseEntity.ok(team);
    }

    @PutMapping("/role")
    @PreAuthorize("hasAuthority('TEAM_WRITE')")
    public ResponseEntity<AdminTeamMemberResponse> assignRole(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdminAssignRoleRequest request
    ) {
        AdminTeamMemberResponse member = adminTeamService.assignRole(jwt.getSubject(), request);

        return ResponseEntity.ok(member);
    }
}
