import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';
import logo from '../../assets/logo.png';
import useAuth from '../../hooks/useAuth';
import useCarritoStore from '../../store/carritoStore';
import { useState } from 'react';

const Navbar = () => {
  const { isLoggedIn, usuario, logout } = useAuth();
  const navigate = useNavigate();
  const totalItems = useCarritoStore((state) => state.totalItems());

  const [mostrarModalSalir, setMostrarModalSalir] = useState(false);
  const [menuUsuario, setMenuUsuario] = useState(false);
  const [menuAbierto, setMenuAbierto] = useState(false);

  const handleLogout = () => {
    setMostrarModalSalir(false);
    setMenuUsuario(false);
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

        {/* Boton hamburguesa */}
        <button
          className="navbar-hamburguesa"
          onClick={() => setMenuAbierto((v) => !v)}
        >
          <span className="material-icons">
            {menuAbierto ? 'close' : 'menu'}
          </span>
        </button>

        {/* Menu del centro */}
        <ul className={`navbar-menu ${menuAbierto ? 'menu-abierto' : ''}`}>
          <li><Link to="/" onClick={() => setMenuAbierto(false)}>Inicio</Link></li>
          <li><Link to="/catalogo" onClick={() => setMenuAbierto(false)}>Catálogo</Link></li>
          <li><Link to="/macetas" onClick={() => setMenuAbierto(false)}>Macetas</Link></li>
          <li><Link to="/guia" onClick={() => setMenuAbierto(false)}>Guía de Cuidado</Link></li>
          <li><Link to="/contacto" onClick={() => setMenuAbierto(false)}>Contacto</Link></li>
        </ul>

        {/* Botones del lado derecho */}
        <div className="navbar-icons">

          {/* Si NO esta logueado muestra el boton Acceder */}
          {!isLoggedIn && (
            <Link to="/login" className="navbar-btn-acceder">
              Acceder
            </Link>
          )}

          {/* Si SI esta logueado */}
          {isLoggedIn && usuario && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              {usuario.rol === 'ADMINISTRADOR' && (
                <Link to="/admin" className="navbar-panel-btn">
                  Panel
                </Link>
              )}

              {/* Botón Mi cuenta con dropdown */}
              <div className="navbar-usuario-wrapper">
                <button
                  className="navbar-mi-cuenta"
                  onClick={() => setMenuUsuario((v) => !v)}
                >
                  <span className="material-icons" style={{ fontSize: '1.3rem' }}>account_circle</span>
                  {usuario.nombre}
                </button>

                {menuUsuario && (
                  <div className="navbar-usuario-dropdown">
                    <Link
                      to="/mi-cuenta"
                      className="navbar-dropdown-item"
                      onClick={() => setMenuUsuario(false)}
                    >
                      <span className="material-icons">manage_accounts</span>
                      Mi cuenta
                    </Link>
                    <button
                      className="navbar-dropdown-item navbar-dropdown-salir"
                      onClick={() => { setMenuUsuario(false); setMostrarModalSalir(true); }}
                    >
                      <span className="material-icons">logout</span>
                      Salir
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Carrito — siempre visible */}
          <Link to="/carrito" className="navbar-carrito">
            <span className="material-icons">shopping_cart</span>
            {totalItems > 0 && (
              <span className="navbar-carrito-badge">{totalItems}</span>
            )}
          </Link>

        </div>
      </div>

      {/* Modal de confirmacion para cerrar sesion */}
      {mostrarModalSalir && (
        <div style={{
          position: 'fixed',
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#fff',
            borderRadius: '12px',
            padding: '2rem',
            maxWidth: '400px',
            textAlign: 'center',
            width: '90%'
          }}>
            <h3 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>
              Cerrar sesión
            </h3>
            <p style={{ color: '#666', marginBottom: '1.5rem' }}>
              ¿Estás seguro que quieres cerrar sesión?
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
              <button
                onClick={() => setMostrarModalSalir(false)}
                style={{
                  padding: '0.6rem 1.5rem',
                  borderRadius: '20px',
                  border: '1px solid #ddd',
                  cursor: 'pointer',
                  backgroundColor: '#fff'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleLogout}
                style={{
                  padding: '0.6rem 1.5rem',
                  borderRadius: '20px',
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: '#2D6A4F',
                  color: '#fff'
                }}
              >
                Cerrar sesión
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Barra de navegacion inferior para movil */}
      <div className="navbar-bottom-mobile">
        <Link to="/" className="navbar-bottom-item" onClick={() => setMenuAbierto(false)}>
          <span className="material-icons">home</span>
          <span>Inicio</span>
        </Link>

        {isLoggedIn && usuario ? (
          <Link to="/mi-cuenta" className="navbar-bottom-item">
            <span className="material-icons">account_circle</span>
            <span>{usuario.nombre.split(' ')[0]}</span>
          </Link>
        ) : (
          <Link to="/login" className="navbar-bottom-item">
            <span className="material-icons">person</span>
            <span>Acceder</span>
          </Link>
        )}

        <Link to="/carrito" className="navbar-bottom-item">
          <span className="material-icons">shopping_cart</span>
          {totalItems > 0 && <span className="navbar-bottom-badge">{totalItems}</span>}
          <span>Carrito</span>
        </Link>

        <button
          className="navbar-bottom-item"
          onClick={() => setMenuAbierto((v) => !v)}
        >
          <span className="material-icons">{menuAbierto ? 'close' : 'menu'}</span>
          <span>Menú</span>
        </button>
      </div>
    </nav>
  );
};

export default Navbar;