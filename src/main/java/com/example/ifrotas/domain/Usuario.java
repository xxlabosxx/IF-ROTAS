package com.example.ifrotas.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String emailInstitucional;
    private String prontuario;
    private String curso;
    private String senha;
    private String minibiografia;
    private String preferenciasConvivencia;

    public boolean autenticar(String senhaInformada) {
        return this.senha != null && this.senha.equals(senhaInformada);
    }

    // SOBRECARGA 1: Atualiza minibiografia e preferências de convivência
    public void atualizarPerfil(String minibiografia, String preferenciasConvivencia) {
        this.minibiografia = minibiografia;
        this.preferenciasConvivencia = preferenciasConvivencia;
    }

    // SOBRECARGA 2: Atualiza apenas a minibiografia mantendo as preferências atuais
    public void atualizarPerfil(String minibiografia) {
        this.minibiografia = minibiografia;
    }

    // Método sobrescrito (@Override) na subclasse Condutor (Polimorfismo)
    public String obterDescricaoPerfil() {
        return "Membro Acadêmico (Passageiro) - Curso: " + (this.curso != null ? this.curso : "Não informado");
    }

    public void inativarConta() {
        this.senha = null;
    }
}
