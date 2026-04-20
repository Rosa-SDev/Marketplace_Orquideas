import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Loading from '../components/ui/Loading';
import useCarritoStore from '../store/carritoStore';
import { consumePendingCartAdd, consumePostLoginRedirect } from '../utils/authFlowStorage';

const AuthCallback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const agregar = useCarritoStore((state) => state.agregar);

  useEffect(() => {
    const token = searchParams.get('token');

    if (!token) {
      setError('No se recibio un token valido desde el inicio de sesion.');
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

        // Modificado (Matt): si habia un "agregar al carrito" pendiente, lo ejecutamos
        const pendingAdd = consumePendingCartAdd();
        if (pendingAdd?.producto) {
          const cantidad = Number(pendingAdd.cantidad) > 0 ? Number(pendingAdd.cantidad) : 1;
          for (let i = 0; i < cantidad; i += 1) {
            agregar(pendingAdd.producto);
          }
        }

        const redirectPath = consumePostLoginRedirect();

        // Si es administrador lo mandamos al panel admin, si no al inicio
        if (redirectPath) {
          navigate(redirectPath);
        } else if (usuario.rol === 'ADMINISTRADOR') {
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

  return <Loading mensaje="Finalizando autenticacion..." />;
};

export default AuthCallback;
