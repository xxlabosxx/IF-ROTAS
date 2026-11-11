package com.example.ifrotas.dto.veiculo;

import lombok.Data;

@Data
public class VeiculoResponseDTO {
    private Long id;
    private String modelo;
    private String cor;
    private String placa;
    private Integer capacidadePassageiros;
}
