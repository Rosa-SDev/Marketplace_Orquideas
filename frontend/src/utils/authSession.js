const TOKEN_KEY = 'token';
const AUTH_TOKEN_CHANGED_EVENT = 'auth-token-changed';
const AUTH_SESSION_EXPIRED_EVENT = 'auth-session-expired';

const emitAuthEvent = (eventName) => {
  window.dispatchEvent(new Event(eventName));
};

export const getStoredToken = () => localStorage.getItem(TOKEN_KEY);

export const saveToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token);
  emitAuthEvent(AUTH_TOKEN_CHANGED_EVENT);
};

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
  emitAuthEvent(AUTH_TOKEN_CHANGED_EVENT);
};

export const expireSession = () => {
  localStorage.removeItem(TOKEN_KEY);
  emitAuthEvent(AUTH_TOKEN_CHANGED_EVENT);
  emitAuthEvent(AUTH_SESSION_EXPIRED_EVENT);
};

export const parseJwtPayload = (token) => {
  try {
    const payloadPart = token.split('.')[1];
    const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
    return JSON.parse(window.atob(padded));
  } catch (error) {
    return null;
  }
};

export const getTokenExpirationMs = (token) => {
  const payload = parseJwtPayload(token);
  if (!payload?.exp) return null;
  return payload.exp * 1000;
};

export const getRemainingSessionMs = (token) => {
  const expirationMs = getTokenExpirationMs(token);
  if (!expirationMs) return 0;
  return expirationMs - Date.now();
};

export const formatRemainingTime = (remainingMs) => {
  const totalSeconds = Math.max(0, Math.ceil(remainingMs / 1000));
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0');
  const seconds = String(totalSeconds % 60).padStart(2, '0');
  return `${minutes}:${seconds}`;
};

export const AUTH_EVENTS = {
  tokenChanged: AUTH_TOKEN_CHANGED_EVENT,
  sessionExpired: AUTH_SESSION_EXPIRED_EVENT,
};
