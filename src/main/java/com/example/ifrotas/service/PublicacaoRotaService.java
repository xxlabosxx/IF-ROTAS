package com.example.ifrotas.service;

import com.example.ifrotas.domain.Condutor;
import com.example.ifrotas.domain.PublicacaoRota;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.domain.Veiculo;
import com.example.ifrotas.dto.rota.PublicacaoRotaRequestDTO;
import com.example.ifrotas.exception.EstadoInvalidoException;
import com.example.ifrotas.exception.RecursoNaoEncontradoException;
import com.example.ifrotas.exception.RegraNegocioException;
import com.example.ifrotas.repository.PublicacaoRotaRepository;
import com.example.ifrotas.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicacaoRotaService {

    private final PublicacaoRotaRepository publicacaoRotaRepository;
    private final UsuarioService usuarioService;
    private final VeiculoRepository veiculoRepository;

    public PublicacaoRotaService(PublicacaoRotaRepository publicacaoRotaRepository, 
                                 UsuarioService usuarioService,
                                 VeiculoRepository veiculoRepository) {
        this.publicacaoRotaRepository = publicacaoRotaRepository;
        this.usuarioService = usuarioService;
        this.veiculoRepository = veiculoRepository;
    }

    public PublicacaoRota publicarRota(Long autorId, PublicacaoRotaRequestDTO dto) {
        Usuario autor = usuarioService.buscarPorId(autorId);
        Veiculo veiculo = null;

        if (dto.getVeiculoId() != null) {
            // RN02 e RN03: É uma oferta de carona, precisa de veículo do condutor
            if (!(autor instanceof Condutor)) {
                throw new RegraNegocioException("Apenas Condutores podem oferecer rotas com veículo.");
            }
            
            veiculo = veiculoRepository.findById(dto.getVeiculoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado."));
            
            if (!veiculo.getCondutor().getId().equals(autor.getId())) {
                throw new RegraNegocioException("O veículo informado não pertence a este condutor.");
            }
        } else {
            if (autor instanceof Condutor) {
                throw new RegraNegocioException("Condutores devem obrigatoriamente vincular um veículo à rota oferecida.");
            }
        }

        PublicacaoRota rota = new PublicacaoRota();
        rota.setBairroOrigem(dto.getBairroOrigem() != null ? dto.getBairroOrigem().trim() : null);
        rota.setDestinoCampus(dto.getDestinoCampus() != null ? dto.getDestinoCampus().trim() : null);
        rota.setDiasSemana(dto.getDiasSemana());
        rota.setHorarioHabitual(dto.getHorarioHabitual());
        rota.setLatitudeOrigem(dto.getLatitudeOrigem());
        rota.setLongitudeOrigem(dto.getLongitudeOrigem());
        rota.setLatitudeDestino(dto.getLatitudeDestino());
        rota.setLongitudeDestino(dto.getLongitudeDestino());
        rota.setDistanciaEstimada(dto.getDistanciaEstimada());
        rota.setDuracaoEstimada(dto.getDuracaoEstimada());
        rota.publicar();
        rota.setAutor(autor);
        rota.setVeiculo(veiculo);

        return publicacaoRotaRepository.save(rota);
    }

    public PublicacaoRota inativarRota(Long rotaId, Long autorId) {
        PublicacaoRota rota = buscarPorId(rotaId);
        
        if (!rota.getAutor().getId().equals(autorId)) {
            throw new RegraNegocioException("Apenas o autor pode inativar esta rota.");
        }
        
        rota.inativar();
        return publicacaoRotaRepository.save(rota);
    }

    // RN06: Busca retorna apenas ATIVOS com filtros opcionais (bairro, turno, curso)
    public List<PublicacaoRota> buscarRotasAtivas(String bairro, String turno, String curso) {
        List<PublicacaoRota> rotas = publicacaoRotaRepository.findByStatus("ATIVO");

        return rotas.stream()
                .filter(r -> {
                    if (bairro != null && !bairro.isBlank()) {
                        if (r.getBairroOrigem() == null || 
                            !normalizarTexto(r.getBairroOrigem()).contains(normalizarTexto(bairro))) {
                            return false;
                        }
                    }
                    if (curso != null && !curso.isBlank()) {
                        if (r.getAutor() == null || r.getAutor().getCurso() == null || 
                            !normalizarTexto(r.getAutor().getCurso()).contains(normalizarTexto(curso))) {
                            return false;
                        }
                    }
                    if (turno != null && !turno.isBlank() && r.getHorarioHabitual() != null) {
                        int hora = r.getHorarioHabitual().getHour();
                        String turnoNorm = normalizarTexto(turno);
                        if ("manha".equals(turnoNorm) && (hora < 5 || hora >= 12)) {
                            return false;
                        }
                        if ("tarde".equals(turnoNorm) && (hora < 12 || hora >= 18)) {
                            return false;
                        }
                        if ("noite".equals(turnoNorm) && (hora < 18 && hora >= 5)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public List<PublicacaoRota> buscarRotasAtivas() {
        return buscarRotasAtivas(null, null, null);
    }

    public PublicacaoRota buscarPorId(Long id) {
        return publicacaoRotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Publicação de rota não encontrada."));
    }
}
