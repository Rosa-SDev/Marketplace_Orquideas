package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.GuiaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.GuiaCuidadoDTO;
import com.orquicombeima.proyecto_orquideas.model.GuiaCuidado;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
import com.orquicombeima.proyecto_orquideas.repository.GuiaCuidadoRepository;
import com.orquicombeima.proyecto_orquideas.repository.OrquideaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuiaAdminServiceTest {

    @Mock private GuiaCuidadoRepository guiaCuidadoRepository;
    @Mock private OrquideaRepository orquideaRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks private GuiaAdminService service;

    private Orquidea orquidea;
    private GuiaCuidado guia;
    private GuiaAdminDTO dto;
    private MultipartFile imagen;

    @BeforeEach
    void setUp() {
        orquidea = new Orquidea();
        orquidea.setId(1L);
        orquidea.setNombre("Cattleya Trianae");

        guia = new GuiaCuidado();
        guia.setId(10L);
        guia.setOrquidea(orquidea);
        guia.setTitulo("Cuidado de la Cattleya");
        guia.setVariedad("Cattleya");
        guia.setContenido("Texto largo...");
        guia.setFrecuenciaRiego("Cada 5-7 días");
        guia.setLuzRequerida("Indirecta brillante");
        guia.setTemperaturaIdeal("18-26 °C");
        guia.setFertilizacion("Mensual");
        guia.setImageUrl("https://cloudinary.com/v1/guias/foto.jpg");

        dto = GuiaAdminDTO.builder()
                .idOrquidea(1L)
                .titulo("Cuidado de la Cattleya")
                .variedad("Cattleya")
                .contenido("Texto largo...")
                .frecuenciaRiego("Cada 5-7 días")
                .luzRequerida("Indirecta brillante")
                .temperaturaIdeal("18-26 °C")
                .fertilizacion("Mensual")
                .build();

        imagen = new MockMultipartFile("imagen", "guia.jpg", "image/jpeg", "img".getBytes());
    }

    @Test
    void listarGuias_devuelveTodasLasGuiasComoDTOs() {
        when(guiaCuidadoRepository.findAll()).thenReturn(List.of(guia));

        List<GuiaCuidadoDTO> resultado = service.listarGuias();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Cuidado de la Cattleya");
        assertThat(resultado.get(0).getIdOrquidea()).isEqualTo(1L);
        assertThat(resultado.get(0).getNombreOrquidea()).isEqualTo("Cattleya Trianae");
    }

    @Test
    void crearGuia_orquideaNoExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearGuia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Orquídea no encontrada");

        verify(guiaCuidadoRepository, never()).save(any());
    }

    @Test
    void crearGuia_orquideaYaTieneGuia_lanzaExcepcion() {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(guiaCuidadoRepository.findByOrquideaId(1L)).thenReturn(Optional.of(guia));

        assertThatThrownBy(() -> service.crearGuia(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya tiene una guía");

        verify(guiaCuidadoRepository, never()).save(any());
    }

    @Test
    void crearGuia_conImagen_subeACloudinaryYGuarda() throws IOException {
        dto.setImagen(imagen);
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(guiaCuidadoRepository.findByOrquideaId(1L)).thenReturn(Optional.empty());
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("guias")))
                .thenReturn("https://cloudinary.com/nueva-guia.jpg");
        when(guiaCuidadoRepository.save(any(GuiaCuidado.class))).thenReturn(guia);

        service.crearGuia(dto);

        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("guias"));
        verify(guiaCuidadoRepository).save(any(GuiaCuidado.class));
    }

    @Test
    void actualizarGuia_noExiste_lanzaExcepcion() {
        when(guiaCuidadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarGuia(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Guía de cuidado no encontrada");
    }

    @Test
    void actualizarGuia_cambiaOrquideaQueYaTieneOtraGuia_lanzaExcepcion() {
        Orquidea otraOrquidea = new Orquidea();
        otraOrquidea.setId(2L);
        otraOrquidea.setNombre("Phalaenopsis");

        GuiaCuidado guiaDeOtra = new GuiaCuidado();
        guiaDeOtra.setId(20L);
        guiaDeOtra.setOrquidea(otraOrquidea);

        dto.setIdOrquidea(2L);

        when(guiaCuidadoRepository.findById(10L)).thenReturn(Optional.of(guia));
        when(orquideaRepository.findById(2L)).thenReturn(Optional.of(otraOrquidea));
        when(guiaCuidadoRepository.findByOrquideaId(2L)).thenReturn(Optional.of(guiaDeOtra));

        assertThatThrownBy(() -> service.actualizarGuia(10L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya tiene otra guía");
    }

    @Test
    void actualizarGuia_conImagenNueva_borraAnteriorYSubeNueva() throws IOException {
        dto.setImagen(imagen);
        when(guiaCuidadoRepository.findById(10L)).thenReturn(Optional.of(guia));
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("guias")))
                .thenReturn("https://cloudinary.com/nueva-guia.jpg");
        when(guiaCuidadoRepository.save(any(GuiaCuidado.class))).thenReturn(guia);

        service.actualizarGuia(10L, dto);

        verify(cloudinaryService).eliminarImagen("https://cloudinary.com/v1/guias/foto.jpg");
        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("guias"));
    }

    @Test
    void eliminarGuia_existe_borraImagenYRegistro() throws IOException {
        when(guiaCuidadoRepository.findById(10L)).thenReturn(Optional.of(guia));

        service.eliminarGuia(10L);

        verify(cloudinaryService).eliminarImagen(guia.getImageUrl());
        verify(guiaCuidadoRepository).delete(guia);
    }

    @Test
    void eliminarGuia_noExiste_lanzaExcepcion() {
        when(guiaCuidadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarGuia(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Guía de cuidado no encontrada");
    }
}