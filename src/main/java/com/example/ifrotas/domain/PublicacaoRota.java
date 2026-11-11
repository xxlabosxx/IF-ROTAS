package com.example.ifrotas.domain;

import com.example.ifrotas.exception.EstadoInvalidoException;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class PublicacaoRota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bairroOrigem;
    private String destinoCampus;
    
    private Double latitudeOrigem;
    private Double longitudeOrigem;
    private Double latitudeDestino;
    private Double longitudeDestino;
    private String distanciaEstimada;
    private String duracaoEstimada;
    
    @ElementCollection
    private List<String> diasSemana = new ArrayList<>();
    
    private LocalTime horarioHabitual;
    private String status;
    
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;
    
    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @OneToMany(mappedBy = "publicacaoRota", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Conexao> conexoes = new ArrayList<>();

    public void publicar() {
        this.status = "ATIVO";
    }

    public void inativar() {
        if ("INATIVO".equals(this.status)) {
            throw new EstadoInvalidoException("Esta rota já encontra-se inativa.");
        }
        this.status = "INATIVO";
    }

    // SOBRECARGA 1: Edita origem e destino do trajeto
    public void editarTrajeto(String bairroOrigem, String destinoCampus) {
        this.bairroOrigem = bairroOrigem;
        this.destinoCampus = destinoCampus;
    }

    // SOBRECARGA 2: Edita origem, destino e também o horário habitual
    public void editarTrajeto(String bairroOrigem, String destinoCampus, LocalTime horarioHabitual) {
        this.bairroOrigem = bairroOrigem;
        this.destinoCampus = destinoCampus;
        this.horarioHabitual = horarioHabitual;
    }
}
