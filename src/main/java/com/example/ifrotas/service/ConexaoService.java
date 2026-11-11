package com.example.ifrotas.service;

import com.example.ifrotas.domain.Conexao;
import com.example.ifrotas.domain.PublicacaoRota;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.exception.EstadoInvalidoException;
import com.example.ifrotas.exception.RecursoNaoEncontradoException;
import com.example.ifrotas.exception.RegraNegocioException;
import com.example.ifrotas.repository.ConexaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class ConexaoService {

    private final ConexaoRepository conexaoRepository;
    private final PublicacaoRotaService publicacaoRotaService;
    private final UsuarioService usuarioService;

    public ConexaoService(ConexaoRepository conexaoRepository, 
                          PublicacaoRotaService publicacaoRotaService,
                          UsuarioService usuarioService) {
        this.conexaoRepository = conexaoRepository;
        this.publicacaoRotaService = publicacaoRotaService;
        this.usuarioService = usuarioService;
    }

    public Conexao solicitarConexao(Long publicacaoRotaId, Long usuarioInteressadoId) {
        PublicacaoRota rota = publicacaoRotaService.buscarPorId(publicacaoRotaId);
        Usuario interessado = usuarioService.buscarPorId(usuarioInteressadoId);

        // RN06: Publicações marcadas como INATIVO deixam de ser listadas em buscas e não recebem novas conexões.
        if ("INATIVO".equals(rota.getStatus())) {
            throw new EstadoInvalidoException("Não é possível solicitar conexão em uma rota inativa.");
        }

        // RN05: É estritamente proibido que um usuário envie solicitação de conexão para uma rota de sua própria autoria.
        if (rota.getAutor().getId().equals(interessado.getId())) {
            throw new RegraNegocioException("O usuário não pode solicitar conexão em uma rota que ele mesmo criou.");
        }

        // RN07: Um interessado não pode ter mais de uma solicitação pendente simultânea para a mesma rota.
        boolean jaExisteConexao = conexaoRepository.existsByInteressadoIdAndPublicacaoRotaIdAndStatusIn(
                interessado.getId(), rota.getId(), Arrays.asList("SOLICITADO", "CONECTADO"));
        
        if (jaExisteConexao) {
            throw new RegraNegocioException("O usuário já possui uma conexão solicitada ou ativa para esta rota.");
        }

        Conexao conexao = new Conexao();
        conexao.setDataSolicitacao(LocalDateTime.now());
        conexao.setStatus("SOLICITADO");
        conexao.setPublicacaoRota(rota);
        conexao.setInteressado(interessado);

        return conexaoRepository.save(conexao);
    }

    public Conexao aceitarConexao(Long conexaoId, Long autorId) {
        Conexao conexao = buscarPorId(conexaoId);
        
        if (!conexao.getPublicacaoRota().getAutor().getId().equals(autorId)) {
            throw new RegraNegocioException("Apenas o autor da rota pode aceitar conexões.");
        }

        if (!"SOLICITADO".equals(conexao.getStatus())) {
            throw new EstadoInvalidoException("A conexão não está no estado SOLICITADO.");
        }

        if ("INATIVO".equals(conexao.getPublicacaoRota().getStatus())) {
            throw new EstadoInvalidoException("Não é possível aceitar conexão de uma rota inativa.");
        }

        conexao.aceitarConexao();
        return conexaoRepository.save(conexao);
    }

    public Conexao recusarConexao(Long conexaoId, Long autorId) {
        Conexao conexao = buscarPorId(conexaoId);
        
        if (!conexao.getPublicacaoRota().getAutor().getId().equals(autorId)) {
            throw new RegraNegocioException("Apenas o autor da rota pode recusar conexões.");
        }

        if (!"SOLICITADO".equals(conexao.getStatus())) {
            throw new EstadoInvalidoException("A conexão não está no estado SOLICITADO.");
        }

        conexao.recusarConexao();
        return conexaoRepository.save(conexao);
    }

    public Conexao buscarPorId(Long id) {
        return conexaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conexão não encontrada."));
    }

    public java.util.List<Conexao> listarConexoesPorUsuario(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return conexaoRepository.findByUsuarioEnvolvido(usuarioId);
    }
}
