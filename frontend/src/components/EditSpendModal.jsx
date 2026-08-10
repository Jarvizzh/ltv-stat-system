import React, { useState, useEffect } from 'react';
import { X, Check, AlertCircle } from 'lucide-react';

export default function EditSpendModal({ isOpen, item, onClose, onSaved, authFetch }) {
  const [spend, setSpend] = useState('');
  const [remark, setRemark] = useState('');
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState('');

  useEffect(() => {
    if (item) {
      setSpend(item.spend != null ? item.spend.toString() : '0');
      setRemark(item.remark || '');
      setErrMsg('');
    }
  }, [item]);

  if (!isOpen || !item) return null;

  const fetchFunc = authFetch || fetch;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrMsg('');

    // Clean thousand-separator commas
    const cleanedSpendStr = spend.toString().replace(/,/g, '').trim();
    const parsedSpend = parseFloat(cleanedSpendStr);

    if (isNaN(parsedSpend) || parsedSpend < 0) {
      setErrMsg('请输入有效的消耗金额数字');
      setLoading(false);
      return;
    }

    try {
      const res = await fetchFunc('/api/ltv/config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          launchDate: item.launchDate,
          spend: parsedSpend,
          remark: remark,
        }),
      });
      const data = await res.json();
      if (data.code === 0) {
        onSaved();
        onClose();
      } else {
        setErrMsg(data.msg || '保存失败');
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        setErrMsg('网络请求发生错误，请稍后重试');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" style={{ maxWidth: '420px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">更新投放配置 ({item.launchDate})</h3>
          <button className="btn btn-secondary" style={{ padding: '0.25rem' }} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {errMsg && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(244, 63, 94, 0.15)', border: '1px solid #f43f5e', color: '#fda4af', padding: '0.65rem 0.85rem', borderRadius: '0.5rem', fontSize: '0.85rem' }}>
                <AlertCircle size={16} flexShrink={0} />
                <span>{errMsg}</span>
              </div>
            )}

            <div className="form-group">
              <label className="form-label">账户消耗 ($):</label>
              <input
                type="text"
                className="form-input"
                value={spend}
                onChange={(e) => setSpend(e.target.value)}
                placeholder="例如: 1,872.28 或 1872.28"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">备注:</label>
              <input
                type="text"
                className="form-input"
                value={remark}
                onChange={(e) => setRemark(e.target.value)}
                placeholder=""
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              取消
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              <Check size={16} />
              <span>{loading ? '更新中...' : '确定保存'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
