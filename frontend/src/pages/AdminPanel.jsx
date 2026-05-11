import { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import useAuth from '../hooks/useAuth';
import api from '../services/api';
import './AdminPanel.css';

const MENU_ITEMS = [
  'Inicio',
  'Productos',
  'Agregar',
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
  Agregar: {
    title: 'Agregar productos',
    text: 'Crea nuevas orquídeas y macetas para publicarlas en el catálogo.',
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

const formatearMoneda = (valor) => `$${Number(valor || 0).toLocaleString('es-CO')}`;
const asegurarArreglo = (valor) => (Array.isArray(valor) ? valor : []);

const ORQUIDEA_INICIAL = {
  nombre: '',
  descripcion: '',
  precio: '',
  stock: '',
  variedad: '',
  colorFlor: '',
  tamanio: '',
  nivelCuidado: '',
  tiempoFloracion: '',
  activo: true,
};

const MACETA_INICIAL = {
  nombre: '',
  descripcion: '',
  precio: '',
  stock: '',
  material: '',
  diametroCm: '',
  color: '',
  estilo: '',
  activo: true,
};

const ESTADOS_COLOR = {
  PENDIENTE: '#f59e0b',
  PAGADO:    '#10b981',
  ENVIADO:   '#3b82f6',
  ENTREGADO: '#6366f1',
  CANCELADO: '#ef4444',
};

const PedidosSidebar = ({ pedidos, cargando }) => (
  <aside className="pedidos-sidebar">
    <h3 className="pedidos-sidebar-title">
      <span>📦</span> Pedidos recientes
    </h3>
    {cargando && <p className="pedidos-sidebar-empty">Cargando...</p>}
    {!cargando && pedidos.length === 0 && (
      <p className="pedidos-sidebar-empty">Sin pedidos recientes.</p>
    )}
    <ul className="pedidos-sidebar-list">
      {pedidos.slice(0, 8).map((p) => (
        <li key={`sidebar-pedido-${p.id}`} className="pedidos-sidebar-item">
          <div className="pedidos-sidebar-row">
            <span className="pedidos-sidebar-id">#{p.id}</span>
            <span
              className="pedidos-sidebar-estado"
              style={{ color: ESTADOS_COLOR[p.estado] || '#6b7280' }}
            >
              {p.estado}
            </span>
          </div>
          <div className="pedidos-sidebar-cliente">{p.nombreCliente}</div>
          <div className="pedidos-sidebar-meta">
            <span>{formatearMoneda(p.total)}</span>
            <span className="pedidos-sidebar-tiempo">{p.tiempoTranscurrido}</span>
          </div>
        </li>
      ))}
    </ul>
  </aside>
);

const AdminPanel = () => {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [activeSection, setActiveSection] = useState('Inicio');
  const [metricas, setMetricas] = useState(null);
  const [productos, setProductos] = useState({ orquideas: [], macetas: [] });
  const [pedidos, setPedidos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [contenidosPagina, setContenidosPagina] = useState([]);
  const [recomendaciones, setRecomendaciones] = useState([]);
  const [cargandoSeccion, setCargandoSeccion] = useState({});
  const [erroresSeccion, setErroresSeccion] = useState({});
  const [seccionesCargadas, setSeccionesCargadas] = useState({});
  const [tipoFormulario, setTipoFormulario] = useState('orquidea');
  const [orquideaForm, setOrquideaForm] = useState(ORQUIDEA_INICIAL);
  const [macetaForm, setMacetaForm] = useState(MACETA_INICIAL);
  const [imagenOrquidea, setImagenOrquidea] = useState(null);
  const [imagenMaceta, setImagenMaceta] = useState(null);
  const [orquideaFileKey, setOrquideaFileKey] = useState(0);
  const [macetaFileKey, setMacetaFileKey] = useState(0);
  const [formularioCargando, setFormularioCargando] = useState(false);
  const [formularioMensaje, setFormularioMensaje] = useState('');
  const [formularioError, setFormularioError] = useState('');
  const [cargandoSidebar, setCargandoSidebar] = useState(false);
  const [pedidosSidebar, setPedidosSidebar] = useState([]);

  const section = useMemo(
    () => CONTENT[activeSection] || CONTENT.Inicio,
    [activeSection]
  );

  const setCargando = (seccion, valor) => {
    setCargandoSeccion((prev) => ({ ...prev, [seccion]: valor }));
  };

  const setError = (seccion, mensaje) => {
    setErroresSeccion((prev) => ({ ...prev, [seccion]: mensaje }));
  };

  const marcarSeccionCargada = (secciones) => {
    setSeccionesCargadas((prev) => ({
      ...prev,
      ...Object.fromEntries(secciones.map((item) => [item, true])),
    }));
  };

  const cargarMetricas = async (seccionOrigen) => {
    marcarSeccionCargada([seccionOrigen]);
    setCargando(seccionOrigen, true);
    setError(seccionOrigen, '');
    try {
      const { data } = await api.get('/admin/estadisticas');
      setMetricas(data);
      marcarSeccionCargada(['Inicio', 'Análisis']);
    } catch {
      setError(seccionOrigen, 'No se pudieron cargar las métricas.');
    } finally {
      setCargando(seccionOrigen, false);
    }
  };

  const cargarProductos = async () => {
    marcarSeccionCargada(['Productos']);
    setCargando('Productos', true);
    setError('Productos', '');
    try {
      const [orquideasResponse, macetasResponse] = await Promise.all([
        api.get('/admin/orquideas'),
        api.get('/admin/macetas'),
      ]);
      setProductos({
        orquideas: asegurarArreglo(orquideasResponse.data),
        macetas: asegurarArreglo(macetasResponse.data),
      });
    } catch {
      setError('Productos', 'No se pudieron cargar los productos.');
    } finally {
      setCargando('Productos', false);
    }
  };

  const cargarPedidos = async () => {
    marcarSeccionCargada(['Pedidos']);
    setCargando('Pedidos', true);
    setError('Pedidos', '');
    try {
      const { data } = await api.get('/admin/pedidos/recientes');
      setPedidos(asegurarArreglo(data));
    } catch {
      setError('Pedidos', 'No se pudieron cargar los pedidos.');
    } finally {
      setCargando('Pedidos', false);
    }
  };

  const cargarClientes = async () => {
    marcarSeccionCargada(['Clientes']);
    setCargando('Clientes', true);
    setError('Clientes', '');
    try {
      const { data } = await api.get('/admin/clientes');
      setClientes(asegurarArreglo(data));
    } catch {
      setError('Clientes', 'No se pudieron cargar los clientes.');
    } finally {
      setCargando('Clientes', false);
    }
  };

  const cargarConfiguracion = async () => {
    marcarSeccionCargada(['Configuración']);
    setCargando('Configuración', true);
    setError('Configuración', '');
    try {
      const [contenidoResponse, recomendacionesResponse] = await Promise.all([
        api.get('/admin/contenido-pagina'),
        api.get('/admin/recomendaciones'),
      ]);
      setContenidosPagina(asegurarArreglo(contenidoResponse.data));
      setRecomendaciones(asegurarArreglo(recomendacionesResponse.data));
    } catch {
      setError('Configuración', 'No se pudo cargar la configuración del panel.');
    } finally {
      setCargando('Configuración', false);
    }
  };

  const cargarSidebarPedidos = useCallback(async () => {
    setCargandoSidebar(true);
    try {
      const { data } = await api.get('/admin/pedidos/recientes');
      setPedidosSidebar(asegurarArreglo(data));
    } catch { /* silencioso */ } finally {
      setCargandoSidebar(false);
    }
  }, []);

  useEffect(() => { cargarSidebarPedidos(); }, [cargarSidebarPedidos]);

  useEffect(() => {
    if (activeSection === 'Inicio' && !seccionesCargadas.Inicio && !cargandoSeccion.Inicio) {
      cargarMetricas('Inicio');
    }
    if (activeSection === 'Análisis' && !seccionesCargadas.Análisis && !cargandoSeccion.Análisis) {
      cargarMetricas('Análisis');
    }
    if (activeSection === 'Productos' && !seccionesCargadas.Productos && !cargandoSeccion.Productos) {
      cargarProductos();
    }
    if (activeSection === 'Pedidos' && !seccionesCargadas.Pedidos && !cargandoSeccion.Pedidos) {
      cargarPedidos();
    }
    if (activeSection === 'Clientes' && !seccionesCargadas.Clientes && !cargandoSeccion.Clientes) {
      cargarClientes();
    }
    if (
      activeSection === 'Configuración' &&
      !seccionesCargadas.Configuración &&
      !cargandoSeccion.Configuración
    ) {
      cargarConfiguracion();
    }
  }, [activeSection, cargandoSeccion, seccionesCargadas]);

  if (!usuario || usuario.rol !== 'ADMINISTRADOR') {
    return <Navigate to="/" replace />;
  }

  const toggleActivoProducto = async (tipo, id) => {
    const endpoint = tipo === 'orquideas' ? `/admin/orquideas/${id}/activo` : `/admin/macetas/${id}/activo`;
    try {
      await api.patch(endpoint);
      await cargarProductos();
    } catch {
      window.alert('No se pudo actualizar el estado del producto.');
    }
  };

  const recargarSeccionActiva = () => {
    if (activeSection === 'Inicio' || activeSection === 'Análisis') {
      cargarMetricas(activeSection);
      return;
    }
    if (activeSection === 'Productos') {
      cargarProductos();
      return;
    }
    if (activeSection === 'Pedidos') {
      cargarPedidos();
      return;
    }
    if (activeSection === 'Clientes') {
      cargarClientes();
      return;
    }
    if (activeSection === 'Configuración') {
      cargarConfiguracion();
    }
  };

  const actualizarFormulario = (tipo, campo, valor) => {
    if (tipo === 'orquidea') {
      setOrquideaForm((prev) => ({ ...prev, [campo]: valor }));
      return;
    }
    setMacetaForm((prev) => ({ ...prev, [campo]: valor }));
  };

  const reiniciarPlantilla = (tipo) => {
    if (tipo === 'orquidea') {
      setOrquideaForm(ORQUIDEA_INICIAL);
      setImagenOrquidea(null);
      setOrquideaFileKey((prev) => prev + 1);
      return;
    }
    setMacetaForm(MACETA_INICIAL);
    setImagenMaceta(null);
    setMacetaFileKey((prev) => prev + 1);
  };

  const crearFormData = (valores, imagen) => {
    const data = new FormData();
    Object.entries(valores).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        data.append(key, value);
      }
    });
    if (imagen) data.append('imagen', imagen);
    return data;
  };

  const subirProducto = async () => {
    setFormularioError('');
    setFormularioMensaje('');
    setFormularioCargando(true);

    try {
      if (tipoFormulario === 'orquidea') {
        const formData = crearFormData(orquideaForm, imagenOrquidea);
        await api.post('/admin/orquideas', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        reiniciarPlantilla('orquidea');
      } else {
        const formData = crearFormData(macetaForm, imagenMaceta);
        await api.post('/admin/macetas', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        reiniciarPlantilla('maceta');
      }

      setFormularioMensaje('Producto subido correctamente a la BD.');
      await cargarProductos();
      setActiveSection('Productos');
    } catch (error) {
      const backendMensaje =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        'No se pudo subir el producto. Revisa los campos obligatorios.';
      setFormularioError(backendMensaje);
    } finally {
      setFormularioCargando(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  const totalProductos = productos.orquideas.length + productos.macetas.length;
  const productosActivos =
    productos.orquideas.filter((item) => item.activo).length +
    productos.macetas.filter((item) => item.activo).length;
  const totalClientes = clientes.length;
  const admins = clientes.filter((item) => item.rol === 'ADMINISTRADOR').length;
  const clientesFinales = totalClientes - admins;
  const formularioActivo = tipoFormulario === 'orquidea' ? orquideaForm : macetaForm;
  const ventasUltimos6Meses = asegurarArreglo(metricas?.ventasPorMes).slice(-6);

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

          {erroresSeccion[activeSection] && (
            <div className="admin-section-feedback">
              <p>{erroresSeccion[activeSection]}</p>
              <button type="button" className="admin-reload-btn" onClick={recargarSeccionActiva}>
                Reintentar
              </button>
            </div>
          )}

          {activeSection === 'Inicio' && (
            <>
              {cargandoSeccion.Inicio && <p>Cargando métricas...</p>}
              {!erroresSeccion.Inicio && metricas && (
                <div className="admin-metrics-grid">
                  <article className="admin-metric-card">
                    <span className="admin-metric-label">Ventas del mes</span>
                    <strong className="admin-metric-value">{formatearMoneda(metricas.ventasMes)}</strong>
                  </article>
                  <article className="admin-metric-card">
                    <span className="admin-metric-label">Pedidos del mes</span>
                    <strong className="admin-metric-value">{metricas.pedidosMes ?? 0}</strong>
                  </article>
                  <article className="admin-metric-card">
                    <span className="admin-metric-label">Clientes registrados</span>
                    <strong className="admin-metric-value">{metricas.clientesRegistrados ?? 0}</strong>
                  </article>
                  <article className="admin-metric-card">
                    <span className="admin-metric-label">Pedidos pendientes</span>
                    <strong className="admin-metric-value">{metricas.pedidosPendientes ?? 0}</strong>
                  </article>
                </div>
              )}
            </>
          )}

          {activeSection === 'Productos' && (
            <>
              {cargandoSeccion.Productos && <p>Cargando productos...</p>}
              {!cargandoSeccion.Productos && !erroresSeccion.Productos && (
                <>
                  <div className="admin-metrics-grid">
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Productos totales</span>
                      <strong className="admin-metric-value">{totalProductos}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Productos activos</span>
                      <strong className="admin-metric-value">{productosActivos}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Orquídeas</span>
                      <strong className="admin-metric-value">{productos.orquideas.length}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Macetas</span>
                      <strong className="admin-metric-value">{productos.macetas.length}</strong>
                    </article>
                  </div>

                  <h3 className="admin-subtitle">Orquídeas</h3>
                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Nombre</th>
                          <th>Precio</th>
                          <th>Stock</th>
                          <th>Estado</th>
                          <th>Acción</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productos.orquideas.map((item) => (
                          <tr key={`orquidea-${item.id}`}>
                            <td>{item.id}</td>
                            <td>{item.nombre}</td>
                            <td>{formatearMoneda(item.precio)}</td>
                            <td>{item.stock}</td>
                            <td>{item.activo ? 'Activo' : 'Inactivo'}</td>
                            <td>
                              <button
                                type="button"
                                className="admin-table-action"
                                onClick={() => toggleActivoProducto('orquideas', item.id)}
                              >
                                {item.activo ? 'Desactivar' : 'Activar'}
                              </button>
                            </td>
                          </tr>
                        ))}
                        {productos.orquideas.length === 0 && (
                          <tr>
                            <td colSpan={6} className="admin-empty-cell">
                              No hay orquídeas registradas.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>

                  <h3 className="admin-subtitle">Macetas</h3>
                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Nombre</th>
                          <th>Precio</th>
                          <th>Stock</th>
                          <th>Estado</th>
                          <th>Acción</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productos.macetas.map((item) => (
                          <tr key={`maceta-${item.id}`}>
                            <td>{item.id}</td>
                            <td>{item.nombre}</td>
                            <td>{formatearMoneda(item.precio)}</td>
                            <td>{item.stock}</td>
                            <td>{item.activo ? 'Activo' : 'Inactivo'}</td>
                            <td>
                              <button
                                type="button"
                                className="admin-table-action"
                                onClick={() => toggleActivoProducto('macetas', item.id)}
                              >
                                {item.activo ? 'Desactivar' : 'Activar'}
                              </button>
                            </td>
                          </tr>
                        ))}
                        {productos.macetas.length === 0 && (
                          <tr>
                            <td colSpan={6} className="admin-empty-cell">
                              No hay macetas registradas.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </>
          )}

          {activeSection === 'Agregar' && (
            <>
              <div className="admin-form-switch">
                <button
                  type="button"
                  className={`admin-form-switch-btn ${tipoFormulario === 'orquidea' ? 'is-active' : ''}`}
                  onClick={() => {
                    setTipoFormulario('orquidea');
                    setFormularioError('');
                    setFormularioMensaje('');
                  }}
                >
                  Nueva orquídea
                </button>
                <button
                  type="button"
                  className={`admin-form-switch-btn ${tipoFormulario === 'maceta' ? 'is-active' : ''}`}
                  onClick={() => {
                    setTipoFormulario('maceta');
                    setFormularioError('');
                    setFormularioMensaje('');
                  }}
                >
                  Nueva maceta
                </button>
              </div>

              {formularioError && <p className="admin-form-error">{formularioError}</p>}
              {formularioMensaje && <p className="admin-form-success">{formularioMensaje}</p>}

              <div className="admin-form-grid">
                <label className="admin-form-field">
                  Nombre
                  <input
                    type="text"
                    value={formularioActivo.nombre}
                    onChange={(e) => actualizarFormulario(tipoFormulario, 'nombre', e.target.value)}
                  />
                </label>

                <label className="admin-form-field">
                  Precio
                  <input
                    type="number"
                    min="1"
                    value={formularioActivo.precio}
                    onChange={(e) => actualizarFormulario(tipoFormulario, 'precio', e.target.value)}
                  />
                </label>

                <label className="admin-form-field">
                  Stock
                  <input
                    type="number"
                    min="0"
                    value={formularioActivo.stock}
                    onChange={(e) => actualizarFormulario(tipoFormulario, 'stock', e.target.value)}
                  />
                </label>

                <label className="admin-form-field">
                  Estado
                  <select
                    value={formularioActivo.activo ? 'true' : 'false'}
                    onChange={(e) =>
                      actualizarFormulario(tipoFormulario, 'activo', e.target.value === 'true')
                    }
                  >
                    <option value="true">Activo</option>
                    <option value="false">Inactivo</option>
                  </select>
                </label>

                <label className="admin-form-field admin-form-field--full">
                  Descripción
                  <textarea
                    rows="3"
                    value={formularioActivo.descripcion}
                    onChange={(e) => actualizarFormulario(tipoFormulario, 'descripcion', e.target.value)}
                  />
                </label>

                {tipoFormulario === 'orquidea' ? (
                  <>
                    <label className="admin-form-field">
                      Variedad
                      <input
                        type="text"
                        value={orquideaForm.variedad}
                        onChange={(e) => actualizarFormulario('orquidea', 'variedad', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field">
                      Tamaño
                      <input
                        type="text"
                        value={orquideaForm.tamanio}
                        onChange={(e) => actualizarFormulario('orquidea', 'tamanio', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field">
                      Color de flor
                      <input
                        type="text"
                        value={orquideaForm.colorFlor}
                        onChange={(e) => actualizarFormulario('orquidea', 'colorFlor', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field">
                      Nivel de cuidado
                      <input
                        type="text"
                        value={orquideaForm.nivelCuidado}
                        onChange={(e) =>
                          actualizarFormulario('orquidea', 'nivelCuidado', e.target.value)
                        }
                      />
                    </label>
                    <label className="admin-form-field">
                      Tiempo de floración
                      <input
                        type="text"
                        value={orquideaForm.tiempoFloracion}
                        onChange={(e) =>
                          actualizarFormulario('orquidea', 'tiempoFloracion', e.target.value)
                        }
                      />
                    </label>
                    <label className="admin-form-field admin-form-field--full">
                      Imagen (opcional)
                      <input
                        key={`orquidea-file-${orquideaFileKey}`}
                        type="file"
                        accept="image/*"
                        onChange={(e) => setImagenOrquidea(e.target.files?.[0] || null)}
                      />
                    </label>
                  </>
                ) : (
                  <>
                    <label className="admin-form-field">
                      Material
                      <input
                        type="text"
                        value={macetaForm.material}
                        onChange={(e) => actualizarFormulario('maceta', 'material', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field">
                      Diámetro (cm)
                      <input
                        type="number"
                        min="1"
                        step="0.1"
                        value={macetaForm.diametroCm}
                        onChange={(e) => actualizarFormulario('maceta', 'diametroCm', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field">
                      Color
                      <input
                        type="text"
                        value={macetaForm.color}
                        onChange={(e) => actualizarFormulario('maceta', 'color', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field">
                      Estilo
                      <input
                        type="text"
                        value={macetaForm.estilo}
                        onChange={(e) => actualizarFormulario('maceta', 'estilo', e.target.value)}
                      />
                    </label>
                    <label className="admin-form-field admin-form-field--full">
                      Imagen (opcional)
                      <input
                        key={`maceta-file-${macetaFileKey}`}
                        type="file"
                        accept="image/*"
                        onChange={(e) => setImagenMaceta(e.target.files?.[0] || null)}
                      />
                    </label>
                  </>
                )}
              </div>

              <div className="admin-form-actions">
                <button
                  type="button"
                  className="admin-reset-btn"
                  onClick={() => reiniciarPlantilla(tipoFormulario)}
                  disabled={formularioCargando}
                >
                  Reiniciar plantilla
                </button>
                <button
                  type="button"
                  className="admin-submit-btn"
                  onClick={subirProducto}
                  disabled={formularioCargando}
                >
                  {formularioCargando ? 'Subiendo...' : 'Subir a la BD'}
                </button>
              </div>
            </>
          )}

          {activeSection === 'Pedidos' && (
            <>
              {cargandoSeccion.Pedidos && <p>Cargando pedidos...</p>}
              {!cargandoSeccion.Pedidos && !erroresSeccion.Pedidos && (
                <>
                  <div className="admin-metrics-grid">
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Pedidos recientes</span>
                      <strong className="admin-metric-value">{pedidos.length}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Pendientes (recientes)</span>
                      <strong className="admin-metric-value">
                        {pedidos.filter((item) => item.estado === 'PENDIENTE').length}
                      </strong>
                    </article>
                  </div>

                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Cliente</th>
                          <th>Total</th>
                          <th>Estado</th>
                          <th>Tiempo</th>
                        </tr>
                      </thead>
                      <tbody>
                        {pedidos.map((item) => (
                          <tr key={`pedido-${item.id}`}>
                            <td>{item.id}</td>
                            <td>{item.nombreCliente}</td>
                            <td>{formatearMoneda(item.total)}</td>
                            <td>{item.estado}</td>
                            <td>{item.tiempoTranscurrido}</td>
                          </tr>
                        ))}
                        {pedidos.length === 0 && (
                          <tr>
                            <td colSpan={5} className="admin-empty-cell">
                              No hay pedidos recientes.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </>
          )}

          {activeSection === 'Clientes' && (
            <>
              {cargandoSeccion.Clientes && <p>Cargando clientes...</p>}
              {!cargandoSeccion.Clientes && !erroresSeccion.Clientes && (
                <>
                  <div className="admin-metrics-grid">
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Usuarios totales</span>
                      <strong className="admin-metric-value">{totalClientes}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Clientes</span>
                      <strong className="admin-metric-value">{clientesFinales}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Administradores</span>
                      <strong className="admin-metric-value">{admins}</strong>
                    </article>
                  </div>

                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Nombre</th>
                          <th>Correo</th>
                          <th>Rol</th>
                        </tr>
                      </thead>
                      <tbody>
                        {clientes.map((item) => (
                          <tr key={`cliente-${item.idUsuario}`}>
                            <td>{item.idUsuario}</td>
                            <td>{item.nombre}</td>
                            <td>{item.correo}</td>
                            <td>{item.rol}</td>
                          </tr>
                        ))}
                        {clientes.length === 0 && (
                          <tr>
                            <td colSpan={4} className="admin-empty-cell">
                              No hay usuarios registrados.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </>
          )}

          {activeSection === 'Análisis' && (
            <>
              {cargandoSeccion.Análisis && <p>Cargando análisis...</p>}
              {!cargandoSeccion.Análisis && !erroresSeccion.Análisis && metricas && (
                <>
                  <div className="admin-metrics-grid">
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Ventas del mes</span>
                      <strong className="admin-metric-value">{formatearMoneda(metricas.ventasMes)}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Pedidos del mes</span>
                      <strong className="admin-metric-value">{metricas.pedidosMes ?? 0}</strong>
                    </article>
                  </div>

                  <h3 className="admin-subtitle">Gráfica de ventas mensuales (últimos 6 meses)</h3>
                  <div className="admin-chart-card">
                    {ventasUltimos6Meses.length > 0 ? (
                      <div className="admin-chart-container">
                        <ResponsiveContainer width="100%" height="100%">
                          <LineChart data={ventasUltimos6Meses}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="mes" />
                            <YAxis tickFormatter={(valor) => formatearMoneda(valor)} />
                            <Tooltip formatter={(valor) => formatearMoneda(valor)} />
                            <Line
                              type="monotone"
                              dataKey="totalVentas"
                              stroke="#2d6a4f"
                              strokeWidth={3}
                              dot={{ r: 4 }}
                              activeDot={{ r: 6 }}
                            />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>
                    ) : (
                      <p className="admin-chart-empty">Aún no hay información de ventas para graficar.</p>
                    )}
                  </div>

                  <h3 className="admin-subtitle">Detalle de ventas por mes</h3>
                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>Mes</th>
                          <th>Total vendido</th>
                        </tr>
                      </thead>
                      <tbody>
                        {ventasUltimos6Meses.map((item, index) => (
                          <tr key={`venta-mes-${item.mes}-${index}`}>
                            <td>{item.mes}</td>
                            <td>{formatearMoneda(item.totalVentas)}</td>
                          </tr>
                        ))}
                        {ventasUltimos6Meses.length === 0 && (
                          <tr>
                            <td colSpan={2} className="admin-empty-cell">
                              Aún no hay información de ventas por mes.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </>
          )}

          {activeSection === 'Configuración' && (
            <>
              {cargandoSeccion.Configuración && <p>Cargando configuración...</p>}
              {!cargandoSeccion.Configuración && !erroresSeccion.Configuración && (
                <>
                  <div className="admin-metrics-grid">
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Bloques de contenido</span>
                      <strong className="admin-metric-value">{contenidosPagina.length}</strong>
                    </article>
                    <article className="admin-metric-card">
                      <span className="admin-metric-label">Recomendaciones</span>
                      <strong className="admin-metric-value">{recomendaciones.length}</strong>
                    </article>
                  </div>

                  <h3 className="admin-subtitle">Contenido de página</h3>
                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Tipo</th>
                          <th>Título</th>
                          <th>Orden</th>
                        </tr>
                      </thead>
                      <tbody>
                        {contenidosPagina.map((item) => (
                          <tr key={`contenido-${item.id}`}>
                            <td>{item.id}</td>
                            <td>{item.tipo}</td>
                            <td>{item.titulo}</td>
                            <td>{item.orden ?? '-'}</td>
                          </tr>
                        ))}
                        {contenidosPagina.length === 0 && (
                          <tr>
                            <td colSpan={4} className="admin-empty-cell">
                              No hay contenido configurado.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>

                  <h3 className="admin-subtitle">Recomendaciones</h3>
                  <div className="admin-table-wrapper">
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>ID</th>
                          <th>Orquídea</th>
                          <th>Maceta</th>
                          <th>Descripción</th>
                        </tr>
                      </thead>
                      <tbody>
                        {recomendaciones.map((item) => (
                          <tr key={`recomendacion-${item.id}`}>
                            <td>{item.id}</td>
                            <td>{item.nombreOrquidea}</td>
                            <td>{item.nombreMaceta}</td>
                            <td>{item.descripcion}</td>
                          </tr>
                        ))}
                        {recomendaciones.length === 0 && (
                          <tr>
                            <td colSpan={4} className="admin-empty-cell">
                              No hay recomendaciones registradas.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </>
          )}

          <p>{section.text}</p>
        </section>

        <PedidosSidebar pedidos={pedidosSidebar} cargando={cargandoSidebar} />

      </div>
    </main>
  );
};

export default AdminPanel;