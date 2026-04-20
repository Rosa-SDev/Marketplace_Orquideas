// authStore.js
// Aqui guardamos los datos del usuario logueado
// Zustand es como una caja global que cualquier componente puede leer

import { create } from 'zustand';

const useAuthStore = create((set) => ({
  // Estado inicial — no hay usuario logueado
  usuario: null,        // { idUsuario, nombre, correo, rol }
  token: null,          // el JWT que nos da el backend
  isLoggedIn: false,    // true si hay sesion activa

  // Guardar usuario y token despues del login
  login: (usuario, token) => {
    // Guardamos el token en localStorage para que persista si se recarga la pagina
    localStorage.setItem('token', token);
    set({ usuario, token, isLoggedIn: true });
  },

  // Cerrar sesion
  logout: () => {
    localStorage.removeItem('token');
    set({ usuario: null, token: null, isLoggedIn: false });
  },

  // Actualizar solo los datos del usuario (sin cambiar el token)
  setUsuario: (usuario) => set({ usuario, isLoggedIn: true }),
}));

export default useAuthStore;