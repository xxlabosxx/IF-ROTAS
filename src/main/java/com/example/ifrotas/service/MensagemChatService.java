package com.example.ifrotas.service;

import com.example.ifrotas.domain.Conexao;
import com.example.ifrotas.domain.MensagemChat;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.exception.EstadoInvalidoException;
import com.example.ifrotas.exception.RegraNegocioException;
import com.example.ifrotas.repository.MensagemChatRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MensagemChatService {

    private final MensagemChatRepository mensagemChatRepository;
    private final ConexaoService conexaoService;
    private final UsuarioService usuarioService;

    public MensagemChatService(MensagemChatRepository mensagemChatRepository,
                               ConexaoService conexaoService,
                               UsuarioService usuarioService) {
        this.mensagemChatRepository = mensagemChatRepository;
        this.conexaoService = conexaoService;
        this.usuarioService = usuarioService;
    }

    public MensagemChat enviarMensagem(Long conexaoId, Long remetenteId, String conteudoTexto) {
        Conexao conexao = conexaoService.buscarPorId(conexaoId);
        Usuario remetente = usuarioService.buscarPorId(remetenteId);

        // RN04: O canal de mensagens diretas (chat) só pode ser aberto após transição da conexão para o estado CONECTADO.
        if (!conexao.liberarChat()) {
            throw new EstadoInvalidoException("Mensagens só podem ser enviadas em conexões com estado CONECTADO.");
        }

        // Validação adicional: O remetente deve fazer parte da conexão (ser o interessado ou o autor da rota)
        boolean isInteressado = conexao.getInteressado().getId().equals(remetente.getId());
        boolean isAutor = conexao.getPublicacaoRota().getAutor().getId().equals(remetente.getId());

        if (!isInteressado && !isAutor) {
            throw new RegraNegocioException("O usuário não faz parte desta conexão.");
        }

        MensagemChat mensagem = new MensagemChat();
        mensagem.enviarMensagem(conteudoTexto);
        mensagem.setConexao(conexao);
        mensagem.setRemetente(remetente);

        return mensagemChatRepository.save(mensagem);
    }

    public java.util.List<MensagemChat> listarMensagensPorConexao(Long conexaoId, Long usuarioId) {
        Conexao conexao = conexaoService.buscarPorId(conexaoId);
        usuarioService.buscarPorId(usuarioId);
        
        boolean isInteressado = conexao.getInteressado().getId().equals(usuarioId);
        boolean isAutor = conexao.getPublicacaoRota().getAutor().getId().equals(usuarioId);

        if (!isInteressado && !isAutor) {
            throw new RegraNegocioException("O usuário não faz parte desta conexão.");
        }

        // RN04: O canal de mensagens diretas (chat) só pode ser aberto após transição da conexão para o estado CONECTADO.
        if (!conexao.liberarChat()) {
            throw new EstadoInvalidoException("Mensagens só podem ser visualizadas em conexões com estado CONECTADO.");
        }

        return mensagemChatRepository.findByConexaoIdOrderByDataEnvioAsc(conexaoId);
    }
}
