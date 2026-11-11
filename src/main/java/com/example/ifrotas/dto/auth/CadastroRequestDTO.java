package com.example.ifrotas.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CadastroRequestDTO {
    @NotBlank
    private String nome;

    @NotBlank
    @Email
    private String emailInstitucional;

    @NotBlank
    private String prontuario;

    @NotBlank
    private String curso;

    @NotBlank
    private String senha;

    @NotBlank
    private String tipoUsuario; // "ESTUDANTE", "CONDUTOR", "SERVIDOR", etc.

    private String cnh; // Obrigatório apenas se tipoUsuario == CONDUTOR

    public void setNome(String nome) {
        this.nome = nome != null ? nome.trim() : null;
    }

    public void setEmailInstitucional(String emailInstitucional) {
        this.emailInstitucional = emailInstitucional != null ? emailInstitucional.trim().toLowerCase() : null;
    }

    public void setProntuario(String prontuario) {
        this.prontuario = prontuario != null ? prontuario.trim().toUpperCase() : null;
    }

    public void setCurso(String curso) {
        this.curso = curso != null ? curso.trim() : null;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario != null ? tipoUsuario.trim().toUpperCase() : null;
    }

    public void setCnh(String cnh) {
        this.cnh = (cnh != null && !cnh.trim().isBlank()) ? cnh.trim() : null;
    }
}
