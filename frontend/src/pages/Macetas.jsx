import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import useConnectionCheck from '../hooks/useConnectionCheck';

// Macetas.jsx
// Pagina que muestra el catalogo completo de macetas
// Por ahora usa datos de prueba, en semana 3 se conecta al backend real

import ProductCard from '../components/ui/ProductCard';

// Datos de prueba basados en el DTO del backend:
// material, diametroCm, color, estilo, precio, stock, imageUrl
const macetasDePrueba = [
  { id: 1, nombre: 'Maceta de Barro Rustica',   precio: 18000, stock: 12, badge: null       },
  { id: 2, nombre: 'Maceta de Ceramica Blanca', precio: 25000, stock: 5,  badge: 'Novedad'  },
  { id: 3, nombre: 'Maceta Plastica Verde',      precio: 9000,  stock: 20, badge: null       },
  { id: 4, nombre: 'Maceta de Madera Natural',  precio: 32000, stock: 0,  badge: null       },
];

const Macetas = () => {
  const { loading, error, retry } = useConnectionCheck();

  if (loading) return <Loading mensaje="Cargando pagina..." />;
  if (error) return <ConnectionError mensaje={error} onRetry={retry} />;

  return (
    <main>

      {/* Encabezado de la pagina */}
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

      {/* Tarjetas de macetas */}
      <section style={{
        padding: '3rem 2rem',
        maxWidth: '1200px',
        margin: '0 auto'
      }}>

        {/* Cantidad de resultados */}
        <p style={{ color: '#1B4332', marginBottom: '1.5rem' }}>
          {macetasDePrueba.length} macetas disponibles
        </p>

        {/* Grid de tarjetas — reutilizamos ProductCard igual que en Home */}
        <div style={{
          display: 'flex',
          gap: '1.5rem',
          flexWrap: 'wrap',
          justifyContent: 'center'
        }}>
          {macetasDePrueba.map(maceta => (
            <ProductCard
              key={maceta.id}
              nombre={maceta.nombre}
              precio={maceta.precio}
              stock={maceta.stock}
              badge={maceta.badge}
            />
          ))}
        </div>

      </section>

    </main>
  );
};

export default Macetas;