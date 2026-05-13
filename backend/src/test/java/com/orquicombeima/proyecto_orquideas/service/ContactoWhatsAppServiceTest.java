package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.model.DireccionEnvio;
import com.orquicombeima.proyecto_orquideas.model.ItemPedido;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.Producto;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactoWhatsAppServiceTest {

    @Mock private PedidoRepository pedidoRepository;

    @InjectMocks private ContactoWhatsAppService service;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // El @Value no se inyecta en unit tests; lo setteamos manualmente
        ReflectionTestUtils.setField(service, "numero", "573001234567");

        Producto cattleya = new Orquidea();
        cattleya.setId(1L);
        cattleya.setNombre("Cattleya");
        cattleya.setPrecio(50000.0);

        ItemPedido item = new ItemPedido();
        item.setProducto(cattleya);
        item.setCantidad(2);
        item.setPrecioUnitario(50000.0);
        item.setSubtotal(100000.0);

        DireccionEnvio direccion = new DireccionEnvio();
        direccion.setDireccion("Calle 10 #5-20");
        direccion.setCiudad("Ibagué");
        direccion.setDepartamento("Tolima");

        pedido = new Pedido();
        pedido.setId(500L);
        pedido.setSubtotal(100000.0);
        pedido.setCostoEnvio(10000.0);
        pedido.setTotal(110000.0);
        pedido.setItems(new ArrayList<>(List.of(item)));
        pedido.setDireccionEnvio(direccion);
    }

    @Test
    void generarEnlaceContacto_pedidoNoExiste_lanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generarEnlaceContacto(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró el pedido");
    }

    @Test
    void generarEnlaceContacto_construyeUrlConBaseDeWhatsAppYNumero() {
        when(pedidoRepository.findById(500L)).thenReturn(Optional.of(pedido));

        String url = service.generarEnlaceContacto(500L);

        assertThat(url).startsWith("https://wa.me/573001234567?text=");
    }

    @Test
    void generarEnlaceContacto_mensajeIncluyeIdPedidoProductosYTotales() {
        when(pedidoRepository.findById(500L)).thenReturn(Optional.of(pedido));

        String url = service.generarEnlaceContacto(500L);

        // Decodificamos para verificar el mensaje original que el cliente verá en WhatsApp
        String mensajeCodificado = url.substring(url.indexOf("?text=") + 6);
        String mensaje = URLDecoder.decode(mensajeCodificado, StandardCharsets.UTF_8);

        assertThat(mensaje).contains("pedido #500");
        assertThat(mensaje).contains("Cattleya");
        assertThat(mensaje).contains("x2");
        assertThat(mensaje).contains("Calle 10 #5-20");
        assertThat(mensaje).contains("Ibagué");
        assertThat(mensaje).contains("Tolima");
    }
}