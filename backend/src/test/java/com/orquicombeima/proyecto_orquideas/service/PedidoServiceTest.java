package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.CrearPedidoDTO;
import com.orquicombeima.proyecto_orquideas.dto.DireccionEnvioDTO;
import com.orquicombeima.proyecto_orquideas.dto.PedidoDTO;
import com.orquicombeima.proyecto_orquideas.model.Carrito;
import com.orquicombeima.proyecto_orquideas.model.ItemCarrito;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.PagoWompi;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPago;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import com.orquicombeima.proyecto_orquideas.repository.CarritoRepository;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private StockReservaService stockReservaService;
    @Mock private CarritoService carritoService;
    @Mock private PagoWompiService pagoWompiService;

    @InjectMocks private PedidoService service;

    private static final String EMAIL = "rosa@test.com";
    private static final double COSTO_ENVIO_FIJO = 10000.0;

    private Usuario usuario;
    private Producto producto1;
    private Producto producto2;
    private Carrito carrito;
    private CrearPedidoDTO request;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(EMAIL);
        usuario.setNombre("Rosa");
        usuario.setRol(Rol.CLIENTE);

        producto1 = new Orquidea();
        producto1.setId(10L);
        producto1.setNombre("Cattleya");
        producto1.setPrecio(50000.0);
        producto1.setImageUrl("https://cdn/cattleya.jpg");

        producto2 = new Orquidea();
        producto2.setId(11L);
        producto2.setNombre("Phalaenopsis");
        producto2.setPrecio(30000.0);
        producto2.setImageUrl("https://cdn/phalaenopsis.jpg");

        carrito = new Carrito();
        carrito.setId(100L);
        carrito.setUsuario(usuario);
        carrito.setItems(new ArrayList<>());

        DireccionEnvioDTO direccion = DireccionEnvioDTO.builder()
                .nombreDestinatario("Rosa Pérez")
                .telefonoDestinatario("3001234567")
                .departamento("Tolima")
                .ciudad("Ibagué")
                .direccion("Calle 10 #5-20")
                .codigoPostal("730001")
                .instruccionesAdicionales("Casa esquinera")
                .build();
        request = new CrearPedidoDTO(direccion);
    }

    // Helper: agrega un item al carrito de prueba
    private void agregarItemAlCarrito(Producto producto, int cantidad) {
        ItemCarrito item = new ItemCarrito();
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(cantidad);
        carrito.getItems().add(item);
    }

    // Helper: simula que pedidoRepository.save() asigna id, fechaPedido y estado (como @PrePersist en producción)
    private void mockPedidoSaveConPrePersist(Long idAsignado) {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(idAsignado);
            }
            if (p.getFechaPedido() == null) {
                p.setFechaPedido(LocalDateTime.now());
            }
            if (p.getEstado() == null) {
                p.setEstado(EstadoPedido.PENDIENTE);
            }
            return p;
        });
    }

    // Helper: simula que generarEnlacePago asocia un PagoWompi al pedido y devuelve la URL
    private void mockGenerarEnlacePagoConPago(String url, String referencia, EstadoPago estado) {
        when(pagoWompiService.generarEnlacePago(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            PagoWompi pago = new PagoWompi();
            pago.setReferenciaPago(referencia);
            pago.setEstado(estado);
            pago.setMonto(p.getTotal());
            p.setPago(pago);
            return url;
        });
    }

    // -----------------------------------------------------------------
    // crearPedido — caminos de error
    // -----------------------------------------------------------------

    @Test
    void crearPedido_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearPedido(EMAIL, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_carritoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearPedido(EMAIL, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrito no encontrado");
    }

    @Test
    void crearPedido_carritoVacio_lanzaExcepcion() {
        // carrito.items está vacío por el setUp
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> service.crearPedido(EMAIL, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("carrito vacío");

        verify(pedidoRepository, never()).save(any());
        verify(stockReservaService, never()).confirmarReservas(anyLong());
    }

    // -----------------------------------------------------------------
    // crearPedido — happy path
    // -----------------------------------------------------------------

    @Test
    void crearPedido_ok_creaPedidoConItemsCorrectos() {
        agregarItemAlCarrito(producto1, 2);  // 50000 * 2 = 100000
        agregarItemAlCarrito(producto2, 1);  // 30000 * 1 = 30000

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(carrito));
        mockPedidoSaveConPrePersist(500L);
        mockGenerarEnlacePagoConPago("https://checkout.wompi.co/p/?ref=abc", "pedido-500-abc", EstadoPago.PENDIENTE);

        PedidoDTO dto = service.crearPedido(EMAIL, request);

        // Pedido guardado con todos los datos correctos
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        Pedido guardado = captor.getValue();
        assertThat(guardado.getUsuario()).isEqualTo(usuario);
        assertThat(guardado.getItems()).hasSize(2);
        assertThat(guardado.getDireccionEnvio().getCiudad()).isEqualTo("Ibagué");

        // DTO de respuesta tiene los campos esperados
        assertThat(dto.getId()).isEqualTo(500L);
        assertThat(dto.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(dto.getEstadoPago()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(dto.getLinkPago()).isEqualTo("https://checkout.wompi.co/p/?ref=abc");
        assertThat(dto.getReferenciaPago()).isEqualTo("pedido-500-abc");
        assertThat(dto.getItems()).hasSize(2);
    }

    @Test
    void crearPedido_calculaSubtotalCostoEnvioYTotalCorrectamente() {
        agregarItemAlCarrito(producto1, 2);  // 100000
        agregarItemAlCarrito(producto2, 1);  // 30000

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(carrito));
        mockPedidoSaveConPrePersist(500L);
        mockGenerarEnlacePagoConPago("url", "ref", EstadoPago.PENDIENTE);

        PedidoDTO dto = service.crearPedido(EMAIL, request);

        assertThat(dto.getSubtotal()).isEqualTo(130000.0);
        assertThat(dto.getCostoEnvio()).isEqualTo(COSTO_ENVIO_FIJO);
        assertThat(dto.getTotal()).isEqualTo(140000.0);
    }

    @Test
    void crearPedido_snapshotPrecioUnitarioEnItemPedido() {
        agregarItemAlCarrito(producto1, 3);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(carrito));
        mockPedidoSaveConPrePersist(500L);
        mockGenerarEnlacePagoConPago("url", "ref", EstadoPago.PENDIENTE);

        PedidoDTO dto = service.crearPedido(EMAIL, request);

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getPrecioUnitario()).isEqualTo(50000.0);
        assertThat(dto.getItems().get(0).getCantidad()).isEqualTo(3);
        assertThat(dto.getItems().get(0).getSubtotal()).isEqualTo(150000.0);
        assertThat(dto.getItems().get(0).getNombreProducto()).isEqualTo("Cattleya");
    }

    @Test
    void crearPedido_confirmaReservasYVaciaCarritoAlFinal() {
        agregarItemAlCarrito(producto1, 1);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(carrito));
        mockPedidoSaveConPrePersist(500L);
        mockGenerarEnlacePagoConPago("url", "ref", EstadoPago.PENDIENTE);

        service.crearPedido(EMAIL, request);

        verify(stockReservaService).confirmarReservas(carrito.getId());  // 100L
        verify(carritoService).vaciarCarrito(EMAIL);
    }

    @Test
    void crearPedido_mapeaDireccionEnvioCorrectamente() {
        agregarItemAlCarrito(producto1, 1);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(carrito));
        mockPedidoSaveConPrePersist(500L);
        mockGenerarEnlacePagoConPago("url", "ref", EstadoPago.PENDIENTE);

        PedidoDTO dto = service.crearPedido(EMAIL, request);

        DireccionEnvioDTO d = dto.getDireccionEnvio();
        assertThat(d.getNombreDestinatario()).isEqualTo("Rosa Pérez");
        assertThat(d.getTelefonoDestinatario()).isEqualTo("3001234567");
        assertThat(d.getDepartamento()).isEqualTo("Tolima");
        assertThat(d.getCiudad()).isEqualTo("Ibagué");
        assertThat(d.getDireccion()).isEqualTo("Calle 10 #5-20");
        assertThat(d.getCodigoPostal()).isEqualTo("730001");
        assertThat(d.getInstruccionesAdicionales()).isEqualTo("Casa esquinera");
    }

    // -----------------------------------------------------------------
    // obtenerHistorial
    // -----------------------------------------------------------------

    @Test
    void obtenerHistorial_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerHistorial(EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void obtenerHistorial_devuelveListaDePedidosOrdenados() {
        Pedido pedido = construirPedido(200L, EstadoPedido.PAGADO, EstadoPago.APROBADO, "ref-200");
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(1L)).thenReturn(List.of(pedido));

        List<PedidoDTO> historial = service.obtenerHistorial(EMAIL);

        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).getId()).isEqualTo(200L);
        assertThat(historial.get(0).getEstado()).isEqualTo(EstadoPedido.PAGADO);
        assertThat(historial.get(0).getEstadoPago()).isEqualTo(EstadoPago.APROBADO);
        // En el historial el linkPago siempre va null
        assertThat(historial.get(0).getLinkPago()).isNull();
        assertThat(historial.get(0).getReferenciaPago()).isEqualTo("ref-200");
    }

    @Test
    void obtenerHistorial_pedidoSinPago_estadoPagoYReferenciaSonNull() {
        Pedido pedido = construirPedido(300L, EstadoPedido.PENDIENTE, null, null);
        pedido.setPago(null);  // sin pago asociado
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(1L)).thenReturn(List.of(pedido));

        List<PedidoDTO> historial = service.obtenerHistorial(EMAIL);

        assertThat(historial.get(0).getEstadoPago()).isNull();
        assertThat(historial.get(0).getReferenciaPago()).isNull();
    }

    @Test
    void obtenerHistorial_sinPedidos_devuelveListaVacia() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(1L)).thenReturn(List.of());

        assertThat(service.obtenerHistorial(EMAIL)).isEmpty();
    }

    // -----------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------

    private Pedido construirPedido(Long id, EstadoPedido estadoPedido, EstadoPago estadoPago, String referencia) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setUsuario(usuario);
        pedido.setEstado(estadoPedido);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setSubtotal(50000.0);
        pedido.setCostoEnvio(COSTO_ENVIO_FIJO);
        pedido.setTotal(60000.0);

        com.orquicombeima.proyecto_orquideas.model.DireccionEnvio dir =
                new com.orquicombeima.proyecto_orquideas.model.DireccionEnvio();
        dir.setNombreDestinatario("Rosa");
        dir.setTelefonoDestinatario("3001234567");
        dir.setDepartamento("Tolima");
        dir.setCiudad("Ibagué");
        dir.setDireccion("Calle 1");
        pedido.setDireccionEnvio(dir);

        pedido.setItems(new ArrayList<>());

        if (estadoPago != null) {
            PagoWompi pago = new PagoWompi();
            pago.setReferenciaPago(referencia);
            pago.setEstado(estadoPago);
            pedido.setPago(pago);
        }
        return pedido;
    }
}