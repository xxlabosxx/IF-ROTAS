package com.example.ifrotas.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Condutor extends Usuario {
    private String cnh;
    
    @OneToMany(mappedBy = "condutor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Veiculo> veiculos = new ArrayList<>();

    public void adicionarVeiculo(Veiculo v) {
        if (this.veiculos == null) {
            this.veiculos = new ArrayList<>();
        }
        this.veiculos.add(v);
        v.setCondutor(this);
    }

    public void removerVeiculo(Veiculo v) {
        if (this.veiculos != null) {
            this.veiculos.remove(v);
            v.setCondutor(null);
        }
    }

    // POLIMORFISMO DE SOBRESCRITA (@Override): Comportamento especializado para Condutor
    @Override
    public String obterDescricaoPerfil() {
        return "Condutor Colaborativo (CNH: " + this.cnh + ") - Curso: " + (this.getCurso() != null ? this.getCurso() : "Não informado");
    }

    @Override
    public void inativarConta() {
        super.inativarConta();
        if (this.veiculos != null) {
            this.veiculos.clear();
        }
    }
}
