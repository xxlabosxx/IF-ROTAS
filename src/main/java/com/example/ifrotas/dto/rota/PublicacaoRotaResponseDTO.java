package com.example.ifrotas.dto.rota;

import com.example.ifrotas.dto.usuario.UsuarioResponseDTO;
import com.example.ifrotas.dto.veiculo.VeiculoResponseDTO;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class PublicacaoRotaResponseDTO {
    private Long id;
    private String bairroOrigem;
    private String destinoCampus;
    private Double latitudeOrigem;
    private Double longitudeOrigem;
    private Double latitudeDestino;
    private Double longitudeDestino;
    private String distanciaEstimada;
    private String duracaoEstimada;
    private List<String> diasSemana;
    private LocalTime horarioHabitual;
    private String status;
    private UsuarioResponseDTO autor;
    private VeiculoResponseDTO veiculo; // Opcional (apenas para ofertas)
}
