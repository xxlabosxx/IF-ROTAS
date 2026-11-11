package com.example.ifrotas.domain;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Conexao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime dataSolicitacao;
    private String status; // SOLICITADO, CONECTADO
    
    @ManyToOne
    @JoinColumn(name = "publicacao_rota_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PublicacaoRota publicacaoRota;
    
    @ManyToOne
    @JoinColumn(name = "interessado_id")
    private Usuario interessado;

    @OneToMany(mappedBy = "conexao", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MensagemChat> mensagens = new ArrayList<>();

    public void aceitarConexao() {
        this.status = "CONECTADO";
    }

    public void recusarConexao() {
        this.status = "RECUSADO";
    }

    public boolean liberarChat() {
        return "CONECTADO".equals(this.status);
    }
}
