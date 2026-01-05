'use client';

import { useState, useEffect } from 'react';
import type { Card } from '../types';
import { apiPost } from '@/app/lib/api';
import AddCardModal from './AddCardModal';

const getCardColor = (type: string): string => {
  const colors: Record<string, string> = {
    visa: 'linear-gradient(135deg, #ff6b35 0%, #ff8c5a 100%)',
    paypay: 'linear-gradient(135deg, #00b2e3 0%, #0088cc 100%)',
    '7-11': 'linear-gradient(135deg, #ff6600 0%, #ff8800 100%)',
    mizuho: 'linear-gradient(135deg, #c41230 0%, #e63950 100%)',
    rakuten: 'linear-gradient(135deg, #bf0000 0%, #e60012 100%)',
  };
  return colors[type.toLowerCase()] || colors.visa;
};

const getCardLogo = (type: string): string => {
  return type.toUpperCase();
};

export default function CardsContent() {
  const [cards, setCards] = useState<Card[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  // TODO: Replace with actual API call to fetch cards
  useEffect(() => {
    // Mock data for now - replace with API call
    const mockCards: Card[] = [
      {
        id: '1',
        number: '3829 4820 4629 5025',
        holder: 'Anita Rose',
        validThru: '09/17',
        type: 'Visa',
      },
    ];
    setCards(mockCards);
    setLoading(false);
  }, []);

  const handleAddCardSuccess = () => {
    // Refresh cards list
    // TODO: Fetch cards from API
    // For now, we'll just close the modal
    // In production, you'd call: fetchCards();
  };

  const formatCardNumber = (number: string): string => {
    // Format as XXXX XXXX XXXX XXXX
    return number.replace(/(.{4})/g, '$1 ').trim();
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>Loading cards...</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>My Cards</h2>
      <div style={styles.cardsGrid}>
        {cards.map((card) => (
          <div
            key={card.id}
            style={{
              ...styles.card,
              background: getCardColor(card.type),
            }}
          >
            <div style={styles.cardHeader}>
              <div style={styles.cardLogo}>{getCardLogo(card.type)}</div>
            </div>
            <div style={styles.cardNumber}>{formatCardNumber(card.number)}</div>
            <div style={styles.cardHolderLabel}>CARD HOLDER NAME</div>
            <div style={styles.cardHolder}>{card.holder}</div>
            <div style={styles.cardFooter}>
              <div>
                <div style={styles.cardValidLabel}>VALID THRU</div>
                <div style={styles.cardValid}>{card.validThru}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
      <button
        style={styles.addCardButton}
        onClick={() => setIsModalOpen(true)}
      >
        <span style={styles.addCardIcon}>+</span>
        Add New Card
      </button>

      <AddCardModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleAddCardSuccess}
      />
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    padding: '20px 0',
  },
  title: {
    fontSize: '24px',
    fontWeight: '600',
    marginBottom: '30px',
    color: '#e8eefc',
  },
  cardsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
    gap: '20px',
    marginBottom: '30px',
  },
  card: {
    aspectRatio: '16/10',
    borderRadius: '16px',
    padding: '24px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    color: '#fff',
    boxShadow: '0 8px 24px rgba(0, 0, 0, 0.3)',
    position: 'relative',
    overflow: 'hidden',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'flex-end',
  },
  cardLogo: {
    fontSize: '18px',
    fontWeight: 'bold',
    opacity: 0.9,
  },
  cardNumber: {
    fontSize: '20px',
    fontWeight: '600',
    letterSpacing: '3px',
    marginBottom: '30px',
    fontFamily: 'monospace',
  },
  cardHolderLabel: {
    fontSize: '10px',
    opacity: 0.8,
    marginBottom: '6px',
    textTransform: 'uppercase',
    letterSpacing: '1px',
  },
  cardHolder: {
    fontSize: '16px',
    fontWeight: '600',
    marginBottom: '20px',
  },
  cardFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
  },
  cardValidLabel: {
    fontSize: '10px',
    opacity: 0.8,
    marginBottom: '4px',
    textTransform: 'uppercase',
    letterSpacing: '1px',
  },
  cardValid: {
    fontSize: '14px',
    fontWeight: '600',
  },
  addCardButton: {
    padding: '16px 24px',
    background: 'rgba(255, 255, 255, 0.05)',
    border: '2px dashed rgba(255, 255, 255, 0.2)',
    borderRadius: '12px',
    color: '#e8eefc',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '10px',
    transition: 'all 0.2s',
  },
  addCardIcon: {
    fontSize: '24px',
    fontWeight: 'bold',
  },
  loading: {
    padding: '40px',
    textAlign: 'center',
    color: 'rgba(232, 238, 252, 0.6)',
    fontSize: '16px',
  },
};

