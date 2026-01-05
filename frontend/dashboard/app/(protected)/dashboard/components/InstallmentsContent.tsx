export default function InstallmentsContent() {
  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Installments</h2>
      <div style={styles.placeholder}>
        <p style={styles.placeholderText}>Installment plans will be displayed here</p>
      </div>
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
  placeholder: {
    padding: '60px 20px',
    background: 'rgba(255, 255, 255, 0.05)',
    borderRadius: '12px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    textAlign: 'center',
  },
  placeholderText: {
    color: 'rgba(232, 238, 252, 0.6)',
    fontSize: '16px',
  },
};

