import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import Button from '../components/ui/Button';
import api from '../services/api';
import useLazyAddToCart from '../hooks/useLazyAddToCart';

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

  return (
      <main style={{ maxWidth: '1100px', margin: '0 auto', padding: '2rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '3rem' }}>

          {/* IZQUIERDA: GALERÍA */}
          <div>
            <div style={{
              height: '400px',
              background: '#f5f5f5',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              borderRadius: '12px',
              marginBottom: '1rem',
              overflow: 'hidden'
            }}>
              <img
                  src={imagenActiva || 'https://placehold.co/400'}
                  alt={orquidea.nombre}
                  style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'cover' }}
              />
            </div>

            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {[orquidea.imageUrl].map((img, i) => (
                  <img
                      key={i}
                      src={img || 'https://placehold.co/80'}
                      alt="thumb"
                      onClick={() => setImagenActiva(img)}
                      style={{
                        width: '80px',
                        height: '80px',
                        cursor: 'pointer',
                        objectFit: 'cover',
                        border: imagenActiva === img ? '2px solid #2D6A4F' : '1px solid #ddd',
                        borderRadius: '8px'
                      }}
                  />
              ))}
            </div>
          </div>

          {/* DERECHA: INFO */}
          <div>
            <h1 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>{orquidea.nombre}</h1>
            <p style={{ color: '#E91E8C', fontSize: '1.4rem', fontWeight: 'bold', marginBottom: '1rem' }}>
              ${orquidea.precio?.toLocaleString('es-CO')}
            </p>

            {/* Stock */}
            <p style={{
              color: stockDisponible > 0 ? '#2D6A4F' : '#E91E8C',
              fontSize: '0.9rem',
              marginBottom: '1.5rem'
            }}>
              {stockDisponible > 0 ? `${stockDisponible} unidades disponibles` : 'Agotado'}
            </p>

            {/* Cantidad mejorada */}
            <div style={{ margin: '1rem 0' }}>
              <h4 style={{ color: '#1B4332', marginBottom: '0.8rem' }}>Cantidad</h4>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0' }}>
                <button
                    onClick={() => setCantidad(Math.max(1, cantidad - 1))}
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '8px 0 0 8px',
                      border: '1px solid #ddd',
                      backgroundColor: '#f5f5f5',
                      cursor: 'pointer',
                      fontSize: '1.2rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}
                >
                  −
                </button>
                <span style={{
                  width: '50px',
                  height: '36px',
                  border: '1px solid #ddd',
                  borderLeft: 'none',
                  borderRight: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '1rem',
                  fontWeight: 'bold',
                  color: '#1B4332'
                }}>
                {cantidad}
              </span>
                <button
                    onClick={() => setCantidad(Math.min(stockDisponible, cantidad + 1))}
                    disabled={cantidad >= stockDisponible}
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '0 8px 8px 0',
                      border: '1px solid #ddd',
                      backgroundColor: cantidad >= stockDisponible ? '#eee' : '#f5f5f5',
                      cursor: cantidad >= stockDisponible ? 'not-allowed' : 'pointer',
                      fontSize: '1.2rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}
                >
                  +
                </button>
              </div>
            </div>

            <div style={{ marginTop: '1.5rem' }}>
              <Button
                  text="Agregar al carrito"
                  disabled={stockDisponible === 0}
                  onClick={() => {
                    agregarConLoginLazy(
                        {
                          id: orquidea.id,
                          nombre: orquidea.nombre,
                          precio: orquidea.precio,
                          imagen: orquidea.imageUrl,
                          stock: orquidea.stock
                        },
                        cantidad
                    );
                  }}
              />
            </div>

            {/* Tabs */}
            <div style={{ marginTop: '2rem' }}>
              <div style={{ display: 'flex', gap: '0', borderBottom: '2px solid #eee' }}>
                {['descripcion', 'cuidados'].map((tab) => (
                    <button
                        key={tab}
                        onClick={() => setTabActiva(tab)}
                        style={{
                          padding: '0.6rem 1.2rem',
                          border: 'none',
                          backgroundColor: 'transparent',
                          cursor: 'pointer',
                          fontSize: '0.95rem',
                          fontWeight: tabActiva === tab ? 'bold' : 'normal',
                          color: tabActiva === tab ? '#2D6A4F' : '#666',
                          borderBottom: tabActiva === tab ? '2px solid #2D6A4F' : '2px solid transparent',
                          marginBottom: '-2px'
                        }}
                    >
                      {tab === 'descripcion' ? 'Descripción' : 'Cuidados'}
                    </button>
                ))}
              </div>

              <div style={{ marginTop: '1rem', color: '#444', lineHeight: '1.6' }}>
                {tabActiva === 'descripcion' && (
                    <p>{orquidea.descripcion || 'Sin descripción disponible.'}</p>
                )}

                {tabActiva === 'cuidados' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                      {orquidea.guiaCuidado ? (
                          <>
                            {orquidea.guiaCuidado.frecuenciaRiego && (
                                <p>💧 <strong>Riego:</strong> {orquidea.guiaCuidado.frecuenciaRiego}</p>
                            )}
                            {orquidea.guiaCuidado.luzRequerida && (
                                <p>☀️ <strong>Luz:</strong> {orquidea.guiaCuidado.luzRequerida}</p>
                            )}
                            {orquidea.guiaCuidado.temperaturaIdeal && (
                                <p>🌡️ <strong>Temperatura:</strong> {orquidea.guiaCuidado.temperaturaIdeal}</p>
                            )}
                            {orquidea.guiaCuidado.fertilizacion && (
                                <p>🌱 <strong>Fertilización:</strong> {orquidea.guiaCuidado.fertilizacion}</p>
                            )}
                            {orquidea.guiaCuidado.contenido && (
                                <p style={{ marginTop: '0.5rem' }}>{orquidea.guiaCuidado.contenido}</p>
                            )}
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
            <section style={{ marginTop: '3rem' }}>
              <h2 style={{ color: '#1B4332', marginBottom: '1.5rem' }}>Macetas recomendadas</h2>
              <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                {orquidea.recomendaciones.map((rec, index) => (
                    <div
                        key={rec.maceta?.id ?? rec.macetaId ?? index}
                        style={{
                          backgroundColor: '#fff',
                          border: '1px solid #eee',
                          borderRadius: '12px',
                          padding: '1rem',
                          width: '200px',
                          boxShadow: '0 2px 8px rgba(0,0,0,0.06)'
                        }}
                    >
                      {(rec.maceta?.imageUrl ?? rec.macetaImageUrl) && (
                          <img
                              src={rec.maceta?.imageUrl ?? rec.macetaImageUrl}
                              alt={rec.maceta?.nombre ?? rec.macetaNombre}
                              style={{ width: '100%', height: '120px', objectFit: 'cover', borderRadius: '8px', marginBottom: '0.8rem' }}
                          />
                      )}
                      <h4 style={{ color: '#1B4332', marginBottom: '0.3rem' }}>
                        {rec.maceta?.nombre ?? rec.macetaNombre ?? 'Maceta recomendada'}
                      </h4>
                      <p style={{ color: '#666', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
                        {rec.descripcion}
                      </p>
                      <p style={{ color: '#E91E8C', fontWeight: 'bold' }}>
                        ${(rec.maceta?.precio ?? rec.macetaPrecio)?.toLocaleString('es-CO')}
                      </p>
                    </div>
                ))}
              </div>
            </section>
        )}
      </main>
  );
};

export default DetalleOrquidea;