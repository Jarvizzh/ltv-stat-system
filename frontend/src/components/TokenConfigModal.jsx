import React, { useState, useEffect } from 'react';
import { X, Key, Check } from 'lucide-react';

export default function TokenConfigModal({ isOpen, onClose, onSaved, authFetch }) {
  const [authorization, setAuthorization] = useState('');
  const [cookie, setCookie] = useState('');
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');

  const fetchFunc = authFetch || fetch;

  useEffect(() => {
    if (isOpen) {
      fetchFunc('/api/token/get')
        .then((res) => res.json())
        .then((data) => {
          if (data.code === 0) {
            setAuthorization(data.authorization || '');
            setCookie(data.cookie || '');
          }
        })
        .catch((err) => console.error(err));
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSave = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMsg('');
    try {
      const res = await fetchFunc('/api/token/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ authorization, cookie }),
      });
      const data = await res.json();
      if (data.code === 0) {
        setMsg('Token 更新成功！将自动重新触发数据拉取...');
        setTimeout(() => {
          setLoading(false);
          onSaved();
          onClose();
        }, 1000);
      } else {
        setMsg(`更新失败: ${data.msg}`);
        setLoading(false);
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        setMsg('网络请求失败');
      }
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Key size={18} color="#3b82f6" />
            <span>订单 API Token / 鉴权配置</span>
          </h3>
          <button className="btn btn-secondary" style={{ padding: '0.25rem' }} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSave}>
          <div className="modal-body">
            <div className="form-group">
              <label className="form-label">
                Authorization Token:
              </label>
              <input
                type="text"
                className="form-input"
                value={authorization}
                onChange={(e) => setAuthorization(e.target.value)}
                placeholder="例如: 0451eaf3-dd00-499d-87b7-de4be2d18a43"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Cookie / JSESSIONID (可选):
              </label>
              <input
                type="text"
                className="form-input"
                value={cookie}
                onChange={(e) => setCookie(e.target.value)}
                placeholder="例如: JSESSIONID=b959ba11-507c-4f63-8a01-b16ab37f96f4"
              />
            </div>

            <div style={{ fontSize: '0.8rem', color: '#94a3b8', background: 'rgba(59, 130, 246, 0.1)', padding: '0.75rem', borderRadius: '0.5rem' }}>
              提示：当目标系统提示 <code>4002 登录信息已过期</code> 时，请在原系统按 <code>F12</code> 开发者工具查看 Fetch/XHR 请求中的最新 <strong>Authorization</strong> Header 粘贴至此处保存即可。
            </div>

            {msg && (
              <div style={{ fontSize: '0.85rem', color: '#34d399', fontWeight: 500 }}>
                {msg}
              </div>
            )}
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              取消
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              <Check size={16} />
              <span>{loading ? '保存中...' : '保存 Token'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
