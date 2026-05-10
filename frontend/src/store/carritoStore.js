import { create } from 'zustand';
import api from '../services/api';

const useCarritoStore = create((set, get) => ({
  items: [],
  cargando: false,

  // Trae el carrito del backend
  cargarCarrito: async () => {
    try {
      const response = await api.get('/carrito');
      const itemsBackend = response.data.items.map(item => ({
        id: item.idProducto,
        idItemCarrito: item.id,
        nombre: item.nombreProducto,
        precio: item.precioUnitario,
        imagen: item.imagenUrl,
        cantidad: item.cantidad,
      }));
      set({ items: itemsBackend });
    } catch (err) {
      // Si falla (ej: no logueado) dejamos el carrito vacio
      set({ items: [] });
    }
  },

  // Agrega un producto al carrito
  agregar: async (producto) => {
    try {
      await api.post('/carrito/agregar', {
        idProducto: producto.id,
        cantidad: 1,
      });
      // Recargamos el carrito del backend para tener datos frescos
      await get().cargarCarrito();
    } catch (err) {
      console.error('Error agregando al carrito:', err);
    }
  },

  // Cambia la cantidad de un item
  cambiarCantidad: async (idItemCarrito, cantidad) => {
    try {
      await api.put(`/carrito/${idItemCarrito}/cantidad?cantidad= ${cantidad}`);
      await get().cargarCarrito();
    } catch (err) {
      console.error('Error cambiando cantidad:', err);
    }
  },

  // Elimina un item del carrito
  eliminar: async (idItemCarrito) => {
    try {
      await api.delete(`/carrito/${idItemCarrito}`);
      await get().cargarCarrito();
    } catch (err) {
      console.error('Error eliminando del carrito:', err);
    }
  },

  // Vacia todo el carrito
  vaciar: async () => {
    try {
      await api.delete('/carrito/vaciar');
      set({ items: [] });
    } catch (err) {
      console.error('Error vaciando carrito:', err);
    }
  },

  totalItems: () => get().items.reduce((acc, item) => acc + item.cantidad, 0),
}));

export default useCarritoStore;