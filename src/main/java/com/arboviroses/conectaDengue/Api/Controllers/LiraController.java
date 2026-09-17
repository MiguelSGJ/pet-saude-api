package com.arboviroses.conectaDengue.Api.Controllers;

import com.arboviroses.conectaDengue.Api.DTO.response.LiraDisponibilidadeResponse;
import com.arboviroses.conectaDengue.Api.DTO.response.LiraParametrosResponse;
import com.arboviroses.conectaDengue.Domain.Entities.Lira.Lira;
import com.arboviroses.conectaDengue.Domain.Services.Lira.LiraService;
import com.arboviroses.conectaDengue.Api.Validation.UploadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;

@RestController
@RequestMapping("/api/lira")
@RequiredArgsConstructor
@Validated
public class LiraController {

    private final LiraService liraService;

    @PostMapping("/upload")
    public ResponseEntity<List<Lira>> uploadLiraFile(@RequestParam("file") MultipartFile file, 
                                                     @RequestParam("ano") @Min(2000) @Max(2100) Integer ano,
                                                     @RequestParam("liraNumber") @Min(1) @Max(6) Integer liraNumber,
                                                     @RequestParam(value = "limiteAlerta", defaultValue = "1.0")
                                                     @DecimalMin("0.0") Double limiteAlerta,
                                                     @RequestParam(value = "limiteRisco", defaultValue = "4.0")
                                                     @DecimalMin("0.0") Double limiteRisco) {
        UploadValidator.validate(file, UploadValidator.MAX_UPLOAD_BYTES, "xlsx");
        try {
            List<Lira> savedData = liraService.saveLiraData(
                    file, ano, liraNumber, limiteAlerta, limiteRisco
            );
            return ResponseEntity.ok(savedData);
        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Lira>> getLiraByAno(@RequestParam("ano") @Min(2000) @Max(2100) Integer ano) {
        List<Lira> liraData = liraService.getLiraByAno(ano);
        return ResponseEntity.ok(liraData);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Lira>> getLiraByAnoAndNumber(@RequestParam("ano") @Min(2000) @Max(2100) Integer ano,
                                                           @RequestParam("liraNumber") @Min(1) @Max(6) Integer liraNumber) {
        List<Lira> liraData = liraService.getLiraByAnoAndNumber(ano, liraNumber);
        return ResponseEntity.ok(liraData);
    }

    @GetMapping("/available")
    public ResponseEntity<List<LiraDisponibilidadeResponse>> getDisponibilidade() {
        return ResponseEntity.ok(liraService.getDisponibilidade());
    }

    @GetMapping("/parametros")
    public ResponseEntity<LiraParametrosResponse> getParametros(
            @RequestParam("ano") @Min(2000) @Max(2100) Integer ano,
            @RequestParam("liraNumber") @Min(1) @Max(6) Integer liraNumber
    ) {
        return ResponseEntity.ok(liraService.getParametros(ano, liraNumber));
    }

    @GetMapping("/parametros/ano")
    public ResponseEntity<List<LiraParametrosResponse>> getParametrosPorAno(
            @RequestParam("ano") @Min(2000) @Max(2100) Integer ano
    ) {
        return ResponseEntity.ok(liraService.getParametrosPorAno(ano));
    }
}
