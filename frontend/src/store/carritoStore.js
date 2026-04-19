// carritoStore.js
// Caja global donde guardamos los productos del carrito
// Cualquier componente puede leer o modificar el carrito desde aqui

import { create } from 'zustand';

const useCarritoStore = create((set, get) => ({
  // Lista de items del carrito
  // Cada item tiene: { id, nombre, precio, imagen, stock, cantidad }
  items: [],

  // Agregar un producto al carrito
  // Si ya existe, solo aumenta la cantidad
  agregar: (producto) => {
    const items = get().items;
    const existe = items.find(item => item.id === producto.id);

    if (existe) {
      set({
        items: items.map(item =>
          item.id === producto.id
            ? { ...item, cantidad: item.cantidad + 1 }
            : item
        )
      });
    } else {
      set({ items: [...items, { ...producto, cantidad: 1 }] });
    }
  },

  // Cambiar la cantidad de un producto
  cambiarCantidad: (id, cantidad) => {
    set({
      items: get().items.map(item =>
        item.id === id ? { ...item, cantidad } : item
      )
    });
  },

  // Eliminar un producto del carrito
  eliminar: (id) => {
    set({ items: get().items.filter(item => item.id !== id) });
  },

  // Vaciar todo el carrito
  vaciar: () => set({ items: [] }),

  // Total de productos (suma de cantidades)
  // Ejemplo: 2 orquideas + 1 maceta = 3
  totalItems: () => get().items.reduce((acc, item) => acc + item.cantidad, 0),
}));

export default useCarritoStore;