package com.example.ifrotas.repository;

import com.example.ifrotas.domain.Conexao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConexaoRepository extends JpaRepository<Conexao, Long> {
    boolean existsByInteressadoIdAndPublicacaoRotaIdAndStatusIn(Long interessadoId, Long publicacaoRotaId, List<String> status);

    @Query("SELECT c FROM Conexao c WHERE c.interessado.id = :usuarioId OR c.publicacaoRota.autor.id = :usuarioId ORDER BY c.dataSolicitacao DESC")
    List<Conexao> findByUsuarioEnvolvido(@Param("usuarioId") Long usuarioId);
}
