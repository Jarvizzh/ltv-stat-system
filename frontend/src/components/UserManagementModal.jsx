import React, { useState, useEffect } from 'react';
import { Users, UserPlus, KeyRound, Trash2, X, Check, Shield, User } from 'lucide-react';
import CustomSelect from './CustomSelect';

const ROLE_OPTIONS = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' },
  { label: '超级管理员', value: 'SUPER_ADMIN' },
];

export default function UserManagementModal({ isOpen, onClose, token, onRefreshUsers, showToast }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState('USER');
  const [editingPasswordUserId, setEditingPasswordUserId] = useState(null);
  const [resetPasswordVal, setResetPasswordVal] = useState('');

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
        if (showToast) showToast('重置密码成功！', 'success');
        setEditingPasswordUserId(null);
        setResetPasswordVal('');
      } else {
        if (showToast) showToast(data.msg || '重置密码失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('重置密码异常', 'error');
    }
  };

  const handleUpdateRole = async (userId, targetRole) => {
    try {
      const res = await fetch(`/api/admin/users/${userId}/role`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ role: targetRole })
      });
      const data = await res.json();
      if (res.ok && data.code === 0) {
        if (showToast) showToast('修改用户角色成功！', 'success');
        fetchUsers();
      } else {
        if (showToast) showToast(data.msg || '修改角色失败', 'error');
      }
    } catch (e) {
      if (showToast) showToast('修改角色异常', 'error');
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

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card modal-card-lg" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 650 }}>
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
              管理员可以自由创建新账号、重置密码以及灵活修改用户角色权限。
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
            <div className="user-table-container" style={{ paddingBottom: '80px' }}>
              <table className="user-mgmt-table">
                <thead>
                  <tr>
                    <th style={{ width: 60 }}>ID</th>
                    <th>账号名称</th>
                    <th>角色权限 (点击切换)</th>
                    <th style={{ width: 140, textAlign: 'right' }}>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <React.Fragment key={u.id}>
                      <tr>
                        <td>{u.id}</td>
                        <td>
                          <span style={{ fontWeight: 600 }}>{u.username}</span>
                        </td>
                        <td>
                          <CustomSelect
                            value={u.role}
                            onChange={(val) => handleUpdateRole(u.id, val)}
                            options={ROLE_OPTIONS}
                            placement="auto"
                            style={{ width: '120px' }}
                          />
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
                            <button
                              className="btn btn-secondary"
                              style={{ padding: '0.25rem 0.4rem', fontSize: '0.75rem' }}
                              title="修改密码"
                              onClick={() => {
                                setEditingPasswordUserId(editingPasswordUserId === u.id ? null : u.id);
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
                      {editingPasswordUserId === u.id && (
                        <tr>
                          <td colSpan={4} style={{ background: 'var(--bg-secondary)', padding: '0.75rem 1rem' }}>
                            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', maxWidth: 450 }}>
                              <span style={{ fontSize: '0.85rem', color: 'var(--text-sub)', whiteSpace: 'nowrap' }}>
                                设置 [{u.username}] 新密码:
                              </span>
                              <input
                                type="password"
                                className="form-input"
                                placeholder="输入新密码"
                                value={resetPasswordVal}
                                onChange={(e) => setResetPasswordVal(e.target.value)}
                                style={{ padding: '0.4rem 0.6rem', fontSize: '0.85rem' }}
                              />
                              <button
                                className="btn btn-primary"
                                style={{ padding: '0.4rem 0.8rem', fontSize: '0.75rem', whiteSpace: 'nowrap' }}
                                onClick={() => handleResetPassword(u.id)}
                              >
                                保存
                              </button>
                              <button
                                className="btn btn-secondary"
                                style={{ padding: '0.4rem 0.6rem', fontSize: '0.75rem' }}
                                onClick={() => setEditingPasswordUserId(null)}
                              >
                                取消
                              </button>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
