package com.example.ifrotas.repository;

import com.example.ifrotas.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByPlaca(String placa);
    Optional<Veiculo> findByPlacaIgnoreCase(String placa);
    List<Veiculo> findByCondutorId(Long condutorId);
}

