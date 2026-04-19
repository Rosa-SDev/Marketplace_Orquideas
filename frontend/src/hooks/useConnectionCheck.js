import { useCallback, useEffect, useState } from 'react';
import api from '../services/api';

const MENSAJE_ERROR_CONEXION =
  'No fue posible conectar con el servidor. Verifica que el backend este encendido e intenta nuevamente.';

const useConnectionCheck = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const verificarConexion = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      await api.get('/orquideas');
    } catch (err) {
      setError(MENSAJE_ERROR_CONEXION);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    verificarConexion();
  }, [verificarConexion]);

  return {
    loading,
    error,
    retry: verificarConexion,
  };
};

export default useConnectionCheck;
