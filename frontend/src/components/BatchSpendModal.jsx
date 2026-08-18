import React, { useState } from 'react';
import { X, Upload, FileText, CheckCircle, AlertCircle } from 'lucide-react';

export default function BatchSpendModal({ isOpen, onClose, onSaved, authFetch, targetUserId }) {
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  if (!isOpen) return null;

  const fetchFunc = authFetch || fetch;
  
  const parseLines = () => {
    const lines = inputText.split(/\r?\n/);
    const parsed = [];
    const errors = [];

    lines.forEach((rawLine, index) => {
      const lineNum = index + 1;
      const line = rawLine.trim();
      if (!line) return; // Skip empty lines

      // Split by tab or space
      const tokens = line.split(/[\t\s]+/);
      if (tokens.length < 2) {
        errors.push(`第 ${lineNum} 行格式不正确: "${line}" (须包含 日期 和 消耗)`);
        return;
      }

      let dateStr = tokens[0].trim();
      const spendStr = tokens[1].trim();
      const remark = tokens.slice(2).join(' ').trim();

      // Validate date (YYYY/M/D, YYYY-M-D, YYYY/MM/DD, YYYY-MM-DD)
      const dateRegex = /^\d{4}[\/\-]\d{1,2}[\/\-]\d{1,2}$/;
      if (!dateRegex.test(dateStr)) {
        errors.push(`第 ${lineNum} 行日期格式错误: "${dateStr}" (需为 2026/7/10 或 2026-07-10)`);
        return;
      }

      // Standardize date to YYYY-MM-DD (zero-padding month & day)
      const parts = dateStr.split(/[\/\-]/);
      const year = parts[0];
      const month = parts[1].padStart(2, '0');
      const day = parts[2].padStart(2, '0');
      const normalizedDate = `${year}-${month}-${day}`;

      // Clean thousand-separator commas (e.g. 12,121,872.28 -> 12121872.28)
      const cleanedSpendStr = spendStr.replace(/,/g, '');
      const spendNum = parseFloat(cleanedSpendStr);
      if (isNaN(spendNum) || spendNum < 0) {
        errors.push(`第 ${lineNum} 行消耗金额错误: "${spendStr}" (需为有效数字)`);
        return;
      }

      parsed.push({
        launchDate: normalizedDate,
        spend: spendNum,
        remark: remark,
      });
    });

    return { parsed, errors };
  };

  const { parsed, errors } = parseLines();

  const handleSubmit = async () => {
    if (parsed.length === 0) {
      setErrorMsg('未检测到有效的消耗数据，请按格式粘贴输入');
      return;
    }

    if (errors.length > 0) {
      setErrorMsg(`格式校验失败 (${errors.length} 处错误)，请修正在提交`);
      return;
    }

    setLoading(true);
    setErrorMsg('');

    try {
      const res = await fetchFunc('/api/ltv/batch-spend', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          items: parsed,
          targetUserId: targetUserId || null,
        }),
      });
      const json = await res.json();
      if (json.code === 0) {
        setInputText('');
        onSaved && onSaved(json.count || parsed.length);
        onClose();
      } else {
        setErrorMsg(json.msg || '批量保存失败');
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        setErrorMsg('网络请求异常，保存失败');
      }
    } finally {
      setLoading(false);
    }
  };

  const sampleText = `2026/7/10\t1,872.28\n2026-7-11\t12,121,872.28\n2026/07/12\t642.84`;

  return (
    <div className="modal-overlay">
      <div className="modal-card" style={{ maxWidth: '650px' }}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Upload size={20} color="var(--accent-blue)" />
            <h3 className="modal-title">批量导入账户消耗</h3>
          </div>
          <button className="btn btn-secondary" style={{ padding: '0.25rem' }} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="modal-body">
          <div style={{ fontSize: '0.85rem', color: 'var(--text-sub)', background: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)' }}>
            <strong>支持格式：</strong><code>日期(yyyy-MM-dd / yyyy/M/d)  消耗金额(支持逗号如12,121,872.28)  [备注]</code>，以空格或 Tab 分隔，多行换行。
          </div>

          <textarea
            className="form-textarea"
            style={{ height: '160px', fontFamily: 'monospace', fontSize: '0.85rem' }}
            placeholder={`在此直接粘贴 EXCEL 或文本数据，例如：\n${sampleText}`}
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
          />

          {/* 校验实时反馈区 */}
          {inputText.trim() && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '120px', overflowY: 'auto' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                <span style={{ color: 'var(--text-sub)' }}>
                  已解析 <strong>{parsed.length}</strong> 条有效数据
                </span>
                {errors.length > 0 && (
                  <span style={{ color: 'var(--accent-rose)', fontWeight: 600 }}>
                    发现 {errors.length} 处格式问题
                  </span>
                )}
              </div>

              {errors.length > 0 && (
                <div style={{ background: 'rgba(244, 63, 94, 0.1)', border: '1px solid rgba(244, 63, 94, 0.3)', padding: '0.5rem', borderRadius: '0.375rem', fontSize: '0.75rem', color: 'var(--accent-rose)' }}>
                  {errors.map((err, i) => (
                    <div key={i}>{err}</div>
                  ))}
                </div>
              )}
            </div>
          )}

          {errorMsg && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(244, 63, 94, 0.15)', border: '1px solid #f43f5e', color: '#fda4af', padding: '0.65rem 0.85rem', borderRadius: '0.5rem', fontSize: '0.85rem' }}>
              <AlertCircle size={16} flexShrink={0} />
              <span>{errorMsg}</span>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose} disabled={loading}>
            取消
          </button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading || parsed.length === 0 || errors.length > 0}>
            <CheckCircle size={16} />
            <span>{loading ? '正在保存重算...' : `确定导入 (${parsed.length} 条)`}</span>
          </button>
        </div>
      </div>
    </div>
  );
}
