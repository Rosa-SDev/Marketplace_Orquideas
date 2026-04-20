// Carrito.jsx
// Pagina que muestra los productos agregados al carrito
// Permite cambiar cantidades, eliminar productos y ver el total

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCarritoStore from '../store/carritoStore';

const Carrito = () => {
  const { items, cambiarCantidad, eliminar, vaciar } = useCarritoStore();
  const navigate = useNavigate();

  // idAEliminar guarda el id del producto que el usuario quiere eliminar
  // Lo usamos para mostrar el modal de confirmacion
  const [idAEliminar, setIdAEliminar] = useState(null);

  // Calculo del subtotal — suma precio * cantidad de cada item
  const subtotal = items.reduce((acc, item) => acc + item.precio * item.cantidad, 0);

  const handleEliminar = (id) => {
    eliminar(id);
    setIdAEliminar(null);
  };

  if (items.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '5rem 2rem' }}>
        <h2 style={{ color: '#1B4332', marginBottom: '1rem' }}>Tu carrito está vacío</h2>
        <p style={{ color: '#666', marginBottom: '2rem' }}>Agrega productos desde el catálogo</p>
        <button
          onClick={() => navigate('/catalogo')}
          style={{
            backgroundColor: '#2D6A4F',
            color: '#FAF7F5',
            border: 'none',
            borderRadius: '20px',
            padding: '0.7rem 1.5rem',
            cursor: 'pointer',
            fontSize: '0.95rem'
          }}
        >
          Ver catálogo
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', padding: '2rem' }}>

      <h1 style={{ color: '#1B4332', marginBottom: '2rem' }}>Tu carrito</h1>

      {/* Lista de productos */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
        {items.map(item => (
          <div
            key={item.id}
            style={{
              backgroundColor: '#fff',
              borderRadius: '12px',
              padding: '1rem',
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
              display: 'flex',
              alignItems: 'center',
              gap: '1rem'
            }}
          >
            {/* Imagen */}
            <img
              src={item.imagen || 'https://placehold.co/80x80?text=Orquidea'}
              alt={item.nombre}
              style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '8px' }}
            />

            {/* Nombre y precio */}
            <div style={{ flex: 1 }}>
              <p style={{ color: '#1B4332', fontWeight: 'bold', margin: 0 }}>{item.nombre}</p>
              <p style={{ color: '#E91E8C', margin: '0.3rem 0' }}>
                ${item.precio?.toLocaleString('es-CO')}
              </p>
            </div>

            {/* Controles de cantidad */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <button
                onClick={() => cambiarCantidad(item.id, Math.max(1, item.cantidad - 1))}
                style={{ width: '30px', height: '30px', borderRadius: '50%', border: '1px solid #ddd', cursor: 'pointer', backgroundColor: '#fff' }}
              >
                -
              </button>
              <span style={{ minWidth: '20px', textAlign: 'center' }}>{item.cantidad}</span>
              <button
                onClick={() => cambiarCantidad(item.id, Math.min(item.stock, item.cantidad + 1))}
                style={{ width: '30px', height: '30px', borderRadius: '50%', border: '1px solid #ddd', cursor: 'pointer', backgroundColor: '#fff' }}
              >
                +
              </button>
            </div>

            {/* Total por producto */}
            <p style={{ color: '#1B4332', fontWeight: 'bold', minWidth: '80px', textAlign: 'right' }}>
              ${(item.precio * item.cantidad).toLocaleString('es-CO')}
            </p>

            {/* Boton eliminar — abre el modal */}
            <button
              onClick={() => setIdAEliminar(item.id)}
              style={{ backgroundColor: 'transparent', border: 'none', color: '#E91E8C', cursor: 'pointer', fontSize: '1.2rem' }}
            >
              X
            </button>
          </div>
        ))}
      </div>

      {/* Resumen del total */}
      <div style={{
        backgroundColor: '#fff',
        borderRadius: '12px',
        padding: '1.5rem',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
          <span style={{ color: '#666' }}>Subtotal</span>
          <span style={{ color: '#1B4332', fontWeight: 'bold' }}>
            ${subtotal.toLocaleString('es-CO')}
          </span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
          <span style={{ color: '#666' }}>Envio</span>
          <span style={{ color: '#2D6A4F' }}>A calcular</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', borderTop: '1px solid #eee', paddingTop: '1rem' }}>
          <span style={{ color: '#1B4332', fontWeight: 'bold', fontSize: '1.1rem' }}>Total</span>
          <span style={{ color: '#E91E8C', fontWeight: 'bold', fontSize: '1.1rem' }}>
            ${subtotal.toLocaleString('es-CO')}
          </span>
        </div>

        <button
          onClick={() => navigate('/checkout')}
          style={{
            width: '100%',
            backgroundColor: '#2D6A4F',
            color: '#FAF7F5',
            border: 'none',
            borderRadius: '20px',
            padding: '0.8rem',
            cursor: 'pointer',
            fontSize: '1rem',
            fontWeight: 'bold'
          }}
        >
          Finalizar compra
        </button>
      </div>

      {/* Modal de confirmacion para eliminar */}
      {idAEliminar && (
        <div style={{
          position: 'fixed',
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#fff',
            borderRadius: '12px',
            padding: '2rem',
            maxWidth: '400px',
            width: '90%',
            textAlign: 'center'
          }}>
            <h3 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>
              Eliminar producto
            </h3>
            <p style={{ color: '#666', marginBottom: '1.5rem' }}>
              ¿Estás seguro que quieres eliminar este producto del carrito?
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <button
                onClick={() => setIdAEliminar(null)}
                style={{
                  padding: '0.6rem 1.5rem',
                  borderRadius: '20px',
                  border: '1px solid #ddd',
                  cursor: 'pointer',
                  backgroundColor: '#fff'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={() => handleEliminar(idAEliminar)}
                style={{
                  padding: '0.6rem 1.5rem',
                  borderRadius: '20px',
                  border: 'none',
                  cursor: 'pointer',
                  backgroundColor: '#E91E8C',
                  color: '#fff'
                }}
              >
                Eliminar
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default Carrito;