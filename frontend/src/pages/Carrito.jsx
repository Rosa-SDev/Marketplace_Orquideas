import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCarritoStore from '../store/carritoStore';
import './Carrito.css';

const Carrito = () => {
  const { items, cambiarCantidad, eliminar } = useCarritoStore();
  const navigate = useNavigate();
  const [idAEliminar, setIdAEliminar] = useState(null);

  const subtotal = items.reduce((acc, item) => acc + item.precio * item.cantidad, 0);

  const handleEliminar = (id) => {
    eliminar(id);
    setIdAEliminar(null);
  };

  if (items.length === 0) {
    return (
      <div className="carrito-vacio">
        <h2>Tu carrito está vacío</h2>
        <p>Agrega productos desde el catálogo</p>
        <button onClick={() => navigate('/catalogo')} className="btn-ver-catalogo">
          Ver catálogo
        </button>
      </div>
    );
  }

  return (
    <div className="carrito-page">
      <h1 className="carrito-titulo">Tu carrito</h1>

      <div className="carrito-lista">
        {items.map(item => (
          <div key={item.id} className="carrito-item">
            <img
              src={item.imagen || 'https://placehold.co/80x80?text=Orquidea'}
              alt={item.nombre}
              className="carrito-item-imagen"
            />

            <div className="carrito-item-info">
              <p className="carrito-item-nombre">{item.nombre}</p>
              <p className="carrito-item-precio">${item.precio?.toLocaleString('es-CO')}</p>
            </div>

            <div className="carrito-item-controles">
              <button
                className="carrito-btn-cantidad"
                onClick={() => cambiarCantidad(item.idItemCarrito, Math.max(1, item.cantidad - 1))}
              >-</button>
              <span className="carrito-item-cantidad">{item.cantidad}</span>
              <button
                className="carrito-btn-cantidad"
                onClick={() => cambiarCantidad(item.idItemCarrito, item.cantidad + 1)}
              >+</button>
            </div>

            <p className="carrito-item-total">
              ${(item.precio * item.cantidad).toLocaleString('es-CO')}
            </p>

            <button
              className="carrito-btn-eliminar"
              onClick={() => setIdAEliminar(item.idItemCarrito)}
            >✕</button>
          </div>
        ))}
      </div>

      <div className="carrito-resumen">
        <div className="carrito-resumen-fila">
          <span>Subtotal</span>
          <span>${subtotal.toLocaleString('es-CO')}</span>
        </div>
        <div className="carrito-resumen-fila envio">
          <span>Envío</span>
          <span>A calcular</span>
        </div>
        <div className="carrito-resumen-total">
          <span>Total</span>
          <span>${subtotal.toLocaleString('es-CO')}</span>
        </div>
        <button className="carrito-btn-checkout" onClick={() => navigate('/checkout')}>
          Finalizar compra
        </button>
      </div>

      {idAEliminar && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: '#fff', borderRadius: '12px', padding: '2rem',
            maxWidth: '400px', width: '90%', textAlign: 'center'
          }}>
            <h3 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>Eliminar producto</h3>
            <p style={{ color: '#666', marginBottom: '1.5rem' }}>
              ¿Estás seguro que quieres eliminar este producto del carrito?
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <button
                onClick={() => setIdAEliminar(null)}
                style={{ padding: '0.6rem 1.5rem', borderRadius: '20px', border: '1px solid #ddd', cursor: 'pointer', backgroundColor: '#fff' }}
              >Cancelar</button>
              <button
                onClick={() => handleEliminar(idAEliminar)}
                style={{ padding: '0.6rem 1.5rem', borderRadius: '20px', border: 'none', cursor: 'pointer', backgroundColor: '#E91E8C', color: '#fff', transition: 'background-color 0.2s, transform 0.1s' }}
                onMouseEnter={e => e.currentTarget.style.backgroundColor = '#c4176f'}
                onMouseLeave={e => e.currentTarget.style.backgroundColor = '#E91E8C'}
                onMouseDown={e => e.currentTarget.style.transform = 'scale(0.97)'}
                onMouseUp={e => e.currentTarget.style.transform = 'scale(1)'}
              >Eliminar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Carrito;