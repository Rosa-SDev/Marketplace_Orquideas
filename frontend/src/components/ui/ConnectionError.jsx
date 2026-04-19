const ConnectionError = ({ mensaje, onRetry }) => {
  return (
    <section
      style={{
        maxWidth: '900px',
        margin: '2rem auto',
        padding: '1.25rem',
        border: '1px solid #f5c2c7',
        backgroundColor: '#fff1f2',
        color: '#7f1d1d',
        borderRadius: '10px',
        textAlign: 'center'
      }}
    >
      <h2 style={{ marginTop: 0 }}>No se pudo cargar la pagina</h2>
      <p style={{ marginBottom: '1rem' }}>{mensaje}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          style={{
            border: 'none',
            borderRadius: '8px',
            padding: '0.6rem 1rem',
            cursor: 'pointer',
            backgroundColor: '#b91c1c',
            color: '#fff'
          }}
        >
          Reintentar
        </button>
      )}
    </section>
  );
};

export default ConnectionError;
