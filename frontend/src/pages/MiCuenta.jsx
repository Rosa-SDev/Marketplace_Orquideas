import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import api from '../services/api';
import Loading from '../components/ui/Loading';
import './MiCuenta.css';

const MiCuenta = () => {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [mostrarModalSalir, setMostrarModalSalir] = useState(false);

  const handleLogout = () => {
    setMostrarModalSalir(false);
    logout();
    navigate('/');
  };
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const cargarPedidos = async () => {
      try {
        const response = await api.get('/pedidos/historial');
        setPedidos(response.data);
      } catch (err) {
        setError('No se pudo cargar el historial de pedidos.');
      } finally {
        setLoading(false);
      }
    };

    cargarPedidos();
  }, []);

  return (
    <main className="mi-cuenta-page">

      <div className="mi-cuenta-header">
        <h1>Mi Cuenta</h1>
        {usuario && (
          <p>Bienvenido, <strong>{usuario.nombre}</strong></p>
        )}
      </div>

      <div className="mi-cuenta-contenido">

        <section className="mi-cuenta-card">
          <h2>Mis datos</h2>
          {usuario && (
            <div>
              <p><strong>Nombre:</strong> {usuario.nombre}</p>
              <p><strong>Correo:</strong> {usuario.correo}</p>
              <p><strong>Rol:</strong> {usuario.rol}</p>
            </div>
          )}
        </section>

        <section className="mi-cuenta-card">
          <h2>Historial de pedidos</h2>

          {loading && <Loading mensaje="Cargando pedidos..." />}

          {error && (
            <p style={{ color: '#E91E8C' }}>{error}</p>
          )}

          {!loading && !error && pedidos.length === 0 && (
            <div>
              <p style={{ color: '#666', marginBottom: '1rem' }}>
                No tienes pedidos todavía.
              </p>
              <button
                onClick={() => navigate('/catalogo')}
                style={{
                  backgroundColor: '#2D6A4F',
                  color: '#FAF7F5',
                  border: 'none',
                  borderRadius: '20px',
                  padding: '0.6rem 1.4rem',
                  cursor: 'pointer'
                }}
              >
                Ver catálogo
              </button>
            </div>
          )}

          {!loading && !error && pedidos.length > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {pedidos.map(pedido => (
                <div
                  key={pedido.id}
                  style={{
                    border: '1px solid #eee',
                    borderRadius: '8px',
                    padding: '1rem'
                  }}
                >
                  <p><strong>Pedido #{pedido.id}</strong></p>
                  <p style={{ color: '#666', fontSize: '0.85rem' }}>
                    Estado: {pedido.estado}
                  </p>
                  <p style={{ color: '#E91E8C', fontWeight: 'bold' }}>
                    Total: ${pedido.total?.toLocaleString('es-CO')}
                  </p>
                </div>
              ))}
            </div>
          )}
        </section>

      </div>

      {/* Botón cerrar sesión — solo visible en móvil */}
      <button
        className="mi-cuenta-btn-salir"
        onClick={() => setMostrarModalSalir(true)}
      >
        Cerrar sesión
      </button>

      {/* Modal confirmación */}
      {mostrarModalSalir && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#fff', borderRadius: '12px', padding: '2rem',
            maxWidth: '400px', width: '90%', textAlign: 'center'
          }}>
            <h3 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>Cerrar sesión</h3>
            <p style={{ color: '#666', marginBottom: '1.5rem' }}>
              ¿Estás seguro que quieres cerrar sesión?
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
              <button
                onClick={() => setMostrarModalSalir(false)}
                style={{ padding: '0.6rem 1.5rem', borderRadius: '20px', border: '1px solid #ddd', cursor: 'pointer', backgroundColor: '#fff' }}
              >Cancelar</button>
              <button onClick={handleLogout} className="btn-confirmar">Cerrar sesión</button>
            </div>
          </div>
        </div>
      )}

    </main>
  );
};

export default MiCuenta;