import React from 'react';
import { LogOut, X, AlertTriangle } from 'lucide-react';

export default function LogoutConfirmModal({ isOpen, onClose, onConfirm, username }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-card"
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: '420px', width: '90%', padding: '1.75rem' }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: '1rem' }}>
          <div
            style={{
              width: '56px',
              height: '56px',
              borderRadius: '50%',
              background: 'rgba(244, 63, 94, 0.12)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '1px solid rgba(244, 63, 94, 0.25)',
              boxShadow: '0 0 20px rgba(244, 63, 94, 0.15)',
            }}
          >
            <LogOut size={26} color="#f43f5e" />
          </div>

          <div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: '600', color: 'var(--text-main)', marginBottom: '0.5rem' }}>
              退出登录确认
            </h3>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-sub)', lineHeight: '1.5' }}>
              确定要退出登录当前账号{' '}
              <span style={{ color: 'var(--accent-blue)', fontWeight: '600' }}>
                {username || localStorage.getItem('admin_username') || ''}
              </span>{' '}
              吗？
            </p>
          </div>

          <div style={{ display: 'flex', gap: '0.75rem', width: '100%', marginTop: '0.5rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ flex: 1, padding: '0.65rem 1rem', fontSize: '0.9rem', justifyContent: 'center' }}
              onClick={onClose}
            >
              取消
            </button>
            <button
              type="button"
              className="btn"
              style={{
                flex: 1,
                padding: '0.65rem 1rem',
                fontSize: '0.9rem',
                justifyContent: 'center',
                background: 'linear-gradient(135deg, #f43f5e, #e11d48)',
                color: '#ffffff',
                border: 'none',
                boxShadow: '0 4px 12px rgba(244, 63, 94, 0.35)',
              }}
              onClick={() => {
                onConfirm();
                onClose();
              }}
            >
              <LogOut size={16} />
              <span>确认退出</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
