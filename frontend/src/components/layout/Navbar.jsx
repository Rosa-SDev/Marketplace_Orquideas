import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';
import logo from '../../assets/logo.png';
import useAuth from '../../hooks/useAuth';

const Navbar = () => {
  const { isLoggedIn, usuario, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">

        {/* Logo */}
        <Link to="/" className="navbar-logo">
          <img src={logo} alt="Logo Orquideas del Combeima" className="navbar-logo-image" />
        </Link>

        {/* Menu del centro */}
        <ul className="navbar-menu">
          <li><Link to="/">Inicio</Link></li>
          <li><Link to="/catalogo">Catálogo</Link></li>
          <li><Link to="/macetas">Macetas</Link></li>
          <li><Link to="/guia">Guía de Cuidado</Link></li>
          <li><Link to="/contacto">Contacto</Link></li>
        </ul>

        {/* Botones del lado derecho */}
        <div className="navbar-icons">

          {/* Si NO esta logueado muestra el boton Acceder */}
          {!isLoggedIn && (
            <Link to="/login" className="navbar-btn-acceder">
              Acceder
            </Link>
          )}

          {/* Si SI esta logueado muestra el nombre y boton de cerrar sesion */}
          {isLoggedIn && usuario && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
              <span style={{ color: '#1B4332', fontSize: '0.9rem' }}>
                Hola, {usuario.nombre}
              </span>
              <button
                onClick={handleLogout}
                style={{
                  backgroundColor: 'transparent',
                  border: '1px solid #2D6A4F',
                  color: '#2D6A4F',
                  padding: '0.4rem 1rem',
                  borderRadius: '20px',
                  cursor: 'pointer',
                  fontSize: '0.85rem'
                }}
              >
                Salir
              </button>
            </div>
          )}

          {/* Carrito — siempre visible */}
          <Link to="/carrito" className="navbar-carrito">
            Carrito (0)
          </Link>

        </div>
      </div>
    </nav>
  );
};

export default Navbar;