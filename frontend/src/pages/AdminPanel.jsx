import { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
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
  PAGADO: '#10b981',
  ENVIADO: '#3b82f6',
  ENTREGADO: '#6366f1',
  CANCELADO: '#ef4444',
};

const PedidosSidebar = ({ pedidos, cargando }) => (
  <aside className="pedidos-sidebar">
    <h3 className="pedidos-sidebar-title">
      <span>Pedidos recientes</span>
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
  const { usuario } = useAuth();
  const [activeSection, setActiveSection] = useState('Inicio');
  const [metricas, setMetricas] = useState(null);
  const [productos, setProductos] = useState({ orquideas: [], macetas: [] });
  const [pedidos, setPedidos] = useState([]);
  const [clientes, setClientes] = useState([]);
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
  const [productoEditando, setProductoEditando] = useState(null);
  const [tipoProductoEditando, setTipoProductoEditando] = useState(null);
  const [guardandoProducto, setGuardandoProducto] = useState(false);
  const [modalEliminar, setModalEliminar] = useState(null);

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

  const cargarSidebarPedidos = useCallback(async () => {
    setCargandoSidebar(true);
    try {
      const { data } = await api.get('/admin/pedidos/recientes');
      setPedidosSidebar(asegurarArreglo(data));
    } catch { /* silencioso */ } finally {
      setCargandoSidebar(false);
    }
  }, []);

  const abrirEditorProducto = (producto, tipo) => {
    setProductoEditando({ ...producto });
    setTipoProductoEditando(tipo);
  };

  const guardarProducto = async () => {
    setGuardandoProducto(true);
    try {
      const endpoint = tipoProductoEditando === 'orquideas'
        ? `/admin/orquideas/${productoEditando.id}`
        : `/admin/macetas/${productoEditando.id}`;
      await api.put(endpoint, productoEditando);
      setProductoEditando(null);
      setTipoProductoEditando(null);
      await cargarProductos();
      window.alert('Producto actualizado correctamente.');
    } catch {
      window.alert('No se pudo actualizar el producto.');
    } finally {
      setGuardandoProducto(false);
    }
  };

  const confirmarEliminar = (tipo, id, nombre) => {
    setModalEliminar({ tipo, id, nombre });
  };

  const ejecutarEliminar = async () => {
    const { tipo, id } = modalEliminar;
    setModalEliminar(null);
    try {
      const endpoint = tipo === 'orquideas'
        ? `/admin/orquideas/${id}`
        : `/admin/macetas/${id}`;
      await api.delete(endpoint);
      await cargarProductos();
    } catch {
      window.alert('No se pudo eliminar el producto.');
    }
  };
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
    if (activeSection === 'Productos') { cargarProductos(); return; }
    if (activeSection === 'Pedidos') { cargarPedidos(); return; }
    if (activeSection === 'Clientes') { cargarClientes(); }
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
        </aside>

        <section className="admin-content">
          <h2>{section.title}</h2>
          <p>{section.text}</p>

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
                          <th>Acciones</th>
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
                            <td style={{ display: 'flex', gap: '0.5rem' }}>
                              <button
                                type="button"
                                className="admin-table-action"
                                onClick={() => abrirEditorProducto(item, 'orquideas')}
                              >
                                Editar
                              </button>
                              <button
                                type="button"
                                className="admin-table-action"
                                onClick={() => toggleActivoProducto('orquideas', item.id)}
                              >
                                {item.activo ? 'Desactivar' : 'Activar'}
                              </button>
                              <button
                                type="button"
                                className="admin-table-action admin-table-action--danger"
                                onClick={() => confirmarEliminar('orquideas', item.id, item.nombre)}
                              >
                                Eliminar
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
                          <th>Acciones</th>
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
                            <td style={{ display: 'flex', gap: '0.5rem' }}>
                              <button
                                type="button"
                                className="admin-table-action"
                                onClick={() => abrirEditorProducto(item, 'macetas')}
                              >
                                Editar
                              </button>
                              <button
                                type="button"
                                className="admin-table-action"
                                onClick={() => toggleActivoProducto('macetas', item.id)}
                              >
                                {item.activo ? 'Desactivar' : 'Activar'}
                              </button>
                              <button
                                type="button"
                                className="admin-table-action admin-table-action--danger"
                                onClick={() => confirmarEliminar('macetas', item.id, item.nombre)}
                              >
                                Eliminar
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
                  {formularioCargando ? 'Agregando...' : 'Agregar producto al catálogo'}
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

        </section>

        <PedidosSidebar pedidos={pedidosSidebar} cargando={cargandoSidebar} />

      </div>

      {/* Modal de edición de producto */}
      {productoEditando && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#fff', borderRadius: '12px', padding: '2rem',
            width: '90%', maxWidth: '600px', maxHeight: '80vh', overflowY: 'auto'
          }}>
            <h3 style={{ color: '#1B4332', marginBottom: '1.5rem' }}>
              Editar {tipoProductoEditando === 'orquideas' ? 'Orquídea' : 'Maceta'}
            </h3>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>

              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                Nombre
                <input value={productoEditando.nombre || ''}
                  onChange={(e) => setProductoEditando(prev => ({ ...prev, nombre: e.target.value }))}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
              </label>

              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                Precio
                <input type="number" value={productoEditando.precio || ''}
                  onChange={(e) => setProductoEditando(prev => ({ ...prev, precio: e.target.value }))}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
              </label>

              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                Stock
                <input type="number" value={productoEditando.stock || ''}
                  onChange={(e) => setProductoEditando(prev => ({ ...prev, stock: e.target.value }))}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
              </label>

              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                Estado
                <select value={productoEditando.activo ? 'true' : 'false'}
                  onChange={(e) => setProductoEditando(prev => ({ ...prev, activo: e.target.value === 'true' }))}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }}>
                  <option value="true">Activo</option>
                  <option value="false">Inactivo</option>
                </select>
              </label>

              <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem', gridColumn: '1 / -1' }}>
                Descripción
                <textarea rows={3} value={productoEditando.descripcion || ''}
                  onChange={(e) => setProductoEditando(prev => ({ ...prev, descripcion: e.target.value }))}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
              </label>

              {tipoProductoEditando === 'orquideas' && (
                <>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Variedad
                    <input value={productoEditando.variedad || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, variedad: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Color de flor
                    <input value={productoEditando.colorFlor || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, colorFlor: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Tamaño
                    <input value={productoEditando.tamanio || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, tamanio: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Nivel de cuidado
                    <input value={productoEditando.nivelCuidado || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, nivelCuidado: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Tiempo de floración
                    <input value={productoEditando.tiempoFloracion || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, tiempoFloracion: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                </>
              )}

              {tipoProductoEditando === 'macetas' && (
                <>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Material
                    <input value={productoEditando.material || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, material: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Diámetro (cm)
                    <input type="number" value={productoEditando.diametroCm || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, diametroCm: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Color
                    <input value={productoEditando.color || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, color: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                  <label style={{ display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                    Estilo
                    <input value={productoEditando.estilo || ''}
                      onChange={(e) => setProductoEditando(prev => ({ ...prev, estilo: e.target.value }))}
                      style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid #ddd' }} />
                  </label>
                </>
              )}

            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button
                type="button"
                onClick={() => setProductoEditando(null)}
                style={{ padding: '0.6rem 1.5rem', borderRadius: '20px', border: '1px solid #ddd', cursor: 'pointer', backgroundColor: '#fff' }}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={guardarProducto}
                disabled={guardandoProducto}
                style={{ padding: '0.6rem 1.5rem', borderRadius: '20px', border: 'none', cursor: 'pointer', backgroundColor: '#2D6A4F', color: '#fff' }}
              >
                {guardandoProducto ? 'Guardando...' : 'Guardar cambios'}
              </button>
            </div>
          </div>
        </div>
      )}

      {modalEliminar && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#fff',
            borderRadius: '12px',
            padding: '2rem',
            maxWidth: '420px',
            width: '90%',
            textAlign: 'center',
            boxShadow: '0 8px 32px rgba(0,0,0,0.15)'
          }}>
            <span className="material-icons" style={{ fontSize: '2.5rem', color: '#ef4444', marginBottom: '0.75rem', display: 'block' }}>
              delete_forever
            </span>
            <h3 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>
              Eliminar producto
            </h3>
            <p style={{ color: '#666', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
              ¿Seguro que quieres eliminar <strong>"{modalEliminar.nombre}"</strong>?<br />
              Esta acción no se puede deshacer.
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
              <button
                onClick={() => setModalEliminar(null)}
                style={{
                  padding: '0.6rem 1.5rem',
                  borderRadius: '20px',
                  border: '1px solid #ddd',
                  cursor: 'pointer',
                  backgroundColor: '#fff',
                  fontSize: '0.9rem'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={ejecutarEliminar}
                style={{
                  padding: '0.6rem 1.5rem',
                  borderRadius: '20px',
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: '#ef4444',
                  color: '#fff',
                  fontSize: '0.9rem'
                }}
              >
                Sí, eliminar
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
};

export default AdminPanel;