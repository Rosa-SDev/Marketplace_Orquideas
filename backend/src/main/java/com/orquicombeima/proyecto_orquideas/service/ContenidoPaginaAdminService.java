package com.orquicombeima.proyecto_orquideas.service;

import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaAdminDTO;
import com.orquicombeima.proyecto_orquideas.dto.ContenidoPaginaDTO;
import com.orquicombeima.proyecto_orquideas.model.ContenidoPagina;
import com.orquicombeima.proyecto_orquideas.model.Usuario;
import com.orquicombeima.proyecto_orquideas.repository.ContenidoPaginaRepository;
import com.orquicombeima.proyecto_orquideas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContenidoPaginaAdminService {

    private final ContenidoPaginaRepository contenidoPaginaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinaryService;

    // Metodo GET para listar todos los contenidos de pagina existentes
    public List<ContenidoPaginaDTO> listarTodo() {
        return contenidoPaginaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Metodo POST para crear un nuevo contenido de pagina
    @Transactional
    public ContenidoPaginaDTO crearContenidoPagina(ContenidoPaginaAdminDTO dto, String emailAdmin) throws IOException{
        Usuario admin =  usuarioRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        ContenidoPagina contenido =  new ContenidoPagina();
        contenido.setAdministrador(admin);
        mapearCampos(contenido, dto);

        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            String imageUrl = cloudinaryService.subirImagen(dto.getImagen(), "contenido");
            contenido.setImagenUrl(imageUrl);
        }

        contenidoPaginaRepository.save(contenido);
        return convertirADTO(contenido);
    }

    // Metodo PUT para actualizar un contenido de pagina
    @Transactional
    public ContenidoPaginaDTO actualizarContenidoPagina(long id, ContenidoPaginaAdminDTO dto) throws IOException {

        ContenidoPagina contenido = contenidoPaginaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con el id " + id));

        mapearCampos(contenido, dto);

        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            if (contenido.getImagenUrl() != null) {
                cloudinaryService.eliminarImagen(contenido.getImagenUrl());
            }

            String imagenUrl = cloudinaryService.subirImagen(dto.getImagen(), "contenido");
            contenido.setImagenUrl(imagenUrl);
        }

        contenidoPaginaRepository.save(contenido);
        return convertirADTO(contenido);
    }

    // Metodo DELETE para eliminar un contenido de pagina
    @Transactional
    public void eliminarContenidoPagina(long id) throws IOException {

        ContenidoPagina contenido = contenidoPaginaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con el id " + id));

        if (contenido.getImagenUrl() != null) {
            cloudinaryService.eliminarImagen(contenido.getImagenUrl());
        }

        contenidoPaginaRepository.delete(contenido);
    }

    // Función para mapear los campos
    private void mapearCampos(ContenidoPagina contenidoPagina, ContenidoPaginaAdminDTO dto) {
        contenidoPagina.setTipo(dto.getTipo());
        contenidoPagina.setTitulo(dto.getTitulo());
        contenidoPagina.setContenido(dto.getContenido());
        contenidoPagina.setOrden(dto.getOrden());
    }

    // Función para convertir a DTO
    private ContenidoPaginaDTO convertirADTO(ContenidoPagina contenidoPagina) {
        return ContenidoPaginaDTO.builder()
                .id(contenidoPagina.getId())
                .tipo(contenidoPagina.getTipo())
                .titulo(contenidoPagina.getTitulo())
                .contenido(contenidoPagina.getContenido())
                .orden(contenidoPagina.getOrden())
                .imagenUrl(contenidoPagina.getImagenUrl())
                .build();
    }

}
