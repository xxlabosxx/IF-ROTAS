package com.example.ifrotas.dto.veiculo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VeiculoRequestDTO {
    @NotBlank
    private String modelo;

    @NotBlank
    private String cor;

    @NotBlank
    private String placa;

    @NotNull
    @Min(1)
    private Integer capacidadePassageiros;
}
