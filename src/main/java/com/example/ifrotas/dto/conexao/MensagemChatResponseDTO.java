package com.example.ifrotas.dto.conexao;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MensagemChatResponseDTO {
    private Long id;
    private String conteudoTexto;
    private LocalDateTime dataEnvio;
    private Long remetenteId;
}
