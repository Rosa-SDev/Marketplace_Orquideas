import { useLocation, useNavigate } from 'react-router-dom';

const PagoRechazado = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const { status, referencia } = location.state || {};

  return (
    <main style={{
      minHeight: '80vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: '#FAF7F5',
      padding: '2rem'
    }}>
      <div style={{
        backgroundColor: '#fff',
        borderRadius: '16px',
        padding: '3rem 2.5rem',
        maxWidth: '500px',
        width: '100%',
        textAlign: 'center',
        boxShadow: '0 4px 20px rgba(0,0,0,0.08)'
      }}>

        {/* Icono de X */}
        <div style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          backgroundColor: '#E91E8C',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 1.5rem'
        }}>
          <span style={{ color: '#fff', fontSize: '2.5rem' }}>✕</span>
        </div>

        <h1 style={{ color: '#1B4332', fontSize: '1.8rem', marginBottom: '0.5rem' }}>
          Pago no procesado
        </h1>
        <p style={{ color: '#666', marginBottom: '2rem' }}>
          No pudimos procesar tu pago. Puedes intentarlo de nuevo o elegir otro método de pago.
        </p>

        {/* Detalles */}
        {(status || referencia) && (
          <div style={{
            backgroundColor: '#FAF7F5',
            borderRadius: '12px',
            padding: '1.2rem',
            marginBottom: '2rem',
            textAlign: 'left'
          }}>
            {referencia && (
              <p style={{ color: '#1B4332', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
                <strong>Referencia:</strong> {referencia}
              </p>
            )}
            {status && (
              <p style={{ color: '#E91E8C', fontSize: '0.9rem' }}>
                <strong>Estado:</strong> {status}
              </p>
            )}
          </div>
        )}

        {/* Botones */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button
            onClick={() => navigate('/checkout')}
            style={{
              backgroundColor: '#2D6A4F',
              color: '#FAF7F5',
              border: 'none',
              borderRadius: '20px',
              padding: '0.8rem',
              cursor: 'pointer',
              fontSize: '0.95rem',
              fontWeight: 'bold'
            }}
          >
            Reintentar pago
          </button>

          <button
            onClick={() => navigate('/carrito')}
            style={{
              backgroundColor: 'transparent',
              color: '#2D6A4F',
              border: '1px solid #2D6A4F',
              borderRadius: '20px',
              padding: '0.8rem',
              cursor: 'pointer',
              fontSize: '0.95rem'
            }}
          >
            Volver al carrito
          </button>
        </div>

      </div>
    </main>
  );
};

export default PagoRechazado;