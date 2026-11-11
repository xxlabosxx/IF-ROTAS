package com.example.ifrotas.dto.rota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class PublicacaoRotaRequestDTO {
    @NotBlank
    private String bairroOrigem;

    @NotBlank
    private String destinoCampus;

    @NotEmpty
    private List<String> diasSemana;

    @NotNull
    private LocalTime horarioHabitual;

    private Long veiculoId; // Obrigatório se for oferta de carona (criado por CONDUTOR)

    private Double latitudeOrigem;
    private Double longitudeOrigem;
    private Double latitudeDestino;
    private Double longitudeDestino;
    private String distanciaEstimada;
    private String duracaoEstimada;
}
