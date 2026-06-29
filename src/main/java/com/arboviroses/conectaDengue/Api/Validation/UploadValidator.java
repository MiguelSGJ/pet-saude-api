package com.arboviroses.conectaDengue.Api.Validation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.arboviroses.conectaDengue.Api.Exceptions.ValidationException;

public final class UploadValidator {
    public static final long MAX_UPLOAD_BYTES = 1024L * 1024 * 1024;

    private UploadValidator() {
    }

    public static void validate(MultipartFile file, long maxBytes, String... allowedExtensions) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Arquivo nao pode ser vazio");
        }

        if (file.getSize() > maxBytes) {
            throw new ValidationException("Arquivo excede o tamanho maximo permitido");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.length() > 180) {
            throw new ValidationException("Nome do arquivo invalido");
        }

        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new ValidationException("Nome do arquivo invalido");
        }

        String extension = extensionOf(filename);
        Set<String> allowed = Arrays.stream(allowedExtensions)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (!allowed.contains(extension)) {
            throw new ValidationException("Tipo de arquivo invalido");
        }
    }

    private static String extensionOf(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
