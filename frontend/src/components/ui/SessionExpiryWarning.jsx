const formatSeconds = (secondsLeft) => {
  const safeSeconds = Math.max(0, secondsLeft || 0);
  const minutes = Math.floor(safeSeconds / 60);
  const seconds = safeSeconds % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
};

const SessionExpiryWarning = ({ secondsLeft, onDismiss, onLoginNow }) => {
  return (
    <div
      style={{
        position: 'fixed',
        right: '1rem',
        bottom: '1rem',
        zIndex: 2000,
        backgroundColor: '#fff7e6',
        border: '1px solid #ffd27f',
        borderRadius: '12px',
        boxShadow: '0 8px 24px rgba(0, 0, 0, 0.12)',
        maxWidth: '360px',
        width: 'calc(100% - 2rem)',
        padding: '1rem'
      }}
    >
      <p style={{ margin: 0, fontWeight: 'bold', color: '#8a5a00' }}>
        Tu sesion esta por expirar
      </p>
      <p style={{ margin: '0.5rem 0 0.9rem 0', color: '#5c4a1f' }}>
        Tiempo restante: {formatSeconds(secondsLeft)}
      </p>

      <div style={{ display: 'flex', gap: '0.6rem', justifyContent: 'flex-end' }}>
        <button
          onClick={onDismiss}
          style={{
            backgroundColor: 'transparent',
            border: '1px solid #d8b873',
            color: '#8a5a00',
            borderRadius: '8px',
            padding: '0.45rem 0.7rem',
            cursor: 'pointer'
          }}
        >
          Cerrar aviso
        </button>
        <button
          onClick={onLoginNow}
          style={{
            backgroundColor: '#2d6a4f',
            border: 'none',
            color: '#fff',
            borderRadius: '8px',
            padding: '0.45rem 0.7rem',
            cursor: 'pointer'
          }}
        >
          Iniciar sesion
        </button>
      </div>
    </div>
  );
};

export default SessionExpiryWarning;
