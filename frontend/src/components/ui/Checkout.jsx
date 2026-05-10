import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCarritoStore from '../store/carritoStore';
import './Checkout.css';

const METODOS_PAGO = [
  { value: 'tarjeta', label: 'Tarjeta débito/crédito' },
  { value: 'transferencia', label: 'Transferencia bancaria' },
  { value: 'contraentrega', label: 'Contraentrega' },
];

const Checkout = () => {
  const { items } = useCarritoStore();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    nombre: '',
    correo: '',
    direccion: '',
  });
  const [metodoPago, setMetodoPago] = useState('tarjeta');
  const [errors, setErrors] = useState({});

  const subtotal = useMemo(
    () => items.reduce((acc, item) => acc + item.precio * item.cantidad, 0),
    [items]
  );
  const envio = 0;
  const total = subtotal + envio;

  const handleChange = (event) => {
    const { name, value } = event.target;
    if (name == 'nombre' && /\d/.test(value)) return; //Evitar números en el campo de nombre
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    const nextErrors = {};

    if (!formData.nombre.trim()) {
      nextErrors.nombre = 'El nombre es obligatorio.';
    }

    if (!formData.correo.trim()) {
      nextErrors.correo = 'El correo es obligatorio.';
    } else if (!/^[^\s@]+@(gmail\.com|outlook\.com)$/.test(formData.correo.trim())) {
      nextErrors.correo = 'Solo se permiten correos de @gmail.com o @outlook.com por el momento.';
    }

    if (!formData.direccion.trim()) {
      nextErrors.direccion = 'La dirección es obligatoria.';
    }

    if (!metodoPago) {
      nextErrors.metodoPago = 'Selecciona un método de pago.';
    }

    return nextErrors;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const nextErrors = validate();
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) return;

    navigate('/carrito');
  };

  if (items.length === 0) {
    return (
      <main className="checkout-page">
        <section className="checkout-card checkout-empty">
          <h1>Tu carrito está vacío</h1>
          <p>Agrega productos antes de continuar con el checkout.</p>
          <button type="button" className="checkout-btn" onClick={() => navigate('/catalogo')}>
            Ir al catálogo
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="checkout-page">
      <h1 className="checkout-title">Checkout</h1>

      <form className="checkout-grid" onSubmit={handleSubmit} noValidate>
        <section className="checkout-card">
          <h2>Datos de entrega</h2>

          <label htmlFor="nombre" className="checkout-label">Nombre</label>
          <input
            id="nombre"
            name="nombre"
            value={formData.nombre}
            onChange={handleChange}
            className="checkout-input"
            placeholder="Tu nombre completo"
          />
          {errors.nombre && <p className="checkout-error">{errors.nombre}</p>}

          <label htmlFor="correo" className="checkout-label">Correo</label>
          <input
            id="correo"
            name="correo"
            type="email"
            value={formData.correo}
            onChange={handleChange}
            className="checkout-input"
            placeholder="tu-correo@ejemplo.com"
          />
          {errors.correo && <p className="checkout-error">{errors.correo}</p>}

          <label htmlFor="direccion" className="checkout-label">Dirección</label>
          <textarea
            id="direccion"
            name="direccion"
            rows={4}
            value={formData.direccion}
            onChange={handleChange}
            className="checkout-input checkout-textarea"
            placeholder="Dirección completa de entrega"
          />
          {errors.direccion && <p className="checkout-error">{errors.direccion}</p>}

          <h2 className="checkout-subtitle">Método de pago</h2>
          <div className="checkout-methods">
            {METODOS_PAGO.map((metodo) => (
              <label key={metodo.value} className="checkout-method">
                <input
                  type="radio"
                  name="metodoPago"
                  value={metodo.value}
                  checked={metodoPago === metodo.value}
                  onChange={(event) => setMetodoPago(event.target.value)}
                />
                <span>{metodo.label}</span>
              </label>
            ))}
          </div>
          {errors.metodoPago && <p className="checkout-error">{errors.metodoPago}</p>}
        </section>

        <aside className="checkout-card">
          <h2>Resumen del pedido</h2>

          <div className="checkout-items">
            {items.map((item) => (
              <div key={item.id} className="checkout-item">
                <div>
                  <p className="checkout-item-name">{item.nombre}</p>
                  <p className="checkout-item-meta">
                    {item.cantidad} x ${item.precio.toLocaleString('es-CO')}
                  </p>
                </div>
                <p className="checkout-item-total">
                  ${(item.precio * item.cantidad).toLocaleString('es-CO')}
                </p>
              </div>
            ))}
          </div>

          <div className="checkout-totals">
            <div className="checkout-row">
              <span>Subtotal</span>
              <span>${subtotal.toLocaleString('es-CO')}</span>
            </div>
            <div className="checkout-row">
              <span>Envío</span>
              <span>{envio === 0 ? 'Gratis' : `$${envio.toLocaleString('es-CO')}`}</span>
            </div>
            <div className="checkout-row checkout-row-total">
              <span>Total</span>
              <span>${total.toLocaleString('es-CO')}</span>
            </div>
          </div>

          <button type="submit" className="checkout-btn">
            Realizar pedido
          </button>
        </aside>
      </form>
    </main>
  );
};

export default Checkout;