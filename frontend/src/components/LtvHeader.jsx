import React from 'react';
import { BarChart3, PieChart, Settings, RefreshCw, Sun, Moon, Upload, Users, LogOut, Shield, Eye, Globe, Download } from 'lucide-react';
import CustomSelect from './CustomSelect';

export default function LtvHeader({
  activeTab,
  onTabChange,
  onOpenConfig,
  onOpenTokenModal,
  onOpenSyncModal,
  onOpenBatchSpend,
  onOpenUserManagement,
  onOpenExportModal,
  currentUser,
  usersList,
  targetUserId,
  onSelectTargetUser,
  onLogout,
  loading,
  theme,
  onToggleTheme
}) {
  const isSuperAdmin = currentUser && currentUser.role === 'SUPER_ADMIN';
  const isAdmin = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'SUPER_ADMIN');
  const isReadOnly = Boolean(isSuperAdmin && targetUserId && currentUser && targetUserId !== currentUser.userId);

  return (
    <header className="app-header">
      <div className="header-left-group">
        <div className="header-brand">
          <div className="brand-icon">
            <BarChart3 size={22} />
          </div>
          <div>
            <h1 className="brand-title">Meta-LTV</h1>
          </div>
        </div>

        {/* 核心导航栏 Tab 切换 */}
        <nav className="header-nav-tabs">
          <button
            className={`nav-tab-btn ${activeTab === 'ltv' ? 'active' : ''}`}
            onClick={() => onTabChange('ltv')}
          >
            <BarChart3 size={16} />
            <span>LTV 报表</span>
          </button>
          <button
            className={`nav-tab-btn ${activeTab === 'distribution' ? 'active' : ''}`}
            onClick={() => onTabChange('distribution')}
          >
            <PieChart size={16} />
            <span>充值分析</span>
          </button>
          {isAdmin && (
            <button
              className={`nav-tab-btn ${activeTab === 'global-distribution' ? 'active' : ''}`}
              onClick={() => onTabChange('global-distribution')}
              title="平台所有订单充值汇总（不区分落地页，全量数据）"
            >
              <Globe size={16} />
              <span>平台汇总</span>
            </button>
          )}
        </nav>
      </div>

      <div className="header-actions">
        {/* 超级管理员视图切换下拉框 (多视图仅超级管理员可见) */}
        {isSuperAdmin && usersList && usersList.length > 0 && (
          <div
            title="超级管理员可无缝切换查看不同账号的数据视图"
            style={{
              position: 'relative',
              zIndex: 1001,
              display: 'flex',
              alignItems: 'center',
              gap: '0.4rem',
              background: isReadOnly ? 'rgba(244, 63, 94, 0.12)' : 'rgba(99, 102, 241, 0.12)',
              border: isReadOnly ? '1px solid rgba(244, 63, 94, 0.3)' : '1px solid rgba(99, 102, 241, 0.3)',
              borderRadius: '0.5rem',
              padding: '0.35rem 0.65rem'
            }}
          >
            <Eye size={16} color={isReadOnly ? '#f43f5e' : '#6366f1'} />
            <span style={{ fontSize: '0.78rem', color: isReadOnly ? '#f43f5e' : '#6366f1', fontWeight: 600, whiteSpace: 'nowrap' }}>
              {isReadOnly ? '视图(只读):' : '视图:'}
            </span>
            <CustomSelect
              value={targetUserId || currentUser?.userId || ''}
              onChange={(val) => onSelectTargetUser(Number(val))}
              options={usersList.map((u) => ({ label: u.username, value: u.id }))}
              style={{ minWidth: '110px' }}
            />
          </div>
        )}

        {/* 超级管理员用户管理按钮 (用户管理仅超级管理员可见) */}
        {isSuperAdmin && (
          <button className="btn btn-secondary" onClick={onOpenUserManagement} title="用户账号与配置管理">
            <Users size={16} color="#6366f1" />
            <span>用户管理</span>
          </button>
        )}

        {/* API 设置按钮 (仅管理员/超级管理员可见) */}
        {isAdmin && (
          <button
            className="btn btn-secondary"
            onClick={onOpenTokenModal}
            disabled={isReadOnly}
            title={isReadOnly ? '非登录用户视图只可查看，不可修改 API Token' : '配置第三方订单数据同步 Token与 Cookie'}
          >
            <Settings size={16} />
            <span>API 设置</span>
          </button>
        )}

        <button
          className="btn btn-secondary"
          onClick={onOpenConfig}
          disabled={isReadOnly}
          title={isReadOnly ? '非登录用户视图只可查看，不可在此编辑落地页' : '管理当前账户绑定的落地页 ID'}
        >
          <Settings size={16} />
          <span>落地页配置</span>
        </button>

        <button
          className="btn btn-secondary"
          onClick={onOpenBatchSpend}
          disabled={isReadOnly}
          title={isReadOnly ? '非登录用户视图只可查看，不可导入消耗' : '批量导入消耗'}
        >
          <Upload size={16} />
          <span>消耗导入</span>
        </button>

        <button
          className="btn btn-secondary"
          onClick={onOpenSyncModal}
          disabled={loading}
        >
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          <span>{loading ? '同步中...' : '数据同步'}</span>
        </button>

        {/* 导出表格按钮 (仅管理员与超级管理员可见) */}
        {isAdmin && (
          <button
            className="theme-toggle-btn"
            onClick={onOpenExportModal}
            title="导出表格数据 (支持自定义时间段)"
            style={{ cursor: 'pointer' }}
          >
            <Download size={18} color="#10b981" />
          </button>
        )}

        <button className="theme-toggle-btn" onClick={onToggleTheme} title={theme === 'light' ? '切换为深色模式' : '切换为浅色模式'}>
          {theme === 'light' ? <Moon size={18} color="#475569" /> : <Sun size={18} color="#fbbf24" />}
        </button>

        {/* 退出登录按钮 (格式：图标 username) */}
        <button
          className="btn btn-secondary"
          style={{ color: '#f43f5e', borderColor: 'rgba(244, 63, 94, 0.25)', gap: '0.4rem' }}
          onClick={onLogout}
          title={`当前账号: ${currentUser?.username || localStorage.getItem('admin_username') || ''} (点击退出登录)`}
        >
          <LogOut size={16} />
          <span>{currentUser?.username || localStorage.getItem('admin_username') || '未知'}</span>
        </button>
      </div>

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
        .spin {
          animation: spin 1s linear infinite;
        }
      `}</style>
    </header>
  );
}
