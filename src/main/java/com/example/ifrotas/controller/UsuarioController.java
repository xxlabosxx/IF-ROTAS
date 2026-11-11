package com.example.ifrotas.controller;

import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.dto.usuario.UsuarioPerfilUpdateDTO;
import com.example.ifrotas.dto.usuario.UsuarioResponseDTO;
import com.example.ifrotas.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponseDTO(usuarioService.buscarPorId(id)));
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<UsuarioResponseDTO> atualizarPerfil(@PathVariable Long id, @RequestBody UsuarioPerfilUpdateDTO dto) {
        Usuario usuario = usuarioService.atualizarPerfil(id, dto);
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
