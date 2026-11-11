package com.example.ifrotas.controller;

import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.dto.auth.CadastroRequestDTO;
import com.example.ifrotas.dto.auth.LoginRequestDTO;
import com.example.ifrotas.dto.usuario.UsuarioResponseDTO;
import com.example.ifrotas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid CadastroRequestDTO dto) {
        Usuario usuario = usuarioService.cadastrarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        Usuario usuario = usuarioService.autenticar(dto);
        return ResponseEntity.ok(toResponseDTO(usuario));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmailInstitucional(usuario.getEmailInstitucional());
        dto.setCurso(usuario.getCurso());
        dto.setProntuario(usuario.getProntuario());
        dto.setTipoUsuario(usuario instanceof com.example.ifrotas.domain.Condutor ? "CONDUTOR" : "PASSAGEIRO");
        dto.setMinibiografia(usuario.getMinibiografia());
        dto.setPreferenciasConvivencia(usuario.getPreferenciasConvivencia());
        return dto;
    }
}
