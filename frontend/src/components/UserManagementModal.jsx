import React, { useState, useEffect, useRef } from 'react';
import { Users, UserPlus, KeyRound, Trash2, X, Check, Eye, Network } from 'lucide-react';
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

export default function UserManagementModal({ isOpen, onClose, token, currentUser, onRefreshUsers, showToast }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState('USER');
  const [editingPasswordUserId, setEditingPasswordUserId] = useState(null);
  const [resetPasswordVal, setResetPasswordVal] = useState('');

  const [editingViewPermissionUserId, setEditingViewPermissionUserId] = useState(null);
  const [selectedViewPermissionIds, setSelectedViewPermissionIds] = useState([]);

  const [editingSubAccountsUserId, setEditingSubAccountsUserId] = useState(null);
  const [selectedSubUserIds, setSelectedSubUserIds] = useState([]);

  const expandedRowRef = useRef(null);

  const isSuperAdmin = currentUser && currentUser.role === 'SUPER_ADMIN';

  useEffect(() => {
    if ((editingViewPermissionUserId || editingSubAccountsUserId) && expandedRowRef.current) {
      setTimeout(() => {
        expandedRowRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }, 50);
    }
  }, [editingViewPermissionUserId, editingSubAccountsUserId]);

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
      setShowAddForm(false);
      setEditingPasswordUserId(null);
      setEditingViewPermissionUserId(null);
      setEditingSubAccountsUserId(null);
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
          role: newRole
        })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('创建用户成功！', 'success');
        setNewUsername('');
        setNewPassword('');
        setShowAddForm(false);
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '创建用户失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('创建用户请求异常', 'error');
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
      <div className="modal-card modal-card-lg" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 860 }}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Users size={20} className="modal-header-icon" />
            <h3 className="modal-title">用户账号与权限管理</h3>
          </div>
          <button className="btn btn-secondary" style={{ padding: '0.25rem' }} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="modal-body" style={{ maxHeight: '72vh', overflowY: 'auto' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-sub)' }}>
              管理员可管理用户账号。超级管理员可标记「主账号(汇总)」、关联分配子账号以及设置跨视图（只读）查看权限。
            </p>
            <button
              className="btn btn-primary"
              style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem' }}
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
              padding: '1rem',
              marginBottom: '1rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem'
            }}>
              <div style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)' }}>新建用户账号</div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 120px auto', gap: '0.5rem', alignItems: 'center' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="账号 (username)"
                  value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)}
                  required
                />
                <input
                  type="password"
                  className="form-input"
                  placeholder="初始密码"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                />
                <CustomSelect
                  value={newRole}
                  onChange={(val) => setNewRole(val)}
                  options={ROLE_OPTIONS}
                  style={{ width: '120px' }}
                />
                <button type="submit" className="btn btn-primary" style={{ padding: '0.5rem 1rem' }}>
                  <Check size={14} /> 创建
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
                    <th style={{ width: 45 }}>ID</th>
                    <th>账号名称</th>
                    <th>角色权限</th>
                    {isSuperAdmin && <th>账户类型</th>}
                    {isSuperAdmin && <th>分配只读视图</th>}
                    {isSuperAdmin && <th>子账号关联</th>}
                    <th style={{ width: 100, textAlign: 'right' }}>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => {
                    const visibleCount = u.visibleUserIds ? u.visibleUserIds.length : 0;
                    const subCount = u.subUserIds ? u.subUserIds.length : 0;
                    const isMaster = u.isMaster === 1;

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
                            <td colSpan={isSuperAdmin ? 7 : 4} style={{ background: 'var(--bg-secondary)', padding: '0.75rem 1rem' }}>
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
                                  style={{ padding: '0.4rem 0.8rem', fontSize: '0.75rem', whiteSpace: 'nowrap', flexShrink: 0 }}
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
                            <td colSpan={7} style={{ background: 'var(--bg-secondary)', padding: '0.85rem 1rem', borderTop: '1px solid var(--border-color)' }}>
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
                            <td colSpan={7} style={{ background: 'var(--bg-secondary)', padding: '0.85rem 1rem', borderTop: '1px solid var(--border-color)' }}>
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
