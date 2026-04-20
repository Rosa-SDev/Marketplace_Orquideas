// AuthCallback.jsx
// Esta pagina recibe el token que manda el backend despues del login con Google
// El backend redirige a /auth/callback?token=XXXXXX
// Nosotros capturamos ese token, pedimos los datos del usuario y guardamos la sesion

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import api from '../services/api';
import Loading from '../components/ui/Loading';

const AuthCallback = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const procesarLogin = async () => {
      // Leemos el token de la URL — window.location.search es "?token=XXXXX"
      const params = new URLSearchParams(window.location.search);
      const token = params.get('token');

      if (!token) {
        // Si no hay token algo salió mal, mandamos al login
        navigate('/login');
        return;
      }

      try {
        // Guardamos el token temporalmente para que api.js lo use en la siguiente peticion
        localStorage.setItem('token', token);

        // Le preguntamos al backend quién es el usuario con ese token
        const response = await api.get('/auth/me');
        const usuario = response.data;

        // Guardamos el usuario y el token en Zustand
        login(usuario, token);

        // Si es administrador lo mandamos al panel admin, si no al inicio
        if (usuario.rol === 'ADMINISTRADOR') {
          navigate('/admin');
        } else {
          navigate('/');
        }
      } catch (err) {
        // Si algo falla limpiamos y mandamos al login
        localStorage.removeItem('token');
        navigate('/login');
      }
    };

    procesarLogin();
  }, []);

  // Mientras procesa mostramos el spinner
  return <Loading mensaje="Iniciando sesion..." />;
};

export default AuthCallback;