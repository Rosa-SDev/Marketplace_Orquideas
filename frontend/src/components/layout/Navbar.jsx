// Importamos Link de react-router-dom
// Importamos el archivo CSS para la Navbar
// Navegar entre páginas sin recargar el navegador
import { Link } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  
  return (

    <nav className="navbar">

      {/* Contenedor principal que organiza todo en fila */}
      <div className="navbar-container">

        {/* Logo del lado izquierdo */}
        {/* Link to="/" lleva al usuario a la página de inicio */}
        <Link to="/" className="navbar-logo">
          Orquideas del Combeima
        </Link>

        {/* Menú del centro con las páginas principales */}
        {/* ul = lista, li = cada elemento de la lista */}
        <ul className="navbar-menu">
          <li><Link to="/">Inicio</Link></li>
          <li><Link to="/catalogo">Catalogo</Link></li>
          <li><Link to="/guia">Guia de Cuidado</Link></li>
          <li><Link to="/contacto">Contacto</Link></li>
        </ul>

        {/* Botones del lado derecho */}
        <div className="navbar-icons">

          {/* Botón para iniciar sesión */}
          <Link to="/login" className="navbar-btn-acceder">
            Acceder
          </Link>

          {/* Carrito de compras con contador */}
          {/* El 0 es el número de productos, después lo haremos dinámico */}
          <Link to="/carrito" className="navbar-carrito">
            Carrito (0)
          </Link>

        </div>
      </div>
    </nav>
  );
};

// Exportamos el componente para poder usarlo en otros archivos
export default Navbar;