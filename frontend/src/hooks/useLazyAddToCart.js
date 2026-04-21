import { useLocation, useNavigate } from 'react-router-dom';
import useAuth from './useAuth';
import useCarritoStore from '../store/carritoStore';
import { savePendingCartAdd, savePostLoginRedirect } from '../utils/authFlowStorage';

const useLazyAddToCart = () => {
  const { isLoggedIn } = useAuth();
  const agregar = useCarritoStore((state) => state.agregar);
  const navigate = useNavigate();
  const location = useLocation();

  const agregarConLoginLazy = (producto, cantidad = 1) => {
    const hasSession = isLoggedIn || Boolean(localStorage.getItem('token'));

    if (!hasSession) {
      savePendingCartAdd({ producto, cantidad });
      savePostLoginRedirect(`${location.pathname}${location.search || ''}`);
      navigate('/login', { state: { from: location.pathname, motivo: 'carrito' } });
      return false;
    }

    for (let i = 0; i < cantidad; i += 1) {
      agregar(producto);
    }

    return true;
  };

  return { agregarConLoginLazy };
};

export default useLazyAddToCart;
