import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';  
import Home   from './pages/Home';                
import Macetas from './pages/Macetas';   
import Guia from './pages/Guia';
import Contacto from './pages/Contacto';
import Catalogo from './pages/Catalogo';
import DetalleOrquidea from './pages/DetalleOrquidea';

const App = () => {
  return (
    <BrowserRouter>

      {/* Navbar aparece en todas las páginas */}
      <Navbar />

      <Routes>
        {/* La página de inicio */}
        <Route path="/" element={<Home />} />
        {/* Página de catálogo de macetas */}
        <Route path="/macetas" element={<Macetas />} />
        {/* Página de guía de cuidado */}
        <Route path="/guia" element={<Guia />} />
        {/* Página de contacto */}
        <Route path="/contacto" element={<Contacto />} />
        {/* Página de catálogo de orquídeas */}
        <Route path="/catalogo" element={<Catalogo />} />
        {/* Página de detalle de orquídea */}
        <Route path="/orquideas/:id" element={<DetalleOrquidea />} />
        
        {/* Otras rutas se agregan aquí en el futuro */}
      </Routes>

      {/* Footer también aparece en todas las páginas */}
      <Footer />

    </BrowserRouter>
  );
};

export default App;