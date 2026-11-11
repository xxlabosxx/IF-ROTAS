package com.example.ifrotas.dto.conexao;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MensagemChatRequestDTO {
    @NotBlank
    private String conteudoTexto;
}
