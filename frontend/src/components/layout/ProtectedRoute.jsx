// ProtectedRoute.jsx
// Protege rutas que requieren estar logueado
// Si el usuario no tiene sesion, lo manda al login
// Uso: <ProtectedRoute> <PaginaProtegida /> </ProtectedRoute>

import { Navigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

const ProtectedRoute = ({ children }) => {
  const { isLoggedIn } = useAuth();

  if (!isLoggedIn) {
    // Navigate reemplaza la pagina actual por /login
    return <Navigate to="/login" />;
  }

  return children;
};

export default ProtectedRoute;