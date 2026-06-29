package com.arboviroses.conectaDengue.Api.Controllers;

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

@RestController
@RequestMapping("/api/lira")
@RequiredArgsConstructor
@Validated
public class LiraController {

    private final LiraService liraService;

    @PostMapping("/upload")
    public ResponseEntity<List<Lira>> uploadLiraFile(@RequestParam("file") MultipartFile file, 
                                                     @RequestParam("ano") @Min(2000) @Max(2100) Integer ano,
                                                     @RequestParam("liraNumber") @Min(1) @Max(4) Integer liraNumber) {
        UploadValidator.validate(file, UploadValidator.MAX_UPLOAD_BYTES, "xlsx");
        try {
            List<Lira> savedData = liraService.saveLiraData(file, ano, liraNumber);
            return ResponseEntity.ok(savedData);
        } catch (IOException e) {
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
                                                           @RequestParam("liraNumber") @Min(1) @Max(4) Integer liraNumber) {
        List<Lira> liraData = liraService.getLiraByAnoAndNumber(ano, liraNumber);
        return ResponseEntity.ok(liraData);
    }
}
