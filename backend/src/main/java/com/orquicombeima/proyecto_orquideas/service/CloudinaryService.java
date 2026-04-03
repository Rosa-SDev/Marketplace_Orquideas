package com.orquicombeima.proyecto_orquideas.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Metodo para subir una imagen a Cloudinary
    public String subirImagen(MultipartFile archivo, String carpeta) throws IOException{
        Map resultado = cloudinary.uploader().upload(
                archivo.getBytes(),                     // Convierte el archivo a bytes para subirlo
                ObjectUtils.asMap(                      // Configura las opciones de subida
                "folder", carpeta,              // Especifica la carpeta donde se guardará la imagen
                        "resource_type", "image"        // Especifica que el recurso es una imagen
                )
        );
        return resultado.get("secure_url").toString();  // Devuelve la URL segura de la imagen subida a Cloudinary
    }

    // Metodo para eliminar una imagen de Cloudinary por su public_id
    public void eliminarImagen(String imageUrl) throws IOException {
        String publicId = extraerPublicId(imageUrl);                            // Extrae el public_id de la URL de la imagen
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());        // Elimina la imagen de Cloudinary usando el public_id
    }

    // Metodo para extraer el public_id de una imagen a partir de su URL
    private String extraerPublicId(String imageUrl) {
        String desdeUpload = imageUrl.split("/upload/")[1];                           // Obtiene la parte de la URL después de "/upload/"
        String sinVersion = desdeUpload.replaceFirst("v\\d+/", "");        // Elimina la parte de la versión
        return sinVersion.substring(0, sinVersion.lastIndexOf("."));                    // Elimina la extensión del archivo
    }
}
