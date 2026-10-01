package com.example.ays.repository;

import com.example.ays.entity.Dues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DuesRepository extends JpaRepository<Dues, Long> {

    List<Dues> findAllByOwnerId(Long ownerId);

    Optional<Dues> findByIdAndOwnerId(Long id, Long ownerId);
}
