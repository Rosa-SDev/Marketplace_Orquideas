package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.ReservaCarrito;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoReserva;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ProductoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ReservaCarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockReservaServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ReservaCarritoRepository reservaCarritoRepository;

    @InjectMocks private StockReservaService service;

    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        producto = new Orquidea();
        producto.setId(1L);
        producto.setNombre("Cattleya");
        producto.setStock(10);
        producto.setStockReservado(0);

        carrito = new Carrito();
        carrito.setId(100L);
    }

    // -----------------------------------------------------------------
    // verificarDisponibilidad
    // -----------------------------------------------------------------

    @Test
    void verificarDisponibilidad_productoNoExiste_lanzaExcepcion() {
        when(productoRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarDisponibilidad(99L, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró el producto");
    }

    @Test
    void verificarDisponibilidad_stockSuficiente_true() {
        producto.setStock(10);
        producto.setStockReservado(2);
        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));

        assertThat(service.verificarDisponibilidad(1L, 5)).isTrue();  // disponible = 8, pide 5
    }

    @Test
    void verificarDisponibilidad_pideExactamenteLoDisponible_true() {
        producto.setStock(10);
        producto.setStockReservado(3);
        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));

        assertThat(service.verificarDisponibilidad(1L, 7)).isTrue();  // disponible = 7, pide 7
    }

    @Test
    void verificarDisponibilidad_pideMasDelDisponible_false() {
        producto.setStock(10);
        producto.setStockReservado(8);
        when(productoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto));

        assertThat(service.verificarDisponibilidad(1L, 5)).isFalse();  // disponible = 2, pide 5
    }

    // -----------------------------------------------------------------
    // crearReserva(Carrito, Producto, Integer) - se llama desde agregarItem
    // -----------------------------------------------------------------

    @Test
    void crearReserva_porProducto_sinDisponibilidad_lanzaExcepcion() {
        producto.setStock(5);
        producto.setStockReservado(4);  // solo queda 1 disponible

        assertThatThrownBy(() -> service.crearReserva(carrito, producto, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay suficiente stock");

        verify(reservaCarritoRepository, never()).save(any());
    }

    @Test
    void crearReserva_porProducto_ok_aumentaStockReservadoYGuardaReserva() {
        producto.setStock(10);
        producto.setStockReservado(0);

        service.crearReserva(carrito, producto, 3);

        assertThat(producto.getStockReservado()).isEqualTo(3);
        verify(productoRepository).save(producto);

        ArgumentCaptor<ReservaCarrito> captor = ArgumentCaptor.forClass(ReservaCarrito.class);
        verify(reservaCarritoRepository).save(captor.capture());
        ReservaCarrito reserva = captor.getValue();
        assertThat(reserva.getCarrito()).isEqualTo(carrito);
        assertThat(reserva.getProducto()).isEqualTo(producto);
        assertThat(reserva.getCantidadReservada()).isEqualTo(3);
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
        // Expira 15 minutos después de la creación (más/menos un par de segundos por la prueba)
        assertThat(reserva.getFechaExpiracion()).isAfter(LocalDateTime.now().plusMinutes(14));
    }

    // -----------------------------------------------------------------
    // crearReserva(Long idCarrito) - se llama al iniciar checkout
    // -----------------------------------------------------------------

    @Test
    void crearReserva_porCarrito_carritoNoExiste_lanzaExcepcion() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearReserva(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró el carrito");
    }

    @Test
    void crearReserva_porCarrito_creaUnaReservaPorCadaItem() {
        Producto otro = new Orquidea();
        otro.setId(2L);
        otro.setNombre("Phalaenopsis");
        otro.setStock(5);
        otro.setStockReservado(0);

        ItemCarrito item1 = new ItemCarrito();
        item1.setProducto(producto);
        item1.setCantidad(2);
        ItemCarrito item2 = new ItemCarrito();
        item2.setProducto(otro);
        item2.setCantidad(3);
        carrito.setItems(new ArrayList<>(List.of(item1, item2)));

        when(carritoRepository.findById(100L)).thenReturn(Optional.of(carrito));

        service.crearReserva(100L);

        assertThat(producto.getStockReservado()).isEqualTo(2);
        assertThat(otro.getStockReservado()).isEqualTo(3);
        verify(reservaCarritoRepository, times(2)).save(any(ReservaCarrito.class));
    }

    @Test
    void crearReserva_porCarrito_unItemSinStock_lanzaExcepcionYNoSigue() {
        producto.setStock(2);
        producto.setStockReservado(2);  // sin disponibilidad

        ItemCarrito item1 = new ItemCarrito();
        item1.setProducto(producto);
        item1.setCantidad(1);
        carrito.setItems(new ArrayList<>(List.of(item1)));

        when(carritoRepository.findById(100L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> service.crearReserva(100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay suficiente stock");

        verify(reservaCarritoRepository, never()).save(any());
    }

    // -----------------------------------------------------------------
    // liberarReservasExpiradas
    // -----------------------------------------------------------------

    @Test
    void liberarReservasExpiradas_devuelveStockYMarcaExpiradas() {
        producto.setStockReservado(5);
        ReservaCarrito reserva = new ReservaCarrito();
        reserva.setProducto(producto);
        reserva.setCantidadReservada(5);
        reserva.setEstado(EstadoReserva.ACTIVA);

        when(reservaCarritoRepository.findByEstadoAndFechaExpiracionBefore(
                eq(EstadoReserva.ACTIVA), any(LocalDateTime.class)))
                .thenReturn(List.of(reserva));

        service.liberarReservasExpiradas();

        assertThat(producto.getStockReservado()).isEqualTo(0);
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.EXPIRADA);
        verify(reservaCarritoRepository).save(reserva);
    }

    @Test
    void liberarReservasExpiradas_sinReservasVencidas_noHaceNada() {
        when(reservaCarritoRepository.findByEstadoAndFechaExpiracionBefore(
                eq(EstadoReserva.ACTIVA), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.liberarReservasExpiradas();

        verify(reservaCarritoRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }

    // -----------------------------------------------------------------
    // confirmarReservas
    // -----------------------------------------------------------------

    @Test
    void confirmarReservas_descuentaStockReal_quitaReservadoYMarcaConfirmadas() {
        producto.setStock(10);
        producto.setStockReservado(3);
        ReservaCarrito reserva = new ReservaCarrito();
        reserva.setProducto(producto);
        reserva.setCantidadReservada(3);
        reserva.setEstado(EstadoReserva.ACTIVA);

        when(reservaCarritoRepository.findByCarritoIdAndEstado(100L, EstadoReserva.ACTIVA))
                .thenReturn(List.of(reserva));

        service.confirmarReservas(100L);

        assertThat(producto.getStock()).isEqualTo(7);          // 10 - 3
        assertThat(producto.getStockReservado()).isEqualTo(0); // 3 - 3
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
    }

    // -----------------------------------------------------------------
    // cancelarReservaCarrito
    // -----------------------------------------------------------------

    @Test
    void cancelarReservaCarrito_devuelveStockReservadoYMarcaCanceladas() {
        producto.setStock(10);
        producto.setStockReservado(4);
        ReservaCarrito reserva = new ReservaCarrito();
        reserva.setProducto(producto);
        reserva.setCantidadReservada(4);
        reserva.setEstado(EstadoReserva.ACTIVA);

        when(reservaCarritoRepository.findByCarritoIdAndEstado(100L, EstadoReserva.ACTIVA))
                .thenReturn(List.of(reserva));

        service.cancelarReservaCarrito(100L);

        assertThat(producto.getStock()).isEqualTo(10);          // stock total NO cambia
        assertThat(producto.getStockReservado()).isEqualTo(0);  // se libera lo reservado
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }
}