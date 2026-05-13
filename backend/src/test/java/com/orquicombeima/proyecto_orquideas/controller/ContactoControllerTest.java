package com.orquicombeima.proyecto_orquideas.controller;

import com.orquicombeima.proyecto_orquideas.dto.ContactoDTO;
import com.orquicombeima.proyecto_orquideas.security.JwtService;
import com.orquicombeima.proyecto_orquideas.service.ContactoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContactoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactoControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ContactoService service;
    @MockitoBean private JwtService jwtService;

    @Test
    void GET_contacto_devuelve200ConDTOCompleto() throws Exception {
        ContactoDTO dto = ContactoDTO.builder()
                .numero("573001234567")
                .mensaje("Hola, quiero información")
                .urlWhatsapp("https://wa.me/573001234567?text=Hola")
                .build();
        when(service.obtenerContacto()).thenReturn(dto);

        mockMvc.perform(get("/api/contacto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("573001234567"))
                .andExpect(jsonPath("$.mensaje").value("Hola, quiero información"))
                .andExpect(jsonPath("$.urlWhatsapp").value("https://wa.me/573001234567?text=Hola"));
    }
}