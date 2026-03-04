package com.arboviroses.conectaDengue.Api.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arboviroses.conectaDengue.Api.DTO.AguaDTO;
import com.arboviroses.conectaDengue.Api.DTO.EducacaoDTO;
import com.arboviroses.conectaDengue.Api.DTO.EscoamentoDTO;
import com.arboviroses.conectaDengue.Api.DTO.LixoDTO;
import com.arboviroses.conectaDengue.Api.DTO.RendaDTO;
import com.arboviroses.conectaDengue.Api.DTO.TratamentoDTO;
import com.arboviroses.conectaDengue.Domain.Entities.Determinantes;
import com.arboviroses.conectaDengue.Domain.Services.Determinantes.SocialDeterminantsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/determinantes")
@RequiredArgsConstructor
public class SocialDeterminantsController {

    private final SocialDeterminantsService service;

    @PostMapping("/upload")
    public void uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ano") Integer ano) {
        try {
            service.saveSocialDeterminants(file, ano);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping
    public List<Determinantes> listAll(
        @RequestParam(required = false) String ubs,
        @RequestParam(required = false) String bairro
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
    public List<AguaDTO> listAguaByUbs(@PathVariable String ubs) {
        return service.listAguaByUbs(ubs);
    }

    @GetMapping("/agua/bairro/{bairro}")
    public List<AguaDTO> aggregateAguaByBairro(@PathVariable String bairro) {
        return service.aggregateAguaByBairro(bairro);
    }

    // Tratamento por UBS/Bairro
    @GetMapping("/tratamento/ubs/{ubs}")
    public List<TratamentoDTO> listTratamentoByUbs(@PathVariable String ubs) {
        return service.listTratamentoByUbs(ubs);
    }

    @GetMapping("/tratamento/bairro/{bairro}")
    public List<TratamentoDTO> aggregateTratamentoByBairro(@PathVariable String bairro) {
        return service.aggregateTratamentoByBairro(bairro);
    }

    // Escoamento por UBS/Bairro
    @GetMapping("/escoamento/ubs/{ubs}")
    public List<EscoamentoDTO> listEscoamentoByUbs(@PathVariable String ubs) {
        return service.listEscoamentoByUbs(ubs);
    }

    @GetMapping("/escoamento/bairro/{bairro}")
    public List<EscoamentoDTO> aggregateEscoamentoByBairro(@PathVariable String bairro) {
        return service.aggregateEscoamentoByBairro(bairro);
    }

    // Lixo por UBS/Bairro
    @GetMapping("/lixo/ubs/{ubs}")
    public List<LixoDTO> listLixoByUbs(@PathVariable String ubs) {
        return service.listLixoByUbs(ubs);
    }

    @GetMapping("/lixo/bairro/{bairro}")
    public List<LixoDTO> aggregateLixoByBairro(@PathVariable String bairro) {
        return service.aggregateLixoByBairro(bairro);
    }

    // Renda por UBS/Bairro
    @GetMapping("/renda/ubs/{ubs}")
    public List<RendaDTO> listRendaByUbs(@PathVariable String ubs) {
        return service.listRendaByUbs(ubs);
    }

    @GetMapping("/renda/bairro/{bairro}")
    public List<RendaDTO> aggregateRendaByBairro(@PathVariable String bairro) {
        return service.aggregateRendaByBairro(bairro);
    }

    // Educacao por UBS/Bairro
    @GetMapping("/educacao/ubs/{ubs}")
    public List<EducacaoDTO> listEducacaoByUbs(@PathVariable String ubs) {
        return service.listEducacaoByUbs(ubs);
    }

    @GetMapping("/educacao/bairro/{bairro}")
    public List<EducacaoDTO> aggregateEducacaoByBairro(@PathVariable String bairro) {
        return service.aggregateEducacaoByBairro(bairro);
    }
}
