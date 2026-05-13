package com.orquicombeima.proyecto_orquideas.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    @InjectMocks private CloudinaryService service;

    @BeforeEach
    void setUp() {
        // Cloudinary.uploader() devuelve el Uploader que vamos a mockear
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void subirImagen_devuelveUrlSeguraDeCloudinary() throws Exception {
        MultipartFile archivo = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "contenido-binario".getBytes());

        // Cloudinary.uploader().upload(byte[], Map) devuelve un Map con "secure_url" entre otras cosas
        Map<String, Object> respuestaCloudinary = new HashMap<>();
        respuestaCloudinary.put("secure_url", "https://res.cloudinary.com/demo/image/upload/v1234/orquideas/foto.jpg");
        respuestaCloudinary.put("public_id", "orquideas/foto");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(respuestaCloudinary);

        String url = service.subirImagen(archivo, "orquideas");

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1234/orquideas/foto.jpg");
    }

    @Test
    void subirImagen_pasaCarpetaCorrectaACloudinary() throws Exception {
        MultipartFile archivo = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "contenido".getBytes());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("secure_url", "https://cloudinary.com/x.jpg");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(respuesta);

        service.subirImagen(archivo, "macetas");

        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), optionsCaptor.capture());
        Map opciones = optionsCaptor.getValue();
        assertThat(opciones.get("folder")).isEqualTo("macetas");
        assertThat(opciones.get("resource_type")).isEqualTo("image");
    }

    @Test
    void eliminarImagen_extraePublicIdSimpleYLoEnviaACloudinary() throws Exception {
        // URL con versión "v1234" y carpeta "orquideas"
        // Esperado: publicId = "orquideas/foto"
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v1234/orquideas/foto.jpg";

        service.eliminarImagen(urlImagen);

        ArgumentCaptor<String> publicIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(uploader).destroy(publicIdCaptor.capture(), any(Map.class));
        assertThat(publicIdCaptor.getValue()).isEqualTo("orquideas/foto");
    }

    @Test
    void eliminarImagen_urlConCarpetaAnidada_extraePublicIdConPath() throws Exception {
        // URL con subcarpetas adicionales
        // Esperado: publicId = "guias/abril/portada"
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v9876/guias/abril/portada.png";

        service.eliminarImagen(urlImagen);

        ArgumentCaptor<String> publicIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(uploader).destroy(publicIdCaptor.capture(), any(Map.class));
        assertThat(publicIdCaptor.getValue()).isEqualTo("guias/abril/portada");
    }

    @Test
    void eliminarImagen_urlConExtensionDistinta_quitaExtension() throws Exception {
        // Verifica que el extractor del publicId siempre quita la extensión, sea jpg, png, webp, etc.
        String urlImagen = "https://res.cloudinary.com/demo/image/upload/v1/macetas/blanca.webp";

        service.eliminarImagen(urlImagen);

        ArgumentCaptor<String> publicIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(uploader).destroy(publicIdCaptor.capture(), any(Map.class));
        assertThat(publicIdCaptor.getValue()).isEqualTo("macetas/blanca");
    }
}