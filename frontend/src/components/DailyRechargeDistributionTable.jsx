import React, { useState } from 'react';
import { DollarSign, Users, UserCheck, RefreshCw, Calendar, TrendingUp } from 'lucide-react';
import DailyRechargeCharts from './DailyRechargeCharts';
import { exportDistributionTable } from '../utils/exportExcel';
import ExportModal from './ExportModal';

export default function DailyRechargeDistributionTable({ distributionData, distributionSummary, isGlobal = false }) {
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const data = Array.isArray(distributionData) ? distributionData : [];

  const handleConfirmExport = (dateRange) => {
    exportDistributionTable(data, isGlobal, dateRange);
  };

  let grandTotalRecharge = 0;
  let grandNewRecharge = 0;
  let grandOldRecharge = 0;
  let grandNewPaidUsers = 0;
  let grandOldPaidUsers = 0;
  let grandTotalPaidUsers = 0;
  let grandRepeatUsers = 0;
  let overallRepeatRate = '0.00';
  let overallNewArpu = '0.00';
  let overallOldArpu = '0.00';

  if (distributionSummary) {
    grandTotalRecharge = parseFloat(distributionSummary.totalRecharge || 0);
    grandNewRecharge = parseFloat(distributionSummary.newRecharge || 0);
    grandOldRecharge = parseFloat(distributionSummary.oldRecharge || 0);
    grandNewPaidUsers = parseInt(distributionSummary.newPaidUsers || 0);
    grandOldPaidUsers = parseInt(distributionSummary.oldPaidUsers || 0);
    grandTotalPaidUsers = parseInt(distributionSummary.totalPaidUsers || 0);
    grandRepeatUsers = parseInt(distributionSummary.repeatPaidUsers || 0);
    overallRepeatRate = (parseFloat(distributionSummary.repeatRate || 0) * 100).toFixed(2);
    overallNewArpu = parseFloat(distributionSummary.newArpu || 0).toFixed(2);
    overallOldArpu = parseFloat(distributionSummary.oldArpu || 0).toFixed(2);
  } else {
    data.forEach((r) => {
      grandTotalRecharge += parseFloat(r.totalRecharge || 0);
      grandNewRecharge += parseFloat(r.newRecharge || 0);
      grandOldRecharge += parseFloat(r.oldRecharge || 0);
      grandNewPaidUsers += (parseInt(r.newPaidUsers) || 0);
      grandOldPaidUsers += (parseInt(r.oldPaidUsers) || 0);
    });
    grandTotalPaidUsers = grandNewPaidUsers + grandOldPaidUsers;
    overallNewArpu = grandNewPaidUsers > 0 ? (grandNewRecharge / grandNewPaidUsers).toFixed(2) : '0.00';
    overallOldArpu = grandOldPaidUsers > 0 ? (grandOldRecharge / grandOldPaidUsers).toFixed(2) : '0.00';
  }

  const formatUsd = (num) => {
    const val = parseFloat(num || 0);
    if (val < 0) {
      return `-$${Math.abs(val).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
    return `$${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatPercent = (val) => {
    const num = parseFloat(val || 0) * 100;
    return `${num.toFixed(2)}%`;
  };

  // 计算本月与上月总充值
  const now = new Date();
  const currentYear = now.getFullYear();
  const currentMonth = now.getMonth(); // 0-11

  const thisMonthPrefix = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}`;
  const lastMonthDate = new Date(currentYear, currentMonth - 1, 1);
  const lastMonthPrefix = `${lastMonthDate.getFullYear()}-${String(lastMonthDate.getMonth() + 1).padStart(2, '0')}`;

  let thisMonthRecharge = 0;
  let thisMonthRefund = 0;
  let lastMonthRecharge = 0;
  let lastMonthRefund = 0;

  if (distributionSummary && distributionSummary.thisMonthRecharge != null) {
    thisMonthRecharge = parseFloat(distributionSummary.thisMonthRecharge || 0);
    thisMonthRefund = parseFloat(distributionSummary.thisMonthRefund || 0);
    lastMonthRecharge = parseFloat(distributionSummary.lastMonthRecharge || 0);
    lastMonthRefund = parseFloat(distributionSummary.lastMonthRefund || 0);
  } else {
    data.forEach((r) => {
      const rec = parseFloat(r.totalRecharge || 0);
      if (r.date && r.date.startsWith(thisMonthPrefix)) {
        thisMonthRecharge += rec;
      } else if (r.date && r.date.startsWith(lastMonthPrefix)) {
        lastMonthRecharge += rec;
      }
    });
  }

  // 提取最新自然日（今日）的明细数据
  const todayStr = new Date().toISOString().slice(0, 10);
  const todayRow = data.find((r) => r.date === todayStr) || data[0];

  const todayTotalRecharge = todayRow ? parseFloat(todayRow.totalRecharge || 0) : 0;
  const todayOldRecharge = todayRow ? parseFloat(todayRow.oldRecharge || 0) : 0;
  const todayPaidUsers = todayRow ? (todayRow.totalPaidUsers || 0) : 0;
  const todayOldArpu = todayRow ? parseFloat(todayRow.oldArpu || 0).toFixed(2) : '0.00';
  const todayOldRechargePct = todayTotalRecharge > 0 ? ((todayOldRecharge / todayTotalRecharge) * 100).toFixed(1) : '0.0';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', width: '100%' }}>
      {/* 顶部统计卡片汇总：全量全局独立去重与今日实时 */}
      <div className="stats-summary" style={{ gridTemplateColumns: 'repeat(6, 1fr)' }}>
        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label">累计总充值</span>
            <DollarSign size={18} color="var(--accent-cyan)" />
          </div>
          <div className="stat-value">{formatUsd(grandTotalRecharge)}</div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginTop: '0.2rem' }}>
            全量自然日充值汇总
          </div>
        </div>

        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label">上月总充值</span>
            <Calendar size={18} color="#8b5cf6" />
          </div>
          <div className="stat-value">{formatUsd(lastMonthRecharge)}</div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginTop: '0.2rem' }}>
            退款: <strong style={{ color: '#f43f5e' }}>{formatUsd(lastMonthRefund)}</strong> | 实充: <strong style={{ color: '#10b981' }}>{formatUsd(lastMonthRecharge - lastMonthRefund)}</strong>
          </div>
        </div>

        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label">本月总充值</span>
            <TrendingUp size={18} color="#3b82f6" />
          </div>
          <div className="stat-value">{formatUsd(thisMonthRecharge)}</div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginTop: '0.2rem' }}>
            退款: <strong style={{ color: '#f43f5e' }}>{formatUsd(thisMonthRefund)}</strong> | 实充: <strong style={{ color: '#10b981' }}>{formatUsd(thisMonthRecharge - thisMonthRefund)}</strong>
          </div>
        </div>

        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label">今日充值</span>
            <UserCheck size={18} color="#10b981" />
          </div>
          <div className="stat-value">{formatUsd(todayTotalRecharge)}</div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginTop: '0.2rem' }}>
            今日付费人数: <strong style={{ color: '#10b981' }}>{todayPaidUsers} 人</strong>
          </div>
        </div>

        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label">今日老用户充值</span>
            <Users size={18} color="#f59e0b" />
          </div>
          <div className="stat-value">{formatUsd(todayOldRecharge)}</div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginTop: '0.2rem' }}>
            占比: <strong style={{ color: '#f59e0b' }}>{todayOldRechargePct}%</strong> | 老用户 ARPU: <strong style={{ color: '#f59e0b' }}>${todayOldArpu}</strong>
          </div>
        </div>

        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span className="stat-label">累计充值人数</span>
            <RefreshCw size={18} color="var(--accent-cyan)" />
          </div>
          <div className="stat-value">{grandTotalPaidUsers} 人</div>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-sub)', marginTop: '0.2rem' }}>
            复充率: <strong style={{ color: 'var(--accent-cyan)' }}>{overallRepeatRate}%</strong> (复充人数: {grandRepeatUsers}人)
          </div>
        </div>
      </div>

      {/* 近 15 天趋势折线图区域 */}
      <DailyRechargeCharts distributionData={data} />

      {/* 每日充值分布全量多级表头表格：统一字体颜色 */}
      <div className="table-container">
        <table className="ltv-single-table auto-fit-table">
          <thead>
            <tr className="header-row-1">
              <th
                className="sticky-col col-0 text-center"
                rowSpan={2}
                style={{ left: 0 }}
              >
                日期
              </th>

              <th colSpan={4} className="text-center day-header-group" style={{ background: 'var(--bg-th)' }}>
                充值总览指标
              </th>
              <th colSpan={5} className="text-center day-header-group" style={{ background: 'rgba(16, 185, 129, 0.12)', color: '#10b981' }}>
                新用户指标
              </th>
              <th colSpan={5} className="text-center day-header-group" style={{ background: 'rgba(245, 158, 11, 0.12)', color: '#f59e0b' }}>
                老用户指标
              </th>
              <th colSpan={2} className="text-center day-header-group" style={{ background: 'rgba(6, 182, 212, 0.12)', color: 'var(--accent-cyan)' }}>
                复充指标
              </th>
            </tr>

            <tr className="header-row-2">
              {/* 充值总览与人数 */}
              <th className="text-center sub-header">总充值($)</th>
              <th className="text-center sub-header">订阅充值</th>
              <th className="text-center sub-header">付费人数</th>
              <th className="text-center sub-header">订阅人数</th>

              {/* 新用户指标 */}
              <th className="text-center sub-header">充值金额</th>
              <th className="text-center sub-header">金额占比</th>
              <th className="text-center sub-header">ARPU</th>
              <th className="text-center sub-header">付费人数</th>
              <th className="text-center sub-header">订阅人数</th>

              {/* 老用户指标 */}
              <th className="text-center sub-header">充值金额</th>
              <th className="text-center sub-header">金额占比</th>
              <th className="text-center sub-header">ARPU</th>
              <th className="text-center sub-header">付费人数</th>
              <th className="text-center sub-header">订阅人数</th>

              {/* 复充统计 (整体) */}
              <th className="text-center sub-header">复充人数</th>
              <th className="text-center sub-header">复充率</th>
            </tr>
          </thead>
          <tbody>
            {data.map((row) => {
              const totalRec = parseFloat(row.totalRecharge || 0);
              const newRec = parseFloat(row.newRecharge || 0);
              const oldRec = parseFloat(row.oldRecharge || 0);

              const newRatio = row.newRechargeRatio != null
                ? parseFloat(row.newRechargeRatio)
                : (totalRec > 0 ? newRec / totalRec : 0);

              const oldRatio = row.oldRechargeRatio != null
                ? parseFloat(row.oldRechargeRatio)
                : (totalRec > 0 ? oldRec / totalRec : 0);

              return (
                <tr key={row.date}>
                  <td
                    className="sticky-col col-0 text-center"
                    style={{ left: 0, fontWeight: 600, color: 'var(--text-main)' }}
                  >
                    {row.date}
                  </td>

                  {/* 充值总览 */}
                  <td className="text-right" style={{ fontWeight: 600, color: 'var(--text-main)' }}>{formatUsd(row.totalRecharge)}</td>
                  <td className="text-right">{formatUsd(row.subsRecharge)}</td>
                  <td className="text-center" style={{ fontWeight: 600 }}>{row.totalPaidUsers || 0}</td>
                  <td className="text-center">{row.subsPaidUsers || 0}</td>

                  {/* 新用户指标 */}
                  <td className="text-right" style={{ fontWeight: 600 }}>{formatUsd(row.newRecharge)}</td>
                  <td className="text-center">{formatPercent(newRatio)}</td>
                  <td className="text-right">{formatUsd(row.newArpu)}</td>
                  <td className="text-center">{row.newPaidUsers || 0}</td>
                  <td className="text-center">{row.newSubsPaidUsers || 0}</td>

                  {/* 老用户指标 */}
                  <td className="text-right" style={{ fontWeight: 600 }}>{formatUsd(row.oldRecharge)}</td>
                  <td className="text-center">{formatPercent(oldRatio)}</td>
                  <td className="text-right">{formatUsd(row.oldArpu)}</td>
                  <td className="text-center">{row.oldPaidUsers || 0}</td>
                  <td className="text-center">{row.oldSubsPaidUsers || 0}</td>

                  {/* 整体复充统计 */}
                  <td className="text-center">{row.repeatPaidUsers || 0}</td>
                  <td className="text-center">{formatPercent(row.repeatRate)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <ExportModal
        isOpen={isExportModalOpen}
        onClose={() => setIsExportModalOpen(false)}
        onConfirmExport={handleConfirmExport}
        title={isGlobal ? '导出全平台充值汇总' : '导出每日充值分布报表'}
        maxDays={90}
        data={data}
        dateField="date"
      />
    </div>
  );
}
