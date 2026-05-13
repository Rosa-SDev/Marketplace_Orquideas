package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.AgregarItemRequestDTO;
import com.orquicombeima.proyecto_orquideas.dto.CarritoDTO;
import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ItemCarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.ProductoRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private StockReservaService stockReservaService;

    @InjectMocks private CarritoService service;

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("rosa@test.com");
        usuario.setNombre("Rosa");
        usuario.setRol(Rol.CLIENTE);

        producto = new Orquidea();
        producto.setId(10L);
        producto.setNombre("Cattleya");
        producto.setPrecio(50000.0);
        producto.setStock(10);
        producto.setStockReservado(0);
        producto.setImageUrl("https://cdn/cattleya.jpg");
        producto.setActivo(true);

        carrito = new Carrito();
        carrito.setId(100L);
        carrito.setUsuario(usuario);
        carrito.setItems(new ArrayList<>());
    }

    // -----------------------------------------------------------------
    // obtenerCarrito
    // -----------------------------------------------------------------

    @Test
    void obtenerCarrito_existe_devuelveDTOConTotalCorrecto() {
        ItemCarrito item = new ItemCarrito();
        item.setId(1L);
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(3);
        carrito.getItems().add(item);

        when(carritoRepository.findByUsuarioEmail("rosa@test.com")).thenReturn(Optional.of(carrito));

        CarritoDTO dto = service.obtenerCarrito("rosa@test.com");

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getIdUsuario()).isEqualTo(1L);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getTotal()).isEqualTo(150000.0); // 50000 * 3
        assertThat(dto.getItems().get(0).getSubtotal()).isEqualTo(150000.0);
    }

    @Test
    void obtenerCarrito_noExiste_lanzaExcepcion() {
        when(carritoRepository.findByUsuarioEmail("fantasma@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerCarrito("fantasma@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró carrito");
    }

    // -----------------------------------------------------------------
    // agregarItem
    // -----------------------------------------------------------------

    @Test
    void agregarItem_usuarioNoExiste_lanzaExcepcion() {
        AgregarItemRequestDTO request = new AgregarItemRequestDTO(10L, 2);
        when(usuarioRepository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agregarItem(request, "fantasma@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró usuario");
    }

    @Test
    void agregarItem_productoNoExisteOInactivo_lanzaExcepcion() {
        AgregarItemRequestDTO request = new AgregarItemRequestDTO(99L, 2);
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agregarItem(request, "rosa@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró producto activo");
    }

    @Test
    void agregarItem_sinCarritoPrevio_creaUnoYAgrega() {
        AgregarItemRequestDTO request = new AgregarItemRequestDTO(10L, 2);
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioEmail("rosa@test.com")).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> {
            Carrito c = inv.getArgument(0);
            c.setId(200L);
            c.setItems(new ArrayList<>());
            return c;
        });
        when(stockReservaService.verificarDisponibilidad(10L, 2)).thenReturn(true);

        service.agregarItem(request, "rosa@test.com");

        verify(carritoRepository).save(any(Carrito.class));         // creó el carrito
        verify(itemCarritoRepository).save(any(ItemCarrito.class)); // creó el item
        verify(stockReservaService).crearReserva(any(Carrito.class), eq(producto), eq(2));
    }

    @Test
    void agregarItem_productoNuevoEnCarritoExistente_agregaItem() {
        AgregarItemRequestDTO request = new AgregarItemRequestDTO(10L, 2);
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioEmail("rosa@test.com")).thenReturn(Optional.of(carrito));
        when(stockReservaService.verificarDisponibilidad(10L, 2)).thenReturn(true);

        service.agregarItem(request, "rosa@test.com");

        verify(itemCarritoRepository).save(any(ItemCarrito.class));
        verify(stockReservaService).crearReserva(eq(carrito), eq(producto), eq(2));
        assertThat(carrito.getItems()).hasSize(1);
    }

    @Test
    void agregarItem_productoYaEnCarrito_sumaCantidadEnLugarDeDuplicar() {
        // Ya hay un item con cantidad 3
        ItemCarrito existente = new ItemCarrito();
        existente.setId(50L);
        existente.setCarrito(carrito);
        existente.setProducto(producto);
        existente.setCantidad(3);
        carrito.getItems().add(existente);

        AgregarItemRequestDTO request = new AgregarItemRequestDTO(10L, 2);  // agrega 2 más → total 5
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioEmail("rosa@test.com")).thenReturn(Optional.of(carrito));
        when(stockReservaService.verificarDisponibilidad(10L, 5)).thenReturn(true); // verifica el total 3+2

        service.agregarItem(request, "rosa@test.com");

        assertThat(carrito.getItems()).hasSize(1); // sigue siendo 1 item
        assertThat(existente.getCantidad()).isEqualTo(5); // cantidad acumulada
        verify(itemCarritoRepository).save(existente);
        verify(stockReservaService).crearReserva(carrito, producto, 2); // reserva solo la cantidad nueva
    }

    @Test
    void agregarItem_sinStockSuficiente_lanzaExcepcion() {
        AgregarItemRequestDTO request = new AgregarItemRequestDTO(10L, 100);
        when(usuarioRepository.findByEmail("rosa@test.com")).thenReturn(Optional.of(usuario));
        when(productoRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioEmail("rosa@test.com")).thenReturn(Optional.of(carrito));
        when(stockReservaService.verificarDisponibilidad(10L, 100)).thenReturn(false);

        assertThatThrownBy(() -> service.agregarItem(request, "rosa@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay suficiente stock");

        verify(itemCarritoRepository, never()).save(any());
        verify(stockReservaService, never()).crearReserva(any(Carrito.class), any(Producto.class), anyInt());
    }

    // -----------------------------------------------------------------
    // actualizarCantidad
    // -----------------------------------------------------------------

    @Test
    void actualizarCantidad_itemNoExiste_lanzaExcepcion() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarCantidad(99L, 3, "rosa@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró el item");
    }

    @Test
    void actualizarCantidad_itemNoEsDelUsuario_lanzaExcepcion() {
        ItemCarrito item = new ItemCarrito();
        item.setId(50L);
        item.setCarrito(carrito);  // dueño es rosa
        item.setProducto(producto);
        item.setCantidad(1);
        when(itemCarritoRepository.findById(50L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.actualizarCantidad(50L, 3, "ladron@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
    }

    @Test
    void actualizarCantidad_cantidadCero_lanzaExcepcion() {
        ItemCarrito item = new ItemCarrito();
        item.setId(50L);
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(2);
        when(itemCarritoRepository.findById(50L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.actualizarCantidad(50L, 0, "rosa@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("debe ser mayor a 0");
    }

    @Test
    void actualizarCantidad_ok_actualizaYGuarda() {
        ItemCarrito item = new ItemCarrito();
        item.setId(50L);
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(2);
        carrito.getItems().add(item);
        when(itemCarritoRepository.findById(50L)).thenReturn(Optional.of(item));

        service.actualizarCantidad(50L, 5, "rosa@test.com");

        assertThat(item.getCantidad()).isEqualTo(5);
        verify(itemCarritoRepository).save(item);
    }

    // -----------------------------------------------------------------
    // eliminarItem
    // -----------------------------------------------------------------

    @Test
    void eliminarItem_noExiste_lanzaExcepcion() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarItem(99L, "rosa@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró el item");
    }

    @Test
    void eliminarItem_noEsDelUsuario_lanzaExcepcion() {
        ItemCarrito item = new ItemCarrito();
        item.setCarrito(carrito);
        item.setProducto(producto);
        when(itemCarritoRepository.findById(50L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.eliminarItem(50L, "ladron@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");
    }

    @Test
    void eliminarItem_ok_loQuitaDelCarritoYLoBorra() {
        ItemCarrito item = new ItemCarrito();
        item.setId(50L);
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(2);
        carrito.getItems().add(item);
        when(itemCarritoRepository.findById(50L)).thenReturn(Optional.of(item));

        service.eliminarItem(50L, "rosa@test.com");

        assertThat(carrito.getItems()).isEmpty();
        verify(itemCarritoRepository).delete(item);
    }

    // -----------------------------------------------------------------
    // vaciarCarrito
    // -----------------------------------------------------------------

    @Test
    void vaciarCarrito_noExiste_lanzaExcepcion() {
        when(carritoRepository.findByUsuarioEmail("fantasma@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vaciarCarrito("fantasma@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró carrito");
    }

    @Test
    void vaciarCarrito_ok_limpiaItemsYGuarda() {
        ItemCarrito item = new ItemCarrito();
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(2);
        carrito.getItems().add(item);
        when(carritoRepository.findByUsuarioEmail("rosa@test.com")).thenReturn(Optional.of(carrito));

        service.vaciarCarrito("rosa@test.com");

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }
}