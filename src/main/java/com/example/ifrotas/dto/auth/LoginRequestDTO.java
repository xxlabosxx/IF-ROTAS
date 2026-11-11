package com.example.ifrotas.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank
    private String identificador; // Pode ser emailInstitucional ou prontuario

    @NotBlank
    private String senha;

    public void setIdentificador(String identificador) {
        this.identificador = identificador != null ? identificador.trim() : null;
    }
}
