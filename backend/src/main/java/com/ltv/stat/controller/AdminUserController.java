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
            dto.setVisibleUserIds(userService.getUserViewPermissionTargetIds(u.getId()));
            dto.setIsMaster(u.getIsMaster());
            dto.setSubUserIds(userService.getSubUserIdsForMaster(u.getId()));
            dto.setPermPredictPayback(u.hasPermPredictPayback() ? 1 : 0);
            dto.setPermRoiPredict(u.hasPermRoiPredict() ? 1 : 0);
            dto.setPermGlobalDistribution(u.hasPermGlobalDistribution() ? 1 : 0);
            dto.setPermExport(u.hasPermExport() ? 1 : 0);
            result.add(dto);
        }

        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @PutMapping("/{id}/master-status")
    public ResponseEntity<?> updateMasterStatus(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作，仅超级管理员可修改主账号属性"));
        }
        try {
            Object isMasterObj = body != null ? body.get("isMaster") : 0;
            Integer isMaster = 0;
            if (isMasterObj != null) {
                if (isMasterObj instanceof Boolean) {
                    isMaster = (Boolean) isMasterObj ? 1 : 0;
                } else {
                    isMaster = Integer.valueOf(isMasterObj.toString().trim());
                }
            }
            userService.updateMasterStatus(id, isMaster);

            ltvStatService.calculateLtvStatsForUser(id);
            dailyRechargeStatService.calculateDailyDistributionStatsForUser(id);

            return ResponseEntity.ok(ApiResponseDto.success("账号类型更新成功！", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "更新失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/sub-accounts")
    public ResponseEntity<?> getSubAccounts(@PathVariable("id") Long id) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作"));
        }
        List<Long> subUserIds = userService.getSubUserIdsForMaster(id);
        return ResponseEntity.ok(ApiResponseDto.success(subUserIds));
    }

    @PutMapping("/{id}/sub-accounts")
    public ResponseEntity<?> updateSubAccounts(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作，仅超级管理员可分配子账号"));
        }
        try {
            List<?> rawList = (body != null && body.get("subUserIds") instanceof List) ? (List<?>) body.get("subUserIds") : Collections.emptyList();
            List<Long> subUserIds = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj != null) {
                    subUserIds.add(Long.valueOf(obj.toString().trim()));
                }
            }
            userService.updateMasterSubAccounts(id, subUserIds);

            // 触发主账号数据重算
            ltvStatService.calculateLtvStatsForUser(id);
            dailyRechargeStatService.calculateDailyDistributionStatsForUser(id);

            return ResponseEntity.ok(ApiResponseDto.success("子账号关联分配成功，主账号汇总数据已秒级重算！", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "保存子账号关联失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/view-permissions")
    public ResponseEntity<?> getViewPermissions(@PathVariable("id") Long id) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作，仅超级管理员可配置视图分配权限"));
        }
        List<Long> targetUserIds = userService.getUserViewPermissionTargetIds(id);
        return ResponseEntity.ok(ApiResponseDto.success(targetUserIds));
    }

    @PutMapping("/{id}/view-permissions")
    public ResponseEntity<?> updateViewPermissions(@PathVariable("id") Long id, @RequestBody UserViewPermissionUpdateRequestDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作，仅超级管理员可分配只读视图权限"));
        }
        try {
            List<Long> targetUserIds = body != null ? body.getTargetUserIds() : Collections.emptyList();
            userService.updateUserViewPermissions(id, targetUserIds);
            return ResponseEntity.ok(ApiResponseDto.success("只读视图权限分配保存成功！", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "更新视图权限失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<?> updatePermissions(@PathVariable("id") Long id, @RequestBody UserPermissionsUpdateRequestDto body) {
        if (!checkSuperAdmin()) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(403, "无权操作，仅超级管理员可分配功能权限"));
        }
        try {
            Integer permPredictPayback = body != null ? body.getPermPredictPayback() : 0;
            Integer permRoiPredict = body != null ? body.getPermRoiPredict() : 0;
            Integer permGlobalDistribution = body != null ? body.getPermGlobalDistribution() : 0;
            Integer permExport = body != null ? body.getPermExport() : 0;

            userService.updateUserPermissions(id, permPredictPayback, permRoiPredict, permGlobalDistribution, permExport);
            return ResponseEntity.ok(ApiResponseDto.success("功能权限分配保存成功！", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.error(500, "更新功能权限失败: " + e.getMessage()));
        }
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
            Integer isMaster = body != null ? body.getIsMaster() : 0;
            List<Long> visibleUserIds = body != null ? body.getVisibleUserIds() : null;
            List<Long> subUserIds = body != null ? body.getSubUserIds() : null;
            Integer permPredictPayback = body != null ? body.getPermPredictPayback() : 0;
            Integer permRoiPredict = body != null ? body.getPermRoiPredict() : 0;
            Integer permGlobalDistribution = body != null ? body.getPermGlobalDistribution() : 0;
            Integer permExport = body != null ? body.getPermExport() : 0;

            userService.createUser(
                    username.trim(),
                    password.trim(),
                    role,
                    isMaster,
                    visibleUserIds,
                    subUserIds,
                    permPredictPayback,
                    permRoiPredict,
                    permGlobalDistribution,
                    permExport
            );
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
