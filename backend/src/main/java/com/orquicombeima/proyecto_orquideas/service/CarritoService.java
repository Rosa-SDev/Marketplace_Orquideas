package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.dto.ItemCarritoDTO;
import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ItemCarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;

    // GET /api/carrito
    @Transactional(readOnly = true)
    public CarritoDTO obtenerCarrito(String emailUsuario) {
        Carrito carrito = carritoRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito para el usuario con correo: " + emailUsuario));

        return convertirADTO(carrito);
    }

    // PUT /api/carrito/{idItem}/cantidad
    @Transactional
    public CarritoDTO actualizarCantidad(Long idItem, int nuevaCantidad, String emailUsuario) {
        ItemCarrito item = itemCarritoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("No se encontró el item con id: " + idItem));

        //Verificar que el item pertenece al carrito del usuario autenticado
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

        // Verificar que el item pertenece al carrito del usuario autenticado
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

    // Convertir a CarritoDTO calculando los subtotales y el total
    private CarritoDTO convertirADTO(Carrito carrito) {
        List<ItemCarritoDTO> itemsDTO = carrito.getItems()
                .stream()
                .map(this::convertirAItemDTO)
                .toList();

        double total = itemsDTO.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(0.0, Double::sum);  // Se suman los subtotales partiendo desde 0.0

        return CarritoDTO.builder()
                .id(carrito.getId())
                .idUsuario(carrito.getUsuario().getId())
                .items(itemsDTO)
                .total(total)
                .build();
    }

    // Convertir a ItemCarritoDTO calculando el subtotal
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
