// Login.jsx
// Pantalla de inicio de sesion con Google

import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import logo from '../assets/logo.png';
import { savePostLoginRedirect } from '../utils/authFlowStorage';

const Login = () => {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Si el usuario ya esta logueado, lo mandamos al inicio
  useEffect(() => {
    if (isLoggedIn) {
      navigate('/');
    }
  }, [isLoggedIn, navigate]);

  const handleLoginGoogle = () => {
    // Guardar a dónde quería ir el usuario
    const from = location.state?.from;
    if (from) savePostLoginRedirect(from);

    window.location.href = `${import.meta.env.VITE_API_URL?.replace('/api', '')}/oauth2/authorization/google`;
  };

  return (
    <div style={{
      minHeight: '80vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: '#FAF7F5',
      padding: '2rem'
    }}>

      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '16px',
        padding: '3rem 2.5rem',
        boxShadow: '0 4px 20px rgba(0,0,0,0.10)',
        width: '100%',
        maxWidth: '420px',
        textAlign: 'center'
      }}>

        <img
          src={logo}
          alt="Logo Orquideas del Combeima"
          style={{ height: '80px', marginBottom: '1.5rem' }}
        />

        <h1 style={{ color: '#1B4332', fontSize: '1.5rem', marginBottom: '0.5rem' }}>
          Bienvenido
        </h1>

        <p style={{ color: '#666', marginBottom: '2rem', fontSize: '0.95rem' }}>
          Inicia sesion para acceder a tu cuenta
        </p>

        {/* Mensajes según motivo */}
        {location.state?.motivo === 'carrito' && (
          <p style={{ color: '#8a5a00', marginBottom: '1rem', fontSize: '0.9rem' }}>
            Necesitas iniciar sesion para agregar productos al carrito.
          </p>
        )}

        {location.state?.motivo === 'expirada' && (
          <p style={{ color: '#8a5a00', marginBottom: '1rem', fontSize: '0.9rem' }}>
            Tu sesion expiro, inicia sesion de nuevo para continuar.
          </p>
        )}

        <button
          onClick={handleLoginGoogle}
          style={{
            width: '100%',
            padding: '0.8rem',
            borderRadius: '8px',
            border: '1px solid #ddd',
            backgroundColor: '#fff',
            cursor: 'pointer',
            fontSize: '0.95rem',
            color: '#333',
            marginBottom: '2rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '0.8rem'
          }}
        >
          <img
            src="https://www.google.com/favicon.ico"
            alt="Google"
            style={{ width: '20px', height: '20px' }}
          />
          Continuar con Google
        </button>

        <div style={{ textAlign: 'left' }}>
          <p style={{ color: '#1B4332', fontWeight: 'bold', marginBottom: '0.8rem', fontSize: '0.9rem' }}>
            Al iniciar sesion puedes:
          </p>

          {[
            'Agregar productos al carrito',
            'Ver el historial de tus pedidos',
            'Guardar tus direcciones de envio',
          ].map((beneficio) => (
            <div key={beneficio} style={{
              display: 'flex',
              gap: '0.5rem',
              marginBottom: '0.5rem',
              alignItems: 'center'
            }}>
              <span style={{ color: '#2D6A4F', fontWeight: 'bold' }}>✓</span>
              <p style={{ color: '#666', fontSize: '0.85rem', margin: 0 }}>
                {beneficio}
              </p>
            </div>
          ))}
        </div>

      </div>
    </div>
  );
};

export default Login;