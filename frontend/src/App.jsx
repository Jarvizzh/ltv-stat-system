import React, { useState, useEffect } from 'react';
import LtvHeader from './components/LtvHeader';
import LtvTable from './components/LtvTable';
import DailyRechargeDistributionTable from './components/DailyRechargeDistributionTable';
import LandingPageConfigModal from './components/LandingPageConfigModal';
import EditSpendModal from './components/EditSpendModal';
import TokenConfigModal from './components/TokenConfigModal';
import SyncModal from './components/SyncModal';
import BatchSpendModal from './components/BatchSpendModal';
import UserManagementModal from './components/UserManagementModal';
import LogoutConfirmModal from './components/LogoutConfirmModal';
import ExportModal from './components/ExportModal';
import { exportLtvTable, exportDistributionTable } from './utils/exportExcel';
import Login from './components/Login';
import Toast from './components/Toast';
import { DollarSign, TrendingUp, Users, Wallet, AlertTriangle, Calendar, Info, X } from 'lucide-react';

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return !!localStorage.getItem('admin_token');
  });

  const [currentUser, setCurrentUser] = useState(() => {
    const uid = localStorage.getItem('admin_user_id');
    return {
      userId: uid ? Number(uid) : 1,
      username: localStorage.getItem('admin_username') || 'admin',
      role: localStorage.getItem('admin_role') || 'USER'
    };
  });

  const [targetUserId, setTargetUserId] = useState(() => {
    const uid = localStorage.getItem('admin_user_id');
    return uid ? Number(uid) : 1;
  });

  const [usersList, setUsersList] = useState([]);
  const [activeTab, setActiveTab] = useState('ltv'); // 'ltv' | 'distribution'
  const [data, setData] = useState([]);
  const [distributionData, setDistributionData] = useState([]);
  const [distributionSummary, setDistributionSummary] = useState(null);
  const [globalDistributionData, setGlobalDistributionData] = useState([]);
  const [globalDistributionSummary, setGlobalDistributionSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingType, setLoadingType] = useState(null);
  const [isConfigOpen, setIsConfigOpen] = useState(false);
  const [isTokenModalOpen, setIsTokenModalOpen] = useState(false);
  const [isSyncModalOpen, setIsSyncModalOpen] = useState(false);
  const [isBatchSpendOpen, setIsBatchSpendOpen] = useState(false);
  const [isUserManagementOpen, setIsUserManagementOpen] = useState(false);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [editingTargetUserLandingPage, setEditingTargetUserLandingPage] = useState(null);

  const [editingRow, setEditingRow] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [toast, setToast] = useState(null);
  const [showLtvTip, setShowLtvTip] = useState(true);

  const handleConfirmExport = (dateRange) => {
    if (activeTab === 'ltv') {
      exportLtvTable(data, true, currentUser?.username || '', dateRange);
    } else if (activeTab === 'distribution') {
      exportDistributionTable(distributionData, false, dateRange);
    } else if (activeTab === 'global-distribution') {
      exportDistributionTable(distributionData, true, dateRange);
    }
  };

  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem('ltv_theme');
    if (saved === 'dark' || saved === 'light') return saved;
    return 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('ltv_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    const nextTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(nextTheme);
  };

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 4000);
  };

  const authFetch = async (url, options = {}) => {
    const token = localStorage.getItem('admin_token');
    const headers = {
      ...(options.headers || {}),
      'Authorization': token ? `Bearer ${token}` : '',
    };

    const res = await fetch(url, { ...options, headers });
    if (res.status === 401) {
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_username');
      localStorage.removeItem('admin_role');
      localStorage.removeItem('admin_user_id');
      setIsAuthenticated(false);
      showToast('未登录或登录凭证已过 3 天有效期，请重新登录', 'warning');
      throw new Error('UNAUTHORIZED');
    }
    return res;
  };

  const fetchUsersList = async () => {
    const token = localStorage.getItem('admin_token');
    if (!token) return;
    try {
      const res = await authFetch('/api/user/visible-accounts');
      const json = await res.json();
      if (json.code === 0 && Array.isArray(json.data)) {
        setUsersList(json.data);
      }
    } catch (e) {
      console.error('Failed to fetch visible accounts:', e);
    }
  };

  const [backendOverallPaybackDays, setBackendOverallPaybackDays] = useState(null);
  const [backendOverallPaybackCycleDays, setBackendOverallPaybackCycleDays] = useState(null);
  const [overallPredictedDay30Roi, setOverallPredictedDay30Roi] = useState(null);
  const [overallPredictedDay60Roi, setOverallPredictedDay60Roi] = useState(null);
  const [overallPredictedDay90Roi, setOverallPredictedDay90Roi] = useState(null);
  const [monthlySummary, setMonthlySummary] = useState(null);
  const [hoveredMonthlyPrediction, setHoveredMonthlyPrediction] = useState(null);
  const [overallRetainedSubUsers, setOverallRetainedSubUsers] = useState(0);
  const [overallRetainedRate, setOverallRetainedRate] = useState('0.00%');

  const fetchLtvData = async (overrideUserId) => {
    if (!localStorage.getItem('admin_token')) return;
    setLoading(true);
    const uid = overrideUserId !== undefined ? overrideUserId : targetUserId;
    try {
      const res = await authFetch(`/api/ltv/list?targetUserId=${uid || ''}`);
      const json = await res.json();
      if (json.code === 0 && Array.isArray(json.data)) {
        setData(json.data);
        if (json.overallPredictedPaybackDays !== undefined) {
          setBackendOverallPaybackDays(json.overallPredictedPaybackDays);
        }
        if (json.overallPaybackCycleDays !== undefined) {
          setBackendOverallPaybackCycleDays(json.overallPaybackCycleDays);
        }
        if (json.overallPredictedDay30Roi !== undefined) {
          setOverallPredictedDay30Roi(json.overallPredictedDay30Roi);
        }
        if (json.overallPredictedDay60Roi !== undefined) {
          setOverallPredictedDay60Roi(json.overallPredictedDay60Roi);
        }
        if (json.overallPredictedDay90Roi !== undefined) {
          setOverallPredictedDay90Roi(json.overallPredictedDay90Roi);
        }
        if (json.monthlySummary) {
          setMonthlySummary(json.monthlySummary);
        }
        if (json.overallRetainedSubUsers !== undefined) {
          setOverallRetainedSubUsers(json.overallRetainedSubUsers);
        }
        if (json.overallRetainedRate !== undefined) {
          setOverallRetainedRate(json.overallRetainedRate);
        }
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        console.error('Failed to fetch LTV data:', err);
        showToast('获取 LTV 统计数据失败，请检查后端服务', 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchDistributionData = async (overrideUserId) => {
    if (!localStorage.getItem('admin_token')) return;
    setLoading(true);
    const uid = overrideUserId !== undefined ? overrideUserId : targetUserId;
    try {
      const res = await authFetch(`/api/ltv/daily-distribution?targetUserId=${uid || ''}`);
      const json = await res.json();
      if (json.code === 0 && Array.isArray(json.data)) {
        setDistributionData(json.data);
        if (json.summary) {
          setDistributionSummary(json.summary);
        }
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        console.error('Failed to fetch daily distribution data:', err);
        showToast('获取每日充值分布数据失败', 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchGlobalDistributionData = async () => {
    if (!localStorage.getItem('admin_token')) return;
    setLoading(true);
    try {
      const res = await authFetch('/api/ltv/global-daily-distribution');
      const json = await res.json();
      if (json.code === 0 && Array.isArray(json.data)) {
        setGlobalDistributionData(json.data);
        if (json.summary) {
          setGlobalDistributionSummary(json.summary);
        }
      } else {
        showToast(json.msg || '获取全量充值分析数据失败', 'error');
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        console.error('Failed to fetch global daily distribution data:', err);
        showToast('获取全量充值分析数据失败', 'error');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchUsersList();
      if (activeTab === 'ltv') {
        fetchLtvData(targetUserId);
      } else if (activeTab === 'distribution') {
        fetchDistributionData(targetUserId);
      } else if (activeTab === 'global-distribution') {
        fetchGlobalDistributionData();
      }
    }
  }, [isAuthenticated, activeTab, targetUserId]);

  const handleSelectTargetUser = (newUserId) => {
    setTargetUserId(newUserId);
    // 切换视图时清空上一视图数据并触发数据自动刷新
    setData([]);
    setDistributionData([]);
    setDistributionSummary(null);

    if (activeTab === 'ltv') {
      fetchLtvData(newUserId);
    } else if (activeTab === 'distribution') {
      fetchDistributionData(newUserId);
    } else if (activeTab === 'global-distribution') {
      fetchGlobalDistributionData();
    }
    const userObj = usersList.find(u => u.id === newUserId);
    showToast(`已切换至用户视图: [${userObj ? userObj.username : newUserId}]，已自动刷新数据`, 'info');
  };

  const handleLoginSuccess = (loginData) => {
    setIsAuthenticated(true);
    const newUid = loginData.userId;
    const userObj = {
      userId: newUid,
      username: loginData.username,
      role: loginData.role
    };
    setCurrentUser(userObj);
    setTargetUserId(newUid);

    // 清空上一个账号的数据缓存
    setData([]);
    setDistributionData([]);
    setDistributionSummary(null);
    setGlobalDistributionData([]);
    setGlobalDistributionSummary(null);

    fetchUsersList();

    // 立即自动拉取刷新新登录账号的数据
    if (activeTab === 'ltv') {
      fetchLtvData(newUid);
    } else if (activeTab === 'distribution') {
      fetchDistributionData(newUid);
    } else if (activeTab === 'global-distribution') {
      fetchGlobalDistributionData();
    }

    showToast(`登录成功！欢迎 ${loginData.username}，已加载最新数据`, 'success');
  };

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_username');
    localStorage.removeItem('admin_role');
    localStorage.removeItem('admin_user_id');
    setIsAuthenticated(false);
    setCurrentUser(null);
    setTargetUserId(1);
    setUsersList([]);
    setData([]);
    setDistributionData([]);
    setDistributionSummary(null);
    setGlobalDistributionData([]);
    setGlobalDistributionSummary(null);
    showToast('已安全退出登录', 'info');
  };

  // 1. 仅抓取/同步订单
  const handleSyncOrdersOnly = async (startTime, endTime) => {
    setLoading(true);
    setLoadingType('orders');
    setErrorMessage('');
    try {
      const res = await authFetch('/api/ltv/sync-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startTime, endTime }),
      });
      const json = await res.json();
      if (json.code === 0) {
        setIsSyncModalOpen(false);
        showToast(`订单同步成功 (${startTime} ~ ${endTime})，共抓取 ${json.totalSyncedOrders} 笔订单！`, 'success');
      } else if (json.code === 4002) {
        setErrorMessage(json.msg || '订单接口 Token 已过期');
        setIsSyncModalOpen(false);
        setIsTokenModalOpen(true);
        showToast('Token 已过期，请更新 API 鉴权凭证', 'error');
      } else {
        showToast(`提示: ${json.msg}`, 'warning');
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        showToast('抓取订单发生网络异常', 'error');
      }
    } finally {
      setLoading(false);
      setLoadingType(null);
    }
  };

  // 2. 仅重算 LTV & 充值分析全量报表
  const handleRecalculateAllReports = async () => {
    setLoading(true);
    setLoadingType('calc');
    try {
      const res = await authFetch(`/api/ltv/recalculate?targetUserId=${targetUserId || ''}`, { method: 'POST' });
      const json = await res.json();
      if (json.code === 0) {
        setIsSyncModalOpen(false);
        fetchLtvData();
        fetchDistributionData();
        fetchGlobalDistributionData();
        showToast('LTV 与 充值分析全量报表重算完成！', 'success');
      } else {
        showToast(`提示: ${json.msg}`, 'warning');
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        showToast('重算报表请求失败', 'error');
      }
    } finally {
      setLoading(false);
      setLoadingType(null);
    }
  };

  // 4. 一键抓取订单 + 重算全量报表
  const handleSyncAndCalcAll = async (startTime, endTime) => {
    setLoading(true);
    setLoadingType('all');
    setErrorMessage('');
    try {
      const res = await authFetch('/api/ltv/sync-and-calc', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startTime, endTime }),
      });
      const json = await res.json();
      if (json.code === 0) {
        setIsSyncModalOpen(false);
        fetchLtvData();
        fetchDistributionData();
        fetchGlobalDistributionData();
        showToast(`全流程抓取与重算完成！`, 'success');
      } else if (json.code === 4002) {
        setErrorMessage(json.msg);
        setIsSyncModalOpen(false);
        setIsTokenModalOpen(true);
        showToast('Token 已过期，请更新 API 鉴权凭证', 'error');
      } else {
        showToast(`提示: ${json.msg}`, 'warning');
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        showToast('全流程请求发生网络异常', 'error');
      }
    } finally {
      setLoading(false);
      setLoadingType(null);
    }
  };

  const handleSpendSaved = () => {
    fetchLtvData();
    showToast('投放消耗与备注更新成功！', 'success');
  };

  const handleBatchSpendSaved = (count) => {
    fetchLtvData();
    showToast(`批量导入成功！共写入/更新 ${count} 条消耗数据`, 'success');
  };

  const handleLandingPagesSaved = () => {
    if (activeTab === 'ltv') fetchLtvData();
    if (activeTab === 'distribution') fetchDistributionData();
    if (currentUser.role === 'ADMIN') fetchUsersList();
    showToast('落地页配置保存成功，已完成专属报表实时重算！', 'success');
  };

  if (!isAuthenticated) {
    return (
      <>
        <Toast toast={toast} onClose={() => setToast(null)} />
        <Login onLoginSuccess={handleLoginSuccess} />
      </>
    );
  }

  const totalSpend = data.reduce((acc, cur) => acc + (parseFloat(cur.spend) || 0), 0);
  const totalRecharge = data.reduce((acc, cur) => acc + (parseFloat(cur.totalRecharge) || 0), 0);
  const totalRefund = data.reduce((acc, cur) => acc + (parseFloat(cur.totalRefund) || 0), 0);
  const totalProfit = totalRecharge - totalRefund - totalSpend;
  const totalSubUsers = data.reduce((acc, cur) => acc + (parseInt(cur.subUserCount) || 0), 0);
  const overallRoi = totalSpend > 0 ? (((totalRecharge - totalRefund) / totalSpend) * 100).toFixed(2) : '0.00';

  // 月度卡片指标完全由后端接口计算并返回 (monthlySummary)，前端不再进行过滤与累加计算
  const thisMonthStr = monthlySummary?.thisMonth?.month || '';
  const lastMonthStr = monthlySummary?.lastMonth?.month || '';

  const thisMonthSpend = monthlySummary?.thisMonth?.spend || 0;
  const lastMonthSpend = monthlySummary?.lastMonth?.spend || 0;

  const thisMonthRecharge = monthlySummary?.thisMonth?.recharge || 0;
  const lastMonthRecharge = monthlySummary?.lastMonth?.recharge || 0;

  const thisMonthRefund = monthlySummary?.thisMonth?.refund || 0;
  const lastMonthRefund = monthlySummary?.lastMonth?.refund || 0;

  const thisMonthProfit = monthlySummary?.thisMonth?.profit || 0;
  const lastMonthProfit = monthlySummary?.lastMonth?.profit || 0;

  const thisMonthRoi = monthlySummary?.thisMonth?.roi !== undefined ? monthlySummary.thisMonth.roi : '0.00';
  const lastMonthRoi = monthlySummary?.lastMonth?.roi !== undefined ? monthlySummary.lastMonth.roi : '0.00';

  const thisMonthSubUsers = monthlySummary?.thisMonth?.subUsers || 0;
  const lastMonthSubUsers = monthlySummary?.lastMonth?.subUsers || 0;

  const thisMonthRetainedSubUsers = monthlySummary?.thisMonth?.retainedSubUsers;
  const thisMonthRetainedRate = monthlySummary?.thisMonth?.retainedRate;

  const lastMonthRetainedSubUsers = monthlySummary?.lastMonth?.retainedSubUsers;
  const lastMonthRetainedRate = monthlySummary?.lastMonth?.retainedRate;

  const thisMonthActualPaybackDays = monthlySummary?.thisMonth?.actualPaybackDays;
  const lastMonthActualPaybackDays = monthlySummary?.lastMonth?.actualPaybackDays;

  const lastMonthPredD30 = monthlySummary?.lastMonth?.predictedDay30Roi;
  const lastMonthPredD60 = monthlySummary?.lastMonth?.predictedDay60Roi;
  const lastMonthPredD90 = monthlySummary?.lastMonth?.predictedDay90Roi;

  const calculateOverallPaybackDays = (rows) => {
    if (!rows || rows.length === 0) return null;
    const validRows = rows.filter(r => parseFloat(r.spend || 0) > 0);
    if (validRows.length === 0) return null;

    const spendSum = validRows.reduce((acc, r) => acc + parseFloat(r.spend || 0), 0);
    const rechargeSum = validRows.reduce((acc, r) => acc + parseFloat(r.totalRecharge || 0), 0);

    if (spendSum > 0 && rechargeSum >= spendSum) {
      return 0; // 已回本
    }

    const xList = [];
    const yList = [];

    for (let t = 1; t <= 30; t++) {
      let cohortSpendSum = 0;
      let cohortRechargeSum = 0;
      let count = 0;

      validRows.forEach(r => {
        const val = r[`day${t}Recharge`];
        if (val !== null && val !== undefined) {
          cohortSpendSum += parseFloat(r.spend || 0);
          cohortRechargeSum += parseFloat(val);
          count++;
        }
      });

      if (count > 0 && cohortSpendSum > 0 && cohortRechargeSum > 0) {
        const roi = cohortRechargeSum / cohortSpendSum;
        xList.push(Math.log(t));
        yList.push(roi);
      }
    }

    if (yList.length < 3) return null;

    const n = xList.length;
    let sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
    for (let i = 0; i < n; i++) {
      sumX += xList[i];
      sumY += yList[i];
      sumXY += xList[i] * yList[i];
      sumXX += xList[i] * xList[i];
    }

    const denominator = n * sumXX - sumX * sumX;
    if (denominator === 0) return -1;

    const a = (n * sumXY - sumX * sumY) / denominator;
    const b = (sumY - a * sumX) / n;

    if (a <= 0.0001) return -1; // 停滞

    const tPayback = Math.exp((1.0 - b) / a);
    if (tPayback > 0 && tPayback <= 365) {
      return Math.round(tPayback);
    }

    return 366; // >365天
  };

  const overallPaybackDays = backendOverallPaybackDays !== null ? backendOverallPaybackDays : calculateOverallPaybackDays(data);

  const calculateOverallPaybackCycleDays = () => {
    if (backendOverallPaybackCycleDays !== null && backendOverallPaybackCycleDays !== undefined) {
      return backendOverallPaybackCycleDays;
    }
    if (!data || data.length === 0 || overallPaybackDays === null || overallPaybackDays < 0 || overallPaybackDays > 365) {
      return null;
    }
    const validDates = data.filter(d => d.launchDate && d.spend > 0).map(d => new Date(d.launchDate));
    if (validDates.length === 0) return null;
    const minDate = new Date(Math.min(...validDates));
    const today = new Date();
    const elapsedDays = Math.max(1, Math.floor((today - minDate) / (1000 * 60 * 60 * 24)) + 1);
    return elapsedDays + overallPaybackDays;
  };

  const overallPaybackCycleDays = calculateOverallPaybackCycleDays();

  const isSuperAdmin = Boolean(currentUser && currentUser.role === 'SUPER_ADMIN');
  const currentTargetUserObj = usersList.find(u => u.id === (targetUserId || currentUser?.userId));
  const isTargetMaster = currentTargetUserObj ? Boolean(currentTargetUserObj.isMaster === 1) : false;
  const isReadOnlyView = Boolean(targetUserId && currentUser && targetUserId !== currentUser.userId) || isTargetMaster;

  const renderActualPaybackTag = (days, monthStr, d30Roi, d60Roi, d90Roi) => {
    if (days === null || days === undefined) return null;
    const hasPred = isSuperAdmin && d30Roi !== null && d30Roi !== undefined;
    return (
      <span
        onMouseEnter={(e) => {
          if (!hasPred) return;
          const rect = e.currentTarget.getBoundingClientRect();
          setHoveredMonthlyPrediction({
            left: rect.left + rect.width / 2,
            top: rect.top - 8,
            month: monthStr,
            d30Roi,
            d60Roi,
            d90Roi
          });
        }}
        onMouseLeave={() => setHoveredMonthlyPrediction(null)}
        style={{
          background: 'rgba(16, 185, 129, 0.15)',
          color: '#10b981',
          border: '1px solid rgba(16, 185, 129, 0.3)',
          padding: '0.1rem 0.35rem',
          borderRadius: '0.25rem',
          fontSize: '0.75rem',
          fontWeight: 600,
          whiteSpace: 'nowrap',
          cursor: hasPred ? 'pointer' : 'default'
        }}
      >
        回本：{days}天
      </span>
    );
  };

  const formatUsd = (val) => {
    const num = parseFloat(val || 0);
    if (num < 0) {
      return `-$${Math.abs(num).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
    return `$${num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  return (
    <div className="app-container">
      {/* 现代 Toast 全局消息浮层 */}
      <Toast toast={toast} onClose={() => setToast(null)} />

      <LtvHeader
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onOpenConfig={() => {
          setEditingTargetUserLandingPage(null);
          setIsConfigOpen(true);
        }}
        onOpenTokenModal={() => setIsTokenModalOpen(true)}
        onOpenSyncModal={() => setIsSyncModalOpen(true)}
        onOpenBatchSpend={() => setIsBatchSpendOpen(true)}
        onOpenUserManagement={() => setIsUserManagementOpen(true)}
        onOpenExportModal={() => setIsExportModalOpen(true)}
        currentUser={currentUser}
        usersList={usersList}
        targetUserId={targetUserId}
        isReadOnly={isReadOnlyView}
        onSelectTargetUser={handleSelectTargetUser}
        loading={loading}
        onLogout={() => setIsLogoutModalOpen(true)}
      />

      <main className="main-content">
        {errorMessage && (
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: 'rgba(244, 63, 94, 0.15)', border: '1px solid #f43f5e', color: '#fda4af', padding: '0.75rem 1rem', borderRadius: '0.5rem', fontSize: '0.875rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <AlertTriangle size={18} color="#f43f5e" />
              <span><strong>提示：</strong> {errorMessage}</span>
            </div>
            <button className="btn btn-primary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.8rem' }} onClick={() => setIsTokenModalOpen(true)}>
              去更新 Token
            </button>
          </div>
        )}

        {/* Tab 1: LTV 报表 */}
        {activeTab === 'ltv' && (
          <>
            {/* LTV 统计逻辑说明 */}
            {showLtvTip && (
              <div style={{
                background: 'rgba(99, 102, 241, 0.08)',
                border: '1px solid rgba(99, 102, 241, 0.25)',
                borderRadius: '0.5rem',
                padding: '0.5rem 0.85rem',
                color: 'var(--text-sub)',
                fontSize: '0.82rem',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '0.5rem',
                lineHeight: '1.6'
              }}>
                <Info size={15} color="#6366f1" style={{ marginTop: '0.1rem', flexShrink: 0 }} />
                <span style={{ flex: 1 }}>
                  <span style={{ color: '#6366f1', fontWeight: 600 }}>说明：</span>
                  LTV 报表是基于用户<strong>注册时间</strong>，统计不同批次用户的全生命周期增长价值。「月度充值」是当月注册的用户至今累计充值，与「充值分析」的统计口径（支付时间）不同，两者数据不可对比。
                </span>
                <button
                  onClick={() => setShowLtvTip(false)}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '0', color: '#6366f1', opacity: 0.6, flexShrink: 0, display: 'flex', alignItems: 'center' }}
                  title="关闭提示"
                >
                  <X size={14} />
                </button>
              </div>
            )}

            <div className="stats-summary">
              {/* 卡片 1: 总消耗 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label" style={{ fontSize: '0.76rem' }}>总消耗 (2026-07-10至今)</span>
                  <DollarSign size={16} color="var(--text-sub)" />
                </div>
                <div className="stat-value" style={{ fontSize: '1.15rem' }}>{formatUsd(totalSpend)}</div>
              </div>

              {/* 卡片 2: 累计充值 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label" style={{ fontSize: '0.76rem' }}>累计充值</span>
                  <Wallet size={16} color="var(--text-sub)" />
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.35rem', marginTop: '0.1rem' }}>
                  <span className="stat-value" style={{ fontSize: '1.15rem' }}>{formatUsd(totalRecharge)}</span>
                  <span style={{ fontSize: '0.74rem', color: 'var(--text-sub)', fontWeight: 500 }}>
                    （退款：{formatUsd(totalRefund)}）
                  </span>
                </div>
              </div>

              {/* 卡片 3: 总 ROI / 盈亏 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label" style={{ fontSize: '0.76rem' }}>总 ROI / 盈亏</span>
                  <TrendingUp size={16} color={overallRoi >= 100 ? '#10b981' : '#f43f5e'} />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'nowrap', whiteSpace: 'nowrap' }}>
                  <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.35rem' }}>
                    <span className="stat-value" style={{ fontSize: '1.15rem', color: overallRoi >= 100 ? '#10b981' : '#f43f5e' }}>
                      {overallRoi}%
                    </span>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-sub)', fontWeight: 400, opacity: 0.6 }}>/</span>
                    <span style={{ fontSize: '0.88rem', fontWeight: 600, color: totalProfit >= 0 ? '#10b981' : '#f43f5e', whiteSpace: 'nowrap' }}>
                      {formatUsd(totalProfit)}
                    </span>
                  </div>
                  {overallPaybackDays === 0 || overallRoi >= 100 ? (
                    <span style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#10b981', border: '1px solid rgba(16, 185, 129, 0.3)', padding: '0.12rem 0.4rem', borderRadius: '0.25rem', fontSize: '0.74rem', fontWeight: 600, whiteSpace: 'nowrap' }}>
                      已回本{overallPaybackCycleDays ? ` / 周期：${overallPaybackCycleDays}天` : ''}
                    </span>
                  ) : (currentUser?.role === 'ADMIN' || currentUser?.role === 'SUPER_ADMIN') && (
                    overallPaybackDays === -1 ? (
                      <span style={{ background: 'rgba(244, 63, 94, 0.15)', color: '#f43f5e', border: '1px solid rgba(244, 63, 94, 0.3)', padding: '0.12rem 0.4rem', borderRadius: '0.25rem', fontSize: '0.74rem', fontWeight: 600, whiteSpace: 'nowrap' }}>
                        回本：停滞
                      </span>
                    ) : overallPaybackDays > 365 ? (
                      <span style={{ background: 'rgba(244, 63, 94, 0.15)', color: '#f43f5e', border: '1px solid rgba(244, 63, 94, 0.3)', padding: '0.12rem 0.4rem', borderRadius: '0.25rem', fontSize: '0.74rem', fontWeight: 600, whiteSpace: 'nowrap' }}>
                        回本：&gt;365天
                      </span>
                    ) : overallPaybackDays !== null ? (
                      <span style={{ background: overallPaybackDays <= 45 ? 'rgba(16, 185, 129, 0.15)' : overallPaybackDays <= 90 ? 'rgba(245, 158, 11, 0.15)' : 'rgba(99, 102, 241, 0.15)', color: overallPaybackDays <= 45 ? '#10b981' : overallPaybackDays <= 90 ? '#f59e0b' : '#6366f1', border: '1px solid currentColor', padding: '0.12rem 0.4rem', borderRadius: '0.25rem', fontSize: '0.74rem', fontWeight: 600, whiteSpace: 'nowrap' }}>
                        回本：{overallPaybackDays}天{overallPaybackCycleDays ? ` / 周期：${overallPaybackCycleDays}天` : ''}
                      </span>
                    ) : null
                  )}
                </div>
              </div>

              {/* 卡片 4: 总订阅用户 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label" style={{ fontSize: '0.76rem' }}>总订阅用户</span>
                  <Users size={16} color="#10b981" />
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', flexWrap: 'nowrap', whiteSpace: 'nowrap', marginTop: '0.2rem' }}>
                  <span className="stat-value" style={{ fontSize: '1.15rem' }}>{totalSubUsers}人</span>
                  {overallRetainedSubUsers !== undefined && (
                    <span style={{ fontSize: '0.74rem', color: 'var(--text-sub)', fontWeight: 500 }}>
                      （留存：{overallRetainedSubUsers}人 / {overallRetainedRate}）
                    </span>
                  )}
                </div>
              </div>

              {/* 卡片 5: 月度消耗 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label">月度消耗</span>
                  <DollarSign size={18} color="var(--text-sub)" />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.2rem' }}>
                  <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{thisMonthStr}</span>
                    <span style={{ fontSize: '1.05rem', fontWeight: 500, color: 'var(--text-main)' }}>{formatUsd(thisMonthSpend)}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', borderTop: '1px dashed var(--border-color)', paddingTop: '0.25rem' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{lastMonthStr}</span>
                    <span style={{ fontSize: '1.05rem', fontWeight: 500, color: 'var(--text-main)' }}>{formatUsd(lastMonthSpend)}</span>
                  </div>
                </div>
              </div>

              {/* 卡片 6: 月度充值 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label">月度充值</span>
                  <Wallet size={18} color="var(--text-sub)" />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.2rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'nowrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{thisMonthStr}</span>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.25rem' }}>
                      <span style={{ fontSize: '1.05rem', fontWeight: 500, color: 'var(--text-main)' }}>{formatUsd(thisMonthRecharge)}</span>
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-sub)', fontWeight: 500 }}>
                        （退款：{formatUsd(thisMonthRefund)}）
                      </span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderTop: '1px dashed var(--border-color)', paddingTop: '0.25rem', flexWrap: 'nowrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{lastMonthStr}</span>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.25rem' }}>
                      <span style={{ fontSize: '1.05rem', fontWeight: 500, color: 'var(--text-main)' }}>{formatUsd(lastMonthRecharge)}</span>
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-sub)', fontWeight: 500 }}>
                        （退款：{formatUsd(lastMonthRefund)}）
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* 卡片 7: 月度 ROI / 盈亏 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label">月度 ROI / 盈亏</span>
                  <TrendingUp size={18} color={thisMonthRoi >= 100 ? '#10b981' : '#f43f5e'} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.2rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'nowrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{thisMonthStr}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <span style={{ fontSize: '1.05rem', fontWeight: 500, color: thisMonthRoi >= 100 ? '#10b981' : '#f43f5e' }}>
                        {thisMonthRoi}%
                      </span>
                      <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', fontWeight: 400, opacity: 0.6 }}>/</span>
                      <span style={{ fontSize: '0.82rem', fontWeight: 600, color: thisMonthProfit >= 0 ? '#10b981' : '#f43f5e' }}>
                        {formatUsd(thisMonthProfit)}
                      </span>
                      {renderActualPaybackTag(thisMonthActualPaybackDays)}
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderTop: '1px dashed var(--border-color)', paddingTop: '0.25rem', flexWrap: 'nowrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{lastMonthStr}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <span style={{ fontSize: '1.05rem', fontWeight: 500, color: lastMonthRoi >= 100 ? '#10b981' : '#f43f5e' }}>
                        {lastMonthRoi}%
                      </span>
                      <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', fontWeight: 400, opacity: 0.6 }}>/</span>
                      <span style={{ fontSize: '0.82rem', fontWeight: 600, color: lastMonthProfit >= 0 ? '#10b981' : '#f43f5e' }}>
                        {formatUsd(lastMonthProfit)}
                      </span>
                      {renderActualPaybackTag(lastMonthActualPaybackDays, lastMonthStr, lastMonthPredD30, lastMonthPredD60, lastMonthPredD90)}
                      {isSuperAdmin && lastMonthActualPaybackDays === null && lastMonthPredD30 !== null && lastMonthPredD30 !== undefined && (
                        <span
                          onMouseEnter={(e) => {
                            const rect = e.currentTarget.getBoundingClientRect();
                            setHoveredMonthlyPrediction({
                              left: rect.left + rect.width / 2,
                              top: rect.top - 8,
                              month: lastMonthStr,
                              d30Roi: lastMonthPredD30,
                              d60Roi: lastMonthPredD60,
                              d90Roi: lastMonthPredD90
                            });
                          }}
                          onMouseLeave={() => setHoveredMonthlyPrediction(null)}
                          style={{
                            background: 'rgba(99, 102, 241, 0.15)',
                            color: '#818cf8',
                            border: '1px solid rgba(99, 102, 241, 0.3)',
                            padding: '0.1rem 0.35rem',
                            borderRadius: '0.25rem',
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            whiteSpace: 'nowrap',
                            cursor: 'pointer'
                          }}
                        >
                          🔮 ROI预测
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* 卡片 8: 月度订阅用户 */}
              <div className="stat-card">
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span className="stat-label">月度订阅用户</span>
                  <Users size={18} color="#10b981" />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', marginTop: '0.2rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'nowrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{thisMonthStr}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                      <span style={{ fontSize: '1.05rem', fontWeight: 500, color: 'var(--text-main)' }}>{thisMonthSubUsers}人</span>
                      {thisMonthRetainedSubUsers !== undefined && (
                        <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', fontWeight: 500 }}>
                          （留存：{thisMonthRetainedSubUsers}人 / {thisMonthRetainedRate}）
                        </span>
                      )}
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderTop: '1px dashed var(--border-color)', paddingTop: '0.25rem', flexWrap: 'nowrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-sub)', fontWeight: 500 }}>{lastMonthStr}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                      <span style={{ fontSize: '1.05rem', fontWeight: 500, color: 'var(--text-main)' }}>{lastMonthSubUsers}人</span>
                      {lastMonthRetainedSubUsers !== undefined && (
                        <span style={{ fontSize: '0.78rem', color: 'var(--text-sub)', fontWeight: 500 }}>
                          （留存：{lastMonthRetainedSubUsers}人 / {lastMonthRetainedRate}）
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <LtvTable
              data={data}
              onEditRow={(row) => {
                if (isReadOnlyView) {
                  const msgText = isTargetMaster
                    ? '主账号为数据汇总账号，消耗由子账号自动累加计算，不可直接修改'
                    : '只读视图模式下不可修改他人账户的消耗与备注';
                  showToast(msgText, 'warning');
                  return;
                }
                setEditingRow(row);
              }}
              isReadOnly={isReadOnlyView}
              isAdmin={currentUser?.role === 'ADMIN' || currentUser?.role === 'SUPER_ADMIN'}
              isSuperAdmin={isSuperAdmin}
            />
          </>
        )}

        {/* Tab 2: 每日充值分布 */}
        {activeTab === 'distribution' && (
          <DailyRechargeDistributionTable
            distributionData={distributionData}
            distributionSummary={distributionSummary}
          />
        )}

        {/* Tab 3: 充值分析 (总) - 平台全量订单汇总 */}
        {activeTab === 'global-distribution' && (
          <DailyRechargeDistributionTable
            distributionData={globalDistributionData}
            distributionSummary={globalDistributionSummary}
            isGlobal={true}
          />
        )}
      </main>

      {/* 弹窗组件 */}
      <LandingPageConfigModal
        isOpen={isConfigOpen}
        targetUser={editingTargetUserLandingPage}
        targetUserId={targetUserId}
        isReadOnly={isReadOnlyView}
        onClose={() => {
          setIsConfigOpen(false);
          setEditingTargetUserLandingPage(null);
        }}
        onSaved={handleLandingPagesSaved}
        authFetch={authFetch}
      />

      {currentUser && currentUser.role === 'SUPER_ADMIN' && (
        <UserManagementModal
          isOpen={isUserManagementOpen}
          onClose={() => setIsUserManagementOpen(false)}
          token={localStorage.getItem('admin_token')}
          currentUser={currentUser}
          onRefreshUsers={fetchUsersList}
          showToast={showToast}
        />
      )}

      {currentUser && (
        <TokenConfigModal
          isOpen={isTokenModalOpen}
          onClose={() => setIsTokenModalOpen(false)}
          onSaved={() => {
            fetchLtvData();
            showToast('API Token 更新成功！', 'success');
          }}
          authFetch={authFetch}
        />
      )}

      <SyncModal
        isOpen={isSyncModalOpen}
        onClose={() => setIsSyncModalOpen(false)}
        onSyncOrders={handleSyncOrdersOnly}
        onRecalculateAllReports={handleRecalculateAllReports}
        onSyncAndCalcAll={handleSyncAndCalcAll}
        loading={loading}
        loadingType={loadingType}
      />

      <BatchSpendModal
        isOpen={isBatchSpendOpen}
        targetUserId={targetUserId}
        onClose={() => setIsBatchSpendOpen(false)}
        onSaved={handleBatchSpendSaved}
        authFetch={authFetch}
      />

      <EditSpendModal
        isOpen={!!editingRow}
        item={editingRow}
        targetUserId={targetUserId}
        onClose={() => setEditingRow(null)}
        onSaved={handleSpendSaved}
        authFetch={authFetch}
      />

      <LogoutConfirmModal
        isOpen={isLogoutModalOpen}
        onClose={() => setIsLogoutModalOpen(false)}
        onConfirm={handleLogout}
        username={currentUser?.username}
      />

      <ExportModal
        isOpen={isExportModalOpen}
        onClose={() => setIsExportModalOpen(false)}
        onConfirmExport={handleConfirmExport}
        title={
          activeTab === 'ltv'
            ? '导出 LTV 统计报表'
            : activeTab === 'global-distribution'
              ? '导出平台充值汇总'
              : '导出每日充值分布报表'
        }
        maxDays={90}
        data={activeTab === 'ltv' ? data : distributionData}
        dateField={activeTab === 'ltv' ? 'launchDate' : 'date'}
      />

      {/* 鼠标悬浮“回本周期”或“ROI预测”按钮展示 D30, D60, D90 ROI 预测 Popover */}
      {hoveredMonthlyPrediction && (
        <div
          className="instant-prediction-popover"
          style={{
            position: 'fixed',
            left: `${hoveredMonthlyPrediction.left}px`,
            top: `${hoveredMonthlyPrediction.top}px`,
            transform: 'translate(-50%, -100%)',
            zIndex: 99999,
            pointerEvents: 'none',
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(12px)',
            WebkitBackdropFilter: 'blur(12px)',
            border: '1px solid rgba(99, 102, 241, 0.35)',
            borderRadius: '0.65rem',
            padding: '0.75rem 0.95rem',
            boxShadow: '0 12px 30px rgba(0, 0, 0, 0.5), 0 0 15px rgba(99, 102, 241, 0.2)',
            minWidth: '230px',
            color: '#f8fafc'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid rgba(255, 255, 255, 0.1)', paddingBottom: '0.4rem', marginBottom: '0.55rem' }}>
            <span style={{ fontSize: '0.82rem', fontWeight: 700, color: '#818cf8', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
              🔮 上月 ROI 预测趋势
            </span>
            <span style={{ fontSize: '0.72rem', color: '#94a3b8' }}>{hoveredMonthlyPrediction.month}</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.45rem', fontSize: '0.8rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#cbd5e1', fontWeight: 500 }}>D30 预测 ROI:</span>
              <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                {hoveredMonthlyPrediction.d30Roi !== null && hoveredMonthlyPrediction.d30Roi !== undefined ? `${(parseFloat(hoveredMonthlyPrediction.d30Roi) * 100).toFixed(2)}%` : '-'}
              </span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#cbd5e1', fontWeight: 500 }}>D60 预测 ROI:</span>
              <span style={{ color: '#818cf8', fontWeight: 700 }}>
                {hoveredMonthlyPrediction.d60Roi !== null && hoveredMonthlyPrediction.d60Roi !== undefined ? `${(parseFloat(hoveredMonthlyPrediction.d60Roi) * 100).toFixed(2)}%` : '-'}
              </span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: '#cbd5e1', fontWeight: 500 }}>D90 预测 ROI:</span>
              <span style={{ color: '#c084fc', fontWeight: 700 }}>
                {hoveredMonthlyPrediction.d90Roi !== null && hoveredMonthlyPrediction.d90Roi !== undefined ? `${(parseFloat(hoveredMonthlyPrediction.d90Roi) * 100).toFixed(2)}%` : '-'}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
