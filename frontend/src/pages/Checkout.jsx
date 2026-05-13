import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useCarritoStore from '../store/carritoStore';
import { openWompiCheckout } from '../services/wompiWidget';
import api from '../services/api';
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
    telefono: '',
    departamento: '',
    ciudad: '',
    direccion: '',
  });
  const [metodoPago, setMetodoPago] = useState('tarjeta');
  const [errors, setErrors] = useState({});
  const [isOpeningWompi, setIsOpeningWompi] = useState(false);

  const subtotal = useMemo(
      () => items.reduce((acc, item) => acc + item.precio * item.cantidad, 0),
      [items]
  );
  const envio = 10000;
  const total = subtotal + envio;

  const handleChange = (event) => {
    const { name, value } = event.target;
    if (name === 'nombre' && /\d/.test(value)) return;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    const nextErrors = {};
    if (!formData.nombre.trim()) nextErrors.nombre = 'El nombre es obligatorio.';
    if (!formData.correo.trim()) {
      nextErrors.correo = 'El correo es obligatorio.';
    } else if (!/^[^\s@]+@(gmail\.com|outlook\.com)$/.test(formData.correo.trim())) {
      nextErrors.correo = 'Solo se permiten correos de @gmail.com o @outlook.com por el momento.';
    }
    if (!formData.telefono.trim()) nextErrors.telefono = 'El teléfono es obligatorio.';
    if (!formData.departamento.trim()) nextErrors.departamento = 'El departamento es obligatorio.';
    if (!formData.ciudad.trim()) nextErrors.ciudad = 'La ciudad es obligatoria.';
    if (!formData.direccion.trim()) nextErrors.direccion = 'La dirección es obligatoria.';
    if (!metodoPago) nextErrors.metodoPago = 'Selecciona un método de pago.';
    return nextErrors;
  };

  const abrirWompi = async () => {
    setIsOpeningWompi(true);
    setErrors((prev) => ({ ...prev, payment: undefined }));

    try {
      // 1. Crear pedido en el backend y obtener firma de integridad
      const response = await api.post('/pedidos', {
        direccionEnvio: {
          nombreDestinatario: formData.nombre.trim(),
          telefonoDestinatario: formData.telefono.trim(),
          departamento: formData.departamento.trim(),
          ciudad: formData.ciudad.trim(),
          direccion: formData.direccion.trim(),
        }
      });

      const pedido = response.data;

      // 2. Abrir widget de Wompi con firma del backend
      await openWompiCheckout({
        amountInCents: Math.round(pedido.total * 100),
        reference: pedido.referenciaPago,
        customerEmail: formData.correo.trim(),
        customerFullName: formData.nombre.trim(),
        firmaIntegridad: pedido.firmaIntegridad,
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
            navigate('/pago-exitoso', {
              state: {
                referencia: transaction.reference || pedido.referenciaPago,
                transactionId: transaction.id || 'Sin ID',
                status: transaction.status,
                items,
                total: pedido.total,
                nombre: formData.nombre.trim(),
                direccion: formData.direccion.trim(),
              }
            });
            return;
          }

          navigate('/pago-rechazado', {
            state: {
              status: transaction.status,
              referencia: transaction.reference || pedido.referenciaPago,
            }
          });
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

  return (
      <main className="checkout-page">
        <h1 className="checkout-title">Checkout</h1>

        <form className="checkout-grid" onSubmit={handleSubmit} noValidate>
          <section className="checkout-card">
            <h2>Datos de entrega</h2>

            <label htmlFor="nombre" className="checkout-label">Nombre</label>
            <input id="nombre" name="nombre" value={formData.nombre} onChange={handleChange} className="checkout-input" placeholder="Tu nombre completo" />
            {errors.nombre && <p className="checkout-error">{errors.nombre}</p>}

            <label htmlFor="correo" className="checkout-label">Correo</label>
            <input id="correo" name="correo" type="email" value={formData.correo} onChange={handleChange} className="checkout-input" placeholder="tu-correo@ejemplo.com" />
            {errors.correo && <p className="checkout-error">{errors.correo}</p>}

            <label htmlFor="telefono" className="checkout-label">Teléfono</label>
            <input id="telefono" name="telefono" value={formData.telefono} onChange={handleChange} className="checkout-input" placeholder="3001234567" />
            {errors.telefono && <p className="checkout-error">{errors.telefono}</p>}

            <label htmlFor="departamento" className="checkout-label">Departamento</label>
            <input id="departamento" name="departamento" value={formData.departamento} onChange={handleChange} className="checkout-input" placeholder="Tolima" />
            {errors.departamento && <p className="checkout-error">{errors.departamento}</p>}

            <label htmlFor="ciudad" className="checkout-label">Ciudad</label>
            <input id="ciudad" name="ciudad" value={formData.ciudad} onChange={handleChange} className="checkout-input" placeholder="Ibagué" />
            {errors.ciudad && <p className="checkout-error">{errors.ciudad}</p>}

            <label htmlFor="direccion" className="checkout-label">Dirección</label>
            <textarea id="direccion" name="direccion" rows={4} value={formData.direccion} onChange={handleChange} className="checkout-input checkout-textarea" placeholder="Dirección completa de entrega" />
            {errors.direccion && <p className="checkout-error">{errors.direccion}</p>}

            <h2 className="checkout-subtitle">Método de pago</h2>
            <div className="checkout-methods">
              {METODOS_PAGO.map((metodo) => (
                  <label key={metodo.value} className="checkout-method">
                    <input type="radio" name="metodoPago" value={metodo.value} checked={metodoPago === metodo.value} onChange={(event) => setMetodoPago(event.target.value)} />
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
                        {item.cantidad} x ${item.precio?.toLocaleString('es-CO')}
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
                <span>${envio.toLocaleString('es-CO')}</span>
              </div>
              <div className="checkout-row checkout-row-total">
                <span>Total</span>
                <span>${total.toLocaleString('es-CO')}</span>
              </div>
            </div>

            <button type="submit" className="checkout-btn" disabled={isOpeningWompi}>
              {isOpeningWompi ? 'Abriendo Wompi...' : metodoPago === 'tarjeta' ? 'Pagar con Wompi' : 'Realizar pedido'}
            </button>
          </aside>
        </form>
      </main>
  );
};

export default Checkout;