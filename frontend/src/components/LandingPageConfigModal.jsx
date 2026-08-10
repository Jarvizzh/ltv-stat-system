import React, { useState, useEffect } from 'react';
import { X, Save, Link2, Plus, Trash2, FileText, List } from 'lucide-react';
import CustomSelect from './CustomSelect';

const TIMEZONE_OPTIONS = [
  { label: '美东时区 (ET)', value: 'ET' },
  { label: '北京时区 (BJ)', value: 'BJ' },
];

export default function LandingPageConfigModal({ isOpen, onClose, onSaved, authFetch, targetUser, targetUserId }) {
  const [items, setItems] = useState([]); // [{ landingPageId: '', timezone: 'BJ' }]
  const [mode, setMode] = useState('list'); // 'list' | 'batch'
  const [batchText, setBatchText] = useState('');
  const [defaultBatchTz, setDefaultBatchTz] = useState('BJ');
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');

  const fetchFunc = authFetch || fetch;

  useEffect(() => {
    if (isOpen) {
      setMsg('');
      setLoading(true);

      const query = targetUserId ? `?targetUserId=${targetUserId}` : '';
      fetchFunc(`/api/user/landing-pages${query}`)
        .then((res) => res.json())
        .then((data) => {
          if (data && data.code === 0) {
            let list = [];
            if (Array.isArray(data.data)) {
              list = data.data.map((item) => {
                if (typeof item === 'string') {
                  return { landingPageId: item, timezone: 'BJ' };
                }
                return {
                  landingPageId: item.landingPageId || '',
                  timezone: item.timezone === 'ET' ? 'ET' : 'BJ',
                };
              });
            }
            if (list.length === 0) {
              list = [{ landingPageId: '', timezone: 'BJ' }];
            }
            setItems(list);

            // Sync batch text
            const batchLines = list
              .filter((it) => it.landingPageId.trim())
              .map((it) => `${it.landingPageId} ${it.timezone === 'ET' ? '美东' : '北京'}`);
            setBatchText(batchLines.join('\n'));
          }
        })
        .catch((err) => console.error(err))
        .finally(() => setLoading(false));
    }
  }, [isOpen, targetUser, targetUserId]);

  if (!isOpen) return null;

  const handleAddItem = () => {
    setItems((prev) => [...prev, { landingPageId: '', timezone: 'BJ' }]);
  };

  const handleRemoveItem = (index) => {
    setItems((prev) => prev.filter((_, i) => i !== index));
  };

  const handleItemChange = (index, field, value) => {
    setItems((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  };



  const handleParseBatchText = () => {
    const lines = batchText.split('\n').map((l) => l.trim()).filter((l) => l.length > 0);
    const parsed = lines.map((line) => {
      const parts = line.split(/[,，\s\t]+/);
      const pid = parts[0].trim();
      let tz = defaultBatchTz;
      if (parts.length > 1) {
        const tag = parts[1].trim().toLowerCase();
        if (tag.includes('bj') || tag.includes('北京') || tag.includes('shanghai')) {
          tz = 'BJ';
        } else if (tag.includes('et') || tag.includes('美东') || tag.includes('york')) {
          tz = 'ET';
        }
      }
      return { landingPageId: pid, timezone: tz };
    });

    if (parsed.length > 0) {
      setItems(parsed);
      setMode('list');
      setMsg(`已从文本成功解析并导入 ${parsed.length} 条落地页配置！`);
    } else {
      setMsg('文本内容为空，请重新粘贴');
    }
  };

  const handleSave = async () => {
    setLoading(true);
    setMsg('保存配置中，并自动完成秒级数据重算...');

    let validItems = items
      .map((it) => ({
        landingPageId: (it.landingPageId || '').trim(),
        timezone: (it.timezone || 'BJ').toUpperCase() === 'ET' ? 'ET' : 'BJ',
      }))
      .filter((it) => it.landingPageId.length > 0);

    if (mode === 'batch' && batchText.trim()) {
      const lines = batchText.split('\n').map((l) => l.trim()).filter((l) => l.length > 0);
      validItems = lines.map((line) => {
        const parts = line.split(/[,，\s\t]+/);
        const pid = parts[0].trim();
        let tz = defaultBatchTz;
        if (parts.length > 1) {
          const tag = parts[1].trim().toLowerCase();
          if (tag.includes('bj') || tag.includes('北京') || tag.includes('shanghai')) {
            tz = 'BJ';
          } else if (tag.includes('et') || tag.includes('美东') || tag.includes('york')) {
            tz = 'ET';
          }
        }
        return { landingPageId: pid, timezone: tz };
      });
    }

    const endpoint = targetUser
      ? `/api/admin/users/${targetUser.id}/landing-pages`
      : '/api/user/landing-pages';

    const method = targetUser ? 'PUT' : 'POST';

    const payload = targetUser
      ? { landingPages: validItems }
      : { landingPages: validItems, targetUserId: targetUserId || null };

    try {
      const res = await fetchFunc(endpoint, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (res.ok && data && data.code === 0) {
        setMsg(data.msg || '保存成功！报表已根据新配置与时区完成实时计算。');
        setTimeout(() => {
          setLoading(false);
          if (onSaved) onSaved();
          onClose();
        }, 800);
      } else {
        const errorText = (data && (data.msg || data.message)) || `HTTP ${res.status}`;
        setMsg(`保存失败: ${errorText}`);
        setLoading(false);
      }
    } catch (err) {
      if (err.message !== 'UNAUTHORIZED') {
        setMsg('网络异常，保存失败，请稍后重试');
      }
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 620, width: '90%' }}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Link2 size={20} className="modal-header-icon" />
            <h3 className="modal-title">
              {targetUser ? `配置用户 [${targetUser.username}] 的落地页配置` : '落地页配置 (含时区设置)'}
            </h3>
          </div>
          <button className="btn btn-secondary" style={{ padding: '0.25rem' }} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="modal-body" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div className="segmented-tab-container">
              <button
                type="button"
                className={`segmented-tab-item ${mode === 'list' ? 'active' : ''}`}
                onClick={() => setMode('list')}
              >
                <List size={15} />
                <span>列表明细编辑</span>
              </button>
              <button
                type="button"
                className={`segmented-tab-item ${mode === 'batch' ? 'active' : ''}`}
                onClick={() => setMode('batch')}
              >
                <FileText size={15} />
                <span>批量粘贴导入</span>
              </button>
            </div>
          </div>

          {mode === 'list' ? (
            <div>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-sub)', marginBottom: '0.75rem' }}>
                配置各落地页 ID 及其归属时区。统计 LTV 报表时，系统将自动按配置的时区转换组别日期。
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {items.map((item, idx) => (
                  <div
                    key={idx}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      background: 'var(--bg-secondary)',
                      padding: '0.4rem 0.6rem',
                      borderRadius: '0.375rem',
                      border: '1px solid var(--border-color)',
                    }}
                  >
                    <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', width: '24px', textAlign: 'center' }}>
                      {idx + 1}
                    </span>
                    <input
                      type="text"
                      className="form-input"
                      style={{ flex: 1, padding: '0.35rem 0.6rem', fontSize: '0.85rem' }}
                      placeholder="落地页 ID (pId)"
                      value={item.landingPageId}
                      onChange={(e) => handleItemChange(idx, 'landingPageId', e.target.value)}
                    />
                    <CustomSelect
                      value={item.timezone}
                      onChange={(val) => handleItemChange(idx, 'timezone', val)}
                      options={TIMEZONE_OPTIONS}
                      style={{ width: '135px' }}
                    />
                    <button
                      type="button"
                      className="btn btn-secondary"
                      style={{ padding: '0.35rem', color: '#f43f5e' }}
                      onClick={() => handleRemoveItem(idx)}
                      title="删除此项"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>

              <button
                type="button"
                className="btn btn-secondary"
                style={{ marginTop: '0.75rem', width: '100%', justifyContent: 'center', borderStyle: 'dashed' }}
                onClick={handleAddItem}
              >
                <Plus size={16} />
                <span>添加落地页</span>
              </button>
            </div>
          ) : (
            <div>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-sub)', marginBottom: '0.5rem' }}>
                按行粘贴落地页 ID，每行一个。支持在 ID 后空格加时区标记（如 `405323 北京` 或 `405323 美东`）。无标记的行默认使用下方选择的时区：
              </p>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem', fontSize: '0.82rem' }}>
                <span style={{ color: 'var(--text-sub)' }}>未标注行的默认时区：</span>
                <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="defaultTz"
                    value="ET"
                    checked={defaultBatchTz === 'ET'}
                    onChange={() => setDefaultBatchTz('ET')}
                  />
                  美东时区 (ET)
                </label>
                <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="defaultTz"
                    value="BJ"
                    checked={defaultBatchTz === 'BJ'}
                    onChange={() => setDefaultBatchTz('BJ')}
                  />
                  北京时区 (BJ)
                </label>
              </div>

              <textarea
                className="form-textarea"
                rows={8}
                placeholder="例如:&#10;405323222546395136&#10;405323222546395137 北京&#10;405323222546395138 美东"
                value={batchText}
                onChange={(e) => setBatchText(e.target.value)}
              />

              <button
                type="button"
                className="btn btn-secondary"
                style={{ marginTop: '0.5rem', width: '100%', justifyContent: 'center' }}
                onClick={handleParseBatchText}
              >
                <span>解析并导入到列表明细</span>
              </button>
            </div>
          )}

          {msg && (
            <div style={{ fontSize: '0.85rem', marginTop: 12, padding: '0.5rem 0.75rem', borderRadius: '0.375rem', background: msg.includes('失败') ? 'rgba(244, 63, 94, 0.15)' : 'rgba(6, 182, 212, 0.15)', color: msg.includes('失败') ? 'var(--accent-rose)' : 'var(--accent-cyan)' }}>
              {msg}
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose} disabled={loading}>
            取消
          </button>

          <button className="btn btn-primary" onClick={handleSave} disabled={loading}>
            <Save size={16} />
            <span>保存配置并秒级重算</span>
          </button>
        </div>
      </div>
    </div>
  );
}
