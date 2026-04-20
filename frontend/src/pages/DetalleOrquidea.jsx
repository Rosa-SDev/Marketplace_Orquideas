import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import Button from '../components/ui/Button';
import api from '../services/api';
import useCarritoStore from '../store/carritoStore';


const MENSAJE_ERROR_CONEXION =
  'No fue posible conectar con el servidor. Verifica que el backend este encendido e intenta nuevamente.';

const DetalleOrquidea = () => {
  const { id } = useParams();

  const [orquidea, setOrquidea] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  //NUEVOS ESTADOS
  const [imagenActiva, setImagenActiva] = useState(null);
  const [cantidad, setCantidad] = useState(1);
  const [tabActiva, setTabActiva] = useState('descripcion');
  const [tamanoSeleccionado, setTamanoSeleccionado] = useState('');
  const agregar = useCarritoStore(state => state.agregar);

  useEffect(() => {
    const cargarDetalle = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await api.get(`/orquideas/${id}`);
        setOrquidea(response.data);

        // 🔧 inicializar imagen principal
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
      console.error('Error recargando detalle:', err);
      setError(MENSAJE_ERROR_CONEXION);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loading mensaje="Cargando detalles..." />;
  if (error) return <ConnectionError mensaje={error} onRetry={reintentar} />;
  if (!orquidea) return <div>No encontrada</div>;

  return (
    <main style={{ padding: '2rem' }}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '3rem' }}>

        {/* IZQUIERDA: GALERÍA */}
        <div>

          {/* Imagen principal */}
          <div style={{
            height: '400px',
            background: '#f5f5f5',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            borderRadius: '12px',
            marginBottom: '1rem'
          }}>
            <img
              src={imagenActiva || 'https://placehold.co/400'}
              alt={orquidea.nombre}
              style={{ maxWidth: '100%', maxHeight: '100%' }}
            />
          </div>

          {/* Miniaturas */}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            {[orquidea.imageUrl, orquidea.imageUrl, orquidea.imageUrl].map((img, i) => (
              <img
                key={i}
                src={img || 'https://placehold.co/80'}
                alt="thumb"
                onClick={() => setImagenActiva(img)}
                style={{
                  width: '80px',
                  height: '80px',
                  cursor: 'pointer',
                  border: imagenActiva === img ? '2px solid green' : '1px solid #ddd',
                  borderRadius: '8px'
                }}
              />
            ))}
          </div>

        </div>

        {/* DERECHA: INFO */}
        <div>

          <h1>{orquidea.nombre}</h1>
          <p>${orquidea.precio?.toLocaleString('es-CO')}</p>

          {/* 🔧 Selector de tamaño */}
          <div style={{ margin: '1rem 0' }}>
            <h4>Tamaño</h4>

            {['Pequeña', 'Mediana', 'Grande'].map(t => (
              <button
                key={t}
                onClick={() => setTamanoSeleccionado(t)}
                style={{
                  marginRight: '0.5rem',
                  padding: '0.5rem 1rem',
                  background: tamanoSeleccionado === t ? '#1B4332' : '#eee',
                  color: tamanoSeleccionado === t ? '#fff' : '#000',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer'
                }}
              >
                {t}
              </button>
            ))}
          </div>

          {/* Contador de cantidad */}
          <div style={{ margin: '1rem 0' }}>
            <h4>Cantidad</h4>

            <button onClick={() => setCantidad(Math.max(1, cantidad - 1))}>-</button>
            <span style={{ margin: '0 1rem' }}>{cantidad}</span>
            <button onClick={() => setCantidad(Math.min(orquidea.stock, cantidad + 1))}>+</button>
          </div>

          <Button
            text="Agregar al carrito"
            onClick={() => {
              for (let i = 0; i < cantidad; i++) {
                agregar({
                  id : orquidea.id,
                  nombre: orquidea.nombre,
                  precio: orquidea.precio,
                  imagen: orquidea.imageUrl,
                  stock: orquidea.stock
                  });
              }
            }}
          />

          {/* 🔧 Tabs */}
          <div style={{ marginTop: '2rem' }}>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button onClick={() => setTabActiva('descripcion')}>
                Descripción
              </button>
              <button onClick={() => setTabActiva('cuidados')}>
                Cuidados
              </button>
            </div>

            <div style={{ marginTop: '1rem' }}>
              {tabActiva === 'descripcion' && (
                <p>{orquidea.descripcion || 'Sin descripción'}</p>
              )}

              {tabActiva === 'cuidados' && (
                <div>
                  <p>Riego: {orquidea.guiaCuidado?.frecuenciaRiego}</p>
                  <p>Luz: {orquidea.guiaCuidado?.luzRequerida}</p>
                  <p>Temperatura: {orquidea.guiaCuidado?.temperaturaIdeal}</p>
                </div>
              )}
            </div>
          </div>

        </div>
      </div>

      {/* RECOMENDACIONES */}
      {orquidea.recomendaciones?.length > 0 && (
        <section style={{ marginTop: '3rem' }}>
          <h2>Macetas recomendadas</h2>

          <div style={{ display: 'flex', gap: '1rem' }}>
            {orquidea.recomendaciones.map((rec, index) => (
              <div
                key={rec.maceta?.id ?? rec.macetaId ?? index}
                style={{ border: '1px solid #ddd', padding: '1rem' }}
              >
                <h4>{rec.maceta?.nombre ?? rec.macetaNombre ?? 'Maceta recomendada'}</h4>
                <p>{rec.descripcion}</p>
                <p>
                  ${
                    (rec.maceta?.precio ?? rec.macetaPrecio)?.toLocaleString('es-CO')
                  }
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