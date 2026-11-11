package com.example.ifrotas.repository;

import com.example.ifrotas.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailInstitucional(String email);
    Optional<Usuario> findByProntuario(String prontuario);
    Optional<Usuario> findByEmailInstitucionalIgnoreCase(String email);
    Optional<Usuario> findByProntuarioIgnoreCase(String prontuario);
}

