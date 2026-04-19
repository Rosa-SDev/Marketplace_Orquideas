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
import WhatsAppBoton from './components/ui/WhatsAppBoton';
import Login from './pages/Login';
import ProtectedRoute from './components/layout/ProtectedRoute';
import SessionExpiryWarning from './components/ui/SessionExpiryWarning';

const App = () => {
  return (
    <BrowserRouter>

      <Navbar />

      {/* Advertencia global de expiración de sesion */}
      <SessionExpiryWarning />

      <Routes>
        <Route path="/" element={<Home />} />
        {/* Inicio de sesion con Google */}
        <Route path="/login" element={<Login />} />
        {/* Callback después de autenticar con Google */}
        <Route path="/auth/callback" element={<AuthCallback />} />
        {/* Página de catálogo de macetas */}
        <Route path="/macetas" element={<Macetas />} />
        <Route path="/guia" element={<Guia />} />
        <Route path="/contacto" element={<Contacto />} />
        <Route path="/catalogo" element={<Catalogo />} />
        <Route path="/orquideas/:id" element={<DetalleOrquidea />} />
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<AuthCallback />} />
        <Route path="/carrito" element={
          <ProtectedRoute>
            <h1>Carrito — proximamente</h1>
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