package com.example.ifrotas.controller;

import com.example.ifrotas.domain.PublicacaoRota;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.domain.Veiculo;
import com.example.ifrotas.dto.rota.PublicacaoRotaRequestDTO;
import com.example.ifrotas.dto.rota.PublicacaoRotaResponseDTO;
import com.example.ifrotas.dto.usuario.UsuarioResponseDTO;
import com.example.ifrotas.dto.veiculo.VeiculoResponseDTO;
import com.example.ifrotas.service.PublicacaoRotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rotas")
public class PublicacaoRotaController {

    private final PublicacaoRotaService publicacaoRotaService;

    public PublicacaoRotaController(PublicacaoRotaService publicacaoRotaService) {
        this.publicacaoRotaService = publicacaoRotaService;
    }

    @PostMapping
    public ResponseEntity<PublicacaoRotaResponseDTO> publicarRota(@RequestParam Long autorId, @RequestBody @Valid PublicacaoRotaRequestDTO dto) {
        PublicacaoRota rota = publicacaoRotaService.publicarRota(autorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(rota));
    }

    @GetMapping
    public ResponseEntity<List<PublicacaoRotaResponseDTO>> listarRotas(
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String turno,
            @RequestParam(required = false) String curso) {
        List<PublicacaoRotaResponseDTO> rotas = publicacaoRotaService.buscarRotasAtivas(bairro, turno, curso).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rotas);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<PublicacaoRotaResponseDTO> inativarRota(@PathVariable Long id, @RequestParam Long autorId) {
        PublicacaoRota rota = publicacaoRotaService.inativarRota(id, autorId);
        return ResponseEntity.ok(toResponseDTO(rota));
    }

    private PublicacaoRotaResponseDTO toResponseDTO(PublicacaoRota rota) {
        PublicacaoRotaResponseDTO dto = new PublicacaoRotaResponseDTO();
        dto.setId(rota.getId());
        dto.setBairroOrigem(rota.getBairroOrigem());
        dto.setDestinoCampus(rota.getDestinoCampus());
        dto.setDiasSemana(rota.getDiasSemana());
        dto.setHorarioHabitual(rota.getHorarioHabitual());
        dto.setStatus(rota.getStatus());
        dto.setLatitudeOrigem(rota.getLatitudeOrigem());
        dto.setLongitudeOrigem(rota.getLongitudeOrigem());
        dto.setLatitudeDestino(rota.getLatitudeDestino());
        dto.setLongitudeDestino(rota.getLongitudeDestino());
        dto.setDistanciaEstimada(rota.getDistanciaEstimada());
        dto.setDuracaoEstimada(rota.getDuracaoEstimada());
        dto.setAutor(toUsuarioDTO(rota.getAutor()));
        
        if (rota.getVeiculo() != null) {
            dto.setVeiculo(toVeiculoDTO(rota.getVeiculo()));
        }
        
        return dto;
    }

    private UsuarioResponseDTO toUsuarioDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmailInstitucional(usuario.getEmailInstitucional());
        dto.setCurso(usuario.getCurso());
        dto.setProntuario(usuario.getProntuario());
        dto.setTipoUsuario(usuario instanceof com.example.ifrotas.domain.Condutor ? "CONDUTOR" : "PASSAGEIRO");
        dto.setMinibiografia(usuario.getMinibiografia());
        dto.setPreferenciasConvivencia(usuario.getPreferenciasConvivencia());
        return dto;
    }

    private VeiculoResponseDTO toVeiculoDTO(Veiculo veiculo) {
        VeiculoResponseDTO dto = new VeiculoResponseDTO();
        dto.setId(veiculo.getId());
        dto.setModelo(veiculo.getModelo());
        dto.setCor(veiculo.getCor());
        dto.setPlaca(veiculo.getPlaca());
        dto.setCapacidadePassageiros(veiculo.getCapacidadePassageiros());
        return dto;
    }
}
