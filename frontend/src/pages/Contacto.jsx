const Contacto = () => {
  return (
    <div>

      <div style={{ backgroundColor: '#1B4332', color: '#FAF7F5', padding: '3rem 2rem', textAlign: 'center' }}>
        <h1>Contacto</h1>
        <p>Estamos aqui para ayudarte</p>
      </div>

      <div style={{ maxWidth: '800px', margin: '0 auto', padding: '3rem 2rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>

        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '1.5rem', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <h2 style={{ color: '#2D6A4F', marginBottom: '0.8rem' }}>WhatsApp</h2>
          <p style={{ color: '#1B4332', marginBottom: '1rem' }}>La forma mas rapida de comunicarte con nosotros.</p>
          <a
            href="https://wa.me/573000000000"
            target="_blank"
            rel="noreferrer"
            style={{ backgroundColor: '#2D6A4F', color: '#FAF7F5', padding: '0.6rem 1.4rem', borderRadius: '20px', textDecoration: 'none', display: 'inline-block' }}
          >
            Escribenos por WhatsApp
          </a>
        </div>

        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '1.5rem', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <h2 style={{ color: '#2D6A4F', marginBottom: '0.8rem' }}>Correo electronico</h2>
          <p style={{ color: '#1B4332' }}>admin.orquicombeima@gmail.com</p>
        </div>

        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '1.5rem', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <h2 style={{ color: '#2D6A4F', marginBottom: '0.8rem' }}>Ubicacion</h2>
          <p style={{ color: '#1B4332' }}>Ibague, Tolima, Colombia</p>
        </div>

      </div>

    </div>
  );
};

export default Contacto;