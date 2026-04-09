// Guia.jsx
// Pagina informativa sobre como cuidar las orquideas
// El contenido real vendra del backend en semana 2 con GET /api/guia
// Por ahora usamos datos de prueba

// Cada seccion tiene un titulo, una descripcion y un consejo corto
const secciones = [
  {
    id: 1,
    titulo: 'Luz',
    descripcion: 'Las orquideas necesitan luz indirecta y brillante. Evita la luz solar directa porque quema las hojas. Un lugar cerca de una ventana con cortina es ideal.',
    consejo: 'Si las hojas se ponen amarillas, puede ser exceso de luz.'
  },
  {
    id: 2,
    titulo: 'Riego',
    descripcion: 'Riega cada 7 a 10 dias en epoca seca y cada 12 a 14 dias en epoca humeda. La raiz debe secarse entre riego y riego. Nunca dejes la maceta con agua acumulada abajo.',
    consejo: 'Mas orquideas mueren por exceso de agua que por falta de ella.'
  },
  {
    id: 3,
    titulo: 'Temperatura',
    descripcion: 'La temperatura ideal esta entre 15 y 28 grados centígrados. Evita corrientes de aire frio directo y cambios bruscos de temperatura.',
    consejo: 'En Ibagué el clima es casi perfecto para las orquideas todo el año.'
  },
  {
    id: 4,
    titulo: 'Fertilización',
    descripcion: 'Usa fertilizante liquido especial para orquideas cada 15 dias durante la epoca de crecimiento. Reduce la fertilizacion en epoca de descanso.',
    consejo: 'Usa siempre la mitad de la dosis recomendada en el empaque.'
  },
  {
    id: 5,
    titulo: 'Humedad',
    descripcion: 'Las orquideas prefieren una humedad entre 50% y 70%. Si el ambiente es muy seco, puedes poner un plato con piedras y agua debajo de la maceta.',
    consejo: 'No rocies agua directamente sobre las flores, solo sobre las raices.'
  },
];

const Guia = () => {
  return (
    <main>

      {/* Encabezado */}
      <section style={{
        backgroundColor: '#1B4332',
        color: '#FAF7F5',
        padding: '3rem 2rem',
        textAlign: 'center'
      }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
          Guía de Cuidado
        </h1>
        <p style={{ opacity: 0.9 }}>
          Todo lo que necesitas saber para mantener tus orquideas saludables
        </p>
      </section>

      {/* Secciones de cuidado */}
      <section style={{
        maxWidth: '800px',
        margin: '0 auto',
        padding: '3rem 2rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '2rem'
      }}>

        {secciones.map(seccion => (
          <div
            key={seccion.id}
            style={{
              backgroundColor: '#FFFFFF',
              borderRadius: '12px',
              padding: '1.5rem',
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
            }}
          >
            {/* Titulo de la seccion */}
            <h2 style={{
              color: '#2D6A4F',
              marginBottom: '0.8rem',
              fontSize: '1.2rem'
            }}>
              {seccion.titulo}
            </h2>

            {/* Descripcion */}
            <p style={{
              color: '#1B4332',
              lineHeight: '1.7',
              marginBottom: '1rem'
            }}>
              {seccion.descripcion}
            </p>

            {/* Tarjeta de consejo */}
            <div style={{
              backgroundColor: '#FAF7F5',
              borderLeft: '4px solid #E91E8C',
              padding: '0.8rem 1rem',
              borderRadius: '0 8px 8px 0'
            }}>
              <p style={{ color: '#1B4332', fontSize: '0.9rem', margin: 0 }}>
                <strong>Consejo:</strong> {seccion.consejo}
              </p>
            </div>

          </div>
        ))}

      </section>

    </main>
  );
};

export default Guia;