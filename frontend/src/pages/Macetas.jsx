import { useState, useEffect } from 'react';
import ProductCard from '../components/ui/ProductCard';
import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import api from '../services/api';

const Macetas = () => {
  const [macetas, setMacetas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const cargarMacetas = async () => {
      try {
        const response = await api.get('/macetas');
        setMacetas(response.data);
      } catch (err) {
        setError('No fue posible conectar con el servidor. Verifica que el backend este encendido e intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    };

    cargarMacetas();
  }, []);

  if (loading) return <Loading mensaje="Cargando macetas..." />;
  if (error) return <ConnectionError mensaje={error} onRetry={() => window.location.reload()} />;

  return (
    <main>

      <section style={{
        backgroundColor: '#1B4332',
        color: '#FAF7F5',
        padding: '3rem 2rem',
        textAlign: 'center'
      }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
          Catálogo de Macetas
        </h1>
        <p style={{ opacity: 0.9 }}>
          Encuentra la maceta perfecta para tu orquídea
        </p>
      </section>

      <section style={{
        padding: '3rem 2rem',
        maxWidth: '1200px',
        margin: '0 auto'
      }}>

        <p style={{ color: '#1B4332', marginBottom: '1.5rem' }}>
          {macetas.length} macetas disponibles
        </p>

        <div style={{
          display: 'flex',
          gap: '1.5rem',
          flexWrap: 'wrap',
          justifyContent: 'center'
        }}>
          {macetas.map(maceta => (
            <ProductCard
              key={maceta.id}
              id={maceta.id}
              tipo="maceta"
              nombre={maceta.nombre}
              precio={maceta.precio}
              stock={maceta.stock}
              imagen={maceta.imageUrl}
              badge={maceta.activo ? null : 'Inactivo'}
            />
          ))}
        </div>

      </section>

    </main>
  );
};

export default Macetas;