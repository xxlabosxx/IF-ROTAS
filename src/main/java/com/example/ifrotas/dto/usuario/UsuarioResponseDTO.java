package com.example.ifrotas.dto.usuario;

import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String emailInstitucional;
    private String curso;
    private String prontuario;
    private String tipoUsuario;
    private String minibiografia;
    private String preferenciasConvivencia;
}
