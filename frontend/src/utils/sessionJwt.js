export const getJwtExpirationMs = (token) => {
  if (!token || typeof token !== 'string') return null;

  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;

    const payloadBase64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(window.atob(payloadBase64));

    if (!payload.exp || typeof payload.exp !== 'number') return null;

    return payload.exp * 1000;
  } catch (error) {
    return null;
  }
};

export const getSecondsUntilExpiration = (token) => {
  const expirationMs = getJwtExpirationMs(token);
  if (!expirationMs) return null;

  return Math.floor((expirationMs - Date.now()) / 1000);
};
