package com.example.ifrotas.service;

import com.example.ifrotas.domain.Condutor;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.domain.Veiculo;
import com.example.ifrotas.dto.veiculo.VeiculoRequestDTO;
import com.example.ifrotas.exception.RegraNegocioException;
import com.example.ifrotas.repository.CondutorRepository;
import com.example.ifrotas.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final CondutorRepository condutorRepository;
    private final UsuarioService usuarioService;

    public VeiculoService(VeiculoRepository veiculoRepository, CondutorRepository condutorRepository, UsuarioService usuarioService) {
        this.veiculoRepository = veiculoRepository;
        this.condutorRepository = condutorRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public Veiculo cadastrarVeiculo(Long condutorId, VeiculoRequestDTO dto) {
        // Verifica se usuário existe e se é Condutor
        Usuario usuario = usuarioService.buscarPorId(condutorId);
        if (!(usuario instanceof Condutor)) {
            throw new RegraNegocioException("Apenas usuários do tipo Condutor podem cadastrar veículos.");
        }
        Condutor condutor = (Condutor) usuario;

        // RN08: Não é permitido o cadastro de dois veículos com a mesma placa no sistema.
        String placaFormatada = dto.getPlaca() != null ? dto.getPlaca().trim().toUpperCase() : "";
        Optional<Veiculo> veiculoExistente = veiculoRepository.findByPlacaIgnoreCase(placaFormatada);
        if (veiculoExistente.isPresent()) {
            throw new RegraNegocioException("Já existe um veículo cadastrado com a placa informada.");
        }

        Veiculo veiculo = new Veiculo();
        veiculo.setModelo(dto.getModelo() != null ? dto.getModelo().trim() : null);
        veiculo.setCor(dto.getCor() != null ? dto.getCor().trim() : null);
        veiculo.setPlaca(placaFormatada);
        veiculo.setCapacidadePassageiros(dto.getCapacidadePassageiros());
        veiculo.setCondutor(condutor);
        condutor.adicionarVeiculo(veiculo);

        return veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listarVeiculosDoCondutor(Long condutorId) {
        Usuario usuario = usuarioService.buscarPorId(condutorId);
        if (!(usuario instanceof Condutor)) {
            throw new RegraNegocioException("Usuário não é um Condutor.");
        }
        return veiculoRepository.findByCondutorId(condutorId);
    }
}
