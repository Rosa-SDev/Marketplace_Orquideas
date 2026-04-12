import { useState, useEffect, useCallback } from 'react';
import ProductCard from '../components/ui/ProductCard';
import Loading from '../components/ui/Loading';
import api from '../services/api';

const Catalogo = () => {
  const [orquideas, setOrquideas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filtros, setFiltros] = useState({
    variedad: '',
    colorFlor: '',
    precioMin: '',
    precioMax: ''
  });

  const cargarOrquideas = useCallback(async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      Object.entries(filtros).forEach(([key, value]) => {
        if (value) params.append(key, value);
      });

      const response = await api.get(`/orquideas?${params}`);
      setOrquideas(response.data);
    } catch (error) {
      console.error('Error cargando orquídeas:', error);
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
          <input
            placeholder="Variedad"
            value={filtros.variedad}
            onChange={(e) => setFiltros(prev => ({ ...prev, variedad: soloTexto(e.target.value) }))}
            style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd' }}
          />
          <input
            placeholder="Color de flor"
            value={filtros.colorFlor}
            onChange={(e) => setFiltros(prev => ({ ...prev, colorFlor: soloTexto(e.target.value) }))}
            style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd' }}
          />
          <input
            type="number"
            placeholder="Precio mínimo"
            min="0"
            value={filtros.precioMin}
            onKeyDown={soloNumeros}
            onChange={(e) => handlePrecioChange('precioMin', e.target.value)}
            style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd' }}
          />
          <input
            type="number"
            placeholder="Precio máximo"
            min="0"
            value={filtros.precioMax}
            onKeyDown={soloNumeros}
            onChange={(e) => handlePrecioChange('precioMax', e.target.value)}
            style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid #ddd' }}
          />

          {/* Botón que dispara la búsqueda */}
          <button
            onClick={cargarOrquideas}
            style={{
              padding: '0.5rem 1.5rem',
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

        {/* Resultados */}
        {loading ? (
          <Loading mensaje="Cargando catálogo..." />
        ) : (
          <>
            <p style={{ color: '#1B4332', marginBottom: '1.5rem' }}>
              {orquideas.length} orquídeas disponibles
            </p>

            {/* Grid de ProductCards */}
            <div style={{
              display: 'flex',
              gap: '1.5rem',
              flexWrap: 'wrap',
              justifyContent: 'center'
            }}>
              {orquideas.map(orquidea => (
                <ProductCard
                  key={orquidea.id}
                  nombre={orquidea.nombre}
                  precio={orquidea.precio}
                  imagen={orquidea.imageUrl}
                  stock={orquidea.stock}
                  badge={orquidea.activo ? null : 'Inactivo'}
                />
              ))}
            </div>
          </>
        )}
      </section>
    </main>
  );
};

export default Catalogo;