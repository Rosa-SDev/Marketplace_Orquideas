import { useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import './AdminPanel.css';

const MENU_ITEMS = [
  'Inicio',
  'Productos',
  'Pedidos',
  'Clientes',
  'Análisis',
  'Configuración',
];

const CONTENT = {
  Inicio: {
    title: 'Inicio',
    text: 'Resumen general del estado del panel administrativo.',
  },
  Productos: {
    title: 'Productos',
    text: 'Aquí podrás administrar catálogo, precios, stock y visibilidad.',
  },
  Pedidos: {
    title: 'Pedidos',
    text: 'Vista de pedidos recientes, estados de pago y seguimiento.',
  },
  Clientes: {
    title: 'Clientes',
    text: 'Consulta de clientes y actividad de compra.',
  },
  Análisis: {
    title: 'Análisis',
    text: 'Métricas del negocio, ventas y tendencias.',
  },
  Configuración: {
    title: 'Configuración',
    text: 'Ajustes operativos y preferencias del panel.',
  },
};

const AdminPanel = () => {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [activeSection, setActiveSection] = useState('Inicio');

  const section = useMemo(
    () => CONTENT[activeSection] || CONTENT.Inicio,
    [activeSection]
  );

  if (!usuario || usuario.rol !== 'ADMINISTRADOR') {
    return <Navigate to="/" replace />;
  }

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  return (
    <main className="admin-panel-page">
      <div className="admin-panel-layout">
        <aside className="admin-sidebar">
          <h1 className="admin-sidebar-title">Panel Admin</h1>

          <nav className="admin-sidebar-nav">
            {MENU_ITEMS.map((item) => (
              <button
                key={item}
                type="button"
                className={`admin-sidebar-item ${activeSection === item ? 'is-active' : ''}`}
                onClick={() => setActiveSection(item)}
              >
                {item}
              </button>
            ))}
          </nav>

          <button
            type="button"
            className="admin-sidebar-item admin-logout-item"
            onClick={handleLogout}
          >
            Cerrar Sesión
          </button>
        </aside>

        <section className="admin-content">
          <h2>{section.title}</h2>
          <p>{section.text}</p>
        </section>
      </div>
    </main>
  );
};

export default AdminPanel;
