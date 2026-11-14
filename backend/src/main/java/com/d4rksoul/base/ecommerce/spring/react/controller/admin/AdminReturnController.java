package com.d4rksoul.base.ecommerce.spring.react.controller.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminReturnSearchRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminRejectReturnRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.PageResponse;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminReturnResponse;
import com.d4rksoul.base.ecommerce.spring.react.service.admin.AdminReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController {

    private final AdminReturnService adminReturnService;

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('RETURNS_WRITE')")
    public ResponseEntity<AdminReturnResponse> approve(@PathVariable Long id) {
        AdminReturnResponse response = adminReturnService.approve(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('RETURNS_WRITE')")
    public ResponseEntity<AdminReturnResponse> reject(
        @PathVariable Long id, @Valid @RequestBody AdminRejectReturnRequest request
    ) {
        AdminReturnResponse response = adminReturnService.reject(id, request.reason());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('RETURNS_WRITE')")
    public ResponseEntity<AdminReturnResponse> receive(@PathVariable Long id) {
        AdminReturnResponse response = adminReturnService.receive(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('RETURNS_WRITE')")
    public ResponseEntity<AdminReturnResponse> refund(@PathVariable Long id) {
        AdminReturnResponse response = adminReturnService.refund(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RETURNS_READ')")
    public ResponseEntity<PageResponse<AdminReturnResponse>> getReturns(
        @ModelAttribute AdminReturnSearchRequest filter
    ) {
        PageResponse<AdminReturnResponse> returns = adminReturnService.getReturns(filter);

        return ResponseEntity.ok(returns);
    }
}
