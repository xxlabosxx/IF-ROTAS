package com.example.ifrotas.repository;

import com.example.ifrotas.domain.PublicacaoRota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacaoRotaRepository extends JpaRepository<PublicacaoRota, Long> {
    List<PublicacaoRota> findByStatus(String status);
}
