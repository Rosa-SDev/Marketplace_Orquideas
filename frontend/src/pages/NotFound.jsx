const NotFound = () => {
  return (
    <main style={{ minHeight: '60vh', display: 'grid', placeItems: 'center', padding: '2rem' }}>
      <section style={{ textAlign: 'center' }}>
        <h1 style={{ color: '#1B4332', marginBottom: '0.5rem' }}>Pagina inexistente</h1>
        <p style={{ color: '#2D6A4F', margin: 0 }}>
          La ruta que intentaste abrir no existe.
        </p>
      </section>
    </main>
  );
};

export default NotFound;
