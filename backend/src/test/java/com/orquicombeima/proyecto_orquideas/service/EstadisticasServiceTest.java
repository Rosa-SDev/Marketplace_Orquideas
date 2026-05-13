package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.EstadisticasDTO;
import com.orquicombeima.proyecto_orquideas.model.Pedido;
import com.orquicombeima.proyecto_orquideas.model.enums.EstadoPedido;
import com.orquicombeima.proyecto_orquideas.repository.PedidoRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EstadisticasServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private EstadisticasService service;

    @BeforeEach
    void setUp() {
        // Stubs por defecto: devolver vacío/cero. Marcados como lenient() porque algunos tests
        // los sobreescriben con datos específicos y Mockito strict-mode reportaría "unnecessary stubbing"
        // en esos casos. Con lenient() solo se valida que TODOS los tests pasen sin importar
        // si cada uno usa o no este stub específico.
        lenient().when(pedidoRepository.findByFechaPedidoBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        lenient().when(pedidoRepository.findByEstado(EstadoPedido.PENDIENTE)).thenReturn(List.of());
        lenient().when(pedidoRepository.findVentasPorMes(any(LocalDateTime.class))).thenReturn(List.of());
        lenient().when(usuarioRepository.count()).thenReturn(0L);
    }

    private Pedido pedidoCon(EstadoPedido estado, double total) {
        Pedido p = new Pedido();
        p.setEstado(estado);
        p.setTotal(total);
        return p;
    }

    @Test
    void obtenerEstadisticas_ventasMesSumaSoloPedidosPagados() {
        // Llegan 3 pedidos: 2 pagados (50000 + 30000) y 1 pendiente (40000)
        // ventasMes debe ser 80000, no 120000
        lenient().when(pedidoRepository.findByFechaPedidoBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        pedidoCon(EstadoPedido.PAGADO, 50000.0),
                        pedidoCon(EstadoPedido.PENDIENTE, 40000.0),
                        pedidoCon(EstadoPedido.PAGADO, 30000.0)));

        EstadisticasDTO dto = service.obtenerEstadisticas();

        assertThat(dto.getVentasMes()).isEqualTo(80000.0);
        assertThat(dto.getPedidosMes()).isEqualTo(3);  // Cuenta TODOS los del mes, sin filtrar estado
    }

    @Test
    void obtenerEstadisticas_pedidosPendientesYClientesRegistrados() {
        lenient().when(pedidoRepository.findByEstado(EstadoPedido.PENDIENTE)).thenReturn(List.of(
                pedidoCon(EstadoPedido.PENDIENTE, 0.0),
                pedidoCon(EstadoPedido.PENDIENTE, 0.0),
                pedidoCon(EstadoPedido.PENDIENTE, 0.0)));
        lenient().when(usuarioRepository.count()).thenReturn(42L);

        EstadisticasDTO dto = service.obtenerEstadisticas();

        assertThat(dto.getPedidosPendientes()).isEqualTo(3);
        assertThat(dto.getClientesRegistrados()).isEqualTo(42);
    }

    @Test
    void obtenerEstadisticas_ventasPorMes_mapeaConNombreMesEnEspanol() {
        // findVentasPorMes devuelve filas [mes, anio, total]
        // Mes 1 = enero, Mes 5 = mayo
        Object[] fila1 = new Object[]{1, 2026, 200000.0};
        Object[] fila2 = new Object[]{5, 2026, 350000.0};
        lenient().when(pedidoRepository.findVentasPorMes(any(LocalDateTime.class))).thenReturn(List.of(fila1, fila2));

        EstadisticasDTO dto = service.obtenerEstadisticas();

        assertThat(dto.getVentasPorMes()).hasSize(2);
        assertThat(dto.getVentasPorMes().get(0).getMes()).isEqualToIgnoringCase("enero");
        assertThat(dto.getVentasPorMes().get(0).getTotalVentas()).isEqualTo(200000.0);
        assertThat(dto.getVentasPorMes().get(1).getMes()).isEqualToIgnoringCase("mayo");
        assertThat(dto.getVentasPorMes().get(1).getTotalVentas()).isEqualTo(350000.0);
    }

    @Test
    void obtenerEstadisticas_sinDatos_devuelveCerosYListaVacia() {
        EstadisticasDTO dto = service.obtenerEstadisticas();

        assertThat(dto.getVentasMes()).isEqualTo(0.0);
        assertThat(dto.getPedidosMes()).isEqualTo(0);
        assertThat(dto.getPedidosPendientes()).isEqualTo(0);
        assertThat(dto.getClientesRegistrados()).isEqualTo(0);
        assertThat(dto.getVentasPorMes()).isEmpty();
    }
}