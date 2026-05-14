import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import Button from '../components/ui/Button';
import api from '../services/api';
import useLazyAddToCart from '../hooks/useLazyAddToCart';
import './DetalleOrquidea.css';

const MENSAJE_ERROR_CONEXION =
    'No fue posible conectar con el servidor. Verifica que el backend este encendido e intenta nuevamente.';

const DetalleOrquidea = () => {
  const { id } = useParams();
  const [orquidea, setOrquidea] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [imagenActiva, setImagenActiva] = useState(null);
  const [cantidad, setCantidad] = useState(1);
  const [tabActiva, setTabActiva] = useState('descripcion');
  const { agregarConLoginLazy } = useLazyAddToCart();

  useEffect(() => {
    const cargarDetalle = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await api.get(`/orquideas/${id}`);
        setOrquidea(response.data);
        setImagenActiva(response.data.imageUrl);
      } catch (err) {
        console.error('Error cargando detalle:', err);
        setError(MENSAJE_ERROR_CONEXION);
        setOrquidea(null);
      } finally {
        setLoading(false);
      }
    };
    if (id) cargarDetalle();
  }, [id]);

  const reintentar = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await api.get(`/orquideas/${id}`);
      setOrquidea(response.data);
      setImagenActiva(response.data.imageUrl);
    } catch (err) {
      setError(MENSAJE_ERROR_CONEXION);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loading mensaje="Cargando detalles..." />;
  if (error) return <ConnectionError mensaje={error} onRetry={reintentar} />;
  if (!orquidea) return <div>No encontrada</div>;

  const stockDisponible = orquidea.stock - (orquidea.stockReservado || 0);
  const stockClase = stockDisponible === 0 ? 'agotado' : stockDisponible === 1 ? 'ultima' : 'disponible';
  const stockTexto = stockDisponible === 0
    ? 'Agotado'
    : stockDisponible === 1
      ? '¡Última unidad disponible!'
      : `${stockDisponible} unidades disponibles`;

  return (
    <main className="detalle-page">
      <div className="detalle-grid">

        {/* GALERÍA */}
        <div>
          <div className="detalle-imagen-principal">
            <img src={imagenActiva || 'https://placehold.co/400'} alt={orquidea.nombre} />
          </div>
          <div className="detalle-thumbnails">
            {[orquidea.imageUrl].map((img, i) => (
              <img
                key={i}
                src={img || 'https://placehold.co/80'}
                alt="thumb"
                onClick={() => setImagenActiva(img)}
                className={`detalle-thumb ${imagenActiva === img ? 'activa' : ''}`}
              />
            ))}
          </div>
        </div>

        {/* INFO */}
        <div>
          <h1 className="detalle-nombre">{orquidea.nombre}</h1>
          <p className="detalle-precio">${orquidea.precio?.toLocaleString('es-CO')}</p>
          <p className={`detalle-stock ${stockClase}`}>{stockTexto}</p>

          <div>
            <h4 className="detalle-cantidad-label">Cantidad</h4>
            <div className="detalle-cantidad-controles">
              <button className="detalle-btn-cantidad" onClick={() => setCantidad(Math.max(1, cantidad - 1))}>−</button>
              <span className="detalle-cantidad-valor">{cantidad}</span>
              <button
                className="detalle-btn-cantidad"
                onClick={() => setCantidad(Math.min(stockDisponible, cantidad + 1))}
                disabled={cantidad >= stockDisponible}
              >+</button>
            </div>
          </div>

          <div className="detalle-agregar">
            <Button
              text="Agregar al carrito"
              disabled={stockDisponible === 0}
              onClick={() => agregarConLoginLazy({ id: orquidea.id, nombre: orquidea.nombre, precio: orquidea.precio, imagen: orquidea.imageUrl, stock: orquidea.stock }, cantidad)}
            />
          </div>

          {/* Tabs */}
          <div className="detalle-tabs">
            <div className="detalle-tabs-header">
              {['descripcion', 'cuidados'].map((tab) => (
                <button
                  key={tab}
                  onClick={() => setTabActiva(tab)}
                  className={`detalle-tab-btn ${tabActiva === tab ? 'activa' : ''}`}
                >
                  {tab === 'descripcion' ? 'Descripción' : 'Cuidados'}
                </button>
              ))}
            </div>
            <div className="detalle-tab-contenido">
              {tabActiva === 'descripcion' && <p>{orquidea.descripcion || 'Sin descripción disponible.'}</p>}
              {tabActiva === 'cuidados' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                  {orquidea.guiaCuidado ? (
                    <>
                      {orquidea.guiaCuidado.frecuenciaRiego && <p>💧 <strong>Riego:</strong> {orquidea.guiaCuidado.frecuenciaRiego}</p>}
                      {orquidea.guiaCuidado.luzRequerida && <p>☀️ <strong>Luz:</strong> {orquidea.guiaCuidado.luzRequerida}</p>}
                      {orquidea.guiaCuidado.temperaturaIdeal && <p>🌡️ <strong>Temperatura:</strong> {orquidea.guiaCuidado.temperaturaIdeal}</p>}
                      {orquidea.guiaCuidado.fertilizacion && <p>🌱 <strong>Fertilización:</strong> {orquidea.guiaCuidado.fertilizacion}</p>}
                      {orquidea.guiaCuidado.contenido && <p style={{ marginTop: '0.5rem' }}>{orquidea.guiaCuidado.contenido}</p>}
                    </>
                  ) : (
                    <p>No hay guía de cuidado disponible para esta orquídea.</p>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* RECOMENDACIONES */}
      {orquidea.recomendaciones?.length > 0 && (
        <section className="detalle-recomendaciones">
          <h2>Macetas recomendadas</h2>
          <div className="detalle-recomendaciones-grid">
            {orquidea.recomendaciones.map((rec, index) => (
              <div key={rec.maceta?.id ?? rec.macetaId ?? index} className="detalle-maceta-card">
                {(rec.maceta?.imageUrl ?? rec.macetaImageUrl) && (
                  <img src={rec.maceta?.imageUrl ?? rec.macetaImageUrl} alt={rec.maceta?.nombre ?? rec.macetaNombre} />
                )}
                <h4>{rec.maceta?.nombre ?? rec.macetaNombre ?? 'Maceta recomendada'}</h4>
                <p>{rec.descripcion}</p>
                <p className="detalle-maceta-precio">${(rec.maceta?.precio ?? rec.macetaPrecio)?.toLocaleString('es-CO')}</p>
              </div>
            ))}
          </div>
        </section>
      )}
    </main>
  );
};

export default DetalleOrquidea;