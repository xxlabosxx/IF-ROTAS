package com.example.ifrotas.repository;

import com.example.ifrotas.domain.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {
    List<MensagemChat> findByConexaoIdOrderByDataEnvioAsc(Long conexaoId);
}
