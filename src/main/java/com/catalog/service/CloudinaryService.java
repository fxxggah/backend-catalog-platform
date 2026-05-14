package com.catalog.service;

import com.catalog.domain.enums.ErrorCode;
import com.catalog.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        validateImage(file);

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("folder", "catalog")
            );

            Object secureUrl = uploadResult.get("secure_url");

            if (secureUrl == null) {
                throw new BadRequestException(
                        ErrorCode.UPLOAD_FAILED,
                        "Erro ao obter URL da imagem enviada."
                );
            }

            return secureUrl.toString();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(
                    ErrorCode.UPLOAD_FAILED,
                    "Erro ao fazer upload da imagem."
            );
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.UPLOAD_FAILED,
                    "Imagem não enviada."
            );
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException(
                    ErrorCode.UPLOAD_FAILED,
                    "Imagem muito grande. O tamanho máximo permitido é 5MB."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/webp"))) {
            throw new BadRequestException(
                    ErrorCode.UPLOAD_FAILED,
                    "Formato de imagem inválido. Use JPG, PNG ou WEBP."
            );
        }
    }
}