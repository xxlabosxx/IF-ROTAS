package com.example.ifrotas.dto.conexao;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConexaoResponseDTO {
    private Long id;
    private LocalDateTime dataSolicitacao;
    private String status;
    private Long publicacaoRotaId;
    private String rotaOrigem;
    private String rotaDestino;
    private Double latitudeOrigem;
    private Double longitudeOrigem;
    private Double latitudeDestino;
    private Double longitudeDestino;
    private String distanciaEstimada;
    private String duracaoEstimada;
    private Long interessadoId;
    private String interessadoNome;
    private Long autorId;
    private String autorNome;
}
