import React, { useState, useEffect } from 'react';
import { X, Download, Calendar, AlertCircle, FileSpreadsheet } from 'lucide-react';

export default function ExportModal({
  isOpen,
  onClose,
  onConfirmExport,
  title = '导出 Excel 表格数据',
  maxDays = 90,
  data = [],
  dateField = 'launchDate' // 'launchDate' or 'date'
}) {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [format, setFormat] = useState('xlsx');
  const [errMsg, setErrMsg] = useState('');

  // 本地日期格式化助手 (避免 toISOString UTC 时区偏差)
  const getLocalDateStr = (d) => {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  // 初始化默认时间：最近 30 天
  useEffect(() => {
    if (isOpen) {
      const today = new Date();
      const endStr = getLocalDateStr(today);
      const startObj = new Date(today);
      startObj.setDate(startObj.getDate() - 29); // 30天
      const startStr = getLocalDateStr(startObj);

      setEndDate(endStr);
      setStartDate(startStr);
      setErrMsg('');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  // 计算所选天数
  let diffDays = 0;
  if (startDate && endDate) {
    const s = new Date(startDate);
    const e = new Date(endDate);
    if (!isNaN(s.getTime()) && !isNaN(e.getTime())) {
      diffDays = Math.floor((e - s) / (1000 * 60 * 60 * 24)) + 1;
    }
  }

  // 快捷设置时间范围
  const handleQuickPreset = (days) => {
    const today = new Date();
    const endStr = getLocalDateStr(today);
    const startObj = new Date(today);
    startObj.setDate(startObj.getDate() - (days - 1));
    const startStr = getLocalDateStr(startObj);

    setEndDate(endStr);
    setStartDate(startStr);
    setErrMsg('');
  };

  const handleThisMonth = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');

    setStartDate(`${year}-${month}-01`);
    setEndDate(`${year}-${month}-${day}`);
    setErrMsg('');
  };

  const handleLastMonth = () => {
    const today = new Date();
    let year = today.getFullYear();
    let month = today.getMonth(); // 0-based: Aug is 7, last month is 6 (July)
    if (month === 0) {
      month = 12;
      year -= 1;
    }
    const monthStr = String(month).padStart(2, '0');
    const lastDayNum = new Date(year, month, 0).getDate();
    const lastDayStr = String(lastDayNum).padStart(2, '0');

    setStartDate(`${year}-${monthStr}-01`);
    setEndDate(`${year}-${monthStr}-${lastDayStr}`);
    setErrMsg('');
  };

  // 匹配数据行数统计
  const filteredCount = data.filter((item) => {
    const itemDateStr = item[dateField] || item.launchDate || item.date;
    if (!itemDateStr) return false;
    if (startDate && itemDateStr < startDate) return false;
    if (endDate && itemDateStr > endDate) return false;
    return true;
  }).length;

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrMsg('');

    if (!startDate || !endDate) {
      setErrMsg('请选择完整的时间范围（开始日期与结束日期）');
      return;
    }

    const s = new Date(startDate);
    const eDate = new Date(endDate);

    if (s > eDate) {
      setErrMsg('开始日期不能晚于结束日期');
      return;
    }

    if (diffDays > maxDays) {
      setErrMsg(`选择的时间跨度为 ${diffDays} 天，不可超过 ${maxDays} 天！请重新调整日期范围`);
      return;
    }

    onConfirmExport({ startDate, endDate, format });
    onClose();
  };

  return (
    <div className="modal-overlay" style={{ zIndex: 99999 }} onClick={onClose}>
      <div
        className="modal-card"
        style={{ maxWidth: '480px', width: '92%' }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <FileSpreadsheet size={20} color="#10b981" />
            <h3 className="modal-title">{title}</h3>
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {errMsg && (
              <div className="alert-banner alert-error" style={{ fontSize: '0.82rem' }}>
                <AlertCircle size={16} />
                <span>{errMsg}</span>
              </div>
            )}

            <div style={{ fontSize: '0.83rem', color: 'var(--text-sub)' }}>
              请选择要导出的数据时间段（天数限制：<strong style={{ color: '#f43f5e' }}>不超过 {maxDays} 天</strong>）：
            </div>

            {/* 快捷设置预设 */}
            <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ fontSize: '0.78rem', padding: '0.25rem 0.55rem' }}
                onClick={() => handleQuickPreset(7)}
              >
                近 7 天
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ fontSize: '0.78rem', padding: '0.25rem 0.55rem' }}
                onClick={() => handleQuickPreset(30)}
              >
                近 30 天
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ fontSize: '0.78rem', padding: '0.25rem 0.55rem' }}
                onClick={() => handleQuickPreset(90)}
              >
                近 90 天
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ fontSize: '0.78rem', padding: '0.25rem 0.55rem' }}
                onClick={handleThisMonth}
              >
                本月
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ fontSize: '0.78rem', padding: '0.25rem 0.55rem' }}
                onClick={handleLastMonth}
              >
                上月
              </button>
            </div>

            {/* 开始与结束日期选择框 */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="form-group">
                <label className="form-label" style={{ fontSize: '0.8rem' }}>
                  <Calendar size={14} style={{ marginRight: '4px' }} />
                  开始日期
                </label>
                <input
                  type="date"
                  className="form-input"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label" style={{ fontSize: '0.8rem' }}>
                  <Calendar size={14} style={{ marginRight: '4px' }} />
                  结束日期
                </label>
                <input
                  type="date"
                  className="form-input"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* 导出文件格式选择 */}
            <div className="form-group">
              <label className="form-label" style={{ fontSize: '0.8rem' }}>
                <FileSpreadsheet size={14} style={{ marginRight: '4px' }} />
                导出文件格式
              </label>
              <div style={{ display: 'flex', gap: '1.2rem', marginTop: '0.2rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.83rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="fileFormat"
                    value="xlsx"
                    checked={format === 'xlsx'}
                    onChange={(e) => setFormat(e.target.value)}
                  />
                  <span style={{ color: 'var(--text-main)', fontWeight: format === 'xlsx' ? 600 : 400 }}>
                    Excel 工作表 (.xlsx)
                  </span>
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.83rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="fileFormat"
                    value="csv"
                    checked={format === 'csv'}
                    onChange={(e) => setFormat(e.target.value)}
                  />
                  <span style={{ color: 'var(--text-main)', fontWeight: format === 'csv' ? 600 : 400 }}>
                    CSV 文本文件 (.csv)
                  </span>
                </label>
              </div>
            </div>

            {/* 时间跨度与匹配条数提示 */}
            <div
              style={{
                background: diffDays > maxDays ? 'rgba(244, 63, 94, 0.1)' : 'var(--bg-hover)',
                border: diffDays > maxDays ? '1px solid rgba(244, 63, 94, 0.3)' : '1px solid var(--border-light)',
                borderRadius: '0.5rem',
                padding: '0.6rem 0.8rem',
                fontSize: '0.82rem',
                color: diffDays > maxDays ? '#f43f5e' : 'var(--text-main)',
                display: 'flex',
                justify: 'space-between',
                alignItems: 'center'
              }}
            >
              <span>选择跨度：<strong>{diffDays > 0 ? diffDays : 0} 天</strong> {diffDays > maxDays ? '(已超出90天限制)' : ''}</span>
              <span style={{ color: 'var(--accent-blue)', fontWeight: 600 }}>匹配数据: {filteredCount} 条</span>
            </div>
          </div>

          <div className="modal-footer" style={{ marginTop: '1rem' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              取消
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={diffDays > maxDays || diffDays <= 0}
              style={{
                background: 'linear-gradient(135deg, #10b981, #059669)',
                borderColor: '#10b981',
                gap: '0.4rem'
              }}
            >
              <Download size={16} />
              <span>确认导出</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
