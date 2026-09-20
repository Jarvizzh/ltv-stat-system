import React, { useState, useEffect, useRef, useMemo } from 'react';
import {
  Users,
  UserPlus,
  KeyRound,
  Trash2,
  X,
  Check,
  Eye,
  Network,
  ShieldCheck,
  Globe,
  Search,
  RefreshCw,
  Crown,
  Shield,
  UserCheck,
  Receipt,
  Sparkles,
  Sliders,
  Filter,
  CheckCircle2
} from 'lucide-react';
import CustomSelect from './CustomSelect';

const ROLE_OPTIONS = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' },
  { label: '超级管理员', value: 'SUPER_ADMIN' },
];

const ACCOUNT_TYPE_OPTIONS = [
  { label: '普通账号', value: 0 },
  { label: '主账号(汇总)', value: 1 },
];

const SETTLEMENT_ATTRIBUTE_OPTIONS = [
  { label: '不结算', value: 0 },
  { label: '参与结算', value: 1 },
];

export default function UserManagementPage({ token, currentUser, onRefreshUsers, showToast }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [availablePlatforms, setAvailablePlatforms] = useState([
    { code: 'ALL', name: '综合大盘' },
    { code: 'rocnovel', name: '中文在线' },
    { code: 'flicknovel', name: '番茄司南' }
  ]);

  // 搜索与过滤筛选条件
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [accountTypeFilter, setAccountTypeFilter] = useState('ALL');
  const [settlementFilter, setSettlementFilter] = useState('ALL');

  // 新增用户表单状态
  const [showAddForm, setShowAddForm] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState('USER');
  const [newIsMaster, setNewIsMaster] = useState(0);
  const [newIsSettlement, setNewIsSettlement] = useState(0);
  const [newVisibleUserIds, setNewVisibleUserIds] = useState([]);
  const [newSubUserIds, setNewSubUserIds] = useState([]);
  const [newAllowedPlatforms, setNewAllowedPlatforms] = useState(['ALL']);
  const [newPermissions, setNewPermissions] = useState({
    permPredictPayback: 0,
    permRoiPredict: 0,
    permGlobalDistribution: 0,
    permExport: 0,
    permSettlement: 0,
    permVideoGen: 0,
  });

  // 编辑操作抽屉状态
  const [editingPasswordUserId, setEditingPasswordUserId] = useState(null);
  const [resetPasswordVal, setResetPasswordVal] = useState('');

  const [editingViewPermissionUserId, setEditingViewPermissionUserId] = useState(null);
  const [selectedViewPermissionIds, setSelectedViewPermissionIds] = useState([]);

  const [editingSubAccountsUserId, setEditingSubAccountsUserId] = useState(null);
  const [selectedSubUserIds, setSelectedSubUserIds] = useState([]);

  const [editingPermissionsUserId, setEditingPermissionsUserId] = useState(null);
  const [selectedAllowedPlatforms, setSelectedAllowedPlatforms] = useState(['ALL']);
  const [selectedPermissions, setSelectedPermissions] = useState({
    permPredictPayback: 0,
    permRoiPredict: 0,
    permGlobalDistribution: 0,
    permExport: 0,
    permSettlement: 0,
    permVideoGen: 0,
  });

  const expandedRowRef = useRef(null);

  const isSuperAdmin = currentUser && currentUser.role === 'SUPER_ADMIN';

  const resetAddForm = () => {
    setNewUsername('');
    setNewPassword('');
    setNewRole('USER');
    setNewIsMaster(0);
    setNewIsSettlement(0);
    setNewVisibleUserIds([]);
    setNewSubUserIds([]);
    setNewAllowedPlatforms(['ALL']);
    setNewPermissions({
      permPredictPayback: 0,
      permRoiPredict: 0,
      permGlobalDistribution: 0,
      permExport: 0,
      permSettlement: 0,
      permVideoGen: 0,
    });
    setShowAddForm(false);
  };

  const togglePlatformCheckbox = (code, currentPlatforms, setPlatforms) => {
    if (code === 'ALL') {
      if (currentPlatforms.includes('ALL')) {
        setPlatforms([]);
      } else {
        const allCodes = availablePlatforms.map(p => p.code);
        setPlatforms(Array.from(new Set(['ALL', ...allCodes])));
      }
      return;
    }

    let next = currentPlatforms.includes(code)
      ? currentPlatforms.filter(c => c !== code)
      : [...currentPlatforms, code];

    const actualCodes = availablePlatforms.filter(p => p.code !== 'ALL').map(p => p.code);
    const hasAllActual = actualCodes.length > 0 && actualCodes.every(c => next.includes(c));
    if (hasAllActual) {
      if (!next.includes('ALL')) next.push('ALL');
    } else {
      next = next.filter(c => c !== 'ALL');
    }
    setPlatforms(next);
  };

  useEffect(() => {
    if ((editingViewPermissionUserId || editingSubAccountsUserId || editingPermissionsUserId || editingPasswordUserId) && expandedRowRef.current) {
      setTimeout(() => {
        expandedRowRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }, 50);
    }
  }, [editingViewPermissionUserId, editingSubAccountsUserId, editingPermissionsUserId, editingPasswordUserId]);

  const fetchPlatforms = async () => {
    if (!token) return;
    try {
      const res = await fetch('/api/platform/list', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok && data.code === 0 && Array.isArray(data.data) && data.data.length > 0) {
        setAvailablePlatforms(data.data);
      }
    } catch (e) {
      console.error('获取平台列表失败:', e);
    }
  };

  const fetchUsers = async () => {
    if (!token) return;
    setLoading(true);
    try {
      const res = await fetch('/api/admin/users', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        setUsers(data.data || []);
        if (onRefreshUsers) onRefreshUsers(data.data || []);
      } else {
        if (showToast) showToast(data.msg || '获取用户列表失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('获取用户列表异常', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchPlatforms();
  }, [token]);

  // 新建用户
  const handleCreateUser = async (e) => {
    e.preventDefault();
    if (!newUsername.trim() || !newPassword.trim()) {
      if (showToast) showToast('用户名和密码不能为空', 'error');
      return;
    }
    try {
      const res = await fetch('/api/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          username: newUsername.trim(),
          password: newPassword.trim(),
          role: newRole,
          isMaster: isSuperAdmin ? newIsMaster : 0,
          isSettlement: isSuperAdmin ? newIsSettlement : 0,
          visibleUserIds: isSuperAdmin ? newVisibleUserIds : [],
          subUserIds: (isSuperAdmin && newIsMaster === 1) ? newSubUserIds : [],
          permPredictPayback: newRole === 'SUPER_ADMIN' ? 1 : newPermissions.permPredictPayback,
          permRoiPredict: newRole === 'SUPER_ADMIN' ? 1 : newPermissions.permRoiPredict,
          permGlobalDistribution: newRole === 'SUPER_ADMIN' ? 1 : newPermissions.permGlobalDistribution,
          permExport: newRole === 'SUPER_ADMIN' ? 1 : newPermissions.permExport,
          permSettlement: newRole === 'SUPER_ADMIN' ? 1 : newPermissions.permSettlement,
          permVideoGen: newRole === 'SUPER_ADMIN' ? 1 : newPermissions.permVideoGen,
          allowedPlatforms: newRole === 'SUPER_ADMIN' ? 'ALL' : (newAllowedPlatforms.length > 0 ? newAllowedPlatforms.join(',') : 'ALL'),
        })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('创建用户成功！已分配相应类型与权限', 'success');
        resetAddForm();
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '创建用户失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('创建用户请求异常', 'error');
    }
  };

  // 结算状态更新
  const handleUpdateSettlementStatus = async (userId, isSettlement) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/settlement-status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ isSettlement })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast(isSettlement === 1 ? '已将该账号设置为参与结算账号！' : '已取消该账号的参与结算属性', 'success');
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '更新结算属性失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('更新结算属性异常', 'error');
    }
  };

  // 重置密码
  const handleResetPassword = async (userId) => {
    if (!resetPasswordVal.trim()) {
      if (showToast) showToast('新密码不能为空', 'error');
      return;
    }
    try {
      const res = await fetch(`/api/admin/users/${userId}/password`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ newPassword: resetPasswordVal.trim() })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('密码重置成功', 'success');
        setEditingPasswordUserId(null);
        setResetPasswordVal('');
      } else {
        if (showToast) showToast(data.msg || '重置密码失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('重置密码异常', 'error');
    }
  };

  // 修改角色
  const handleUpdateRole = async (userId, newRoleVal) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/role`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ role: newRoleVal })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('角色更新成功', 'success');
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '更新角色失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('更新角色异常', 'error');
    }
  };

  // 更新主账号类型
  const handleUpdateMasterStatus = async (userId, isMasterVal) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/master-status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ isMaster: isMasterVal })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('账号类型更新成功！', 'success');
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '更新账号类型失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('更新账号类型异常', 'error');
    }
  };

  // 保存视图查看授权
  const handleSaveViewPermissions = async (userId) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/view-permissions`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ targetUserIds: selectedViewPermissionIds })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('只读视图权限分配成功！', 'success');
        setEditingViewPermissionUserId(null);
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '保存视图权限失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('保存视图权限异常', 'error');
    }
  };

  // 保存子账号关联
  const handleSaveSubAccounts = async (masterUserId) => {
    try {
      const res = await fetch(`/api/admin/users/${masterUserId}/sub-accounts`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ subUserIds: selectedSubUserIds })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('子账号关联分配成功！已自动解重聚合与计算主账号数据', 'success');
        setEditingSubAccountsUserId(null);
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '保存子账号关联失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('保存子账号关联异常', 'error');
    }
  };

  // 保存平台与细粒度功能权限
  const handleSavePermissions = async (userId) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/permissions`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          ...selectedPermissions,
          allowedPlatforms: selectedAllowedPlatforms.length > 0 ? selectedAllowedPlatforms.join(',') : 'ALL'
        })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('权限分配保存成功！', 'success');
        setEditingPermissionsUserId(null);
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '保存权限失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('保存权限异常', 'error');
    }
  };

  // 删除用户
  const handleDeleteUser = async (user) => {
    if (!window.confirm(`确定要彻底删除用户 "${user.username}" (ID: ${user.id}) 吗？此操作无法撤销。`)) return;
    try {
      const res = await fetch(`/api/admin/users/${user.id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('删除用户成功', 'success');
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '删除用户失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('删除用户异常', 'error');
    }
  };

  // 统计概览计算
  const stats = useMemo(() => {
    const total = users.length;
    const superAdmins = users.filter(u => u.role === 'SUPER_ADMIN').length;
    const admins = users.filter(u => u.role === 'ADMIN').length;
    const masters = users.filter(u => u.isMaster === 1).length;
    const settlements = users.filter(u => u.isSettlement === 1).length;
    return {
      total,
      admins: superAdmins + admins,
      masters,
      settlements
    };
  }, [users]);

  // 根据搜索与过滤条件计算展示列表
  const filteredUsers = useMemo(() => {
    return users.filter(u => {
      // 搜索关键字
      if (searchTerm.trim()) {
        const term = searchTerm.trim().toLowerCase();
        const matchName = u.username && u.username.toLowerCase().includes(term);
        const matchId = String(u.id).includes(term);
        if (!matchName && !matchId) return false;
      }
      // 角色筛选
      if (roleFilter !== 'ALL' && u.role !== roleFilter) {
        return false;
      }
      // 账号类型筛选
      if (accountTypeFilter !== 'ALL') {
        const isMaster = Number(accountTypeFilter) === 1;
        if (Boolean(u.isMaster === 1) !== isMaster) return false;
      }
      // 结算属性筛选
      if (settlementFilter !== 'ALL') {
        const isSettle = Number(settlementFilter) === 1;
        if (Boolean(u.isSettlement === 1) !== isSettle) return false;
      }
      return true;
    });
  }, [users, searchTerm, roleFilter, accountTypeFilter, settlementFilter]);

  return (
    <div className="user-management-page" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', paddingBottom: '3rem' }}>
      {/* 顶部标题栏与简介 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(6, 182, 212, 0.2))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#6366f1',
              border: '1px solid rgba(99, 102, 241, 0.3)'
            }}>
              <Users size={20} />
            </div>
            <div>
              <h2 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.02em' }}>
                用户管理
              </h2>
              <p style={{ margin: '0.15rem 0 0', fontSize: '0.82rem', color: 'var(--text-sub)' }}>
                统一管理系统账号角色、主子账号汇总、多平台接入访问范围、只读视图跨账号授权及 6 项核心功能权限。
              </p>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <button
            className="btn btn-secondary"
            onClick={fetchUsers}
            disabled={loading}
            title="重新获取最新用户列表"
            style={{ fontSize: '0.82rem', padding: '0.45rem 0.8rem', gap: '0.35rem' }}
          >
            <RefreshCw size={14} className={loading ? 'spin' : ''} />
            <span>刷新</span>
          </button>

          <button
            className="btn btn-primary"
            onClick={() => setShowAddForm(!showAddForm)}
            style={{ fontSize: '0.82rem', padding: '0.45rem 0.9rem', gap: '0.35rem' }}
          >
            <UserPlus size={15} />
            <span>{showAddForm ? '收起新增表单' : '新建用户'}</span>
          </button>
        </div>
      </div>

      {/* 统计指标卡片 (Metric Summary Cards) */}
      <div className="stats-summary" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.75rem' }}>
        <div className="stat-card" style={{ padding: '0.85rem 1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label" style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-sub)' }}>总账号数</span>
            <Users size={16} color="#6366f1" />
          </div>
          <div className="stat-value" style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--text-main)', marginTop: '0.2rem' }}>
            {stats.total} <span style={{ fontSize: '0.75rem', fontWeight: 400, color: 'var(--text-sub)' }}>人</span>
          </div>
        </div>

        <div className="stat-card" style={{ padding: '0.85rem 1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label" style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-sub)' }}>管理级账号</span>
            <Shield size={16} color="#06b6d4" />
          </div>
          <div className="stat-value" style={{ fontSize: '1.35rem', fontWeight: 700, color: '#06b6d4', marginTop: '0.2rem' }}>
            {stats.admins} <span style={{ fontSize: '0.75rem', fontWeight: 400, color: 'var(--text-sub)' }}>人 (含超管)</span>
          </div>
        </div>

        <div className="stat-card" style={{ padding: '0.85rem 1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label" style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-sub)' }}>主账号 (汇总)</span>
            <Crown size={16} color="#8b5cf6" />
          </div>
          <div className="stat-value" style={{ fontSize: '1.35rem', fontWeight: 700, color: '#8b5cf6', marginTop: '0.2rem' }}>
            {stats.masters} <span style={{ fontSize: '0.75rem', fontWeight: 400, color: 'var(--text-sub)' }}>个</span>
          </div>
        </div>

        <div className="stat-card" style={{ padding: '0.85rem 1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label" style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-sub)' }}>参与结算账号</span>
            <Receipt size={16} color="#10b981" />
          </div>
          <div className="stat-value" style={{ fontSize: '1.35rem', fontWeight: 700, color: '#10b981', marginTop: '0.2rem' }}>
            {stats.settlements} <span style={{ fontSize: '0.75rem', fontWeight: 400, color: 'var(--text-sub)' }}>个</span>
          </div>
        </div>
      </div>

      {/* 新增用户表单折叠卡片 */}
      {showAddForm && (
        <form onSubmit={handleCreateUser} style={{
          background: 'var(--bg-secondary)',
          border: '1px solid var(--border-color)',
          borderRadius: '0.65rem',
          padding: '1.25rem',
          display: 'flex',
          flexDirection: 'column',
          gap: '1rem',
          boxShadow: '0 8px 24px rgba(0,0,0,0.12)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.65rem' }}>
            <div style={{ fontSize: '0.98rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <UserPlus size={18} color="#6366f1" />
              <span>创建新用户账号与初始权限配置</span>
            </div>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
              onClick={resetAddForm}
            >
              取消
            </button>
          </div>

          {/* 第一行：基本账号信息 */}
          <div style={{ display: 'grid', gridTemplateColumns: isSuperAdmin ? 'repeat(auto-fit, minmax(180px, 1fr))' : 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.85rem', alignItems: 'center' }}>
            <div>
              <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.3rem', display: 'block', fontWeight: 500 }}>账号名称 (Username)</label>
              <input
                type="text"
                className="form-input"
                placeholder="请输入用户名"
                value={newUsername}
                onChange={(e) => setNewUsername(e.target.value)}
                required
              />
            </div>
            <div>
              <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.3rem', display: 'block', fontWeight: 500 }}>初始登录密码</label>
              <input
                type="password"
                className="form-input"
                placeholder="请输入登录密码"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
            </div>
            <div>
              <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.3rem', display: 'block', fontWeight: 500 }}>角色级别</label>
              <CustomSelect
                value={newRole}
                onChange={(val) => setNewRole(val)}
                options={ROLE_OPTIONS}
                style={{ width: '100%' }}
              />
            </div>
            {isSuperAdmin && (
              <div>
                <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.3rem', display: 'block', fontWeight: 500 }}>账号类型</label>
                <CustomSelect
                  value={newIsMaster}
                  onChange={(val) => setNewIsMaster(Number(val))}
                  options={ACCOUNT_TYPE_OPTIONS}
                  style={{ width: '100%' }}
                />
              </div>
            )}
            {isSuperAdmin && (
              <div>
                <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.3rem', display: 'block', fontWeight: 500 }}>参与结算属性</label>
                <CustomSelect
                  value={newIsSettlement}
                  onChange={(val) => setNewIsSettlement(Number(val))}
                  options={SETTLEMENT_ATTRIBUTE_OPTIONS}
                  style={{ width: '100%' }}
                />
              </div>
            )}
          </div>

          {/* 第二行：平台数据权限配置 */}
          {isSuperAdmin && (
            <div style={{ background: 'var(--bg-hover)', padding: '0.85rem 1rem', borderRadius: '0.5rem', border: '1px solid var(--border-light)' }}>
              <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.6rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                  <Globe size={15} color="#06b6d4" />
                  <span>平台数据权限配置</span>
                  {newRole === 'SUPER_ADMIN' ? (
                    <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 400 }}>（超级管理员默认拥有所有平台访问权限）</span>
                  ) : (
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-sub)', fontWeight: 400 }}>（勾选允许该账号查看与操作的数据平台）</span>
                  )}
                </div>
                {newRole !== 'SUPER_ADMIN' && (
                  <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.75rem' }}>
                    <button
                      type="button"
                      onClick={() => setNewAllowedPlatforms(Array.from(new Set(['ALL', ...availablePlatforms.map(p => p.code)])))}
                      style={{ background: 'none', border: 'none', color: '#06b6d4', cursor: 'pointer', padding: 0 }}
                    >
                      全选
                    </button>
                    <span style={{ color: 'var(--border-color)' }}>|</span>
                    <button
                      type="button"
                      onClick={() => setNewAllowedPlatforms([])}
                      style={{ background: 'none', border: 'none', color: 'var(--text-sub)', cursor: 'pointer', padding: 0 }}
                    >
                      清空
                    </button>
                  </div>
                )}
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.6rem' }}>
                {availablePlatforms.map(p => {
                  const isSuper = newRole === 'SUPER_ADMIN';
                  const checked = isSuper || newAllowedPlatforms.includes('ALL') || newAllowedPlatforms.includes(p.code);
                  return (
                    <label
                      key={p.code}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.45rem',
                        fontSize: '0.82rem',
                        cursor: isSuper ? 'not-allowed' : 'pointer',
                        userSelect: 'none',
                        color: checked ? '#0891b2' : 'var(--text-main)',
                        fontWeight: checked ? 600 : 400
                      }}
                    >
                      <input
                        type="checkbox"
                        disabled={isSuper}
                        checked={checked}
                        onChange={() => togglePlatformCheckbox(p.code, newAllowedPlatforms, setNewAllowedPlatforms)}
                        style={{ accentColor: '#06b6d4', width: 15, height: 15 }}
                      />
                      <span>{p.name} ({p.code})</span>
                    </label>
                  );
                })}
              </div>
            </div>
          )}

          {/* 第三行：功能权限分配 (6 项) */}
          {isSuperAdmin && (
            <div style={{ background: 'var(--bg-hover)', padding: '0.85rem 1rem', borderRadius: '0.5rem', border: '1px solid var(--border-light)' }}>
              <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.6rem', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                <ShieldCheck size={15} color="#3b82f6" />
                <span>功能权限配置</span>
                {newRole === 'SUPER_ADMIN' && (
                  <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 400 }}>（超级管理员默认拥有全量功能权限）</span>
                )}
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.6rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permPredictPayback) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permPredictPayback) ? 600 : 400 }}>
                  <input
                    type="checkbox"
                    disabled={newRole === 'SUPER_ADMIN'}
                    checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permPredictPayback)}
                    onChange={(e) => setNewPermissions(prev => ({ ...prev, permPredictPayback: e.target.checked ? 1 : 0 }))}
                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                  />
                  <span>📈 预测回本（含LTV表格列）</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permRoiPredict) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permRoiPredict) ? 600 : 400 }}>
                  <input
                    type="checkbox"
                    disabled={newRole === 'SUPER_ADMIN'}
                    checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permRoiPredict)}
                    onChange={(e) => setNewPermissions(prev => ({ ...prev, permRoiPredict: e.target.checked ? 1 : 0 }))}
                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                  />
                  <span>🎯（D30~D90）ROI 预测</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permGlobalDistribution) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permGlobalDistribution) ? 600 : 400 }}>
                  <input
                    type="checkbox"
                    disabled={newRole === 'SUPER_ADMIN'}
                    checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permGlobalDistribution)}
                    onChange={(e) => setNewPermissions(prev => ({ ...prev, permGlobalDistribution: e.target.checked ? 1 : 0 }))}
                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                  />
                  <span>🌐 平台汇总</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permExport) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permExport) ? 600 : 400 }}>
                  <input
                    type="checkbox"
                    disabled={newRole === 'SUPER_ADMIN'}
                    checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permExport)}
                    onChange={(e) => setNewPermissions(prev => ({ ...prev, permExport: e.target.checked ? 1 : 0 }))}
                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                  />
                  <span>📥 数据导出</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permSettlement) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permSettlement) ? 600 : 400 }}>
                  <input
                    type="checkbox"
                    disabled={newRole === 'SUPER_ADMIN'}
                    checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permSettlement)}
                    onChange={(e) => setNewPermissions(prev => ({ ...prev, permSettlement: e.target.checked ? 1 : 0 }))}
                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                  />
                  <span>💳 月份结算</span>
                </label>

                <label style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permVideoGen) ? '#8b5cf6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permVideoGen) ? 600 : 400 }}>
                  <input
                    type="checkbox"
                    disabled={newRole === 'SUPER_ADMIN'}
                    checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permVideoGen)}
                    onChange={(e) => setNewPermissions(prev => ({ ...prev, permVideoGen: e.target.checked ? 1 : 0 }))}
                    style={{ accentColor: '#8b5cf6', width: 15, height: 15 }}
                  />
                  <span>🎬 AI视频生成</span>
                </label>
              </div>
            </div>
          )}

          {/* 第四行：视图分配 (可选) */}
          {isSuperAdmin && newRole !== 'SUPER_ADMIN' && users.length > 0 && (
            <div style={{ background: 'var(--bg-hover)', padding: '0.85rem 1rem', borderRadius: '0.5rem', border: '1px solid var(--border-light)' }}>
              <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                <Eye size={15} color="#6366f1" />
                <span>视图分配（勾选允许该新账号跨视图查看的其他账户）：</span>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem' }}>
                {users.map(target => {
                  const checked = newVisibleUserIds.includes(target.id);
                  return (
                    <label
                      key={target.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.35rem',
                        fontSize: '0.82rem',
                        cursor: 'pointer',
                        userSelect: 'none',
                        color: checked ? '#6366f1' : 'var(--text-main)',
                        fontWeight: checked ? 600 : 400
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => {
                          if (newVisibleUserIds.includes(target.id)) {
                            setNewVisibleUserIds(newVisibleUserIds.filter(id => id !== target.id));
                          } else {
                            setNewVisibleUserIds([...newVisibleUserIds, target.id]);
                          }
                        }}
                        style={{ accentColor: '#6366f1' }}
                      />
                      <span>{target.username} (ID: {target.id})</span>
                    </label>
                  );
                })}
              </div>
            </div>
          )}

          {/* 第五行：主账号子账号关联 (若 newIsMaster === 1) */}
          {isSuperAdmin && newIsMaster === 1 && (
            <div style={{ background: 'var(--bg-hover)', padding: '0.85rem 1rem', borderRadius: '0.5rem', border: '1px solid var(--border-light)' }}>
              <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                <Network size={15} color="#8b5cf6" />
                <span>关联子账号（勾选归属于该主账号的子账号，落地页自动解重聚合数据）：</span>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem' }}>
                {users.filter(target => target.isMaster !== 1).map(target => {
                  const checked = newSubUserIds.includes(target.id);
                  return (
                    <label
                      key={target.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.35rem',
                        fontSize: '0.82rem',
                        cursor: 'pointer',
                        userSelect: 'none',
                        color: checked ? '#8b5cf6' : 'var(--text-main)',
                        fontWeight: checked ? 600 : 400
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={checked}
                        onChange={() => {
                          if (newSubUserIds.includes(target.id)) {
                            setNewSubUserIds(newSubUserIds.filter(id => id !== target.id));
                          } else {
                            setNewSubUserIds([...newSubUserIds, target.id]);
                          }
                        }}
                        style={{ accentColor: '#8b5cf6' }}
                      />
                      <span>{target.username} (ID: {target.id})</span>
                    </label>
                  );
                })}
              </div>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.6rem', marginTop: '0.4rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={resetAddForm}
              style={{ fontSize: '0.82rem' }}
            >
              取消
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              style={{ fontSize: '0.82rem', padding: '0.4rem 1.25rem' }}
            >
              立即创建
            </button>
          </div>
        </form>
      )}

      {/* 搜索过滤工具栏 (Search & Filters Bar) */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '0.75rem',
        background: 'var(--bg-secondary)',
        padding: '0.75rem 1rem',
        borderRadius: '0.5rem',
        border: '1px solid var(--border-color)'
      }}>
        {/* 左侧：搜索输入框 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: 1, minWidth: '220px', maxWidth: '360px' }}>
          <div style={{ position: 'relative', width: '100%' }}>
            <Search size={15} color="var(--text-sub)" style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              className="form-input"
              style={{ paddingLeft: '2rem', fontSize: '0.82rem', height: '34px' }}
              placeholder="搜索用户名或账号ID..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            {searchTerm && (
              <button
                type="button"
                onClick={() => setSearchTerm('')}
                style={{ position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--text-sub)', cursor: 'pointer', padding: 2 }}
              >
                <X size={13} />
              </button>
            )}
          </div>
        </div>

        {/* 右侧：角色筛选、类型筛选、结算筛选 */}
        <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '0.6rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', whiteSpace: 'nowrap' }}>角色:</span>
            <CustomSelect
              value={roleFilter}
              onChange={setRoleFilter}
              options={[
                { label: '全部角色', value: 'ALL' },
                { label: '普通用户', value: 'USER' },
                { label: '管理员', value: 'ADMIN' },
                { label: '超级管理员', value: 'SUPER_ADMIN' },
              ]}
              className="custom-select-sm"
              style={{ minWidth: '110px' }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', whiteSpace: 'nowrap' }}>类型:</span>
            <CustomSelect
              value={accountTypeFilter}
              onChange={setAccountTypeFilter}
              options={[
                { label: '全部类型', value: 'ALL' },
                { label: '普通账号', value: 0 },
                { label: '主账号(汇总)', value: 1 },
              ]}
              className="custom-select-sm"
              style={{ minWidth: '110px' }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', whiteSpace: 'nowrap' }}>结算:</span>
            <CustomSelect
              value={settlementFilter}
              onChange={setSettlementFilter}
              options={[
                { label: '全部结算', value: 'ALL' },
                { label: '参与结算', value: 1 },
                { label: '不结算', value: 0 },
              ]}
              className="custom-select-sm"
              style={{ minWidth: '105px' }}
            />
          </div>

          {(searchTerm || roleFilter !== 'ALL' || accountTypeFilter !== 'ALL' || settlementFilter !== 'ALL') && (
            <button
              className="btn btn-secondary"
              onClick={() => {
                setSearchTerm('');
                setRoleFilter('ALL');
                setAccountTypeFilter('ALL');
                setSettlementFilter('ALL');
              }}
              style={{ fontSize: '0.75rem', padding: '0.25rem 0.55rem' }}
            >
              重置筛选
            </button>
          )}
        </div>
      </div>

      {/* 用户数据主列表表格 */}
      <div className="table-responsive" style={{
        background: 'var(--bg-secondary)',
        border: '1px solid var(--border-color)',
        borderRadius: '0.65rem',
        overflow: 'hidden',
        boxShadow: 'var(--shadow-md)'
      }}>
        <table className="user-mgmt-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={{ width: '60px', textAlign: 'center' }}>ID</th>
              <th style={{ minWidth: '150px' }}>用户信息</th>
              <th style={{ width: '120px' }}>角色级别</th>
              {isSuperAdmin && <th style={{ width: '130px' }}>账号类型</th>}
              {isSuperAdmin && <th style={{ width: '120px' }}>结算属性</th>}
              {isSuperAdmin && <th style={{ width: '120px' }}>视图分配</th>}
              {isSuperAdmin && <th style={{ width: '120px' }}>子账号关联</th>}
              {isSuperAdmin && <th style={{ minWidth: '180px' }}>平台与功能权限</th>}
              <th style={{ width: '140px', textAlign: 'center' }}>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading && users.length === 0 ? (
              <tr>
                <td colSpan={isSuperAdmin ? 9 : 4} style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-sub)' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.6rem' }}>
                    <RefreshCw size={24} className="spin" color="#6366f1" />
                    <span>正在加载用户数据...</span>
                  </div>
                </td>
              </tr>
            ) : filteredUsers.length === 0 ? (
              <tr>
                <td colSpan={isSuperAdmin ? 9 : 4} style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-sub)' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem' }}>
                    <Users size={28} opacity={0.3} />
                    <span>未找到匹配的用户账号</span>
                    {(searchTerm || roleFilter !== 'ALL' || accountTypeFilter !== 'ALL' || settlementFilter !== 'ALL') && (
                      <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>尝试更改搜索关键词或重置筛选条件</span>
                    )}
                  </div>
                </td>
              </tr>
            ) : (
              filteredUsers.map(u => {
                const isMaster = u.isMaster === 1;
                const isSettlement = u.isSettlement === 1;
                const visibleCount = (u.visibleUserIds || []).length;
                const subCount = (u.subUserIds || []).length;
                const permCount = [
                  u.permPredictPayback,
                  u.permRoiPredict,
                  u.permGlobalDistribution,
                  u.permExport,
                  u.permSettlement,
                  u.permVideoGen
                ].filter(p => p === 1).length;

                const isCurrentSelf = currentUser && (u.id === currentUser.userId || u.id === currentUser.id);

                return (
                  <React.Fragment key={u.id}>
                    <tr style={{
                      background: (editingViewPermissionUserId === u.id || editingSubAccountsUserId === u.id || editingPermissionsUserId === u.id || editingPasswordUserId === u.id)
                        ? 'rgba(99, 102, 241, 0.05)'
                        : undefined
                    }}>
                      <td style={{ textAlign: 'center', color: 'var(--text-sub)', fontSize: '0.8rem', fontWeight: 500 }}>
                        {u.id}
                      </td>

                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                          <div style={{
                            width: '32px',
                            height: '32px',
                            borderRadius: '50%',
                            background: u.role === 'SUPER_ADMIN' ? 'linear-gradient(135deg, #8b5cf6, #ec4899)' : (u.role === 'ADMIN' ? 'linear-gradient(135deg, #6366f1, #06b6d4)' : 'linear-gradient(135deg, #10b981, #059669)'),
                            color: '#fff',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 700,
                            fontSize: '0.85rem',
                            flexShrink: 0
                          }}>
                            {u.username ? u.username.charAt(0).toUpperCase() : 'U'}
                          </div>
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                              <span style={{ fontWeight: 600, color: 'var(--text-main)', fontSize: '0.9rem' }}>{u.username}</span>
                              {isCurrentSelf && (
                                <span style={{ fontSize: '0.68rem', padding: '0.05rem 0.35rem', borderRadius: 4, background: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', fontWeight: 600 }}>
                                  当前登录
                                </span>
                              )}
                              {isMaster && (
                                <span style={{ fontSize: '0.68rem', padding: '0.05rem 0.35rem', borderRadius: 4, background: 'rgba(139, 92, 246, 0.15)', color: '#8b5cf6', fontWeight: 600 }}>
                                  主账号
                                </span>
                              )}
                            </div>
                            <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.1rem' }}>
                              创建于: {u.createdAt ? u.createdAt.replace('T', ' ').substring(0, 16) : '-'}
                            </div>
                          </div>
                        </div>
                      </td>

                      <td>
                        <CustomSelect
                          value={u.role}
                          onChange={(val) => handleUpdateRole(u.id, val)}
                          options={ROLE_OPTIONS}
                          placement="auto"
                          style={{ width: '110px' }}
                        />
                      </td>

                      {isSuperAdmin && (
                        <td>
                          <CustomSelect
                            value={u.isMaster || 0}
                            onChange={(val) => handleUpdateMasterStatus(u.id, val)}
                            options={ACCOUNT_TYPE_OPTIONS}
                            placement="auto"
                            style={{ width: '120px' }}
                          />
                        </td>
                      )}

                      {isSuperAdmin && (
                        <td>
                          <CustomSelect
                            value={u.isSettlement || 0}
                            onChange={(val) => handleUpdateSettlementStatus(u.id, val)}
                            options={SETTLEMENT_ATTRIBUTE_OPTIONS}
                            placement="auto"
                            style={{ width: '110px' }}
                          />
                        </td>
                      )}

                      {isSuperAdmin && (
                        <td>
                          {u.role === 'SUPER_ADMIN' ? (
                            <span style={{ fontSize: '0.78rem', color: '#10b981', fontWeight: 500 }} title="超级管理员无需分配，默认可见所有账户视图">
                              全量可看
                            </span>
                          ) : (
                            <button
                              className="btn btn-secondary"
                              style={{
                                padding: '0.25rem 0.55rem',
                                fontSize: '0.75rem',
                                gap: '0.3rem',
                                borderColor: editingViewPermissionUserId === u.id ? '#6366f1' : undefined
                              }}
                              title="点击分配该账户可查看的其他账户视图（只读）"
                              onClick={() => {
                                if (editingViewPermissionUserId === u.id) {
                                  setEditingViewPermissionUserId(null);
                                } else {
                                  setEditingViewPermissionUserId(u.id);
                                  setEditingSubAccountsUserId(null);
                                  setEditingPasswordUserId(null);
                                  setEditingPermissionsUserId(null);
                                  setSelectedViewPermissionIds(u.visibleUserIds || []);
                                }
                              }}
                            >
                              <Eye size={13} color="#6366f1" />
                              <span>{visibleCount > 0 ? `已分配 ${visibleCount}个` : '分配视图'}</span>
                            </button>
                          )}
                        </td>
                      )}

                      {isSuperAdmin && (
                        <td>
                          {isMaster ? (
                            <button
                              className="btn btn-secondary"
                              style={{
                                padding: '0.25rem 0.55rem',
                                fontSize: '0.75rem',
                                gap: '0.3rem',
                                borderColor: editingSubAccountsUserId === u.id ? '#8b5cf6' : 'rgba(139, 92, 246, 0.4)',
                                color: '#8b5cf6'
                              }}
                              title="点击勾选分配归属于该主账号的子账号"
                              onClick={() => {
                                if (editingSubAccountsUserId === u.id) {
                                  setEditingSubAccountsUserId(null);
                                } else {
                                  setEditingSubAccountsUserId(u.id);
                                  setEditingViewPermissionUserId(null);
                                  setEditingPasswordUserId(null);
                                  setEditingPermissionsUserId(null);
                                  setSelectedSubUserIds(u.subUserIds || []);
                                }
                              }}
                            >
                              <Network size={13} color="#8b5cf6" />
                              <span>{subCount > 0 ? `已关联 ${subCount}个` : '分配子账号'}</span>
                            </button>
                          ) : (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-sub)' }}>-</span>
                          )}
                        </td>
                      )}

                      {isSuperAdmin && (
                        <td>
                          {u.role === 'SUPER_ADMIN' ? (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
                              <span style={{ fontSize: '0.78rem', color: '#10b981', fontWeight: 600 }} title="超级管理员默认拥有所有平台与功能权限">
                                全量权限
                              </span>
                              <span style={{ fontSize: '0.7rem', color: '#059669', background: 'rgba(16,185,129,0.12)', padding: '0.05rem 0.35rem', borderRadius: 4, width: 'fit-content' }}>
                                默认全平台
                              </span>
                            </div>
                          ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', alignItems: 'flex-start' }}>
                              <button
                                className="btn btn-secondary"
                                style={{
                                  padding: '0.25rem 0.55rem',
                                  fontSize: '0.75rem',
                                  gap: '0.3rem',
                                  borderColor: editingPermissionsUserId === u.id ? '#0891b2' : '#3b82f6',
                                  color: editingPermissionsUserId === u.id ? '#0891b2' : '#3b82f6'
                                }}
                                title="点击配置该账户的平台数据权限与 6 项功能权限"
                                onClick={() => {
                                  if (editingPermissionsUserId === u.id) {
                                    setEditingPermissionsUserId(null);
                                  } else {
                                    setEditingPermissionsUserId(u.id);
                                    setEditingViewPermissionUserId(null);
                                    setEditingSubAccountsUserId(null);
                                    setEditingPasswordUserId(null);
                                    setSelectedPermissions({
                                      permPredictPayback: u.permPredictPayback || 0,
                                      permRoiPredict: u.permRoiPredict || 0,
                                      permGlobalDistribution: u.permGlobalDistribution || 0,
                                      permExport: u.permExport || 0,
                                      permSettlement: u.permSettlement || 0,
                                      permVideoGen: u.permVideoGen || 0,
                                    });
                                    const rawPlatforms = u.allowedPlatforms || 'ALL';
                                    setSelectedAllowedPlatforms(rawPlatforms.split(',').map(s => s.trim()).filter(Boolean));
                                  }
                                }}
                              >
                                <ShieldCheck size={13} color="#3b82f6" />
                                <span>{permCount > 0 ? `已开通 ${permCount}项` : '分配权限'}</span>
                              </button>
                              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                                {u.allowedPlatforms === 'ALL' || !u.allowedPlatforms ? '全平台' : u.allowedPlatforms}
                              </div>
                            </div>
                          )}
                        </td>
                      )}

                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.45rem' }}>
                          <button
                            className="btn btn-secondary"
                            style={{
                              padding: '0.25rem 0.45rem',
                              fontSize: '0.75rem',
                              borderColor: editingPasswordUserId === u.id ? '#f59e0b' : undefined,
                              color: editingPasswordUserId === u.id ? '#f59e0b' : undefined
                            }}
                            title="修改/重置登录密码"
                            onClick={() => {
                              if (editingPasswordUserId === u.id) {
                                setEditingPasswordUserId(null);
                              } else {
                                setEditingPasswordUserId(u.id);
                                setEditingViewPermissionUserId(null);
                                setEditingSubAccountsUserId(null);
                                setEditingPermissionsUserId(null);
                                setResetPasswordVal('');
                              }
                            }}
                          >
                            <KeyRound size={13} />
                            <span>改密</span>
                          </button>

                          <button
                            className="btn btn-danger"
                            style={{ padding: '0.25rem 0.45rem', fontSize: '0.75rem' }}
                            title={isCurrentSelf ? '不可删除当前登录账号' : '彻底删除该用户'}
                            disabled={isCurrentSelf}
                            onClick={() => handleDeleteUser(u)}
                          >
                            <Trash2 size={13} />
                          </button>
                        </div>
                      </td>
                    </tr>

                    {/* 展开面板 1：细粒度功能权限与平台配置 */}
                    {editingPermissionsUserId === u.id && (
                      <tr ref={expandedRowRef}>
                        <td colSpan={isSuperAdmin ? 9 : 4} style={{ background: 'var(--bg-hover)', padding: '1rem 1.25rem', borderBottom: '2px solid #3b82f6' }}>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                              <div style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                                <ShieldCheck size={16} color="#3b82f6" />
                                <span>配置用户「{u.username}」的平台数据权限与 6 项专属功能权限</span>
                              </div>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                                onClick={() => setEditingPermissionsUserId(null)}
                              >
                                取消
                              </button>
                            </div>

                            {/* 平台权限勾选 */}
                            <div>
                              <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                                  <Globe size={14} color="#06b6d4" />
                                  <span>允许访问的平台数据：</span>
                                </span>
                                <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.75rem' }}>
                                  <button
                                    type="button"
                                    onClick={() => setSelectedAllowedPlatforms(Array.from(new Set(['ALL', ...availablePlatforms.map(p => p.code)])))}
                                    style={{ background: 'none', border: 'none', color: '#06b6d4', cursor: 'pointer', padding: 0 }}
                                  >
                                    全选
                                  </button>
                                  <span style={{ color: 'var(--border-color)' }}>|</span>
                                  <button
                                    type="button"
                                    onClick={() => setSelectedAllowedPlatforms([])}
                                    style={{ background: 'none', border: 'none', color: 'var(--text-sub)', cursor: 'pointer', padding: 0 }}
                                  >
                                    清空
                                  </button>
                                </div>
                              </div>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.5rem' }}>
                                {availablePlatforms.map(p => {
                                  const checked = selectedAllowedPlatforms.includes('ALL') || selectedAllowedPlatforms.includes(p.code);
                                  return (
                                    <label
                                      key={p.code}
                                      style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '0.4rem',
                                        fontSize: '0.82rem',
                                        cursor: 'pointer',
                                        userSelect: 'none',
                                        color: checked ? '#0891b2' : 'var(--text-main)',
                                        fontWeight: checked ? 600 : 400
                                      }}
                                    >
                                      <input
                                        type="checkbox"
                                        checked={checked}
                                        onChange={() => togglePlatformCheckbox(p.code, selectedAllowedPlatforms, setSelectedAllowedPlatforms)}
                                        style={{ accentColor: '#06b6d4', width: 15, height: 15 }}
                                      />
                                      <span>{p.name} ({p.code})</span>
                                    </label>
                                  );
                                })}
                              </div>
                            </div>

                            {/* 6 项功能权限勾选 */}
                            <div>
                              <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                                <ShieldCheck size={14} color="#3b82f6" />
                                <span>专属功能模块权限：</span>
                              </div>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.6rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permPredictPayback ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permPredictPayback ? 600 : 400 }}>
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedPermissions.permPredictPayback)}
                                    onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permPredictPayback: e.target.checked ? 1 : 0 }))}
                                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                                  />
                                  <span>📈 预测回本（含LTV表格列）</span>
                                </label>

                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permRoiPredict ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permRoiPredict ? 600 : 400 }}>
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedPermissions.permRoiPredict)}
                                    onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permRoiPredict: e.target.checked ? 1 : 0 }))}
                                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                                  />
                                  <span>🎯（D30~D90）ROI 预测</span>
                                </label>

                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permGlobalDistribution ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permGlobalDistribution ? 600 : 400 }}>
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedPermissions.permGlobalDistribution)}
                                    onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permGlobalDistribution: e.target.checked ? 1 : 0 }))}
                                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                                  />
                                  <span>🌐 平台汇总</span>
                                </label>

                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permExport ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permExport ? 600 : 400 }}>
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedPermissions.permExport)}
                                    onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permExport: e.target.checked ? 1 : 0 }))}
                                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                                  />
                                  <span>📥 数据导出</span>
                                </label>

                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permSettlement ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permSettlement ? 600 : 400 }}>
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedPermissions.permSettlement)}
                                    onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permSettlement: e.target.checked ? 1 : 0 }))}
                                    style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                                  />
                                  <span>💳 月份结算</span>
                                </label>

                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permVideoGen ? '#8b5cf6' : 'var(--text-main)', fontWeight: selectedPermissions.permVideoGen ? 600 : 400 }}>
                                  <input
                                    type="checkbox"
                                    checked={Boolean(selectedPermissions.permVideoGen)}
                                    onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permVideoGen: e.target.checked ? 1 : 0 }))}
                                    style={{ accentColor: '#8b5cf6', width: 15, height: 15 }}
                                  />
                                  <span>🎬 AI视频生成</span>
                                </label>
                              </div>
                            </div>

                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '0.35rem' }}>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.25rem 0.65rem', fontSize: '0.78rem' }}
                                onClick={() => setEditingPermissionsUserId(null)}
                              >
                                取消
                              </button>
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.25rem 0.85rem', fontSize: '0.78rem' }}
                                onClick={() => handleSavePermissions(u.id)}
                              >
                                保存权限配置
                              </button>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}

                    {/* 展开面板 2：视图分配面板 */}
                    {editingViewPermissionUserId === u.id && (
                      <tr ref={expandedRowRef}>
                        <td colSpan={isSuperAdmin ? 9 : 4} style={{ background: 'var(--bg-hover)', padding: '1rem 1.25rem', borderBottom: '2px solid #6366f1' }}>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                              <div style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                                <Eye size={16} color="#6366f1" />
                                <span>配置用户「{u.username}」的跨视图只读查看权限</span>
                              </div>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                                onClick={() => setEditingViewPermissionUserId(null)}
                              >
                                取消
                              </button>
                            </div>
                            <p style={{ margin: 0, fontSize: '0.78rem', color: 'var(--text-sub)' }}>
                              勾选下方账号后，该用户登录系统后可在顶部「视图」切换框中切换查看被授权账号的数据（以只读形式，不可更改消耗或落地页配置）。
                            </p>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.85rem' }}>
                              {users.filter(target => target.id !== u.id).map(target => {
                                const checked = selectedViewPermissionIds.includes(target.id);
                                return (
                                  <label
                                    key={target.id}
                                    style={{
                                      display: 'flex',
                                      alignItems: 'center',
                                      gap: '0.35rem',
                                      fontSize: '0.82rem',
                                      cursor: 'pointer',
                                      userSelect: 'none',
                                      color: checked ? '#6366f1' : 'var(--text-main)',
                                      fontWeight: checked ? 600 : 400
                                    }}
                                  >
                                    <input
                                      type="checkbox"
                                      checked={checked}
                                      onChange={() => {
                                        if (selectedViewPermissionIds.includes(target.id)) {
                                          setSelectedViewPermissionIds(selectedViewPermissionIds.filter(id => id !== target.id));
                                        } else {
                                          setSelectedViewPermissionIds([...selectedViewPermissionIds, target.id]);
                                        }
                                      }}
                                      style={{ accentColor: '#6366f1' }}
                                    />
                                    <span>{target.username} (ID: {target.id})</span>
                                  </label>
                                );
                              })}
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '0.3rem' }}>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.25rem 0.65rem', fontSize: '0.78rem' }}
                                onClick={() => setEditingViewPermissionUserId(null)}
                              >
                                取消
                              </button>
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.25rem 0.85rem', fontSize: '0.78rem' }}
                                onClick={() => handleSaveViewPermissions(u.id)}
                              >
                                保存视图分配
                              </button>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}

                    {/* 展开面板 3：子账号关联面板 */}
                    {editingSubAccountsUserId === u.id && (
                      <tr ref={expandedRowRef}>
                        <td colSpan={isSuperAdmin ? 9 : 4} style={{ background: 'var(--bg-hover)', padding: '1rem 1.25rem', borderBottom: '2px solid #8b5cf6' }}>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                              <div style={{ fontSize: '0.88rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                                <Network size={16} color="#8b5cf6" />
                                <span>配置主账号「{u.username}」归属的子账号</span>
                              </div>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                                onClick={() => setEditingSubAccountsUserId(null)}
                              >
                                取消
                              </button>
                            </div>
                            <p style={{ margin: 0, fontSize: '0.78rem', color: 'var(--text-sub)' }}>
                              关联后，被勾选的子账号的落地页与投放消耗将自动解重并汇聚至该主账号中进行全局 LTV 和充值统计展示。
                            </p>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.85rem' }}>
                              {users.filter(target => target.id !== u.id && target.isMaster !== 1).map(target => {
                                const checked = selectedSubUserIds.includes(target.id);
                                return (
                                  <label
                                    key={target.id}
                                    style={{
                                      display: 'flex',
                                      alignItems: 'center',
                                      gap: '0.35rem',
                                      fontSize: '0.82rem',
                                      cursor: 'pointer',
                                      userSelect: 'none',
                                      color: checked ? '#8b5cf6' : 'var(--text-main)',
                                      fontWeight: checked ? 600 : 400
                                    }}
                                  >
                                    <input
                                      type="checkbox"
                                      checked={checked}
                                      onChange={() => {
                                        if (selectedSubUserIds.includes(target.id)) {
                                          setSelectedSubUserIds(selectedSubUserIds.filter(id => id !== target.id));
                                        } else {
                                          setSelectedSubUserIds([...selectedSubUserIds, target.id]);
                                        }
                                      }}
                                      style={{ accentColor: '#8b5cf6' }}
                                    />
                                    <span>{target.username} (ID: {target.id})</span>
                                  </label>
                                );
                              })}
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '0.3rem' }}>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.25rem 0.65rem', fontSize: '0.78rem' }}
                                onClick={() => setEditingSubAccountsUserId(null)}
                              >
                                取消
                              </button>
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.25rem 0.85rem', fontSize: '0.78rem', backgroundColor: '#8b5cf6', borderColor: '#8b5cf6' }}
                                onClick={() => handleSaveSubAccounts(u.id)}
                              >
                                保存子账号关联
                              </button>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}

                    {/* 展开面板 4：改密面板 */}
                    {editingPasswordUserId === u.id && (
                      <tr ref={expandedRowRef}>
                        <td colSpan={isSuperAdmin ? 9 : 4} style={{ background: 'var(--bg-hover)', padding: '0.9rem 1.25rem', borderBottom: '2px solid #f59e0b' }}>
                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                              <KeyRound size={16} color="#f59e0b" />
                              <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)' }}>
                                重置用户「{u.username}」的登录密码：
                              </span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: 1, maxWidth: '380px' }}>
                              <input
                                type="password"
                                className="form-input"
                                placeholder="输入新密码 (至少6位)"
                                value={resetPasswordVal}
                                onChange={(e) => setResetPasswordVal(e.target.value)}
                                style={{ fontSize: '0.8rem', padding: '0.3rem 0.6rem' }}
                              />
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.3rem 0.75rem', fontSize: '0.78rem', whiteSpace: 'nowrap', backgroundColor: '#f59e0b', borderColor: '#f59e0b' }}
                                onClick={() => handleResetPassword(u.id)}
                              >
                                确认重置
                              </button>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.3rem 0.6rem', fontSize: '0.78rem', whiteSpace: 'nowrap' }}
                                onClick={() => setEditingPasswordUserId(null)}
                              >
                                取消
                              </button>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
