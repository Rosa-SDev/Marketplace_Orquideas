package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDetalleDTO;
import com.orquicombeima.proyecto_orquideas.dto.RecomendacionMacetaDTO;
import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.model.RecomendacionMaceta;
import com.orquicombeima.proyecto_orquideas.repository.GuiaCuidadoRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import com.orquicombeima.proyecto_orquideas.repository.RecomendacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrquideaServiceTest {

    @Mock private OrquideaRepository orquideaRepository;
    @Mock private GuiaCuidadoRepository guiaCuidadoRepository;
    @Mock private RecomendacionRepository recomendacionRepository;

    @InjectMocks private OrquideaService service;

    private Orquidea orquidea;
    private GuiaCuidado guia;
    private Maceta maceta;
    private RecomendacionMaceta recomendacion;

    @BeforeEach
    void setUp() {
        orquidea = new Orquidea();
        orquidea.setId(1L);
        orquidea.setNombre("Cattleya Trianae");
        orquidea.setPrecio(85000.0);
        orquidea.setStock(10);
        orquidea.setImageUrl("https://cloudinary.com/orq.jpg");
        orquidea.setActivo(true);
        orquidea.setVariedad("Cattleya");
        orquidea.setColorFlor("Rosado");
        orquidea.setTamanio("Mediana");
        orquidea.setNivelCuidado("Intermedio");
        orquidea.setTiempoFloracion("Primavera");

        guia = new GuiaCuidado();
        guia.setId(10L);
        guia.setOrquidea(orquidea);
        guia.setTitulo("Cuidado Cattleya");
        guia.setVariedad("Cattleya");
        guia.setContenido("Texto");
        guia.setFrecuenciaRiego("Cada 5-7 días");
        guia.setLuzRequerida("Indirecta");
        guia.setTemperaturaIdeal("18-26 °C");
        guia.setFertilizacion("Mensual");
        guia.setImageUrl("https://cloudinary.com/guia.jpg");

        maceta = new Maceta();
        maceta.setId(5L);
        maceta.setNombre("Maceta Cerámica");
        maceta.setPrecio(25000.0);
        maceta.setMaterial("Cerámica");
        maceta.setColor("Blanco");
        maceta.setEstilo("Moderno");
        maceta.setImageUrl("https://cloudinary.com/maceta.jpg");

        recomendacion = new RecomendacionMaceta();
        recomendacion.setDescripcion("Ideal por el tamaño");
        recomendacion.setOrquidea(orquidea);
        recomendacion.setMaceta(maceta);
    }

    @Test
    void listarTodas_sinFiltros_devuelvePrimeraPaginaConDTOs() {
        Page<Orquidea> page = new PageImpl<>(List.of(orquidea));
        when(orquideaRepository.findWithFilters(eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        List<OrquideaDTO> resultado = service.listarTodas(null, null, null, null, 0, 9);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Cattleya Trianae");
        assertThat(resultado.get(0).getVariedad()).isEqualTo("Cattleya");
    }

    @Test
    void listarTodas_pasaFiltrosYPaginableAlRepositorio() {
        Page<Orquidea> page = new PageImpl<>(List.of(orquidea));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(orquideaRepository.findWithFilters(eq("Cattleya"), eq("Rosado"), eq(50000.0), eq(100000.0), pageableCaptor.capture()))
                .thenReturn(page);

        service.listarTodas("Cattleya", "Rosado", 50000.0, 100000.0, 1, 5);

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable).isEqualTo(PageRequest.of(1, 5));
    }

    @Test
    void listarTodas_sinResultados_devuelveListaVacia() {
        Page<Orquidea> page = new PageImpl<>(List.of());
        when(orquideaRepository.findWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        List<OrquideaDTO> resultado = service.listarTodas(null, null, null, null, 0, 9);

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerPorId_existe_devuelveDTO() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));

        OrquideaDTO resultado = service.obtenerPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Cattleya Trianae");
    }

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró una orquídea");
    }

    @Test
    void obtenerDetallePorId_conGuiaYRecomendaciones_devuelveTodoCompleto() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(guiaCuidadoRepository.findByOrquideaId(1L)).thenReturn(Optional.of(guia));
        when(recomendacionRepository.findByOrquideaId(1L)).thenReturn(List.of(recomendacion));

        OrquideaDetalleDTO detalle = service.obtenerDetallePorId(1L);

        assertThat(detalle.getNombre()).isEqualTo("Cattleya Trianae");
        assertThat(detalle.getGuiaCuidado()).isNotNull();
        assertThat(detalle.getGuiaCuidado().getTitulo()).isEqualTo("Cuidado Cattleya");
        assertThat(detalle.getRecomendaciones()).hasSize(1);
        assertThat(detalle.getRecomendaciones().get(0).getMacetaNombre()).isEqualTo("Maceta Cerámica");
    }

    @Test
    void obtenerDetallePorId_sinGuia_devuelveDetalleConGuiaNull() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(guiaCuidadoRepository.findByOrquideaId(1L)).thenReturn(Optional.empty());
        when(recomendacionRepository.findByOrquideaId(1L)).thenReturn(List.of());

        OrquideaDetalleDTO detalle = service.obtenerDetallePorId(1L);

        assertThat(detalle.getGuiaCuidado()).isNull();
        assertThat(detalle.getRecomendaciones()).isEmpty();
    }

    @Test
    void obtenerDetallePorId_idNoExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerDetallePorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró una orquídea");
    }

    @Test
    void obtenerRecomendaciones_orquideaExiste_devuelveListaDeRecomendacionDTOs() {
        when(orquideaRepository.existsById(1L)).thenReturn(true);
        when(recomendacionRepository.findByOrquideaId(1L)).thenReturn(List.of(recomendacion));

        List<RecomendacionMacetaDTO> resultado = service.obtenerRecomendaciones(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMacetaId()).isEqualTo(5L);
        assertThat(resultado.get(0).getMaterial()).isEqualTo("Cerámica");
    }

    @Test
    void obtenerRecomendaciones_orquideaNoExiste_lanzaExcepcion() {
        when(orquideaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.obtenerRecomendaciones(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró una orquídea");
    }
}