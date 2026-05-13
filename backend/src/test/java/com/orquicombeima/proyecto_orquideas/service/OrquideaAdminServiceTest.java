package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.OrquideaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.OrquideaDTO;
import com.orquicombeima.proyecto_orquideas.model.Orquidea;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrquideaAdminServiceTest {

    @Mock private OrquideaRepository orquideaRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks private OrquideaAdminService service;

    private Orquidea orquidea;
    private OrquideaAdminDTO dto;
    private MultipartFile imagen;

    @BeforeEach
    void setUp() {
        orquidea = new Orquidea();
        orquidea.setId(1L);
        orquidea.setNombre("Cattleya Trianae");
        orquidea.setPrecio(85000.0);
        orquidea.setStock(10);
        orquidea.setVariedad("Cattleya");
        orquidea.setColorFlor("Rosado");
        orquidea.setTamanio("Mediana");
        orquidea.setActivo(true);
        orquidea.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1/orquideas/foto.jpg");

        dto = OrquideaAdminDTO.builder()
                .nombre("Cattleya Trianae")
                .descripcion("Flor nacional")
                .precio(85000.0)
                .stock(10)
                .variedad("Cattleya")
                .colorFlor("Rosado")
                .tamanio("Mediana")
                .nivelCuidado("Intermedio")
                .tiempoFloracion("Primavera")
                .activo(true)
                .build();

        imagen = new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", "contenido".getBytes());
    }

    @Test
    void listarOrquideas_devuelveTodasLasOrquideasComoDTOs() {
        when(orquideaRepository.findAll()).thenReturn(List.of(orquidea));

        List<OrquideaDTO> resultado = service.listarOrquideas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Cattleya Trianae");
        assertThat(resultado.get(0).getPrecio()).isEqualTo(85000.0);
    }

    @Test
    void crearOrquidea_sinImagen_guardaSinSubirACloudinary() throws IOException {
        dto.setImagen(null);
        when(orquideaRepository.save(any(Orquidea.class))).thenReturn(orquidea);

        OrquideaDTO resultado = service.crearOrquidea(dto);

        assertThat(resultado.getNombre()).isEqualTo("Cattleya Trianae");
        verify(cloudinaryService, never()).subirImagen(any(), anyString());
        verify(orquideaRepository).save(any(Orquidea.class));
    }

    @Test
    void crearOrquidea_conImagen_subeACloudinaryYGuardaUrl() throws IOException {
        dto.setImagen(imagen);
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("orquideas")))
                .thenReturn("https://cloudinary.com/nueva.jpg");
        when(orquideaRepository.save(any(Orquidea.class))).thenReturn(orquidea);

        service.crearOrquidea(dto);

        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("orquideas"));
        verify(orquideaRepository).save(any(Orquidea.class));
    }

    @Test
    void actualizarOrquidea_idNoExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarOrquidea(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Orquídea no encontrada");

        verify(orquideaRepository, never()).save(any());
    }

    @Test
    void actualizarOrquidea_conImagenNueva_eliminaImagenAnteriorYSubeNueva() throws IOException {
        dto.setImagen(imagen);
        String urlAnterior = orquidea.getImageUrl();  // ← capturar ANTES de llamar al service
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("orquideas")))
                .thenReturn("https://cloudinary.com/nueva.jpg");
        when(orquideaRepository.save(any(Orquidea.class))).thenReturn(orquidea);

        service.actualizarOrquidea(1L, dto);

        verify(cloudinaryService).eliminarImagen(urlAnterior);  // ← usar la guardada
        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("orquideas"));
    }

    @Test
    void actualizarOrquidea_sinImagenNueva_noTocaCloudinary() throws IOException {
        dto.setImagen(null);
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(orquideaRepository.save(any(Orquidea.class))).thenReturn(orquidea);

        service.actualizarOrquidea(1L, dto);

        verify(cloudinaryService, never()).eliminarImagen(anyString());
        verify(cloudinaryService, never()).subirImagen(any(), anyString());
    }

    @Test
    void eliminarOrquidea_existe_borraImagenYRegistro() throws IOException {
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));

        service.eliminarOrquidea(1L);

        verify(cloudinaryService).eliminarImagen(orquidea.getImageUrl());
        verify(orquideaRepository).delete(orquidea);
    }

    @Test
    void eliminarOrquidea_sinImagen_soloBorraRegistro() throws IOException {
        orquidea.setImageUrl(null);
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));

        service.eliminarOrquidea(1L);

        verify(cloudinaryService, never()).eliminarImagen(anyString());
        verify(orquideaRepository).delete(orquidea);
    }

    @Test
    void eliminarOrquidea_noExiste_lanzaExcepcion() {
        when(orquideaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarOrquidea(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Orquídea no encontrada");
    }

    @Test
    void establecerActivo_alternaElEstado() {
        orquidea.setActivo(true);
        when(orquideaRepository.findById(1L)).thenReturn(Optional.of(orquidea));
        when(orquideaRepository.save(any(Orquidea.class))).thenReturn(orquidea);

        service.establecerActivo(1L);

        assertThat(orquidea.getActivo()).isFalse();
        verify(orquideaRepository).save(orquidea);
    }
}