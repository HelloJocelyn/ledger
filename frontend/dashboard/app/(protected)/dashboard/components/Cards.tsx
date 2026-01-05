export default function Cards() {
  return (
    <section style={styles.rightSection}>
      <div style={styles.rightSectionHeader}>
        <h3 style={styles.rightSectionTitle}>Your cards</h3>
        <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
          <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
        </svg>
      </div>
      <div style={styles.cardStack}>
        <div style={styles.card}>
          <div style={styles.cardNumber}>3829 4820 4629 5025</div>
          <div style={styles.cardHolder}>CARD HOLDER NAME</div>
          <div style={styles.cardName}>Anita Rose</div>
          <div style={styles.cardValid}>VALID THRU 09/17</div>
        </div>
      </div>
    </section>
  );
}

const styles: Record<string, React.CSSProperties> = {
  rightSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  rightSectionHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  rightSectionTitle: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#e8eefc',
  },
  cardStack: {
    position: 'relative',
  },
  card: {
    width: '100%',
    aspectRatio: '16/10',
    background: 'linear-gradient(135deg, #ff6b35 0%, #ff8c5a 100%)',
    borderRadius: '16px',
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    color: '#fff',
    boxShadow: '0 8px 24px rgba(255, 107, 53, 0.3)',
  },
  cardNumber: {
    fontSize: '16px',
    fontWeight: '600',
    letterSpacing: '2px',
    marginBottom: '20px',
  },
  cardHolder: {
    fontSize: '10px',
    opacity: 0.8,
    marginBottom: '4px',
  },
  cardName: {
    fontSize: '14px',
    fontWeight: '600',
    marginBottom: '12px',
  },
  cardValid: {
    fontSize: '12px',
    opacity: 0.8,
  },
};

