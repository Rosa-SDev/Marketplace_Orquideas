import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import WhatsAppBoton from './components/ui/WhatsAppBoton';
import ProtectedRoute from './components/layout/ProtectedRoute';

import Home from './pages/Home';
import Macetas from './pages/Macetas';
import Guia from './pages/Guia';
import Contacto from './pages/Contacto';
import Catalogo from './pages/Catalogo';
import DetalleOrquidea from './pages/DetalleOrquidea';
import Login from './pages/Login';
import AuthCallback from './pages/AuthCallback';
import NotFound from './pages/NotFound';
import Carrito from './pages/Carrito';


const App = () => {
  return (
    <BrowserRouter>

      <Navbar />

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/macetas" element={<Macetas />} />
        <Route path="/guia" element={<Guia />} />
        <Route path="/contacto" element={<Contacto />} />
        <Route path="/catalogo" element={<Catalogo />} />
        <Route path="/orquideas/:id" element={<DetalleOrquidea />} />
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
        <Route path="/carrito" element={
          <ProtectedRoute>
            <carrito />
          </ProtectedRoute>
        } />

        
        {/* Siempre de ultima */}
        <Route path="*" element={<NotFound />} />
      </Routes>

      <Footer />
      <WhatsAppBoton />

    </BrowserRouter>
  );
};

export default App;