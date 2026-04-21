import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import api from '../services/api';
import Loading from '../components/ui/Loading';
import useCarritoStore from '../store/carritoStore';
import {
  consumePendingCartAdd,
  consumePostLoginRedirect
} from '../utils/authFlowStorage';

const AuthCallback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const agregar = useCarritoStore((state) => state.agregar);

  useEffect(() => {
    const procesarLogin = async () => {
      const token = searchParams.get('token');

      if (!token) {
        navigate('/login', { replace: true });
        return;
      }

      try {
        // Guardar token
        localStorage.setItem('token', token);

        // Obtener usuario
        const response = await api.get('/auth/me');
        const usuario = response.data;

        // Guardar sesión global
        login(usuario, token);

        // Ejecutar acción pendiente (ej: agregar al carrito)
        const pendingAdd = consumePendingCartAdd();
        if (pendingAdd?.producto) {
          const cantidad =
            Number(pendingAdd.cantidad) > 0
              ? Number(pendingAdd.cantidad)
              : 1;

          for (let i = 0; i < cantidad; i++) {
            agregar(pendingAdd.producto);
          }
        }

        // Redirección inteligente
        const redirectPath = consumePostLoginRedirect();

        if (redirectPath) {
          navigate(redirectPath, { replace: true });
        } else if (usuario.rol === 'ADMINISTRADOR') {
          navigate('/admin', { replace: true });
        } else {
          navigate('/', { replace: true });
        }

      } catch (err) {
        localStorage.removeItem('token');
        navigate('/login', { replace: true });
      }
    };

    procesarLogin();
  }, [agregar, login, navigate, searchParams]);

  return <Loading mensaje="Finalizando autenticación..." />;
};

export default AuthCallback;