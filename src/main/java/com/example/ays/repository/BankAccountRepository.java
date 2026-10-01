package com.example.ays.repository;

import com.example.ays.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findAllByOwnerId(Long ownerId);

    Optional<BankAccount> findByIdAndOwnerId(Long id, Long ownerId);
}
