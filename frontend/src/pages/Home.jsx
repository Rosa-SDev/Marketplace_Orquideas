import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import ProductCard from '../components/ui/ProductCard';
import api from '../services/api';

const Home = () => {
  const [busqueda, setBusqueda] = useState('');
  const [orquideasDestacadas, setOrquideasDestacadas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const cargarDestacadas = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await api.get('/orquideas');
      setOrquideasDestacadas(response.data.slice(0, 3));
    } catch (err) {
      console.error('Error cargando orquídeas destacadas:', err);
      setError('No fue posible conectar con el servidor. Verifica que el backend este encendido e intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDestacadas();
  }, []);

  const irACatalogo = () => {
    const termino = busqueda.trim();

    if (!termino) {
      navigate('/catalogo');
      return;
    }

    navigate(`/catalogo?busqueda=${encodeURIComponent(termino)}`);
  };

  if (loading) return <Loading mensaje="Cargando pagina..." />;
  if (error) return <ConnectionError mensaje={error} onRetry={cargarDestacadas} />;

  return (
    <main>

      {/* Seccion principal de bienvenida */}
      <section style={{
        backgroundImage: "linear-gradient(rgba(27, 67, 50, 0.55), rgba(27, 67, 50, 0.55)), url('/hero-home.png')",
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        color: '#FAF7F5',
        padding: '5rem 2rem',
        textAlign: 'center'
      }}>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>
          Bienvenido a Orquídeas del Combeima
        </h1>
        <p style={{ fontSize: '1.1rem', marginBottom: '2rem', opacity: 0.9 }}>
          Las orquídeas más hermosas del Tolima, directo a tu hogar
        </p>

        {/* Buscador — la funcionalidad real se conecta en semana 3 */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem' }}>
          <input
            type="text"
            placeholder="Buscar orquídeas..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                irACatalogo();
              }
            }}
            style={{
              padding: '0.7rem 1.2rem',
              borderRadius: '20px',
              border: 'none',
              width: '300px',
              fontSize: '0.95rem'
            }}
          />
          <button
            onClick={irACatalogo}
            style={{
            backgroundColor: '#E91E8C',
            color: '#fff',
            border: 'none',
            borderRadius: '20px',
            padding: '0.7rem 1.5rem',
            cursor: 'pointer',
            fontSize: '0.95rem'
            }}
          >
            Buscar
          </button>
        </div>
      </section>

      {/* Badges de confianza */}
      <section style={{
        display: 'flex',
        justifyContent: 'center',
        gap: '3rem',
        flexWrap: 'wrap',
        padding: '2rem',
        backgroundColor: '#fff'
      }}>
        {[
          { icono: 'Garantia de Calidad', descripcion: 'Garantia de Calidad' },
          { icono: 'Pago Seguro',         descripcion: 'Pago Seguro'         },
        ].map(({ descripcion }) => (
          <div key={descripcion} style={{ textAlign: 'center' }}>
            <p style={{ color: '#1B4332', fontWeight: 'bold' }}>{descripcion}</p>
          </div>
        ))}
      </section>

      {/* Orquideas destacadas */}
      <section style={{ padding: '3rem 2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <h2 style={{ color: '#1B4332', marginBottom: '2rem', textAlign: 'center' }}>
          Orquídeas Destacadas
        </h2>

        <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', justifyContent: 'center' }}>
          {orquideasDestacadas.map(orquidea => (
            <ProductCard
              key={orquidea.id}
              id={orquidea.id}
              nombre={orquidea.nombre}
              precio={orquidea.precio}
              imagen={orquidea.imageUrl}
              stock={orquidea.stock}
            />
          ))}
        </div>
      </section>

    </main>
  );
};

export default Home;