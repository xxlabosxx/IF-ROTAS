package com.example.ifrotas.service;

import com.example.ifrotas.domain.Condutor;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.dto.auth.CadastroRequestDTO;
import com.example.ifrotas.dto.usuario.UsuarioPerfilUpdateDTO;
import com.example.ifrotas.exception.RecursoNaoEncontradoException;
import com.example.ifrotas.exception.RegraNegocioException;
import com.example.ifrotas.repository.CondutorRepository;
import com.example.ifrotas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CondutorRepository condutorRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, CondutorRepository condutorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.condutorRepository = condutorRepository;
    }

    public Usuario cadastrarUsuario(CadastroRequestDTO dto) {
        validarRegrasCadastro(dto);

        String nome = dto.getNome() != null ? dto.getNome().trim() : null;
        String email = dto.getEmailInstitucional() != null ? dto.getEmailInstitucional().trim().toLowerCase() : null;
        String prontuario = dto.getProntuario() != null ? dto.getProntuario().trim().toUpperCase() : null;
        String curso = dto.getCurso() != null ? dto.getCurso().trim() : null;
        String cnh = (dto.getCnh() != null && !dto.getCnh().trim().isBlank()) ? dto.getCnh().trim() : null;
        String tipoUsuario = dto.getTipoUsuario() != null ? dto.getTipoUsuario().trim().toUpperCase() : "";

        Usuario novoUsuario;

        if ("CONDUTOR".equalsIgnoreCase(tipoUsuario)) {
            if (cnh == null) {
                throw new RegraNegocioException("CNH é obrigatória para cadastro de Condutor.");
            }
            Condutor condutor = new Condutor();
            condutor.setCnh(cnh);
            novoUsuario = condutor;
        } else {
            novoUsuario = new Usuario();
        }

        novoUsuario.setNome(nome);
        novoUsuario.setEmailInstitucional(email);
        novoUsuario.setProntuario(prontuario);
        novoUsuario.setCurso(curso);
        novoUsuario.setSenha(dto.getSenha()); // TODO: BCrypt integration if needed later

        return usuarioRepository.save(novoUsuario);
    }

    private void validarRegrasCadastro(CadastroRequestDTO dto) {
        String email = dto.getEmailInstitucional() != null ? dto.getEmailInstitucional().trim().toLowerCase() : "";
        String prontuario = dto.getProntuario() != null ? dto.getProntuario().trim().toUpperCase() : "";

        // RN01: Domínio de e-mail válido (case-insensitive)
        if (!email.endsWith("@aluno.ifsp.edu.br") &&
            !email.endsWith("@ifsp.edu.br")) {
            throw new RegraNegocioException("O e-mail informado não pertence a um domínio institucional válido.");
        }

        // RN01: Unicidade de e-mail e prontuário (case-insensitive)
        Optional<Usuario> usuarioPorEmail = usuarioRepository.findByEmailInstitucionalIgnoreCase(email);
        if (usuarioPorEmail.isPresent()) {
            throw new RegraNegocioException("E-mail institucional já cadastrado.");
        }

        Optional<Usuario> usuarioPorProntuario = usuarioRepository.findByProntuarioIgnoreCase(prontuario);
        if (usuarioPorProntuario.isPresent()) {
            throw new RegraNegocioException("Prontuário já cadastrado.");
        }
    }

    public Usuario atualizarPerfil(Long usuarioId, UsuarioPerfilUpdateDTO dto) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.atualizarPerfil(dto.getMinibiografia(), dto.getPreferenciasConvivencia());
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    public Usuario autenticar(com.example.ifrotas.dto.auth.LoginRequestDTO dto) {
        String identificador = dto.getIdentificador() != null ? dto.getIdentificador().trim() : "";

        Usuario usuario = usuarioRepository.findByEmailInstitucionalIgnoreCase(identificador)
                .orElseGet(() -> usuarioRepository.findByProntuarioIgnoreCase(identificador)
                        .orElseThrow(() -> new RegraNegocioException("Credenciais inválidas.")));

        if (!usuario.autenticar(dto.getSenha())) {
            throw new RegraNegocioException("Credenciais inválidas.");
        }
        return usuario;
    }
}
