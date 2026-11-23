package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sube y borra fotos en Cloudinary.
 *
 * <p>Las credenciales llegan SOLO por variables de entorno
 * ({@code CLOUDINARY_CLOUD_NAME}, {@code CLOUDINARY_API_KEY},
 * {@code CLOUDINARY_API_SECRET}); nunca se guardan en la base de datos ni se
 * exponen en la API. Si aún no están definidas, la app arranca igual y la
 * subida responde 503 con un mensaje claro.</p>
 */
@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;
    private final boolean configurado;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.configurado = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);

        if (configurado) {
            log.info("Cloudinary configurado (cloud_name={})", cloudName);
        } else {
            log.warn("Cloudinary NO configurado: defina CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET");
        }
    }

    /**
     * Sube una foto a la carpeta indicada y devuelve su URL pública y su public_id.
     */
    public FotoSubida subirFoto(MultipartFile archivo, String carpeta) {
        if (!configurado) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary no configurado: defina CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET");
        }
        try {
            Map<String, Object> opciones = ObjectUtils.asMap("folder", carpeta);
            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = (Map<String, Object>) cloudinary.uploader().upload(archivo.getBytes(), opciones);

            String url = String.valueOf(resultado.get("secure_url"));
            String publicId = String.valueOf(resultado.get("public_id"));
            log.info("Foto subida a Cloudinary: {}", publicId);
            return new FotoSubida(url, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen a Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Borra la foto de Cloudinary usando su public_id (mejora esfuerzo si falla: solo loguea).
     */
    public void eliminarFoto(String publicId) {
        if (!configurado || publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Foto borrada de Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("No se pudo borrar {} de Cloudinary: {}", publicId, e.getMessage());
        }
    }

    /**
     * Resultado de una subida: URL pública (para pintar en Android) y public_id (para borrar).
     */
    public record FotoSubida(String url, String publicId) {
    }
}
