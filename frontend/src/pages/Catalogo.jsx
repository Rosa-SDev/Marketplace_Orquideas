import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductCard from '../components/ui/ProductCard';
import Loading from '../components/ui/Loading';
import ConnectionError from '../components/ui/ConnectionError';
import api from '../services/api';

const MENSAJE_ERROR_CONEXION =
  'No fue posible conectar con el servidor. Verifica que el backend este encendido e intenta nuevamente.';

const normalizarTexto = (texto) =>
  texto
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '');

const coincidePorPalabras = (nombre, busqueda) => {
  const busquedaNormalizada = normalizarTexto(busqueda).trim();

  if (!busquedaNormalizada) return true;

  const palabrasBusqueda = busquedaNormalizada.split(/\s+/).filter(Boolean);
  const nombreNormalizado = normalizarTexto(nombre);

  return palabrasBusqueda.every((palabra) => nombreNormalizado.includes(palabra));
};

const Catalogo = () => {
  const [searchParams] = useSearchParams();
  const [orquideas, setOrquideas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [errorPrecio, setErrorPrecio] = useState('');
  const busquedaHero = searchParams.get('busqueda') || '';
  const [filtros, setFiltros] = useState(() => ({
    variedad: searchParams.get('variedad') || '',
    colorFlor: searchParams.get('colorFlor') || '',
    precioMin: searchParams.get('precioMin') || '',
    precioMax: searchParams.get('precioMax') || ''
  }));

  const cargarOrquideas = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const params = new URLSearchParams();
      Object.entries(filtros).forEach(([key, value]) => {
        if (value) params.append(key, value);
      });

      const response = await api.get(`/orquideas?${params}`);
      setOrquideas(response.data);
    } catch (err) {
      console.error('Error cargando orquídeas:', err);
      setError(MENSAJE_ERROR_CONEXION);
    } finally {
      setLoading(false);
    }
  }, [filtros]);

  useEffect(() => {
    cargarOrquideas();
  }, [/*Esto tiene que queda vacio, NO TOCAR*/]);

  const handlePrecioChange = (campo, valor) => {
    if (valor === '') {
      setFiltros(prev => ({ ...prev, [campo]: '' }));
      return;
    }
    const numero = Math.max(0, Number(valor));
    setFiltros(prev => ({ ...prev, [campo]: String(numero) }));
  };

  // Elimina cualquier carácter que NO sea letra, espacio o acento
  const soloTexto = (valor) => valor.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ ]/g, '');

  // Permite solo: dígitos, Backspace, Delete, Tab, flechas
  const soloNumeros = (e) => {
    if (
      !/[0-9]/.test(e.key) &&
      !['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight'].includes(e.key)
    ) {
      e.preventDefault();
    }
  };

  const orquideasVisibles = orquideas.filter((orquidea) =>
  coincidePorPalabras(orquidea.nombre, busquedaHero) &&
  (filtros.variedad === '' || normalizarTexto(orquidea.variedad).includes(normalizarTexto(filtros.variedad)))
  );

  return (
    <main>
      {/* Encabezado */}
      <section style={{
        backgroundColor: '#1B4332',
        color: '#FAF7F5',
        padding: '3rem 2rem',
        textAlign: 'center'
      }}>
        <h1>Catálogo de Orquídeas</h1>
        <p>Descubre nuestra colección exclusiva</p>
      </section>

      {/* Filtros */}
      <section style={{
        maxWidth: '1200px',
        margin: '2rem auto',
        padding: '0 2rem'
      }}>
        <div style={{
          display: 'flex',
          gap: '1rem',
          flexWrap: 'wrap',
          marginBottom: '1rem'
        }}>
          
          {/* Variedad - búsqueda parcial */}
          <input
          placeholder="Variedad (ej: Cattleya)"
          value={filtros.variedad}
          onChange={(e) => setFiltros(prev => ({ ...prev, variedad: soloTexto(e.target.value)}))}
          style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd'}}
          />

          {/* Color de flor - desplegable */}
          <select
            value={filtros.colorFlor}
            onChange={(e) => setFiltros(prev => ({ ...prev, colorFlor: e.target.value }))}
            style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd', backgroundColor: '#fff' }}
          >
            <option value="">Seleccionar color</option>
            <option value="Blanco">Blanco</option>
            <option value="Rojo">Rojo</option>
            <option value="Amarillo">Amarillo</option>
            <option value="Morado">Morado</option>
            <option value="Rosa">Rosa</option>
          </select>

          {/* Precio mínimo */}
          <input
            type="number"
            placeholder="Precio mínimo"
            min='0'
            value={filtros.precioMin}
            onChange={(e) => handlePrecioChange('precioMin', e.target.value)}
            onKeyDown={soloNumeros}
            style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd' }}
          />

          {/* Precio máximo */}
          <input
            type="number"
            placeholder="Precio máximo"
            min={filtros.precioMin || '0'}
            value={filtros.precioMax}
            onChange={(e) => {
              const valor = e.target.value;
              handlePrecioChange('precioMax', valor);
            }}
            onKeyDown={soloNumeros}
            style={{ padding: '0.5rem', borderRadius: '8px', border: filtros.precioMin && filtros.precioMax && Number(filtros.precioMax) < Number(filtros.precioMin)
              ? '1px solid #E91E8C'
              : '1px solid #ddd'
             }}
          />

          <button
            onClick={() => {
              if (filtros.precioMin && filtros.precioMax && Number(filtros.precioMax) < Number(filtros.precioMin)) {
                setErrorPrecio('El precio máximo no puede ser menor al precio mínimo');
                return;
              }
              setErrorPrecio('');
              cargarOrquideas();
            }}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#1B4332', 
              color: '#FAF7F5',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: 'bold'
            }}
          >
            Buscar
          </button>

        </div>

        {errorPrecio && (
          <p style={{ color: '#E91E8C', marginBottom: '1rem', fontSize: '0.9rem' }}>
            {errorPrecio}
          </p>
        )}

        {/* Resultados */}
        {loading ? (
          <Loading mensaje="Cargando catálogo..." />
        ) : error ? (
          <ConnectionError mensaje={error} onRetry={cargarOrquideas} />
        ) : (
          <>
            <p style={{ color: '#1B4332', marginBottom: '1.5rem' }}>
              {orquideasVisibles.length} orquídeas disponibles
            </p>

            {busquedaHero && (
              <p style={{ color: '#2D6A4F', marginBottom: '1rem' }}>
                Resultados para: <strong>{busquedaHero}</strong>
              </p>
            )}

            {/* Grid de ProductCards */}
            <div style={{
              display: 'flex',
              gap: '1.5rem',
              flexWrap: 'wrap',
              justifyContent: 'center'
            }}>
              {orquideasVisibles.map(orquidea => (
                <ProductCard
                  key={orquidea.id}
                  id={orquidea.id}
                  nombre={orquidea.nombre}
                  precio={orquidea.precio}
                  imagen={orquidea.imageUrl}
                  stock={orquidea.stock}
                  badge={orquidea.activo ? null : 'Inactivo'}
                />
              ))}
            </div>

            {orquideasVisibles.length === 0 && (
              <p style={{ color: '#1B4332', marginTop: '1.5rem', textAlign: 'center' }}>
                No encontramos orquídeas que coincidan con tu búsqueda.
              </p>
            )}
          </>
        )}
      </section>
    </main>
  );
};

export default Catalogo;