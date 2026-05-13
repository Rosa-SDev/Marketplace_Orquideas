package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.MacetaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
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
class MacetaAdminServiceTest {

    @Mock private MacetaRepository macetaRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks private MacetaAdminService service;

    private Maceta maceta;
    private MacetaAdminDTO dto;
    private MultipartFile imagen;

    @BeforeEach
    void setUp() {
        maceta = new Maceta();
        maceta.setId(1L);
        maceta.setNombre("Maceta Cerámica");
        maceta.setDescripcion("Para orquídea mediana");
        maceta.setPrecio(35000.0);
        maceta.setStock(20);
        maceta.setActivo(true);
        maceta.setImageUrl("https://res.cloudinary.com/v1/macetas/foto.jpg");
        maceta.setMaterial("Cerámica");
        maceta.setDiametroCm(15);
        maceta.setColor("Blanco");
        maceta.setEstilo("Moderno");

        dto = MacetaAdminDTO.builder()
                .nombre("Maceta Cerámica")
                .descripcion("Para orquídea mediana")
                .precio(35000.0)
                .stock(20)
                .material("Cerámica")
                .diametroCm(15)
                .color("Blanco")
                .estilo("Moderno")
                .activo(true)
                .build();

        imagen = new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", "contenido".getBytes());
    }

    @Test
    void listarMacetas_devuelveTodasComoDTOs() {
        when(macetaRepository.findAll()).thenReturn(List.of(maceta));

        List<MacetaDTO> resultado = service.listarMacetas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Maceta Cerámica");
        assertThat(resultado.get(0).getMaterial()).isEqualTo("Cerámica");
    }

    @Test
    void crearMaceta_sinImagen_guardaSinTocarCloudinary() throws IOException {
        dto.setImagen(null);
        when(macetaRepository.save(any(Maceta.class))).thenReturn(maceta);

        service.crearMaceta(dto);

        verify(cloudinaryService, never()).subirImagen(any(), anyString());
        verify(macetaRepository).save(any(Maceta.class));
    }

    @Test
    void crearMaceta_conImagen_subeACloudinaryYGuarda() throws IOException {
        dto.setImagen(imagen);
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("macetas")))
                .thenReturn("https://cloudinary.com/nueva.jpg");
        when(macetaRepository.save(any(Maceta.class))).thenReturn(maceta);

        service.crearMaceta(dto);

        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("macetas"));
        verify(macetaRepository).save(any(Maceta.class));
    }

    @Test
    void actualizarMaceta_idNoExiste_lanzaExcepcion() {
        when(macetaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarMaceta(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maceta no encontrada");

        verify(macetaRepository, never()).save(any());
    }

    @Test
    void actualizarMaceta_conImagenNueva_eliminaAnteriorYSubeNueva() throws IOException {
        dto.setImagen(imagen);
        String urlAnterior = maceta.getImageUrl();
        when(macetaRepository.findById(1L)).thenReturn(Optional.of(maceta));
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("macetas")))
                .thenReturn("https://cloudinary.com/nueva.jpg");
        when(macetaRepository.save(any(Maceta.class))).thenReturn(maceta);

        service.actualizarMaceta(1L, dto);

        verify(cloudinaryService).eliminarImagen(urlAnterior);
        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("macetas"));
    }

    @Test
    void actualizarMaceta_sinImagenNueva_noTocaCloudinary() throws IOException {
        dto.setImagen(null);
        when(macetaRepository.findById(1L)).thenReturn(Optional.of(maceta));
        when(macetaRepository.save(any(Maceta.class))).thenReturn(maceta);

        service.actualizarMaceta(1L, dto);

        verify(cloudinaryService, never()).eliminarImagen(anyString());
        verify(cloudinaryService, never()).subirImagen(any(), anyString());
    }

    @Test
    void eliminarMaceta_existe_borraImagenYRegistro() throws IOException {
        when(macetaRepository.findById(1L)).thenReturn(Optional.of(maceta));

        service.eliminarMaceta(1L);

        verify(cloudinaryService).eliminarImagen(maceta.getImageUrl());
        verify(macetaRepository).delete(maceta);
    }

    @Test
    void eliminarMaceta_sinImagen_soloBorraRegistro() throws IOException {
        maceta.setImageUrl(null);
        when(macetaRepository.findById(1L)).thenReturn(Optional.of(maceta));

        service.eliminarMaceta(1L);

        verify(cloudinaryService, never()).eliminarImagen(anyString());
        verify(macetaRepository).delete(maceta);
    }

    @Test
    void eliminarMaceta_noExiste_lanzaExcepcion() {
        when(macetaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarMaceta(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maceta no encontrada");
    }

    @Test
    void establecerActivo_alternaElEstado() {
        maceta.setActivo(true);
        when(macetaRepository.findById(1L)).thenReturn(Optional.of(maceta));
        when(macetaRepository.save(any(Maceta.class))).thenReturn(maceta);

        service.establecerActivo(1L);

        assertThat(maceta.getActivo()).isFalse();
        verify(macetaRepository).save(maceta);
    }
}