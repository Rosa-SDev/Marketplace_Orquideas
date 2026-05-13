package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.model.ContenidoPagina;
import com.orquicombeima.proyecto_orquideas.repository.ContenidoPaginaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContenidoPaginaServiceTest {

    @Mock private ContenidoPaginaRepository repository;

    @InjectMocks private ContenidoPaginaService service;

    private ContenidoPagina banner;
    private ContenidoPagina hero;

    @BeforeEach
    void setUp() {
        banner = new ContenidoPagina();
        banner.setId(1L);
        banner.setTipo("banner");
        banner.setTitulo("Promo de mayo");
        banner.setContenido("20% de descuento en orquídeas");
        banner.setImagenUrl("https://cdn/banner.jpg");
        banner.setOrden(1);

        hero = new ContenidoPagina();
        hero.setId(2L);
        hero.setTipo("hero");
        hero.setTitulo("Bienvenida");
        hero.setContenido("Las mejores orquídeas del Tolima");
        hero.setImagenUrl("https://cdn/hero.jpg");
        hero.setOrden(0);
    }

    @Test
    void obtenerTodoElContenido_devuelveListaDeDTOs() {
        when(repository.findAll()).thenReturn(List.of(banner, hero));

        List<ContenidoPaginaDTO> resultado = service.obtenerTodoElContenido();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getTipo()).isEqualTo("banner");
        assertThat(resultado.get(1).getTipo()).isEqualTo("hero");
    }

    @Test
    void obtenerTodoElContenido_sinResultados_devuelveListaVacia() {
        when(repository.findAll()).thenReturn(List.of());

        List<ContenidoPaginaDTO> resultado = service.obtenerTodoElContenido();

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerContenidoPorTipo_devuelveSoloDelTipoPedido() {
        when(repository.findByTipoOrderByOrdenAsc("banner")).thenReturn(List.of(banner));

        List<ContenidoPaginaDTO> resultado = service.obtenerContenidoPorTipo("banner");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo("banner");
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Promo de mayo");
        assertThat(resultado.get(0).getOrden()).isEqualTo(1);
    }

    @Test
    void obtenerContenidoPorTipo_tipoInexistente_devuelveListaVacia() {
        when(repository.findByTipoOrderByOrdenAsc("inexistente")).thenReturn(List.of());

        List<ContenidoPaginaDTO> resultado = service.obtenerContenidoPorTipo("inexistente");

        assertThat(resultado).isEmpty();
    }
}