package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.AgregarItemRequestDTO;
import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.dto.ItemCarritoDTO;
import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ItemCarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ProductoRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final StockReservaService stockReservaService;

    // GET /api/carrito
    @Transactional(readOnly = true)
    public CarritoDTO obtenerCarrito(String emailUsuario) {
        Carrito carrito = carritoRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito para el usuario con correo: " + emailUsuario));

        return convertirADTO(carrito);
    }

    // POST /api/carrito/agregar
    // Agrega un producto al carrito del usuario autenticado
    // Si el producto ya estaba en el carrito, le suma la cantidad nueva a la existente
    // Además crea una reserva de stock para apartar el producto por 30 minutos
    @Transactional
    public CarritoDTO agregarItem(AgregarItemRequestDTO request, String emailUsuario) {
        // Buscamos al usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("No se encontró usuario con correo: " + emailUsuario));

        // Buscamos el producto y validamos que esté activo en el catálogo
        Producto producto = productoRepository.findByIdAndActivoTrue(request.getIdProducto())
                .orElseThrow(() -> new RuntimeException("No se encontró producto activo con id: " + request.getIdProducto()));

        // Buscamos el carrito del usuario; si no existe lo creamos en el momento
        Carrito carrito = carritoRepository.findByUsuarioEmail(emailUsuario)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    nuevo.setItems(new ArrayList<>());
                    return carritoRepository.save(nuevo);
                });

        // Revisamos si el producto ya estaba en el carrito (para sumarle en vez de duplicar)
        ItemCarrito itemExistente = null;
        for (ItemCarrito item : carrito.getItems()) {
            if (item.getProducto().getId().equals(producto.getId())) {
                itemExistente = item;
                break;
            }
        }

        // Calculamos la cantidad total que quedaría: la que ya había + la nueva que se agrega
        int cantidadTotal;
        if (itemExistente != null) {
            cantidadTotal = itemExistente.getCantidad() + request.getCantidad();
        } else {
            cantidadTotal = request.getCantidad();
        }

        // Verificamos que haya suficiente stock disponible para esa cantidad total
        boolean hayDisponibilidad = stockReservaService.verificarDisponibilidad(producto.getId(), cantidadTotal);
        if (!hayDisponibilidad) {
            throw new RuntimeException("No hay suficiente stock disponible para el producto: " + producto.getNombre());
        }

        // Si ya existía el item, le sumamos la cantidad; si no, creamos uno nuevo
        if (itemExistente != null) {
            itemExistente.setCantidad(cantidadTotal);
            itemCarritoRepository.save(itemExistente);
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(request.getCantidad());
            itemCarritoRepository.save(nuevoItem);
            carrito.getItems().add(nuevoItem);
        }

        // Creamos la reserva de stock: apartamos la cantidad nueva por 30 minutos
        // Esto también incrementa el stock_reservado del producto
        stockReservaService.crearReserva(carrito, producto, request.getCantidad());

        return convertirADTO(carrito);
    }

    // PUT /api/carrito/{idItem}/cantidad
    @Transactional
    public CarritoDTO actualizarCantidad(Long idItem, int nuevaCantidad, String emailUsuario) {
        ItemCarrito item = itemCarritoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("No se encontró el item con id: " + idItem));

        // Verificar que el item pertenece al carrito del usuario autenticado
        if (!item.getCarrito().getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para modificar este item");
        }

        if (nuevaCantidad < 1) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        item.setCantidad(nuevaCantidad);
        itemCarritoRepository.save(item);

        return convertirADTO(item.getCarrito());
    }

    // DELETE /api/carrito/{idItem}
    @Transactional
    public CarritoDTO eliminarItem(Long idItem, String emailUsuario) {
        ItemCarrito item = itemCarritoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("No se encontró el item con id: " + idItem));

        if (!item.getCarrito().getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para eliminar este item");
        }

        Carrito carrito = item.getCarrito();
        carrito.getItems().remove(item);
        itemCarritoRepository.delete(item);

        return convertirADTO(carrito);
    }

    // DELETE /api/carrito/vaciar
    @Transactional
    public void vaciarCarrito(String emailUsuario) {
        Carrito carrito = carritoRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito para el usuario con correo: " + emailUsuario));

        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    // Convierte el carrito a DTO calculando los subtotales de cada item y el total del carrito
    private CarritoDTO convertirADTO(Carrito carrito) {
        List<ItemCarritoDTO> itemsDTO = new ArrayList<>();
        double total = 0.0;
        for (ItemCarrito item : carrito.getItems()) {
            ItemCarritoDTO itemDTO = convertirAItemDTO(item);
            itemsDTO.add(itemDTO);
            total = total + itemDTO.getSubtotal();
        }

        return CarritoDTO.builder()
                .id(carrito.getId())
                .idUsuario(carrito.getUsuario().getId())
                .items(itemsDTO)
                .total(total)
                .build();
    }

    // Convierte un ItemCarrito a ItemCarritoDTO calculando su subtotal (precio x cantidad)
    private ItemCarritoDTO convertirAItemDTO(ItemCarrito item) {
        double precio = item.getProducto().getPrecio();
        double subtotal = precio * item.getCantidad();

        return ItemCarritoDTO.builder()
                .id(item.getId())
                .idProducto(item.getProducto().getId())
                .nombreProducto(item.getProducto().getNombre())
                .imagenUrl(item.getProducto().getImageUrl())
                .precioUnitario(precio)
                .cantidad(item.getCantidad())
                .subtotal(subtotal)
                .build();
    }
}