// Importar BrowserRouter y Routes para manejar la navegación entre páginas
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// Importar componente Navbar
import Navbar from './components/layout/Navbar';

const App = () => {
  return (
    // BrowserRouter envuelve todo para habilitar la navegación
    <BrowserRouter>

      {/* El Navbar aparece en todas las páginas porque está fuera de Routes */}
      <Navbar />

      {/* Routes es el contenedor de todas las páginas */}
      {/* Cada Route es una página diferente según la URL */}
      <Routes>

        {/* Por ahora solo tenemos la página de inicio */}
        {/* path="/" significa que es la página principal */}
        <Route path="/" element={<h1>Bienvenido a Orquídeas del Combeima</h1>} />

      </Routes>

    </BrowserRouter>
  );
};

export default App;