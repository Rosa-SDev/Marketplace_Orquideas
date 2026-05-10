import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCarritoStore from '../store/carritoStore';
import { openWompiCheckout } from '../services/wompiWidget';
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
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [transactionData, setTransactionData] = useState(null);
  const [isOpeningWompi, setIsOpeningWompi] = useState(false);

  const subtotal = useMemo(
    () => items.reduce((acc, item) => acc + item.precio * item.cantidad, 0),
    [items]
  );
  const envio = 0;
  const total = subtotal + envio;

  const handleChange = (event) => {
    const { name, value } = event.target;
    if (name === 'nombre' && /\d/.test(value)) return;
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

  const abrirWompi = async () => {
    const reference = `ORQ-${Date.now()}`;

    setIsOpeningWompi(true);
    setErrors((prev) => ({ ...prev, payment: undefined }));

    try {
      await openWompiCheckout({
        amountInCents: total * 100,
        reference,
        customerEmail: formData.correo.trim(),
        customerFullName: formData.nombre.trim(),
        onResult: (result) => {
          const transaction = result?.transaction;
          if (!transaction?.status) {
            setErrors((prev) => ({
              ...prev,
              payment: 'Pago cancelado o sin respuesta final de Wompi.',
            }));
            return;
          }

          if (transaction.status === 'APPROVED' || transaction.status === 'PENDING') {
            setPaymentSuccess(true);
            setTransactionData({
              reference: transaction.reference || reference,
              transactionId: transaction.id || 'Sin ID',
              status: transaction.status,
            });
            return;
          }

          setErrors((prev) => ({
            ...prev,
            payment: `El pago terminó con estado ${transaction.status}.`,
          }));
        },
      });

      if (document.hasFocus()) {
        setIsOpeningWompi(false);
      } else {
        const unlockOnFocus = () => {
          setIsOpeningWompi(false);
          window.removeEventListener('focus', unlockOnFocus);
        };
        window.addEventListener('focus', unlockOnFocus, { once: true });
      }
    } catch (error) {
      setErrors((prev) => ({
        ...prev,
        payment: error.message || 'No fue posible iniciar el pago con Wompi.',
      }));
      setIsOpeningWompi(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const nextErrors = validate();
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) return;

    if (metodoPago === 'tarjeta') {
      await abrirWompi();
      return;
    }

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

  if (paymentSuccess) {
    return (
      <main className="checkout-page">
        <section className="checkout-card checkout-success">
          <h1>✓ Pago realizado correctamente</h1>
          <p>Referencia de transacción: <strong>{transactionData.reference}</strong></p>
          <p>ID de transacción: <strong>{transactionData.transactionId}</strong></p>
          <p>Estado: <strong>{transactionData.status}</strong></p>
          <p>Recibirás un correo de confirmación en breve.</p>
          <button type="button" className="checkout-btn" onClick={() => navigate('/')}>
            Volver al inicio
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
          {metodoPago === 'tarjeta' && (
            <p className="checkout-payment-helper">
              Al continuar, se abrirá el widget oficial de Wompi para completar el pago.
            </p>
          )}
          {errors.payment && <p className="checkout-error">{errors.payment}</p>}
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

          <button type="submit" className="checkout-btn" disabled={isOpeningWompi}>
            {isOpeningWompi
              ? 'Abriendo Wompi...'
              : metodoPago === 'tarjeta'
                ? 'Pagar con Wompi'
                : 'Realizar pedido'}
          </button>
        </aside>
      </form>
    </main>
  );
};

export default Checkout;