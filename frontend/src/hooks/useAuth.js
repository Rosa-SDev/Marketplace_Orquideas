// useAuth.js
// Este hook es un atajo para usar el authStore facilmente
// En lugar de importar useAuthStore en cada componente, importamos useAuth

import useAuthStore from '../store/authStore';
import { useEffect } from 'react';
import api from '../services/api';

const useAuth = () => {
  const { usuario, token, isLoggedIn, login, logout, setUsuario } = useAuthStore();

  // Cuando la app carga, verificamos si ya habia una sesion guardada
  // Esto evita que el usuario tenga que loguearse cada vez que recarga
  useEffect(() => {
    const tokenGuardado = localStorage.getItem('token');

    if (tokenGuardado && !isLoggedIn) {
      // Si hay token guardado, le preguntamos al backend quien es el usuario
      api.get('/auth/me')
        .then((response) => {
          setUsuario(response.data);
        })
        .catch(() => {
          // Si el token ya no es valido, limpiamos todo
          localStorage.removeItem('token');
        });
    }
  }, []);

  return { usuario, token, isLoggedIn, login, logout };
};

export default useAuth;