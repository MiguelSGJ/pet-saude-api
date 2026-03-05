package com.arboviroses.conectaDengue.Api.Controllers;

import com.arboviroses.conectaDengue.Domain.Services.Determinantes.SocialDeterminantsService;
import com.arboviroses.conectaDengue.Utils.MossoroData.NeighborhoodsMossoro;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mossoro")
@RequiredArgsConstructor
public class MossoroController {

    private final SocialDeterminantsService socialDeterminantsService;

    @GetMapping("/neighborhoods")
    public ResponseEntity<?> getMossoroNeighborhoods() {
        return ResponseEntity.ok(
                NeighborhoodsMossoro.getNeighborhoods()
        );
    }

    @GetMapping("/ubs")
    public ResponseEntity<?> getMossoroUbs() {
        return ResponseEntity.ok(
                socialDeterminantsService.getAllUbs()
        );
    }
}
