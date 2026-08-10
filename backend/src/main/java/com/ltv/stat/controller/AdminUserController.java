package com.ltv.stat.controller;

import com.ltv.stat.dto.*;
import com.ltv.stat.entity.SysUser;
import com.ltv.stat.service.DailyRechargeStatService;
import com.ltv.stat.service.LtvStatService;
import com.ltv.stat.service.UserService;
import com.ltv.stat.util.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final LtvStatService ltvStatService;
    private final DailyRechargeStatService dailyRechargeStatService;

    public AdminUserController(UserService userService, LtvStatService ltvStatService, DailyRechargeStatService dailyRechargeStatService) {
        this.userService = userService;
        this.ltvStatService = ltvStatService;
        this.dailyRechargeStatService = dailyRechargeStatService;
    }

    private boolean checkSuperAdmin() {
        TokenInfo currentUser = UserContext.getCurrentUser();
        return currentUser != null && currentUser.isSuperAdmin();
    }

    @GetMapping
    public ResponseEntity<?> listUsers() {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权访问，仅超级管理员可管理用户"));
        }

        List<SysUser> users = userService.listAllUsers();
        List<UserInfoDto> result = new ArrayList<>();
        for (SysUser u : users) {
            UserInfoDto dto = new UserInfoDto();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setRole(u.getRole());
            dto.setStatus(u.getStatus());
            dto.setCreatedAt(u.getCreatedAt());

            List<String> pageIds = userService.getUserLandingPageIds(u.getId());
            dto.setLandingPageIds(pageIds);
            dto.setLandingPageCount(pageIds.size());
            result.add(dto);
        }

        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequestDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作"));
        }

        String username = body != null ? body.getUsername() : null;
        String password = body != null ? body.getPassword() : null;
        String role = (body != null && body.getRole() != null) ? body.getRole() : "USER";

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, "用户名和密码不能为空"));
        }

        try {
            userService.createUser(username.trim(), password.trim(), role);
            return ResponseEntity.ok(ApiResponseDto.success("创建成功", null));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, ie.getMessage()));
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable("id") Long id, @RequestBody ResetPasswordRequestDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作"));
        }

        String newPassword = body != null ? body.getNewPassword() : null;
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, "新密码不能为空"));
        }

        try {
            userService.resetPassword(id, newPassword.trim());
            return ResponseEntity.ok(ApiResponseDto.success("密码重置成功", null));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, ie.getMessage()));
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable("id") Long id, @RequestBody UpdateRoleRequestDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作"));
        }

        String newRole = body != null ? body.getRole() : null;
        if (newRole == null || newRole.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, "角色不能为空"));
        }

        try {
            userService.updateUserRole(id, newRole.trim());
            return ResponseEntity.ok(ApiResponseDto.success("角色更新成功", null));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, ie.getMessage()));
        }
    }

    @PutMapping("/{id}/landing-pages")
    public ResponseEntity<?> updateLandingPages(@PathVariable("id") Long id, @RequestBody UserLandingPageUpdateRequestDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作"));
        }

        try {
            if (body != null && body.getLandingPages() != null) {
                userService.updateUserLandingPageConfigs(id, body.getLandingPages());
            } else if (body != null && body.getLandingPageIds() != null) {
                userService.updateUserLandingPageIds(id, body.getLandingPageIds());
            }

            // 重算该用户的统计
            ltvStatService.calculateLtvStatsForUser(id);
            dailyRechargeStatService.calculateDailyDistributionStatsForUser(id);

            return ResponseEntity.ok(ApiResponseDto.success("落地页配置保存成功，报表已同步计算完成！", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "更新落地页失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作"));
        }

        TokenInfo currentUser = UserContext.getCurrentUser();
        if (currentUser != null && currentUser.getUserId().equals(id)) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(400, "不能删除当前登录的管理员账号"));
        }

        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDto.success("删除用户成功", null));
    }
}
