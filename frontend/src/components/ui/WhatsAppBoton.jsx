// WhatsAppBoton.jsx
// Boton flotante de WhatsApp que aparece en todas las paginas
// position: fixed hace que se quede fijo aunque el usuario haga scroll

const WhatsAppBoton = () => {
  return (
    <a
      href="https://wa.me/573014791094" 
      target="_blank"
      rel="noreferrer"
      style={{
        position: 'fixed',
        bottom: '2rem',
        right: '2rem',
        backgroundColor: '#25D366',
        color: '#fff',
        padding: '0.8rem',
        borderRadius: '50px',
        textDecoration: 'none',
        fontWeight: 'bold',
        fontSize: '0.9rem',
        boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
        zIndex: 999,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}
    >
      <img 
        src="/whatsapp.png" 
        alt="Contacto por WhatsApp" 
        style={{
          width: '28px',
          height: '28px',
          display: 'block'
        }}
      />
    </a>
  );
};

export default WhatsAppBoton;