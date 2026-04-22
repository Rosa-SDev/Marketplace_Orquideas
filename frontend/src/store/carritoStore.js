import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// persist hace que el carrito se guarde en localStorage automaticamente
// asi no se pierde cuando el usuario recarga la pagina

const useCarritoStore = create(
  persist(
    (set, get) => ({
      items: [],

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

      cambiarCantidad: (id, cantidad) => {
        set({
          items: get().items.map(item =>
            item.id === id ? { ...item, cantidad } : item
          )
        });
      },

      eliminar: (id) => {
        set({ items: get().items.filter(item => item.id !== id) });
      },

      vaciar: () => set({ items: [] }),

      totalItems: () => get().items.reduce((acc, item) => acc + item.cantidad, 0),
    }),
    {
      name: 'carrito', // nombre de la clave en localStorage
    }
  )
);

export default useCarritoStore;