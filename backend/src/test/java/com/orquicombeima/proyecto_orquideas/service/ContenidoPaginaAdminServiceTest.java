package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.model.ContenidoPagina;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.model.enums.Rol;
import com.orquicombeima.proyecto_orquideas.repository.ContenidoPaginaRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
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
class ContenidoPaginaAdminServiceTest {

    @Mock private ContenidoPaginaRepository contenidoPaginaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks private ContenidoPaginaAdminService service;

    private static final String EMAIL_ADMIN = "admin@test.com";

    private Usuario admin;
    private ContenidoPagina contenido;
    private ContenidoPaginaAdminDTO dto;
    private MultipartFile imagen;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(1L);
        admin.setEmail(EMAIL_ADMIN);
        admin.setNombre("Admin");
        admin.setRol(Rol.ADMINISTRADOR);

        contenido = new ContenidoPagina();
        contenido.setId(10L);
        contenido.setAdministrador(admin);
        contenido.setTipo("banner");
        contenido.setTitulo("Promo");
        contenido.setContenido("Texto");
        contenido.setOrden(1);
        contenido.setImagenUrl("https://cloudinary.com/v1/contenido/foto.jpg");

        dto = ContenidoPaginaAdminDTO.builder()
                .tipo("banner")
                .titulo("Promo")
                .contenido("Texto")
                .orden(1)
                .build();

        imagen = new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", "img".getBytes());
    }

    @Test
    void listarTodo_devuelveTodosLosContenidos() {
        when(contenidoPaginaRepository.findAll()).thenReturn(List.of(contenido));

        List<ContenidoPaginaDTO> resultado = service.listarTodo();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo("banner");
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Promo");
    }

    @Test
    void crearContenidoPagina_adminNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail(EMAIL_ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearContenidoPagina(dto, EMAIL_ADMIN))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin no encontrado");

        verify(contenidoPaginaRepository, never()).save(any());
    }

    @Test
    void crearContenidoPagina_sinImagen_guardaSinTocarCloudinary() throws IOException {
        dto.setImagen(null);
        when(usuarioRepository.findByEmail(EMAIL_ADMIN)).thenReturn(Optional.of(admin));
        when(contenidoPaginaRepository.save(any(ContenidoPagina.class))).thenReturn(contenido);

        service.crearContenidoPagina(dto, EMAIL_ADMIN);

        verify(cloudinaryService, never()).subirImagen(any(), anyString());
        verify(contenidoPaginaRepository).save(any(ContenidoPagina.class));
    }

    @Test
    void crearContenidoPagina_conImagen_subeACloudinaryEnCarpetaContenido() throws IOException {
        dto.setImagen(imagen);
        when(usuarioRepository.findByEmail(EMAIL_ADMIN)).thenReturn(Optional.of(admin));
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("contenido")))
                .thenReturn("https://cloudinary.com/nuevo.jpg");
        when(contenidoPaginaRepository.save(any(ContenidoPagina.class))).thenReturn(contenido);

        service.crearContenidoPagina(dto, EMAIL_ADMIN);

        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("contenido"));
    }

    @Test
    void actualizarContenidoPagina_idNoExiste_lanzaExcepcion() throws IOException {
        when(contenidoPaginaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarContenidoPagina(99L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contenido no encontrado");
    }

    @Test
    void actualizarContenidoPagina_conImagenNueva_eliminaAnteriorYSubeNueva() throws IOException {
        dto.setImagen(imagen);
        String urlAnterior = contenido.getImagenUrl();
        when(contenidoPaginaRepository.findById(10L)).thenReturn(Optional.of(contenido));
        when(cloudinaryService.subirImagen(any(MultipartFile.class), eq("contenido")))
                .thenReturn("https://cloudinary.com/nuevo.jpg");
        when(contenidoPaginaRepository.save(any(ContenidoPagina.class))).thenReturn(contenido);

        service.actualizarContenidoPagina(10L, dto);

        verify(cloudinaryService).eliminarImagen(urlAnterior);
        verify(cloudinaryService).subirImagen(any(MultipartFile.class), eq("contenido"));
    }

    @Test
    void eliminarContenidoPagina_existe_borraImagenYRegistro() throws IOException {
        when(contenidoPaginaRepository.findById(10L)).thenReturn(Optional.of(contenido));

        service.eliminarContenidoPagina(10L);

        verify(cloudinaryService).eliminarImagen(contenido.getImagenUrl());
        verify(contenidoPaginaRepository).delete(contenido);
    }

    @Test
    void eliminarContenidoPagina_noExiste_lanzaExcepcion() {
        when(contenidoPaginaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarContenidoPagina(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contenido no encontrado");
    }
}