package com.ltv.stat.service;

import com.ltv.stat.dto.TokenInfo;
import com.ltv.stat.dto.VisibleAccountDto;
import com.ltv.stat.entity.SysUser;
import com.ltv.stat.entity.UserLandingPage;
import com.ltv.stat.entity.UserViewPermission;
import com.ltv.stat.repository.SysUserRepository;
import com.ltv.stat.repository.UserLandingPageRepository;
import com.ltv.stat.repository.UserViewPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final SysUserRepository sysUserRepository;
    private final UserLandingPageRepository userLandingPageRepository;
    private final UserViewPermissionRepository userViewPermissionRepository;
    private final com.ltv.stat.repository.UserSubAccountRepository userSubAccountRepository;

    @Value("${app.auth.username:superadmin}")
    private String defaultSuperAdminUsername;

    @Value("${app.auth.password:superadmin}")
    private String defaultSuperAdminPassword;

    public UserService(SysUserRepository sysUserRepository,
                       UserLandingPageRepository userLandingPageRepository,
                       UserViewPermissionRepository userViewPermissionRepository,
                       com.ltv.stat.repository.UserSubAccountRepository userSubAccountRepository) {
        this.sysUserRepository = sysUserRepository;
        this.userLandingPageRepository = userLandingPageRepository;
        this.userViewPermissionRepository = userViewPermissionRepository;
        this.userSubAccountRepository = userSubAccountRepository;
    }

    @PostConstruct
    @Transactional
    public void initDefaultUsers() {
        // 1. 初始化或升级默认超级管理员 superadmin (SUPER_ADMIN)
        Optional<SysUser> superAdminOpt = sysUserRepository.findByUsername(defaultSuperAdminUsername);
        if (!superAdminOpt.isPresent()) {
            SysUser superAdmin = new SysUser();
            superAdmin.setUsername(defaultSuperAdminUsername);
            superAdmin.setPasswordHash(hashPassword(defaultSuperAdminPassword));
            superAdmin.setRole("SUPER_ADMIN");
            superAdmin.setStatus(1);
            sysUserRepository.save(superAdmin);
            log.info("Initialized default super admin user: {}", defaultSuperAdminUsername);
        } else {
            SysUser superAdmin = superAdminOpt.get();
            if (!"SUPER_ADMIN".equalsIgnoreCase(superAdmin.getRole())) {
                superAdmin.setRole("SUPER_ADMIN");
                sysUserRepository.save(superAdmin);
                log.info("Ensured super admin user role: {}", defaultSuperAdminUsername);
            }
        }

        // 2. 确保 admin 账号角色归位为普通管理员 ADMIN
        Optional<SysUser> adminOpt = sysUserRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            SysUser admin = adminOpt.get();
            if ("SUPER_ADMIN".equalsIgnoreCase(admin.getRole())) {
                admin.setRole("ADMIN");
                sysUserRepository.save(admin);
                log.info("Reset admin user role back to ADMIN");
            }
        } else {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPasswordHash(hashPassword("admin666"));
            admin.setRole("ADMIN");
            admin.setStatus(1);
            sysUserRepository.save(admin);
            log.info("Initialized default admin user: admin");
        }
    }

    public Optional<SysUser> findByUsername(String username) {
        return sysUserRepository.findByUsername(username);
    }

    public Optional<SysUser> findById(Long id) {
        return sysUserRepository.findById(id);
    }

    public List<SysUser> listAllUsers() {
        return sysUserRepository.findAllByOrderByCreatedAtDesc();
    }

    public boolean validatePassword(SysUser user, String rawPassword) {
        if (user == null || rawPassword == null) return false;
        return user.getPasswordHash().equals(hashPassword(rawPassword));
    }

    @Transactional
    public SysUser createUser(String username, String rawPassword, String role) {
        return createUser(username, rawPassword, role, 0, 0, null, null, 0, 0, 0, 0, 0);
    }

    @Transactional
    public SysUser createUser(
            String username,
            String rawPassword,
            String role,
            Integer isMaster,
            Integer isSettlement,
            List<Long> visibleUserIds,
            List<Long> subUserIds,
            Integer permPredictPayback,
            Integer permRoiPredict,
            Integer permGlobalDistribution,
            Integer permExport,
            Integer permSettlement
    ) {
        if (sysUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        SysUser user = new SysUser();
        user.setUsername(username.trim());
        user.setPasswordHash(hashPassword(rawPassword));
        user.setRole(role != null ? role.toUpperCase() : "USER");
        user.setStatus(1);
        user.setIsMaster(isMaster != null ? isMaster : 0);
        user.setIsSettlement(isSettlement != null ? isSettlement : 0);
        user.setPermPredictPayback(permPredictPayback != null ? permPredictPayback : 0);
        user.setPermRoiPredict(permRoiPredict != null ? permRoiPredict : 0);
        user.setPermGlobalDistribution(permGlobalDistribution != null ? permGlobalDistribution : 0);
        user.setPermExport(permExport != null ? permExport : 0);
        user.setPermSettlement(permSettlement != null ? permSettlement : 0);

        SysUser savedUser = sysUserRepository.save(user);

        // 如果配置了只读视图
        if (visibleUserIds != null && !visibleUserIds.isEmpty()) {
            updateUserViewPermissions(savedUser.getId(), visibleUserIds);
        }

        // 如果是主账号并配置了子账号
        if (Integer.valueOf(1).equals(user.getIsMaster()) && subUserIds != null && !subUserIds.isEmpty()) {
            updateMasterSubAccounts(savedUser.getId(), subUserIds);
        }

        return savedUser;
    }

    @Transactional
    public void resetPassword(Long userId, String newRawPassword) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        user.setPasswordHash(hashPassword(newRawPassword));
        sysUserRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Long userId, String newRole) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        user.setRole(newRole != null ? newRole.toUpperCase() : "USER");
        sysUserRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        user.setStatus(status);
        sysUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userLandingPageRepository.deleteByUserId(userId);
        userViewPermissionRepository.deleteByUserId(userId);
        userViewPermissionRepository.deleteByTargetUserId(userId);
        userSubAccountRepository.deleteByMasterUserId(userId);
        userSubAccountRepository.deleteBySubUserId(userId);
        sysUserRepository.deleteById(userId);
    }

    public boolean isMasterAccount(Long userId) {
        if (userId == null) return false;
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        return user != null && user.isMasterAccount();
    }

    public List<Long> getSubUserIdsForMaster(Long masterUserId) {
        if (masterUserId == null) return Collections.emptyList();
        return userSubAccountRepository.findByMasterUserId(masterUserId).stream()
                .map(com.ltv.stat.entity.UserSubAccount::getSubUserId)
                .collect(Collectors.toList());
    }

    public List<Long> getMasterUserIdsForSub(Long subUserId) {
        if (subUserId == null) return Collections.emptyList();
        return userSubAccountRepository.findBySubUserId(subUserId).stream()
                .map(com.ltv.stat.entity.UserSubAccount::getMasterUserId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMasterStatus(Long userId, Integer isMaster) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        user.setIsMaster(isMaster != null ? isMaster : 0);
        sysUserRepository.save(user);
        if (Integer.valueOf(1).equals(isMaster)) {
            // 提升为主账号时，解除作为其他主账号子账号的关联
            userSubAccountRepository.deleteBySubUserId(userId);
        } else {
            // 取消主账号时，删除关联的所有子账号关系
            userSubAccountRepository.deleteByMasterUserId(userId);
        }
    }

    @Transactional
    public void updateMasterSubAccounts(Long masterUserId, List<Long> subUserIds) {
        SysUser masterUser = sysUserRepository.findById(masterUserId)
                .orElseThrow(() -> new IllegalArgumentException("主账号不存在: " + masterUserId));

        userSubAccountRepository.deleteByMasterUserId(masterUserId);
        userSubAccountRepository.flush();

        if (subUserIds != null && !subUserIds.isEmpty()) {
            List<com.ltv.stat.entity.UserSubAccount> list = new ArrayList<>();
            Set<Long> uniqueSubs = new HashSet<>(subUserIds);
            for (Long subId : uniqueSubs) {
                if (subId != null && !subId.equals(masterUserId)) {
                    SysUser subUser = sysUserRepository.findById(subId).orElse(null);
                    if (subUser != null && !subUser.isMasterAccount()) {
                        list.add(new com.ltv.stat.entity.UserSubAccount(masterUserId, subId));
                    }
                }
            }
            if (!list.isEmpty()) {
                userSubAccountRepository.saveAll(list);
                userSubAccountRepository.flush();
            }
        }
    }

    public List<Long> getUserViewPermissionTargetIds(Long userId) {
        if (userId == null) return Collections.emptyList();
        return userViewPermissionRepository.findByUserId(userId).stream()
                .map(UserViewPermission::getTargetUserId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserViewPermissions(Long userId, List<Long> targetUserIds) {
        if (userId == null) return;
        if (!sysUserRepository.existsById(userId)) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        userViewPermissionRepository.deleteByUserId(userId);
        userViewPermissionRepository.flush();

        if (targetUserIds != null && !targetUserIds.isEmpty()) {
            List<UserViewPermission> list = new ArrayList<>();
            Set<Long> uniqueTargets = new HashSet<>(targetUserIds);
            for (Long targetId : uniqueTargets) {
                if (targetId != null && !targetId.equals(userId) && sysUserRepository.existsById(targetId)) {
                    UserViewPermission uvp = new UserViewPermission();
                    uvp.setUserId(userId);
                    uvp.setTargetUserId(targetId);
                    list.add(uvp);
                }
            }
            if (!list.isEmpty()) {
                userViewPermissionRepository.saveAll(list);
                userViewPermissionRepository.flush();
            }
        }
    }

    @Transactional
    public void updateUserPermissions(Long userId, Integer permPredictPayback, Integer permRoiPredict, Integer permGlobalDistribution, Integer permExport, Integer permSettlement) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        if (permPredictPayback != null) user.setPermPredictPayback(permPredictPayback);
        if (permRoiPredict != null) user.setPermRoiPredict(permRoiPredict);
        if (permGlobalDistribution != null) user.setPermGlobalDistribution(permGlobalDistribution);
        if (permExport != null) user.setPermExport(permExport);
        if (permSettlement != null) user.setPermSettlement(permSettlement);

        sysUserRepository.save(user);
    }

    public boolean hasPermSettlement(Long userId) {
        if (userId == null) return false;
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        return user != null && user.hasPermSettlement();
    }

    public boolean hasPermGlobalDistribution(Long userId) {
        if (userId == null) return false;
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        return user != null && user.hasPermGlobalDistribution();
    }

    public boolean hasPermExport(Long userId) {
        if (userId == null) return false;
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        return user != null && user.hasPermExport();
    }

    public boolean hasPermPredictPayback(Long userId) {
        if (userId == null) return false;
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        return user != null && user.hasPermPredictPayback();
    }

    public boolean hasPermRoiPredict(Long userId) {
        if (userId == null) return false;
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        return user != null && user.hasPermRoiPredict();
    }

    public List<VisibleAccountDto> getVisibleAccountsForUser(Long userId) {
        if (userId == null) return Collections.emptyList();
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        List<SysUser> allUsers = sysUserRepository.findAllByOrderByCreatedAtDesc();

        // 超级管理员：可查看系统中所有活跃用户
        if ("SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
            List<VisibleAccountDto> list = new ArrayList<>();
            for (SysUser u : allUsers) {
                if (u.getStatus() != null && u.getStatus() == 1) {
                    boolean isSelf = u.getId().equals(userId);
                    int subCount = isMasterAccount(u.getId()) ? getSubUserIdsForMaster(u.getId()).size() : 0;
                    list.add(new VisibleAccountDto(u.getId(), u.getUsername(), u.getRole(), isSelf, u.getIsMaster(), subCount));
                }
            }
            return list;
        }

        // 普通管理员 / 普通用户：包含自身主账户 + 被分配允许查看的目标账户 + 若为主账号则自动包含名下子账号
        List<Long> grantedTargetIds = getUserViewPermissionTargetIds(userId);
        Set<Long> visibleSet = new HashSet<>(grantedTargetIds);
        visibleSet.add(userId);
        if (isMasterAccount(userId)) {
            visibleSet.addAll(getSubUserIdsForMaster(userId));
        }

        List<VisibleAccountDto> result = new ArrayList<>();
        // 首先加入本人账户
        int selfSubCount = isMasterAccount(user.getId()) ? getSubUserIdsForMaster(user.getId()).size() : 0;
        result.add(new VisibleAccountDto(user.getId(), user.getUsername(), user.getRole(), true, user.getIsMaster(), selfSubCount));

        // 其它被授权的账户 (包含权限表授权 + 主子账号归属)
        for (SysUser u : allUsers) {
            if (!u.getId().equals(userId) && visibleSet.contains(u.getId()) && u.getStatus() != null && u.getStatus() == 1) {
                int subCount = isMasterAccount(u.getId()) ? getSubUserIdsForMaster(u.getId()).size() : 0;
                result.add(new VisibleAccountDto(u.getId(), u.getUsername(), u.getRole(), false, u.getIsMaster(), subCount));
            }
        }

        return result;
    }

    @Transactional
    public void updateSettlementStatus(Long userId, Integer isSettlement) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        user.setIsSettlement(isSettlement != null ? isSettlement : 0);
        sysUserRepository.save(user);
    }

    public List<VisibleAccountDto> getSettlementAccountsForUser(Long userId) {
        if (userId == null) return Collections.emptyList();
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        List<SysUser> allUsers = sysUserRepository.findAllByOrderByCreatedAtDesc();

        // 超级管理员 / 管理员：返回系统中所有被勾选了参与结算属性（isSettlement == 1）的账号，且当前账号置顶
        if (user.isAdmin()) {
            List<VisibleAccountDto> list = new ArrayList<>();
            // 若当前登录账号自身也是结算账号或者管理员/超管，置顶
            if (user.isSettlementAccount() || user.isAdmin()) {
                int selfSubCount = isMasterAccount(user.getId()) ? getSubUserIdsForMaster(user.getId()).size() : 0;
                list.add(new VisibleAccountDto(user.getId(), user.getUsername(), user.getRole(), true, user.getIsMaster(), user.getIsSettlement(), selfSubCount));
            }

            for (SysUser u : allUsers) {
                if (!u.getId().equals(userId) && u.getStatus() != null && u.getStatus() == 1 && u.isSettlementAccount()) {
                    int subCount = isMasterAccount(u.getId()) ? getSubUserIdsForMaster(u.getId()).size() : 0;
                    list.add(new VisibleAccountDto(u.getId(), u.getUsername(), u.getRole(), false, u.getIsMaster(), u.getIsSettlement(), subCount));
                }
            }
            return list;
        }

        // 普通用户：若有结算权限，结算账号列表只能查看自身登录账号，不可查看其他结算账号
        List<VisibleAccountDto> result = new ArrayList<>();
        int selfSubCount = isMasterAccount(user.getId()) ? getSubUserIdsForMaster(user.getId()).size() : 0;
        result.add(new VisibleAccountDto(user.getId(), user.getUsername(), user.getRole(), true, user.getIsMaster(), user.getIsSettlement(), selfSubCount));
        return result;
    }

    public boolean canUserViewTarget(TokenInfo currentUser, Long targetUserId) {
        if (currentUser == null || currentUser.getUserId() == null) return false;
        if (targetUserId == null || targetUserId.equals(currentUser.getUserId())) return true;
        if (currentUser.isSuperAdmin()) return true;

        if (userSubAccountRepository.existsByMasterUserIdAndSubUserId(currentUser.getUserId(), targetUserId)) {
            return true;
        }

        return userViewPermissionRepository.existsByUserIdAndTargetUserId(currentUser.getUserId(), targetUserId);
    }

    public boolean canUserModifyTarget(TokenInfo currentUser, Long targetUserId) {
        if (currentUser == null || currentUser.getUserId() == null) return false;
        if (targetUserId == null || targetUserId.equals(currentUser.getUserId())) return true;
        if (currentUser.isSuperAdmin()) return true;

        if (userSubAccountRepository.existsByMasterUserIdAndSubUserId(currentUser.getUserId(), targetUserId)) {
            return true;
        }

        return false;
    }


    public Set<String> getAdminLandingPageIds(Long excludeUserId) {
        List<SysUser> adminUsers = sysUserRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(u.getRole()))
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .collect(Collectors.toList());
        Set<String> adminPids = new HashSet<>();
        for (SysUser admin : adminUsers) {
            List<UserLandingPage> pages = userLandingPageRepository.findByUserId(admin.getId());
            for (UserLandingPage page : pages) {
                if (page.getLandingPageId() != null && !page.getLandingPageId().trim().isEmpty()) {
                    adminPids.add(page.getLandingPageId().trim());
                }
            }
        }
        return adminPids;
    }

    public List<String> getUserLandingPageIds(Long userId) {
        if (userId == null) return Collections.emptyList();
        return getUserLandingPageConfigs(userId).stream()
                .map(com.ltv.stat.dto.LandingPageConfigItem::getLandingPageId)
                .collect(Collectors.toList());
    }

    public List<com.ltv.stat.dto.LandingPageConfigItem> getUserLandingPageConfigs(Long userId) {
        if (userId == null) return Collections.emptyList();
        SysUser user = sysUserRepository.findById(userId).orElse(null);
        if (user == null) return Collections.emptyList();

        // 若为主账号，自动聚合所有子账号配置的落地页（去重）
        if (user.isMasterAccount()) {
            List<Long> subUserIds = getSubUserIdsForMaster(userId);
            Set<String> uniquePids = new HashSet<>();
            List<com.ltv.stat.dto.LandingPageConfigItem> aggregated = new ArrayList<>();
            for (Long subId : subUserIds) {
                List<com.ltv.stat.dto.LandingPageConfigItem> subConfigs = getUserLandingPageConfigs(subId);
                for (com.ltv.stat.dto.LandingPageConfigItem item : subConfigs) {
                    if (item != null && item.getLandingPageId() != null && !item.getLandingPageId().trim().isEmpty()) {
                        String pid = item.getLandingPageId().trim();
                        if (!uniquePids.contains(pid)) {
                            uniquePids.add(pid);
                            aggregated.add(item);
                        }
                    }
                }
            }
            return aggregated;
        }

        List<UserLandingPage> list = userLandingPageRepository.findByUserId(userId);

        // 如果是普通用户 (USER)，剔除已被管理员配置的隔离落地页 ID
        if ("USER".equalsIgnoreCase(user.getRole())) {
            Set<String> adminPids = getAdminLandingPageIds(userId);
            list = list.stream()
                    .filter(ulp -> ulp.getLandingPageId() != null && !adminPids.contains(ulp.getLandingPageId().trim()))
                    .collect(Collectors.toList());
        }

        return list.stream()
                .map(ulp -> new com.ltv.stat.dto.LandingPageConfigItem(ulp.getLandingPageId(), ulp.getTimezone()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserLandingPageConfigs(Long userId, List<com.ltv.stat.dto.LandingPageConfigItem> items) {
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

        if (user.isMasterAccount()) {
            throw new IllegalArgumentException("主账号为数据汇总账号，落地页由关联子账号自动聚合，不可直接编辑！");
        }

        // 如果是普通用户 (USER)，拦截校验：不允许配置已被管理员 (ADMIN / SUPER_ADMIN) 配置的独占隔离落地页
        if ("USER".equalsIgnoreCase(user.getRole())) {
            Set<String> adminPids = getAdminLandingPageIds(userId);
            if (items != null) {
                for (com.ltv.stat.dto.LandingPageConfigItem item : items) {
                    if (item != null && item.getLandingPageId() != null) {
                        String pid = item.getLandingPageId().trim();
                        if (adminPids.contains(pid)) {
                            throw new IllegalArgumentException("落地页 ID [" + pid + "] 为管理员独占/隔离落地页，普通用户无法配置！");
                        }
                    }
                }
            }
        }

        userLandingPageRepository.deleteByUserId(userId);
        userLandingPageRepository.flush();
        if (items != null) {
            Map<String, String> pidTzMap = new java.util.LinkedHashMap<>();
            for (com.ltv.stat.dto.LandingPageConfigItem item : items) {
                if (item != null && item.getLandingPageId() != null && !item.getLandingPageId().trim().isEmpty()) {
                    String pid = item.getLandingPageId().trim();
                    String tz = (item.getTimezone() != null && "ET".equalsIgnoreCase(item.getTimezone().trim())) ? "ET" : "BJ";
                    pidTzMap.put(pid, tz);
                }
            }

            List<UserLandingPage> list = new ArrayList<>();
            for (Map.Entry<String, String> entry : pidTzMap.entrySet()) {
                UserLandingPage ulp = new UserLandingPage();
                ulp.setUserId(userId);
                ulp.setLandingPageId(entry.getKey());
                ulp.setTimezone(entry.getValue());
                list.add(ulp);
            }
            userLandingPageRepository.saveAll(list);
            userLandingPageRepository.flush();
        }
    }

    @Transactional
    public void updateUserLandingPageIds(Long userId, List<String> pageIds) {
        if (pageIds == null) {
            updateUserLandingPageConfigs(userId, Collections.emptyList());
            return;
        }
        List<com.ltv.stat.dto.LandingPageConfigItem> items = pageIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .map(id -> new com.ltv.stat.dto.LandingPageConfigItem(id.trim(), "BJ"))
                .collect(Collectors.toList());
        updateUserLandingPageConfigs(userId, items);
    }

    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(("zw-ltv-salt-" + rawPassword).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return rawPassword;
        }
    }
}
