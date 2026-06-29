package com.arboviroses.conectaDengue.Api.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import com.arboviroses.conectaDengue.Api.DTO.AguaDTO;
import com.arboviroses.conectaDengue.Api.DTO.EducacaoDTO;
import com.arboviroses.conectaDengue.Api.DTO.EscoamentoDTO;
import com.arboviroses.conectaDengue.Api.DTO.LixoDTO;
import com.arboviroses.conectaDengue.Api.DTO.RendaDTO;
import com.arboviroses.conectaDengue.Api.DTO.TratamentoDTO;
import com.arboviroses.conectaDengue.Api.Validation.InputPatterns;
import com.arboviroses.conectaDengue.Api.Validation.UploadValidator;
import com.arboviroses.conectaDengue.Domain.Entities.Determinantes;
import com.arboviroses.conectaDengue.Domain.Services.Determinantes.SocialDeterminantsService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/determinantes")
@RequiredArgsConstructor
@Validated
public class SocialDeterminantsController {

    private final SocialDeterminantsService service;

    @PostMapping("/upload")
    public void uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ano") @Min(2000) @Max(2100) Integer ano) throws Exception {
        UploadValidator.validate(file, UploadValidator.MAX_UPLOAD_BYTES, "xlsx");
        service.saveSocialDeterminants(file, ano);
    }

    @GetMapping
    public List<Determinantes> listAll(
        @RequestParam(required = false) @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs,
        @RequestParam(required = false) @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro
    ) {
        if (ubs != null)
            return service.listAllByUbs(ubs);

        if (bairro != null)
            return service.listAllByBairro(bairro);
        
        return service.listAll();
    }

    @GetMapping("/agua")
    public List<AguaDTO> listAgua() {
        return service.listAgua();
    }

    @GetMapping("/tratamento")
    public List<TratamentoDTO> listTratamento() {
        return service.listTratamento();
    }

    @GetMapping("/escoamento")
    public List<EscoamentoDTO> listEscoamento() {
        return service.listEscoamento();
    }

    @GetMapping("/lixo")
    public List<LixoDTO> listLixo() {
        return service.listLixo();
    }

    @GetMapping("/renda")
    public List<RendaDTO> listRenda() {
        return service.listRenda();
    }

    @GetMapping("/educacao")
    public List<EducacaoDTO> listEducacao() {
        return service.listEducacao();
    }

    // Água por UBS/Bairro
    @GetMapping("/agua/ubs/{ubs}")
    public List<AguaDTO> listAguaByUbs(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs) {
        return service.listAguaByUbs(ubs);
    }

    @GetMapping("/agua/bairro/{bairro}")
    public List<AguaDTO> aggregateAguaByBairro(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro) {
        return service.aggregateAguaByBairro(bairro);
    }

    // Tratamento por UBS/Bairro
    @GetMapping("/tratamento/ubs/{ubs}")
    public List<TratamentoDTO> listTratamentoByUbs(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs) {
        return service.listTratamentoByUbs(ubs);
    }

    @GetMapping("/tratamento/bairro/{bairro}")
    public List<TratamentoDTO> aggregateTratamentoByBairro(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro) {
        return service.aggregateTratamentoByBairro(bairro);
    }

    // Escoamento por UBS/Bairro
    @GetMapping("/escoamento/ubs/{ubs}")
    public List<EscoamentoDTO> listEscoamentoByUbs(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs) {
        return service.listEscoamentoByUbs(ubs);
    }

    @GetMapping("/escoamento/bairro/{bairro}")
    public List<EscoamentoDTO> aggregateEscoamentoByBairro(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro) {
        return service.aggregateEscoamentoByBairro(bairro);
    }

    // Lixo por UBS/Bairro
    @GetMapping("/lixo/ubs/{ubs}")
    public List<LixoDTO> listLixoByUbs(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs) {
        return service.listLixoByUbs(ubs);
    }

    @GetMapping("/lixo/bairro/{bairro}")
    public List<LixoDTO> aggregateLixoByBairro(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro) {
        return service.aggregateLixoByBairro(bairro);
    }

    // Renda por UBS/Bairro
    @GetMapping("/renda/ubs/{ubs}")
    public List<RendaDTO> listRendaByUbs(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs) {
        return service.listRendaByUbs(ubs);
    }

    @GetMapping("/renda/bairro/{bairro}")
    public List<RendaDTO> aggregateRendaByBairro(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro) {
        return service.aggregateRendaByBairro(bairro);
    }

    // Educacao por UBS/Bairro
    @GetMapping("/educacao/ubs/{ubs}")
    public List<EducacaoDTO> listEducacaoByUbs(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String ubs) {
        return service.listEducacaoByUbs(ubs);
    }

    @GetMapping("/educacao/bairro/{bairro}")
    public List<EducacaoDTO> aggregateEducacaoByBairro(@PathVariable @Size(max = 120) @Pattern(regexp = InputPatterns.SAFE_TEXT) String bairro) {
        return service.aggregateEducacaoByBairro(bairro);
    }
}
