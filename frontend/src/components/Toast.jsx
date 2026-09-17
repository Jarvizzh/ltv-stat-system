import React from 'react';
import { CheckCircle2, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

export default function Toast({ toast, onClose }) {
  if (!toast) return null;

  const { message, type } = toast;

  const getIcon = () => {
    switch (type) {
      case 'success':
        return <CheckCircle2 size={15} color="#34d399" />;
      case 'error':
        return <AlertCircle size={15} color="#fb7185" />;
      case 'warning':
        return <AlertTriangle size={15} color="#fbbf24" />;
      case 'info':
      default:
        return <Info size={15} color="#60a5fa" />;
    }
  };

  const getBorderColor = () => {
    switch (type) {
      case 'success': return '1px solid rgba(16, 185, 129, 0.4)';
      case 'error': return '1px solid rgba(244, 63, 94, 0.4)';
      case 'warning': return '1px solid rgba(245, 158, 11, 0.4)';
      case 'info':
      default: return '1px solid rgba(59, 130, 246, 0.4)';
    }
  };

  const getBgColor = () => {
    switch (type) {
      case 'success': return '#064e3b';
      case 'error': return '#881337';
      case 'warning': return '#78350f';
      case 'info':
      default: return '#1e3a8a';
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: '18px',
        right: '18px',
        zIndex: 999999,
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        padding: '7px 12px',
        background: getBgColor(),
        border: getBorderColor(),
        borderRadius: '7px',
        color: '#ffffff',
        boxShadow: '0 8px 22px rgba(0, 0, 0, 0.45), 0 0 0 1px rgba(255, 255, 255, 0.12)',
        backdropFilter: 'none',
        WebkitBackdropFilter: 'none',
        fontSize: '0.8rem',
        fontWeight: 500,
        minWidth: '200px',
        maxWidth: '380px',
        lineHeight: 1.4,
        animation: 'toastSlideIn 0.25s cubic-bezier(0.16, 1, 0.3, 1)'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', flexShrink: 0 }}>
        {getIcon()}
      </div>
      <div style={{ flex: 1, wordBreak: 'break-word' }}>
        {message}
      </div>
      <button
        onClick={onClose}
        style={{
          background: 'none',
          border: 'none',
          color: '#94a3b8',
          cursor: 'pointer',
          padding: '2px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderRadius: '4px',
          marginLeft: '4px'
        }}
      >
        <X size={14} />
      </button>

      <style>{`
        @keyframes toastSlideIn {
          from {
            transform: translateY(-20px) scale(0.95);
            opacity: 0;
          }
          to {
            transform: translateY(0) scale(1);
            opacity: 1;
          }
        }
      `}</style>
    </div>
  );
}
