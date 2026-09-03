import React, { useState, useEffect, useRef } from 'react';
import { Users, UserPlus, KeyRound, Trash2, X, Check, Eye, Network, ShieldCheck } from 'lucide-react';
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

export default function UserManagementModal({ isOpen, onClose, token, currentUser, onRefreshUsers, showToast }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState('USER');
  const [newIsMaster, setNewIsMaster] = useState(0);
  const [newIsSettlement, setNewIsSettlement] = useState(0);
  const [newVisibleUserIds, setNewVisibleUserIds] = useState([]);
  const [newSubUserIds, setNewSubUserIds] = useState([]);
  const [newPermissions, setNewPermissions] = useState({
    permPredictPayback: 0,
    permRoiPredict: 0,
    permGlobalDistribution: 0,
    permExport: 0,
    permSettlement: 0,
  });

  const [editingPasswordUserId, setEditingPasswordUserId] = useState(null);
  const [resetPasswordVal, setResetPasswordVal] = useState('');

  const [editingViewPermissionUserId, setEditingViewPermissionUserId] = useState(null);
  const [selectedViewPermissionIds, setSelectedViewPermissionIds] = useState([]);

  const [editingSubAccountsUserId, setEditingSubAccountsUserId] = useState(null);
  const [selectedSubUserIds, setSelectedSubUserIds] = useState([]);

  const [editingPermissionsUserId, setEditingPermissionsUserId] = useState(null);
  const [selectedPermissions, setSelectedPermissions] = useState({
    permPredictPayback: 0,
    permRoiPredict: 0,
    permGlobalDistribution: 0,
    permExport: 0,
    permSettlement: 0,
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
    setNewPermissions({
      permPredictPayback: 0,
      permRoiPredict: 0,
      permGlobalDistribution: 0,
      permExport: 0,
      permSettlement: 0,
    });
    setShowAddForm(false);
  };

  useEffect(() => {
    if ((editingViewPermissionUserId || editingSubAccountsUserId || editingPermissionsUserId) && expandedRowRef.current) {
      setTimeout(() => {
        expandedRowRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }, 50);
    }
  }, [editingViewPermissionUserId, editingSubAccountsUserId, editingPermissionsUserId]);

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
    if (isOpen) {
      fetchUsers();
      resetAddForm();
      setEditingPasswordUserId(null);
      setEditingViewPermissionUserId(null);
      setEditingSubAccountsUserId(null);
      setEditingPermissionsUserId(null);
    }
  }, [isOpen]);

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

  const handleUpdateRole = async (userId, newRole) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/role`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ role: newRole })
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

  const handleUpdateMasterStatus = async (userId, isMaster) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/master-status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ isMaster })
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

  const handleSavePermissions = async (userId) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/permissions`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(selectedPermissions)
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('功能权限分配保存成功！', 'success');
        setEditingPermissionsUserId(null);
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '保存功能权限失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('保存功能权限异常', 'error');
    }
  };

  const handleDeleteUser = async (user) => {
    if (!window.confirm(`确定要删除用户 "${user.username}" 吗？此操作无法撤销。`)) return;
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

  const toggleViewPermissionCheckbox = (targetId) => {
    if (selectedViewPermissionIds.includes(targetId)) {
      setSelectedViewPermissionIds(selectedViewPermissionIds.filter(id => id !== targetId));
    } else {
      setSelectedViewPermissionIds([...selectedViewPermissionIds, targetId]);
    }
  };

  const toggleSubAccountCheckbox = (subId) => {
    if (selectedSubUserIds.includes(subId)) {
      setSelectedSubUserIds(selectedSubUserIds.filter(id => id !== subId));
    } else {
      setSelectedSubUserIds([...selectedSubUserIds, subId]);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card modal-card-lg" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 1140, width: '92vw' }}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Users size={20} className="modal-header-icon" />
            <h3 className="modal-title">用户账号与权限管理</h3>
          </div>
          <button className="btn btn-secondary" style={{ padding: '0.25rem' }} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="modal-body" style={{ maxHeight: '78vh', overflowY: 'auto' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-sub)' }}>
              管理员可管理用户账号。超级管理员可标记「主账号(汇总)」、关联分配子账号、设置跨视图（只读）查看权限以及分配 4 项专属功能权限。
            </p>
            <button
              className="btn btn-primary"
              style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem', whiteSpace: 'nowrap', flexShrink: 0 }}
              onClick={() => setShowAddForm(!showAddForm)}
            >
              <UserPlus size={14} />
              <span>{showAddForm ? '取消新增' : '新增用户'}</span>
            </button>
          </div>

          {showAddForm && (
            <form onSubmit={handleCreateUser} style={{
              background: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)',
              borderRadius: '0.5rem',
              padding: '1.15rem',
              marginBottom: '1.25rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.9rem',
              boxShadow: '0 4px 14px rgba(0,0,0,0.08)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                <div style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <UserPlus size={16} color="#6366f1" />
                  <span>新建用户账号及权限配置</span>
                </div>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ padding: '0.2rem 0.55rem', fontSize: '0.75rem' }}
                  onClick={resetAddForm}
                >
                  取消
                </button>
              </div>

              {/* 第一行：基本账号信息 */}
              <div style={{ display: 'grid', gridTemplateColumns: isSuperAdmin ? '1fr 1fr 130px 140px' : '1fr 1fr 140px', gap: '0.75rem', alignItems: 'center' }}>
                <div>
                  <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.25rem', display: 'block', fontWeight: 500 }}>账号名称</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="请输入账号 (username)"
                    value={newUsername}
                    onChange={(e) => setNewUsername(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.25rem', display: 'block', fontWeight: 500 }}>初始密码</label>
                  <input
                    type="password"
                    className="form-input"
                    placeholder="请输入初始密码"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.25rem', display: 'block', fontWeight: 500 }}>角色级别</label>
                  <CustomSelect
                    value={newRole}
                    onChange={(val) => setNewRole(val)}
                    options={ROLE_OPTIONS}
                    style={{ width: '100%' }}
                  />
                </div>
                {isSuperAdmin && (
                  <div>
                    <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.25rem', display: 'block', fontWeight: 500 }}>账户类型</label>
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
                    <label style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginBottom: '0.25rem', display: 'block', fontWeight: 500 }}>参与结算</label>
                    <CustomSelect
                      value={newIsSettlement}
                      onChange={(val) => setNewIsSettlement(Number(val))}
                      options={SETTLEMENT_ATTRIBUTE_OPTIONS}
                      style={{ width: '100%' }}
                    />
                  </div>
                )}
              </div>

              {/* 第二行：功能权限分配 (4 项) */}
              {isSuperAdmin && (
                <div style={{ background: 'var(--bg-hover)', padding: '0.75rem 1rem', borderRadius: '0.4rem', border: '1px solid var(--border-light)' }}>
                  <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <ShieldCheck size={14} color="#3b82f6" />
                    <span>功能权限配置</span>
                    {newRole === 'SUPER_ADMIN' && (
                      <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 400 }}>（超级管理员默认拥有全量权限）</span>
                    )}
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))', gap: '0.6rem' }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permPredictPayback) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permPredictPayback) ? 600 : 400 }}>
                      <input
                        type="checkbox"
                        disabled={newRole === 'SUPER_ADMIN'}
                        checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permPredictPayback)}
                        onChange={(e) => setNewPermissions(prev => ({ ...prev, permPredictPayback: e.target.checked ? 1 : 0 }))}
                        style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                      />
                      <span>📈 预测回本（含LTV表格列）</span>
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permRoiPredict) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permRoiPredict) ? 600 : 400 }}>
                      <input
                        type="checkbox"
                        disabled={newRole === 'SUPER_ADMIN'}
                        checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permRoiPredict)}
                        onChange={(e) => setNewPermissions(prev => ({ ...prev, permRoiPredict: e.target.checked ? 1 : 0 }))}
                        style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                      />
                      <span>🎯（D30~D90）ROI 预测</span>
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permGlobalDistribution) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permGlobalDistribution) ? 600 : 400 }}>
                      <input
                        type="checkbox"
                        disabled={newRole === 'SUPER_ADMIN'}
                        checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permGlobalDistribution)}
                        onChange={(e) => setNewPermissions(prev => ({ ...prev, permGlobalDistribution: e.target.checked ? 1 : 0 }))}
                        style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                      />
                      <span>🌐 平台汇总</span>
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permExport) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permExport) ? 600 : 400 }}>
                      <input
                        type="checkbox"
                        disabled={newRole === 'SUPER_ADMIN'}
                        checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permExport)}
                        onChange={(e) => setNewPermissions(prev => ({ ...prev, permExport: e.target.checked ? 1 : 0 }))}
                        style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                      />
                      <span>📥 数据导出</span>
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', cursor: newRole === 'SUPER_ADMIN' ? 'not-allowed' : 'pointer', userSelect: 'none', color: (newRole === 'SUPER_ADMIN' || newPermissions.permSettlement) ? '#3b82f6' : 'var(--text-main)', fontWeight: (newRole === 'SUPER_ADMIN' || newPermissions.permSettlement) ? 600 : 400 }}>
                      <input
                        type="checkbox"
                        disabled={newRole === 'SUPER_ADMIN'}
                        checked={newRole === 'SUPER_ADMIN' || Boolean(newPermissions.permSettlement)}
                        onChange={(e) => setNewPermissions(prev => ({ ...prev, permSettlement: e.target.checked ? 1 : 0 }))}
                        style={{ accentColor: '#3b82f6', width: 15, height: 15 }}
                      />
                      <span>💳 结算</span>
                    </label>
                  </div>
                </div>
              )}

              {/* 第三行：视图分配 (可选) */}
              {isSuperAdmin && newRole !== 'SUPER_ADMIN' && users.length > 0 && (
                <div style={{ background: 'var(--bg-hover)', padding: '0.75rem 1rem', borderRadius: '0.4rem', border: '1px solid var(--border-light)' }}>
                  <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Eye size={14} color="#6366f1" />
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

              {/* 第四行：主账号子账号关联 (若 newIsMaster === 1) */}
              {isSuperAdmin && newIsMaster === 1 && (
                <div style={{ background: 'var(--bg-hover)', padding: '0.75rem 1rem', borderRadius: '0.4rem', border: '1px solid var(--border-light)' }}>
                  <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Network size={14} color="#8b5cf6" />
                    <span>关联子账号（勾选归属于该主账号的子账号，消耗自动累加，落地页自动解重）：</span>
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
                    {users.filter(target => target.isMaster !== 1).length === 0 && (
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)' }}>暂无可关联的普通/子账号</span>
                    )}
                  </div>
                </div>
              )}

              {/* 底部提交按钮 */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.6rem', marginTop: '0.25rem' }}>
                <button type="button" className="btn btn-secondary" onClick={resetAddForm} style={{ padding: '0.45rem 1rem' }}>
                  取消
                </button>
                <button type="submit" className="btn btn-primary" style={{ padding: '0.45rem 1.25rem' }}>
                  <Check size={15} /> 确认创建用户
                </button>
              </div>
            </form>
          )}

          {loading ? (
            <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-sub)' }}>加载用户列表中...</div>
          ) : (
            <div className="user-table-container" style={{ paddingBottom: '180px' }}>
              <table className="user-mgmt-table">
                <thead>
                  <tr>
                    <th style={{ width: 45, whiteSpace: 'nowrap' }}>ID</th>
                    <th style={{ minWidth: 120, whiteSpace: 'nowrap' }}>账号名称</th>
                    <th style={{ width: 125, whiteSpace: 'nowrap' }}>角色权限</th>
                    {isSuperAdmin && <th style={{ width: 135, whiteSpace: 'nowrap' }}>账户类型</th>}
                    {isSuperAdmin && <th style={{ width: 115, whiteSpace: 'nowrap' }}>参与结算</th>}
                    {isSuperAdmin && <th style={{ width: 125, whiteSpace: 'nowrap' }}>视图分配</th>}
                    {isSuperAdmin && <th style={{ width: 125, whiteSpace: 'nowrap' }}>子账号关联</th>}
                    {isSuperAdmin && <th style={{ width: 125, whiteSpace: 'nowrap' }}>功能权限</th>}
                    <th style={{ width: 90, textAlign: 'right', whiteSpace: 'nowrap' }}>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => {
                    const visibleCount = u.visibleUserIds ? u.visibleUserIds.length : 0;
                    const subCount = u.subUserIds ? u.subUserIds.length : 0;
                    const isMaster = u.isMaster === 1;
                    const permCount = [u.permPredictPayback, u.permRoiPredict, u.permGlobalDistribution, u.permExport, u.permSettlement].filter(p => p === 1).length;

                    return (
                      <React.Fragment key={u.id}>
                        <tr>
                          <td>{u.id}</td>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                              <span style={{ fontWeight: 600 }}>{u.username}</span>
                              {isMaster && (
                                <span style={{
                                  fontSize: '0.7rem',
                                  padding: '0.1rem 0.35rem',
                                  borderRadius: 4,
                                  background: 'rgba(139, 92, 246, 0.15)',
                                  color: '#8b5cf6',
                                  fontWeight: 600,
                                  whiteSpace: 'nowrap'
                                }}>
                                  主账号
                                </span>
                              )}
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
                                  style={{ padding: '0.25rem 0.45rem', fontSize: '0.75rem', gap: '0.25rem' }}
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
                                  style={{ padding: '0.25rem 0.45rem', fontSize: '0.75rem', gap: '0.25rem', borderColor: '#8b5cf6', color: '#8b5cf6' }}
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
                                <span style={{ fontSize: '0.78rem', color: '#10b981', fontWeight: 500 }} title="超级管理员默认拥有所有功能权限">
                                  全量权限
                                </span>
                              ) : (
                                <button
                                  className="btn btn-secondary"
                                  style={{ padding: '0.25rem 0.45rem', fontSize: '0.75rem', gap: '0.25rem', borderColor: '#3b82f6', color: '#3b82f6' }}
                                  title="点击分配该账户的 5 项功能权限（预测回本、ROI预测、平台汇总、数据导出、月份结算）"
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
                                      });
                                    }
                                  }}
                                >
                                  <ShieldCheck size={13} color="#3b82f6" />
                                  <span>{permCount > 0 ? `已开通 ${permCount}项` : '分配权限'}</span>
                                </button>
                              )}
                            </td>
                          )}

                          <td style={{ textAlign: 'right' }}>
                            <div style={{ display: 'flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.25rem 0.4rem', fontSize: '0.75rem' }}
                                title="修改密码"
                                onClick={() => {
                                  setEditingPasswordUserId(editingPasswordUserId === u.id ? null : u.id);
                                  setEditingViewPermissionUserId(null);
                                  setEditingSubAccountsUserId(null);
                                  setEditingPermissionsUserId(null);
                                  setResetPasswordVal('');
                                }}
                              >
                                <KeyRound size={13} />
                              </button>

                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.25rem 0.4rem', fontSize: '0.75rem', color: 'var(--accent-rose)' }}
                                title="删除用户"
                                onClick={() => handleDeleteUser(u)}
                              >
                                <Trash2 size={13} />
                              </button>
                            </div>
                          </td>
                        </tr>

                        {/* 修改密码展开行 */}
                        {editingPasswordUserId === u.id && (
                          <tr>
                            <td colSpan={isSuperAdmin ? 9 : 4} style={{ background: 'var(--bg-secondary)', padding: '0.75rem 1rem' }}>
                              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'nowrap', width: '100%', maxWidth: 560 }}>
                                <span style={{ fontSize: '0.85rem', color: 'var(--text-sub)', whiteSpace: 'nowrap', flexShrink: 0 }}>
                                  设置 [{u.username}] 新密码:
                                </span>
                                <input
                                  type="password"
                                  className="form-input"
                                  placeholder="输入新密码"
                                  value={resetPasswordVal}
                                  onChange={(e) => setResetPasswordVal(e.target.value)}
                                  style={{ padding: '0.4rem 0.6rem', fontSize: '0.85rem', flex: 1, minWidth: 120 }}
                                />
                                <button
                                  className="btn btn-primary"
                                  style={{ padding: '0.4rem 0.8rem', fontSize: '0.75rem', whiteSpace: 'nowrap', flexShrink: 0 }}
                                  onClick={() => handleResetPassword(u.id)}
                                >
                                  保存
                                </button>
                                <button
                                  className="btn btn-secondary"
                                  style={{ padding: '0.4rem 0.6rem', fontSize: '0.75rem', whiteSpace: 'nowrap', flexShrink: 0 }}
                                  onClick={() => setEditingPasswordUserId(null)}
                                >
                                  取消
                                </button>
                              </div>
                            </td>
                          </tr>
                        )}

                        {/* 分配只读视图权限展开行 */}
                        {isSuperAdmin && editingViewPermissionUserId === u.id && (
                          <tr ref={expandedRowRef}>
                            <td colSpan={9} style={{ background: 'var(--bg-secondary)', padding: '0.85rem 1rem', borderTop: '1px solid var(--border-color)' }}>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)' }}>
                                    勾选允许账号 <span style={{ color: '#6366f1' }}>[{u.username}]</span> 跨视图查看（只读）的其他账户：
                                  </span>
                                  <div style={{ display: 'flex', gap: '0.4rem' }}>
                                    <button
                                      className="btn btn-primary"
                                      style={{ padding: '0.35rem 0.75rem', fontSize: '0.78rem' }}
                                      onClick={() => handleSaveViewPermissions(u.id)}
                                    >
                                      保存分配
                                    </button>
                                    <button
                                      className="btn btn-secondary"
                                      style={{ padding: '0.35rem 0.6rem', fontSize: '0.78rem' }}
                                      onClick={() => setEditingViewPermissionUserId(null)}
                                    >
                                      取消
                                    </button>
                                  </div>
                                </div>

                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', background: 'var(--bg-hover)', padding: '0.65rem 0.85rem', borderRadius: '0.4rem', border: '1px solid var(--border-light)' }}>
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
                                          onChange={() => toggleViewPermissionCheckbox(target.id)}
                                          style={{ accentColor: '#6366f1' }}
                                        />
                                        <span>{target.username} (ID: {target.id})</span>
                                      </label>
                                    );
                                  })}
                                  {users.length <= 1 && (
                                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)' }}>暂无其它账户可分配</span>
                                  )}
                                </div>
                              </div>
                            </td>
                          </tr>
                        )}

                        {/* 分配子账号关联展开行 */}
                        {isSuperAdmin && editingSubAccountsUserId === u.id && (
                          <tr ref={expandedRowRef}>
                            <td colSpan={9} style={{ background: 'var(--bg-secondary)', padding: '0.85rem 1rem', borderTop: '1px solid var(--border-color)' }}>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)' }}>
                                    勾选归属于主账号 <span style={{ color: '#8b5cf6' }}>[{u.username}]</span> 的子账号（消耗将自动累加求和，落地页自动解重聚合）：
                                  </span>
                                  <div style={{ display: 'flex', gap: '0.4rem' }}>
                                    <button
                                      className="btn btn-primary"
                                      style={{ padding: '0.35rem 0.75rem', fontSize: '0.78rem', backgroundColor: '#8b5cf6', borderColor: '#8b5cf6' }}
                                      onClick={() => handleSaveSubAccounts(u.id)}
                                    >
                                      保存关联
                                    </button>
                                    <button
                                      className="btn btn-secondary"
                                      style={{ padding: '0.35rem 0.6rem', fontSize: '0.78rem' }}
                                      onClick={() => setEditingSubAccountsUserId(null)}
                                    >
                                      取消
                                    </button>
                                  </div>
                                </div>

                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', background: 'var(--bg-hover)', padding: '0.65rem 0.85rem', borderRadius: '0.4rem', border: '1px solid var(--border-light)' }}>
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
                                          onChange={() => toggleSubAccountCheckbox(target.id)}
                                          style={{ accentColor: '#8b5cf6' }}
                                        />
                                        <span>{target.username} (ID: {target.id})</span>
                                      </label>
                                    );
                                  })}
                                  {users.filter(target => target.id !== u.id && target.isMaster !== 1).length === 0 && (
                                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)' }}>暂无可分配的普通/子账号</span>
                                  )}
                                </div>
                              </div>
                            </td>
                          </tr>
                        )}

                        {/* 分配功能权限展开行 */}
                        {isSuperAdmin && editingPermissionsUserId === u.id && (
                          <tr ref={expandedRowRef}>
                            <td colSpan={9} style={{ background: 'var(--bg-secondary)', padding: '0.85rem 1rem', borderTop: '1px solid var(--border-color)' }}>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)' }}>
                                    设置账号 <span style={{ color: '#3b82f6' }}>[{u.username}]</span> 的 5 项专属功能权限：
                                  </span>
                                  <div style={{ display: 'flex', gap: '0.4rem' }}>
                                    <button
                                      className="btn btn-primary"
                                      style={{ padding: '0.35rem 0.75rem', fontSize: '0.78rem', backgroundColor: '#3b82f6', borderColor: '#3b82f6' }}
                                      onClick={() => handleSavePermissions(u.id)}
                                    >
                                      保存权限
                                    </button>
                                    <button
                                      className="btn btn-secondary"
                                      style={{ padding: '0.35rem 0.6rem', fontSize: '0.78rem' }}
                                      onClick={() => setEditingPermissionsUserId(null)}
                                    >
                                      取消
                                    </button>
                                  </div>
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.75rem', background: 'var(--bg-hover)', padding: '0.85rem 1rem', borderRadius: '0.4rem', border: '1px solid var(--border-light)' }}>
                                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permPredictPayback ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permPredictPayback ? 600 : 400 }}>
                                    <input
                                      type="checkbox"
                                      checked={Boolean(selectedPermissions.permPredictPayback)}
                                      onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permPredictPayback: e.target.checked ? 1 : 0 }))}
                                      style={{ accentColor: '#3b82f6', width: 16, height: 16 }}
                                    />
                                    <span>📈 预测回本（含LTV表格列）</span>
                                  </label>

                                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permRoiPredict ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permRoiPredict ? 600 : 400 }}>
                                    <input
                                      type="checkbox"
                                      checked={Boolean(selectedPermissions.permRoiPredict)}
                                      onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permRoiPredict: e.target.checked ? 1 : 0 }))}
                                      style={{ accentColor: '#3b82f6', width: 16, height: 16 }}
                                    />
                                    <span>🎯（D30~D90）ROI 预测</span>
                                  </label>

                                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permGlobalDistribution ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permGlobalDistribution ? 600 : 400 }}>
                                    <input
                                      type="checkbox"
                                      checked={Boolean(selectedPermissions.permGlobalDistribution)}
                                      onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permGlobalDistribution: e.target.checked ? 1 : 0 }))}
                                      style={{ accentColor: '#3b82f6', width: 16, height: 16 }}
                                    />
                                    <span>🌐 平台汇总</span>
                                  </label>

                                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permExport ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permExport ? 600 : 400 }}>
                                    <input
                                      type="checkbox"
                                      checked={Boolean(selectedPermissions.permExport)}
                                      onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permExport: e.target.checked ? 1 : 0 }))}
                                      style={{ accentColor: '#3b82f6', width: 16, height: 16 }}
                                    />
                                    <span>📥 数据导出</span>
                                  </label>

                                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.82rem', cursor: 'pointer', userSelect: 'none', color: selectedPermissions.permSettlement ? '#3b82f6' : 'var(--text-main)', fontWeight: selectedPermissions.permSettlement ? 600 : 400 }}>
                                    <input
                                      type="checkbox"
                                      checked={Boolean(selectedPermissions.permSettlement)}
                                      onChange={(e) => setSelectedPermissions(prev => ({ ...prev, permSettlement: e.target.checked ? 1 : 0 }))}
                                      style={{ accentColor: '#3b82f6', width: 16, height: 16 }}
                                    />
                                    <span>💳 结算</span>
                                  </label>
                                </div>
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
