package com.example.ifrotas.repository;

import com.example.ifrotas.domain.Condutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CondutorRepository extends JpaRepository<Condutor, Long> {
}
