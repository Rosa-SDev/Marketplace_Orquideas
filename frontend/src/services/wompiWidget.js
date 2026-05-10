const WOMPI_WIDGET_SCRIPT = 'https://checkout.wompi.co/widget.js';

let wompiScriptPromise;

const loadWompiScript = () => {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('Wompi solo puede ejecutarse en el navegador.'));
  }

  if (window.WidgetCheckout) {
    return Promise.resolve();
  }

  if (!wompiScriptPromise) {
    wompiScriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = WOMPI_WIDGET_SCRIPT;
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('No se pudo cargar el widget de Wompi.'));
      document.body.appendChild(script);
    });
  }

  return wompiScriptPromise;
};

export const openWompiCheckout = async ({
  amountInCents,
  reference,
  customerEmail,
  customerFullName,
  onResult,
}) => {
  const publicKey = import.meta.env.VITE_WOMPI_PUBLIC_KEY;

  if (!publicKey) {
    throw new Error('Falta VITE_WOMPI_PUBLIC_KEY en el frontend.');
  }

  if (!Number.isFinite(amountInCents) || amountInCents <= 0) {
    throw new Error('El monto para Wompi no es válido.');
  }

  await loadWompiScript();

  const checkoutOptions = {
    currency: 'COP',
    amountInCents: Math.round(amountInCents),
    reference,
    publicKey,
    customerData: {
      email: customerEmail,
      fullName: customerFullName,
    },
  };

  const integritySignature = import.meta.env.VITE_WOMPI_INTEGRITY_SIGNATURE;
  if (integritySignature) {
    checkoutOptions.signature = {
      integrity: integritySignature,
    };
  }

  try {
    const checkout = new window.WidgetCheckout(checkoutOptions);
    checkout.open((result) => {
      if (typeof onResult === 'function') {
        onResult(result);
      }
    });
    return checkout;
  } catch (error) {
    throw (error instanceof Error ? error : new Error('No fue posible abrir Wompi.'));
  }
};