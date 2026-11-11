package com.example.ifrotas.controller;

import com.example.ifrotas.domain.Veiculo;
import com.example.ifrotas.dto.veiculo.VeiculoRequestDTO;
import com.example.ifrotas.dto.veiculo.VeiculoResponseDTO;
import com.example.ifrotas.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/condutores")
public class CondutorController {

    private final VeiculoService veiculoService;

    public CondutorController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping("/{id}/veiculos")
    public ResponseEntity<VeiculoResponseDTO> cadastrarVeiculo(@PathVariable Long id, @RequestBody @Valid VeiculoRequestDTO dto) {
        Veiculo veiculo = veiculoService.cadastrarVeiculo(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(veiculo));
    }

    @GetMapping("/{id}/veiculos")
    public ResponseEntity<List<VeiculoResponseDTO>> listarVeiculos(@PathVariable Long id) {
        List<VeiculoResponseDTO> veiculos = veiculoService.listarVeiculosDoCondutor(id).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(veiculos);
    }

    private VeiculoResponseDTO toResponseDTO(Veiculo veiculo) {
        VeiculoResponseDTO dto = new VeiculoResponseDTO();
        dto.setId(veiculo.getId());
        dto.setModelo(veiculo.getModelo());
        dto.setCor(veiculo.getCor());
        dto.setPlaca(veiculo.getPlaca());
        dto.setCapacidadePassageiros(veiculo.getCapacidadePassageiros());
        return dto;
    }
}
