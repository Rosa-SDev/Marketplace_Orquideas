package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.ReservaCarrito;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoReserva;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ProductoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ReservaCarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Este servicio se encarga de toda la lógica de reserva de stock:
//  - Verificar si hay disponibilidad antes de agregar al carrito
//  - Crear reservas cuando el usuario agrega al carrito o va a pagar (aparta el stock por un tiempo)
//  - Liberar reservas que se vencieron (si el usuario abandona el checkout)
//  - Confirmar reservas cuando el pago se completa (descuenta el stock real)
//  - Cancelar reservas si el usuario cancela antes de pagar
@Service
@RequiredArgsConstructor
public class StockReservaService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final ReservaCarritoRepository reservaCarritoRepository;

    // Cuánto tiempo dura una reserva antes de expirar (en minutos)
    // Si el usuario no completa el pago en este tiempo, la reserva se libera sola
    private static final int MINUTOS_EXPIRACION = 15;

    // Revisa si hay suficiente stock disponible para un producto
    // stock disponible = stock total - stock que ya está reservado por otros usuarios
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long idProducto, Integer cantidadSolicitada) {
        Producto producto = productoRepository.findByIdAndActivoTrue(idProducto)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con id: " + idProducto));

        int stockDisponible = producto.getStock() - producto.getStockReservado();
        return stockDisponible >= cantidadSolicitada;
    }

    // Crea una reserva para UN SOLO producto (se usa al agregar un item al carrito)
    // Aparta esa cantidad del stock disponible para que nadie más la compre durante 15 minutos
    @Transactional
    public void crearReserva(Carrito carrito, Producto producto, Integer cantidad) {
        // Revisamos una vez más que todavía haya disponibilidad para este producto
        int stockDisponible = producto.getStock() - producto.getStockReservado();
        if (stockDisponible < cantidad) {
            throw new RuntimeException("No hay suficiente stock para el producto: " + producto.getNombre());
        }

        // Subimos el stockReservado del producto (lo apartamos del stock disponible)
        producto.setStockReservado(producto.getStockReservado() + cantidad);
        productoRepository.save(producto);

        // Creamos la reserva con estado ACTIVA y una fecha de expiración a futuro
        ReservaCarrito reserva = new ReservaCarrito();
        reserva.setCarrito(carrito);
        reserva.setProducto(producto);
        reserva.setCantidadReservada(cantidad);
        reserva.setEstado(EstadoReserva.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());
        reserva.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

        reservaCarritoRepository.save(reserva);
    }

    // Crea reservas para TODOS los items del carrito (se usa al iniciar el checkout)
    // Esto se llama cuando el usuario da click en "Ir a pagar"
    @Transactional
    public void crearReserva(Long idCarrito) {
        Carrito carrito = carritoRepository.findById(idCarrito)
                .orElseThrow(() -> new RuntimeException("No se encontró el carrito con id: " + idCarrito));

        // Recorremos cada item del carrito y creamos su propia reserva
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = item.getProducto();
            Integer cantidad = item.getCantidad();

            // Revisamos una vez más que todavía haya disponibilidad para este producto
            int stockDisponible = producto.getStock() - producto.getStockReservado();
            if (stockDisponible < cantidad) {
                throw new RuntimeException("No hay suficiente stock para el producto: " + producto.getNombre());
            }

            // Subimos el stockReservado del producto (lo apartamos del stock disponible)
            producto.setStockReservado(producto.getStockReservado() + cantidad);
            productoRepository.save(producto);

            // Creamos la reserva con estado ACTIVA y una fecha de expiración a futuro
            ReservaCarrito reserva = new ReservaCarrito();
            reserva.setCarrito(carrito);
            reserva.setProducto(producto);
            reserva.setCantidadReservada(cantidad);
            reserva.setEstado(EstadoReserva.ACTIVA);
            reserva.setFechaCreacion(LocalDateTime.now());
            reserva.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));

            reservaCarritoRepository.save(reserva);
        }
    }

    // Libera todas las reservas cuya fecha de expiración ya pasó
    // La tarea programada llama a este método cada 5 minutos
    @Transactional
    public void liberarReservasExpiradas() {
        LocalDateTime ahora = LocalDateTime.now();

        // Buscamos todas las reservas que siguen ACTIVAS pero con fecha de expiración vencida
        List<ReservaCarrito> reservasExpiradas = reservaCarritoRepository
                .findByEstadoAndFechaExpiracionBefore(EstadoReserva.ACTIVA, ahora);

        // Por cada reserva vencida devolvemos el stock al producto y cambiamos el estado a EXPIRADA
        for (ReservaCarrito reserva : reservasExpiradas) {
            Producto producto = reserva.getProducto();
            producto.setStockReservado(producto.getStockReservado() - reserva.getCantidadReservada());
            productoRepository.save(producto);

            reserva.setEstado(EstadoReserva.EXPIRADA);
            reservaCarritoRepository.save(reserva);
        }
    }

    // Confirma las reservas activas de un carrito cuando el pago fue exitoso
    // Descuenta el stock real (porque ya se vendió) y limpia el stockReservado
    @Transactional
    public void confirmarReservas(Long idCarrito) {
        List<ReservaCarrito> reservasActivas = reservaCarritoRepository
                .findByCarritoIdAndEstado(idCarrito, EstadoReserva.ACTIVA);

        for (ReservaCarrito reserva : reservasActivas) {
            Producto producto = reserva.getProducto();
            // Bajamos el stock total porque el producto ya se vendió
            producto.setStock(producto.getStock() - reserva.getCantidadReservada());
            // Y también lo quitamos del stockReservado porque ya no está simplemente apartado, ya se vendió
            producto.setStockReservado(producto.getStockReservado() - reserva.getCantidadReservada());
            productoRepository.save(producto);

            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reservaCarritoRepository.save(reserva);
        }
    }

    // Cancela las reservas activas de un carrito (por ejemplo si el usuario cancela antes de pagar)
    // Devuelve las unidades al stock disponible para que otros puedan comprarlas
    @Transactional
    public void cancelarReservaCarrito(Long idCarrito) {
        List<ReservaCarrito> reservasActivas = reservaCarritoRepository
                .findByCarritoIdAndEstado(idCarrito, EstadoReserva.ACTIVA);

        for (ReservaCarrito reserva : reservasActivas) {
            Producto producto = reserva.getProducto();
            producto.setStockReservado(producto.getStockReservado() - reserva.getCantidadReservada());
            productoRepository.save(producto);

            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaCarritoRepository.save(reserva);
        }
    }
}