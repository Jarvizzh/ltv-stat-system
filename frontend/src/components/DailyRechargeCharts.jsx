import React, { useState } from 'react';
import { TrendingUp, PieChart, Users, DollarSign } from 'lucide-react';

export default function DailyRechargeCharts({ distributionData = [] }) {
  const dataList = Array.isArray(distributionData) ? distributionData : [];

  // 取按日期升序排列的最近 15 天数据
  const sortedData = [...dataList]
    .filter((item) => item && item.date)
    .sort((a, b) => (a.date > b.date ? 1 : -1))
    .slice(-15);

  const [hoverIndex1, setHoverIndex1] = useState(null);
  const [hoverIndex2, setHoverIndex2] = useState(null);

  if (sortedData.length === 0) {
    return null;
  }

  // 格式化金额
  const formatUsd = (val) => {
    const num = parseFloat(val || 0);
    return `$${num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  // 生成平滑贝塞尔曲线路径
  const getSmoothPath = (points) => {
    if (!points || points.length === 0) return '';
    if (points.length === 1) return `M ${points[0].x} ${points[0].y}`;
    let d = `M ${points[0].x} ${points[0].y}`;
    for (let i = 0; i < points.length - 1; i++) {
      const curr = points[i];
      const next = points[i + 1];
      const mx = (curr.x + next.x) / 2;
      d += ` C ${mx} ${curr.y}, ${mx} ${next.y}, ${next.x} ${next.y}`;
    }
    return d;
  };

  // 生成曲线下方的渐变填充路径
  const getAreaPath = (points, bottomY) => {
    if (!points || points.length === 0) return '';
    const lineD = getSmoothPath(points);
    const first = points[0];
    const last = points[points.length - 1];
    return `${lineD} L ${last.x} ${bottomY} L ${first.x} ${bottomY} Z`;
  };

  // ==================== 图表 1: 充值金额 & 付费人数 趋势图 ====================
  const svgWidth1 = 650;
  const svgHeight1 = 230;
  const padding1 = { top: 30, right: 55, bottom: 40, left: 55 };
  const graphW1 = svgWidth1 - padding1.left - padding1.right;
  const graphH1 = svgHeight1 - padding1.top - padding1.bottom;

  // 极大值计算
  const maxRecharge = Math.max(...sortedData.map((d) => parseFloat(d.totalRecharge || 0)), 100);
  const maxPaidUsers = Math.max(...sortedData.map((d) => parseInt(d.totalPaidUsers || 0)), 10);

  const yMax1 = Math.ceil((maxRecharge * 1.15) / 100) * 100;
  const yMax2 = Math.ceil(maxPaidUsers * 1.15);

  const count1 = sortedData.length;
  const stepX1 = count1 > 1 ? graphW1 / (count1 - 1) : graphW1;

  // 坐标数据映射
  const pointsRecharge = sortedData.map((item, idx) => {
    const x = padding1.left + idx * stepX1;
    const val = parseFloat(item.totalRecharge || 0);
    const y = padding1.top + graphH1 * (1 - val / yMax1);
    return { x, y, val, item };
  });

  const pointsUsers = sortedData.map((item, idx) => {
    const x = padding1.left + idx * stepX1;
    const val = parseInt(item.totalPaidUsers || 0);
    const y = padding1.top + graphH1 * (1 - val / yMax2);
    return { x, y, val, item };
  });

  const bottomY1 = padding1.top + graphH1;

  // ==================== 图表 2: 老用户充值占比 趋势图 ====================
  const svgWidth2 = 650;
  const svgHeight2 = 230;
  const padding2 = { top: 30, right: 40, bottom: 40, left: 50 };
  const graphW2 = svgWidth2 - padding2.left - padding2.right;
  const graphH2 = svgHeight2 - padding2.top - padding2.bottom;

  // 老用户占比计算 (0% ~ 100%)
  const ratioData = sortedData.map((item) => {
    const total = parseFloat(item.totalRecharge || 0);
    const old = parseFloat(item.oldRecharge || 0);
    const ratio = item.oldRechargeRatio != null
      ? parseFloat(item.oldRechargeRatio) * 100
      : (total > 0 ? (old / total) * 100 : 0);
    return {
      ratio: Math.min(100, Math.max(0, ratio)),
      item,
    };
  });

  const maxRatio = Math.max(...ratioData.map((d) => d.ratio), 10);
  const yMaxRatio = Math.min(100, Math.ceil((maxRatio * 1.15) / 10) * 10);

  const stepX2 = count1 > 1 ? graphW2 / (count1 - 1) : graphW2;

  const pointsRatio = ratioData.map((d, idx) => {
    const x = padding2.left + idx * stepX2;
    const y = padding2.top + graphH2 * (1 - d.ratio / yMaxRatio);
    return { x, y, val: d.ratio, item: d.item };
  });

  const bottomY2 = padding2.top + graphH2;

  return (
    <div className="charts-grid-container">
      {/* -------------------- 图表 1 -------------------- */}
      <div className="chart-card">
        <div className="chart-header">
          <div className="chart-title-group">
            <TrendingUp size={18} color="#10b981" />
            <span className="chart-title">近 15 天充值金额 / 人数趋势</span>
          </div>
          <div className="chart-legends">
            <span className="legend-item">
              <span className="legend-dot" style={{ backgroundColor: '#10b981' }}></span>
              充值金额 ($)
            </span>
            <span className="legend-item">
              <span className="legend-dot" style={{ backgroundColor: '#8b5cf6' }}></span>
              付费人数 (人)
            </span>
          </div>
        </div>

        <div className="chart-body">
          <svg viewBox={`0 0 ${svgWidth1} ${svgHeight1}`} className="chart-svg">
            <defs>
              <linearGradient id="gradientRecharge" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#10b981" stopOpacity="0.35" />
                <stop offset="100%" stopColor="#10b981" stopOpacity="0.0" />
              </linearGradient>
              <linearGradient id="gradientUsers" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#8b5cf6" stopOpacity="0.2" />
                <stop offset="100%" stopColor="#8b5cf6" stopOpacity="0.0" />
              </linearGradient>
            </defs>

            {/* 背景水平参考网格线 (4条) */}
            {[0, 0.33, 0.66, 1].map((ratio, idx) => {
              const y = padding1.top + graphH1 * (1 - ratio);
              const val1 = Math.round(yMax1 * ratio);
              const val2 = Math.round(yMax2 * ratio);
              return (
                <g key={idx}>
                  <line
                    x1={padding1.left}
                    y1={y}
                    x2={svgWidth1 - padding1.right}
                    y2={y}
                    stroke="var(--border-color)"
                    strokeDasharray="4 4"
                    strokeOpacity="0.5"
                  />
                  {/* 左侧金额刻度 */}
                  <text
                    x={padding1.left - 8}
                    y={y + 4}
                    fill="var(--text-sub)"
                    fontSize="10"
                    textAnchor="end"
                  >
                    ${val1 >= 1000 ? `${(val1 / 1000).toFixed(1)}k` : val1}
                  </text>
                  {/* 右侧人数刻度 */}
                  <text
                    x={svgWidth1 - padding1.right + 8}
                    y={y + 4}
                    fill="var(--text-sub)"
                    fontSize="10"
                    textAnchor="start"
                  >
                    {val2}人
                  </text>
                </g>
              );
            })}

            {/* X 轴日期 Label */}
            {pointsRecharge.map((pt, idx) => {
              const dateStr = pt.item.date ? pt.item.date.slice(5) : '';
              return (
                <text
                  key={idx}
                  x={pt.x}
                  y={svgHeight1 - 12}
                  fill={hoverIndex1 === idx ? 'var(--text-main)' : 'var(--text-sub)'}
                  fontSize="11"
                  fontWeight={hoverIndex1 === idx ? '600' : '400'}
                  textAnchor="middle"
                >
                  {dateStr}
                </text>
              );
            })}

            {/* 充值金额 渐变包络 & 折线 */}
            <path d={getAreaPath(pointsRecharge, bottomY1)} fill="url(#gradientRecharge)" />
            <path
              d={getSmoothPath(pointsRecharge)}
              fill="none"
              stroke="#10b981"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />

            {/* 付费人数 渐变包络 & 折线 */}
            <path d={getAreaPath(pointsUsers, bottomY1)} fill="url(#gradientUsers)" />
            <path
              d={getSmoothPath(pointsUsers)}
              fill="none"
              stroke="#8b5cf6"
              strokeWidth="2"
              strokeDasharray="5 3"
              strokeLinecap="round"
              strokeLinejoin="round"
            />

            {/* 数据圆点 */}
            {pointsRecharge.map((pt, idx) => (
              <circle
                key={`recharge-${idx}`}
                cx={pt.x}
                cy={pt.y}
                r={hoverIndex1 === idx ? 5 : 3.5}
                fill="#10b981"
                stroke="var(--bg-card)"
                strokeWidth="1.5"
              />
            ))}

            {pointsUsers.map((pt, idx) => (
              <circle
                key={`users-${idx}`}
                cx={pt.x}
                cy={pt.y}
                r={hoverIndex1 === idx ? 5 : 3.5}
                fill="#8b5cf6"
                stroke="var(--bg-card)"
                strokeWidth="1.5"
              />
            ))}

            {/* Hover 垂直参考高亮线与隐形捕获区域 */}
            {pointsRecharge.map((pt, idx) => {
              const isHover = hoverIndex1 === idx;
              const hoverW = stepX1;
              return (
                <g key={`hover-zone-${idx}`}>
                  {isHover && (
                    <line
                      x1={pt.x}
                      y1={padding1.top}
                      x2={pt.x}
                      y2={bottomY1}
                      stroke="var(--accent-cyan)"
                      strokeWidth="1.5"
                      strokeDasharray="3 3"
                    />
                  )}
                  <rect
                    x={pt.x - hoverW / 2}
                    y={padding1.top}
                    width={hoverW}
                    height={graphH1}
                    fill="transparent"
                    onMouseEnter={() => setHoverIndex1(idx)}
                    onMouseLeave={() => setHoverIndex1(null)}
                    style={{ cursor: 'pointer' }}
                  />
                </g>
              );
            })}
          </svg>

          {/* Hover Tooltip 浮层 */}
          {hoverIndex1 !== null && pointsRecharge[hoverIndex1] && (
            <div
              className="chart-tooltip-box"
              style={{
                left: `${(pointsRecharge[hoverIndex1].x / svgWidth1) * 100}%`,
                top: '20px',
                transform: hoverIndex1 > count1 / 2 ? 'translate(-105%, 0)' : 'translate(5%, 0)',
              }}
            >
              <div className="tooltip-date">{pointsRecharge[hoverIndex1].item.date}</div>
              <div className="tooltip-row">
                <span className="tooltip-dot" style={{ backgroundColor: '#10b981' }}></span>
                <span>总充值: </span>
                <strong>{formatUsd(pointsRecharge[hoverIndex1].val)}</strong>
              </div>
              <div className="tooltip-row">
                <span className="tooltip-dot" style={{ backgroundColor: '#8b5cf6' }}></span>
                <span>付费人数: </span>
                <strong>{pointsUsers[hoverIndex1].val} 人</strong>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* -------------------- 图表 2 -------------------- */}
      <div className="chart-card">
        <div className="chart-header">
          <div className="chart-title-group">
            <PieChart size={18} color="#f59e0b" />
            <span className="chart-title">近 15 天老用户充值占比趋势</span>
          </div>
          <div className="chart-legends">
            <span className="legend-item">
              <span className="legend-dot" style={{ backgroundColor: '#f59e0b' }}></span>
              老用户充值占比 (%)
            </span>
          </div>
        </div>

        <div className="chart-body">
          <svg viewBox={`0 0 ${svgWidth2} ${svgHeight2}`} className="chart-svg">
            <defs>
              <linearGradient id="gradientRatio" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#f59e0b" stopOpacity="0.35" />
                <stop offset="100%" stopColor="#f59e0b" stopOpacity="0.0" />
              </linearGradient>
            </defs>

            {/* 水平参考线 (4条: 0%, 25%, 50%, 75%, 100%) */}
            {[0, 0.25, 0.5, 0.75, 1].map((ratio, idx) => {
              const y = padding2.top + graphH2 * (1 - ratio);
              const valPct = (yMaxRatio * ratio).toFixed(0);
              return (
                <g key={idx}>
                  <line
                    x1={padding2.left}
                    y1={y}
                    x2={svgWidth2 - padding2.right}
                    y2={y}
                    stroke="var(--border-color)"
                    strokeDasharray="4 4"
                    strokeOpacity="0.5"
                  />
                  <text
                    x={padding2.left - 8}
                    y={y + 4}
                    fill="var(--text-sub)"
                    fontSize="10"
                    textAnchor="end"
                  >
                    {valPct}%
                  </text>
                </g>
              );
            })}

            {/* X 轴日期 Label */}
            {pointsRatio.map((pt, idx) => {
              const dateStr = pt.item.date ? pt.item.date.slice(5) : '';
              return (
                <text
                  key={idx}
                  x={pt.x}
                  y={svgHeight2 - 12}
                  fill={hoverIndex2 === idx ? 'var(--text-main)' : 'var(--text-sub)'}
                  fontSize="11"
                  fontWeight={hoverIndex2 === idx ? '600' : '400'}
                  textAnchor="middle"
                >
                  {dateStr}
                </text>
              );
            })}

            {/* 老用户占比 渐变包络 & 折线 */}
            <path d={getAreaPath(pointsRatio, bottomY2)} fill="url(#gradientRatio)" />
            <path
              d={getSmoothPath(pointsRatio)}
              fill="none"
              stroke="#f59e0b"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />

            {/* 数据圆点 */}
            {pointsRatio.map((pt, idx) => (
              <circle
                key={`ratio-${idx}`}
                cx={pt.x}
                cy={pt.y}
                r={hoverIndex2 === idx ? 5.5 : 3.5}
                fill="#f59e0b"
                stroke="var(--bg-card)"
                strokeWidth="1.5"
              />
            ))}

            {/* Hover 交互区域 */}
            {pointsRatio.map((pt, idx) => {
              const isHover = hoverIndex2 === idx;
              const hoverW = stepX2;
              return (
                <g key={`hover-zone2-${idx}`}>
                  {isHover && (
                    <line
                      x1={pt.x}
                      y1={padding2.top}
                      x2={pt.x}
                      y2={bottomY2}
                      stroke="#f59e0b"
                      strokeWidth="1.5"
                      strokeDasharray="3 3"
                    />
                  )}
                  <rect
                    x={pt.x - hoverW / 2}
                    y={padding2.top}
                    width={hoverW}
                    height={graphH2}
                    fill="transparent"
                    onMouseEnter={() => setHoverIndex2(idx)}
                    onMouseLeave={() => setHoverIndex2(null)}
                    style={{ cursor: 'pointer' }}
                  />
                </g>
              );
            })}
          </svg>

          {/* Hover Tooltip 浮层 */}
          {hoverIndex2 !== null && pointsRatio[hoverIndex2] && (
            <div
              className="chart-tooltip-box"
              style={{
                left: `${(pointsRatio[hoverIndex2].x / svgWidth2) * 100}%`,
                top: '20px',
                transform: hoverIndex2 > count1 / 2 ? 'translate(-105%, 0)' : 'translate(5%, 0)',
              }}
            >
              <div className="tooltip-date">{pointsRatio[hoverIndex2].item.date}</div>
              <div className="tooltip-row">
                <span className="tooltip-dot" style={{ backgroundColor: '#f59e0b' }}></span>
                <span>老用户占比: </span>
                <strong style={{ color: '#f59e0b' }}>{pointsRatio[hoverIndex2].val.toFixed(2)}%</strong>
              </div>
              <div className="tooltip-row">
                <span className="tooltip-dot" style={{ backgroundColor: 'var(--text-sub)' }}></span>
                <span>老用户充值: </span>
                <strong>{formatUsd(pointsRatio[hoverIndex2].item.oldRecharge)}</strong>
              </div>
              <div className="tooltip-row">
                <span className="tooltip-dot" style={{ backgroundColor: 'var(--text-sub)' }}></span>
                <span>老用户人数: </span>
                <strong>{pointsRatio[hoverIndex2].item.oldPaidUsers || 0} 人</strong>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
