import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Check } from 'lucide-react';

export default function CustomSelect({ value, onChange, options = [], placeholder = '请选择', style, className, disabled, placement = 'auto' }) {
  const [isOpen, setIsOpen] = useState(false);
  const [dropUp, setDropUp] = useState(false);
  const containerRef = useRef(null);

  const selectedOption = options.find((opt) => String(opt.value) === String(value));

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleToggle = () => {
    if (disabled) return;
    if (!isOpen && containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      const spaceBelow = window.innerHeight - rect.bottom;
      if (placement === 'top' || (placement === 'auto' && spaceBelow < 200)) {
        setDropUp(true);
      } else {
        setDropUp(false);
      }
    }
    setIsOpen(!isOpen);
  };

  return (
    <div
      ref={containerRef}
      className={`custom-select-container ${disabled ? 'disabled' : ''} ${className || ''}`}
      style={{ position: 'relative', display: 'inline-block', zIndex: isOpen ? 10000 : 1, ...style }}
    >
      <button
        type="button"
        className={`custom-select-trigger ${isOpen ? 'open' : ''}`}
        onClick={handleToggle}
        disabled={disabled}
      >
        <span className="custom-select-label">
          {selectedOption ? selectedOption.label : placeholder}
        </span>
        <ChevronDown
          size={14}
          className="custom-select-arrow"
          style={{
            transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)',
            transition: 'transform 0.2s ease',
            color: 'var(--text-sub)',
            flexShrink: 0,
          }}
        />
      </button>

      {isOpen && (
        <div
          className="custom-select-dropdown"
          style={dropUp ? { top: 'auto', bottom: '100%', marginBottom: '4px' } : {}}
        >
          {options.map((option) => {
            const isSelected = String(option.value) === String(value);
            return (
              <div
                key={option.value}
                className={`custom-select-option ${isSelected ? 'selected' : ''}`}
                onClick={() => {
                  onChange(option.value);
                  setIsOpen(false);
                }}
              >
                <span>{option.label}</span>
                {isSelected && <Check size={14} className="option-check-icon" />}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
