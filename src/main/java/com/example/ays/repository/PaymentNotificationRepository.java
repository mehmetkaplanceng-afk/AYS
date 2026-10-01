package com.example.ays.repository;

import com.example.ays.entity.PaymentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, Long> {
    List<PaymentNotification> findAllByUserId(Long userId);
    List<PaymentNotification> findAllByOrderByNotificationDateDesc();
    List<PaymentNotification> findAllByUserIdOrderByNotificationDateDesc(Long userId);
}
