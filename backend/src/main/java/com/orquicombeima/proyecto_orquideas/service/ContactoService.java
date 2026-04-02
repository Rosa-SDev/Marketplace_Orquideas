package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.ContactoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ContactoService {
    @Value("${contacto.whatsapp.numero}")
    private String numero;

    @Value("${contacto.whatsapp.mensaje}")
    private String mensaje;

    // Metodo para obtener el contacto formateado con el mensaje y el numero de whatsapp
    public ContactoDTO obtenerContacto()  {
        String urlWhatsapp = "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

        return ContactoDTO.builder()
                .numero(numero)
                .mensaje(mensaje)
                .urlWhatsapp(urlWhatsapp)
                .build();
    }
}
