package com.example.ifrotas.controller;

import com.example.ifrotas.domain.Conexao;
import com.example.ifrotas.dto.conexao.ConexaoResponseDTO;
import com.example.ifrotas.service.ConexaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ConexaoController {

    private final ConexaoService conexaoService;

    public ConexaoController(ConexaoService conexaoService) {
        this.conexaoService = conexaoService;
    }

    @GetMapping("/conexoes")
    public ResponseEntity<java.util.List<ConexaoResponseDTO>> listarConexoes(@RequestParam Long usuarioId) {
        java.util.List<ConexaoResponseDTO> conexoes = conexaoService.listarConexoesPorUsuario(usuarioId).stream()
                .map(this::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(conexoes);
    }

    @PostMapping("/rotas/{id}/conectar")
    public ResponseEntity<ConexaoResponseDTO> solicitarConexao(@PathVariable Long id, @RequestParam Long usuarioInteressadoId) {
        Conexao conexao = conexaoService.solicitarConexao(id, usuarioInteressadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(conexao));
    }

    @PatchMapping("/conexoes/{id}/aceitar")
    public ResponseEntity<ConexaoResponseDTO> aceitarConexao(@PathVariable Long id, @RequestParam Long autorId) {
        Conexao conexao = conexaoService.aceitarConexao(id, autorId);
        return ResponseEntity.ok(toResponseDTO(conexao));
    }

    @PatchMapping("/conexoes/{id}/recusar")
    public ResponseEntity<ConexaoResponseDTO> recusarConexao(@PathVariable Long id, @RequestParam Long autorId) {
        Conexao conexao = conexaoService.recusarConexao(id, autorId);
        return ResponseEntity.ok(toResponseDTO(conexao));
    }

    private ConexaoResponseDTO toResponseDTO(Conexao conexao) {
        ConexaoResponseDTO dto = new ConexaoResponseDTO();
        dto.setId(conexao.getId());
        dto.setDataSolicitacao(conexao.getDataSolicitacao());
        dto.setStatus(conexao.getStatus());
        if (conexao.getPublicacaoRota() != null) {
            dto.setPublicacaoRotaId(conexao.getPublicacaoRota().getId());
            dto.setRotaOrigem(conexao.getPublicacaoRota().getBairroOrigem());
            dto.setRotaDestino(conexao.getPublicacaoRota().getDestinoCampus());
            dto.setLatitudeOrigem(conexao.getPublicacaoRota().getLatitudeOrigem());
            dto.setLongitudeOrigem(conexao.getPublicacaoRota().getLongitudeOrigem());
            dto.setLatitudeDestino(conexao.getPublicacaoRota().getLatitudeDestino());
            dto.setLongitudeDestino(conexao.getPublicacaoRota().getLongitudeDestino());
            dto.setDistanciaEstimada(conexao.getPublicacaoRota().getDistanciaEstimada());
            dto.setDuracaoEstimada(conexao.getPublicacaoRota().getDuracaoEstimada());
            if (conexao.getPublicacaoRota().getAutor() != null) {
                dto.setAutorId(conexao.getPublicacaoRota().getAutor().getId());
                dto.setAutorNome(conexao.getPublicacaoRota().getAutor().getNome());
            }
        }
        if (conexao.getInteressado() != null) {
            dto.setInteressadoId(conexao.getInteressado().getId());
            dto.setInteressadoNome(conexao.getInteressado().getNome());
        }
        return dto;
    }
}
