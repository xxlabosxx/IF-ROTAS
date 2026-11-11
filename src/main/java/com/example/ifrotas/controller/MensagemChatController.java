package com.example.ifrotas.controller;

import com.example.ifrotas.domain.MensagemChat;
import com.example.ifrotas.dto.conexao.MensagemChatRequestDTO;
import com.example.ifrotas.dto.conexao.MensagemChatResponseDTO;
import com.example.ifrotas.service.MensagemChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conexoes/{id}/mensagens")
public class MensagemChatController {

    private final MensagemChatService mensagemChatService;

    public MensagemChatController(MensagemChatService mensagemChatService) {
        this.mensagemChatService = mensagemChatService;
    }

    @PostMapping
    public ResponseEntity<MensagemChatResponseDTO> enviarMensagem(
            @PathVariable Long id,
            @RequestParam Long remetenteId,
            @RequestBody @Valid MensagemChatRequestDTO dto) {
        MensagemChat mensagem = mensagemChatService.enviarMensagem(id, remetenteId, dto.getConteudoTexto());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(mensagem));
    }

    @GetMapping
    public ResponseEntity<List<MensagemChatResponseDTO>> listarMensagens(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        List<MensagemChatResponseDTO> mensagens = mensagemChatService.listarMensagensPorConexao(id, usuarioId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mensagens);
    }

    private MensagemChatResponseDTO toResponseDTO(MensagemChat mensagem) {
        MensagemChatResponseDTO dto = new MensagemChatResponseDTO();
        dto.setId(mensagem.getId());
        dto.setConteudoTexto(mensagem.getConteudoTexto());
        dto.setDataEnvio(mensagem.getDataEnvio());
        dto.setRemetenteId(mensagem.getRemetente().getId());
        return dto;
    }
}
