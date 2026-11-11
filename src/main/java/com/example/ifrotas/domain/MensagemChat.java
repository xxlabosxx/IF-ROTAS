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

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class MensagemChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String conteudoTexto;
    private LocalDateTime dataEnvio;
    
    @ManyToOne
    @JoinColumn(name = "conexao_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Conexao conexao;
    
    @ManyToOne
    @JoinColumn(name = "remetente_id")
    private Usuario remetente;

    public void enviarMensagem(String conteudo) {
        this.conteudoTexto = conteudo;
        this.dataEnvio = LocalDateTime.now();
    }

    public void marcarComoLida() {
        // Para implementações futuras de status da mensagem
    }
}
