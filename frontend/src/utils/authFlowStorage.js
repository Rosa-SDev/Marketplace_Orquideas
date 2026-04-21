const PENDING_CART_ADD_KEY = 'pending_cart_add';
const POST_LOGIN_REDIRECT_KEY = 'post_login_redirect';

export const savePendingCartAdd = (payload) => {
  sessionStorage.setItem(PENDING_CART_ADD_KEY, JSON.stringify(payload));
};

export const consumePendingCartAdd = () => {
  const raw = sessionStorage.getItem(PENDING_CART_ADD_KEY);
  sessionStorage.removeItem(PENDING_CART_ADD_KEY);

  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

export const savePostLoginRedirect = (path) => {
  if (!path) return;
  sessionStorage.setItem(POST_LOGIN_REDIRECT_KEY, path);
};

export const consumePostLoginRedirect = () => {
  const path = sessionStorage.getItem(POST_LOGIN_REDIRECT_KEY);
  sessionStorage.removeItem(POST_LOGIN_REDIRECT_KEY);
  return path;
};
