'use client';

import { useState } from 'react';
import type { CardType, CreateCardRequest } from '../types';
import { apiPost } from '@/app/lib/api';

interface AddCardModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const CARD_TYPES: CardType[] = ['paypay', '7-11', 'Mizuho', 'Rakuten', 'Visa'];

export default function AddCardModal({ isOpen, onClose, onSuccess }: AddCardModalProps) {
  const [cardType, setCardType] = useState<CardType>('Visa');
  const [cardNumber, setCardNumber] = useState('');
  const [cardHolder, setCardHolder] = useState('');
  const [validThru, setValidThru] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Validate inputs
      if (!cardNumber.trim() || !cardHolder.trim() || !validThru.trim()) {
        setError('Please fill in all fields');
        setLoading(false);
        return;
      }

      // Format card number (remove spaces)
      const formattedNumber = cardNumber.replace(/\s/g, '');

      // Validate card number (basic check)
      if (formattedNumber.length < 13 || formattedNumber.length > 19) {
        setError('Card number must be between 13 and 19 digits');
        setLoading(false);
        return;
      }

      // Validate valid thru format (MM/YY)
      const validThruRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
      if (!validThruRegex.test(validThru)) {
        setError('Valid thru must be in MM/YY format');
        setLoading(false);
        return;
      }

      const request: CreateCardRequest = {
        type: cardType,
        number: formattedNumber,
        holder: cardHolder.trim(),
        validThru: validThru.trim(),
      };

      await apiPost('/api/ledger/cards', request);

      // Reset form
      setCardType('Visa');
      setCardNumber('');
      setCardHolder('');
      setValidThru('');
      setError('');

      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to create card. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let value = e.target.value.replace(/\s/g, '');
    // Add spaces every 4 digits
    value = value.match(/.{1,4}/g)?.join(' ') || value;
    // Limit to 19 digits (16 digits + 3 spaces)
    if (value.length <= 19) {
      setCardNumber(value);
    }
  };

  const handleValidThruChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let value = e.target.value.replace(/\D/g, '');
    // Format as MM/YY
    if (value.length >= 2) {
      value = value.slice(0, 2) + '/' + value.slice(2, 4);
    }
    if (value.length <= 5) {
      setValidThru(value);
    }
  };

  if (!isOpen) return null;

  return (
    <div style={styles.overlay} onClick={onClose}>
      <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div style={styles.header}>
          <h2 style={styles.title}>Add New Card</h2>
          <button style={styles.closeButton} onClick={onClose}>
            <svg width="24" height="24" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>
              Card Type
            </label>
            <select
              value={cardType}
              onChange={(e) => setCardType(e.target.value as CardType)}
              style={styles.select}
              required
            >
              {CARD_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              Card Number
            </label>
            <input
              type="text"
              value={cardNumber}
              onChange={handleCardNumberChange}
              placeholder="1234 5678 9012 3456"
              style={styles.input}
              required
              maxLength={19}
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              Card Holder Name
            </label>
            <input
              type="text"
              value={cardHolder}
              onChange={(e) => setCardHolder(e.target.value)}
              placeholder="John Doe"
              style={styles.input}
              required
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              Valid Thru
            </label>
            <input
              type="text"
              value={validThru}
              onChange={handleValidThruChange}
              placeholder="MM/YY"
              style={styles.input}
              required
              maxLength={5}
            />
          </div>

          {error && (
            <div style={styles.error}>
              {error}
            </div>
          )}

          <div style={styles.actions}>
            <button
              type="button"
              onClick={onClose}
              style={styles.cancelButton}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              style={styles.submitButton}
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Add Card'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0, 0, 0, 0.7)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
    padding: '20px',
  },
  modal: {
    background: 'linear-gradient(180deg, #0b1020 0%, #0a0f1a 60%, #070b12 100%)',
    borderRadius: '16px',
    padding: '30px',
    width: '100%',
    maxWidth: '500px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
  },
  title: {
    fontSize: '24px',
    fontWeight: '600',
    color: '#e8eefc',
    margin: 0,
  },
  closeButton: {
    background: 'transparent',
    border: 'none',
    color: '#e8eefc',
    cursor: 'pointer',
    padding: '4px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    opacity: 0.7,
    transition: 'opacity 0.2s',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    fontSize: '14px',
    fontWeight: '500',
    color: '#e8eefc',
  },
  input: {
    padding: '12px 16px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    color: '#e8eefc',
    fontSize: '14px',
    outline: 'none',
    transition: 'all 0.2s',
  },
  select: {
    padding: '12px 16px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    borderRadius: '8px',
    color: '#e8eefc',
    fontSize: '14px',
    outline: 'none',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
  error: {
    padding: '12px',
    background: 'rgba(231, 76, 60, 0.1)',
    border: '1px solid rgba(231, 76, 60, 0.3)',
    borderRadius: '8px',
    color: '#e74c3c',
    fontSize: '14px',
  },
  actions: {
    display: 'flex',
    gap: '12px',
    marginTop: '8px',
  },
  cancelButton: {
    flex: 1,
    padding: '12px 24px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '1px solid rgba(255, 255, 255, 0.2)',
    borderRadius: '8px',
    color: '#e8eefc',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
  submitButton: {
    flex: 1,
    padding: '12px 24px',
    background: '#ff6b35',
    border: 'none',
    borderRadius: '8px',
    color: '#fff',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
};

