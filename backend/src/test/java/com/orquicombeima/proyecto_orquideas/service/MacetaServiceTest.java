package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.MacetaDTO;
import com.orquicombeima.proyecto_orquideas.model.Maceta;
import com.orquicombeima.proyecto_orquideas.repository.MacetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MacetaServiceTest {

    @Mock private MacetaRepository macetaRepository;

    @InjectMocks private MacetaService service;

    @Test
    void obtenerMacetasActivas_devuelveSoloActivasComoDTOs() {
        Maceta m1 = new Maceta();
        m1.setId(1L);
        m1.setNombre("Maceta Cerámica");
        m1.setPrecio(25000.0);
        m1.setStock(15);
        m1.setMaterial("Cerámica");
        m1.setDiametroCm(15.0);
        m1.setColor("Blanco");
        m1.setEstilo("Moderno");
        m1.setActivo(true);
        m1.setImageUrl("https://cloudinary.com/m1.jpg");

        Maceta m2 = new Maceta();
        m2.setId(2L);
        m2.setNombre("Maceta Barro");
        m2.setPrecio(15000.0);
        m2.setStock(20);
        m2.setMaterial("Barro");
        m2.setDiametroCm(12.0);
        m2.setColor("Terracota");
        m2.setEstilo("Rústico");
        m2.setActivo(true);

        when(macetaRepository.findByActivoTrue()).thenReturn(List.of(m1, m2));

        List<MacetaDTO> resultado = service.obtenerMacetasActivas();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Maceta Cerámica");
        assertThat(resultado.get(0).getMaterial()).isEqualTo("Cerámica");
        assertThat(resultado.get(0).getDiametroCm()).isEqualTo(15.0);
        assertThat(resultado.get(1).getNombre()).isEqualTo("Maceta Barro");
    }

    @Test
    void obtenerMacetasActivas_sinResultados_devuelveListaVacia() {
        when(macetaRepository.findByActivoTrue()).thenReturn(List.of());

        List<MacetaDTO> resultado = service.obtenerMacetasActivas();

        assertThat(resultado).isEmpty();
    }
}