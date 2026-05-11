import { useLocation, useNavigate } from 'react-router-dom';

const PagoExitoso = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Recibimos los datos de la transaccion desde el Checkout
  const { referencia, transactionId, status, items, total, nombre, direccion } = location.state || {};

  const compartirWhatsApp = async () => {
    const productos = items
      ?.map(item => `- ${item.nombre} x${item.cantidad} -> $${(item.precio * item.cantidad).toLocaleString('es-CO')}`)
      .join('\n') || '';

    const mensaje = [
      'Hola, acabo de completar mi compra en Orquídeas del Combeima.',
      '',
      'Resumen del pedido:',
      productos,
      '',
      `Total: $${total?.toLocaleString('es-CO')}`,
      '',
      `Nombre: ${nombre}`,
      `Dirección: ${direccion}`,
      '',
      `Referencia: ${referencia}`,
      `Estado: ${status}`,
    ].join('\n');

    const url = `https://wa.me/573014791094?text=${encodeURIComponent(mensaje)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

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

        {/* Icono de check */}
        <div style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          backgroundColor: '#2D6A4F',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 1.5rem'
        }}>
          <span style={{ color: '#fff', fontSize: '2.5rem' }}>✓</span>
        </div>

        <h1 style={{ color: '#1B4332', fontSize: '1.8rem', marginBottom: '0.5rem' }}>
          Pago realizado
        </h1>
        <p style={{ color: '#666', marginBottom: '2rem' }}>
          Tu pedido ha sido procesado correctamente.
        </p>

        {/* Detalles de la transaccion */}
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
          {transactionId && (
            <p style={{ color: '#1B4332', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
              <strong>ID de transaccion:</strong> {transactionId}
            </p>
          )}
          {status && (
            <p style={{ color: '#1B4332', fontSize: '0.9rem' }}>
              <strong>Estado:</strong> {status}
            </p>
          )}
        </div>

        <p style={{ color: '#666', fontSize: '0.9rem', marginBottom: '2rem' }}>
          Recibirás un correo de confirmación en breve.
        </p>

        {/* Botones */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button
            onClick={compartirWhatsApp}
            style={{
              backgroundColor: '#25D366',
              color: '#fff',
              border: 'none',
              borderRadius: '20px',
              padding: '0.8rem',
              cursor: 'pointer',
              fontSize: '0.95rem',
              fontWeight: 'bold'
            }}
          >
            Enviar resumen por WhatsApp
          </button>

          <button
            onClick={() => navigate('/mi-cuenta')}
            style={{
              backgroundColor: '#2D6A4F',
              color: '#FAF7F5',
              border: 'none',
              borderRadius: '20px',
              padding: '0.8rem',
              cursor: 'pointer',
              fontSize: '0.95rem'
            }}
          >
            Ver mis pedidos
          </button>

          <button
            onClick={() => navigate('/')}
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
            Volver al inicio
          </button>
        </div>

      </div>
    </main>
  );
};

export default PagoExitoso;