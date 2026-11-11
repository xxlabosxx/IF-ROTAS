package com.example.ifrotas.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String modelo;
    private String cor;
    private String placa;
    private Integer capacidadePassageiros;
    
    @ManyToOne
    @JoinColumn(name = "condutor_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Condutor condutor;

    public void atualizarDados(String modelo, String cor, Integer capacidadePassageiros) {
        this.modelo = modelo;
        this.cor = cor;
        this.capacidadePassageiros = capacidadePassageiros;
    }

    public boolean verificarCapacidade(Integer passageirosDesejados) {
        return this.capacidadePassageiros != null && passageirosDesejados <= this.capacidadePassageiros;
    }
}
