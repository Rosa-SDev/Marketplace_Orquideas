import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import api from '../../services/api';
import { getJwtExpirationMs } from '../../utils/sessionJwt';
import SessionExpiryWarning from '../ui/SessionExpiryWarning';
import { savePostLoginRedirect } from '../../utils/authFlowStorage';

const WARNING_SECONDS = 5 * 60;

const SessionManager = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [secondsLeft, setSecondsLeft] = useState(null);
  const [tokenKey, setTokenKey] = useState('');
  const [dismissedKey, setDismissedKey] = useState('');

  useEffect(() => {
    const syncCountdown = () => {
      const token = localStorage.getItem('token');

      if (!token) {
        setSecondsLeft(null);
        setTokenKey('');
        return;
      }

      const expirationMs = getJwtExpirationMs(token);
      if (!expirationMs) return;

      const currentKey = `${expirationMs}`;
      const remaining = Math.floor((expirationMs - Date.now()) / 1000);

      setTokenKey(currentKey);
      setSecondsLeft(remaining);

      if (remaining <= 0) {
        logout();
        savePostLoginRedirect(`${location.pathname}${location.search || ''}`);
        navigate('/login', { replace: true, state: { motivo: 'expirada' } });
      }
    };

    syncCountdown();
    const intervalId = window.setInterval(syncCountdown, 1000);

    return () => window.clearInterval(intervalId);
  }, [location.pathname, location.search, logout, navigate]);

  useEffect(() => {
    if (dismissedKey && tokenKey && dismissedKey !== tokenKey) {
      setDismissedKey('');
    }
  }, [dismissedKey, tokenKey]);

  useEffect(() => {
    const interceptorId = api.interceptors.response.use(
      (response) => response,
      (error) => {
        const status = error?.response?.status;

        if (status === 401 && localStorage.getItem('token')) {
          logout();
          savePostLoginRedirect(`${location.pathname}${location.search || ''}`);
          navigate('/login', { replace: true, state: { motivo: 'expirada' } });
        }

        return Promise.reject(error);
      }
    );

    return () => api.interceptors.response.eject(interceptorId);
  }, [location.pathname, location.search, logout, navigate]);

  const showWarning = useMemo(() => {
    if (secondsLeft === null || secondsLeft > WARNING_SECONDS) return false;
    if (!tokenKey) return false;
    return dismissedKey !== tokenKey;
  }, [dismissedKey, secondsLeft, tokenKey]);

  if (!showWarning) return null;

  return (
    <SessionExpiryWarning
      secondsLeft={secondsLeft}
      onDismiss={() => setDismissedKey(tokenKey)}
      onLoginNow={() => {
        logout();
        savePostLoginRedirect(`${location.pathname}${location.search || ''}`);
        navigate('/login', { replace: true, state: { motivo: 'expirada' } });
      }}
    />
  );
};

export default SessionManager;
