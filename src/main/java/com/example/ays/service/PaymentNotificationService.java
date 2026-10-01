package com.example.ays.service;

import com.example.ays.dto.PaymentNotificationCreateRequest;
import com.example.ays.entity.AppUser;
import com.example.ays.entity.PaymentNotification;
import com.example.ays.repository.PaymentNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final PaymentNotificationRepository paymentNotificationRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public PaymentNotification create(PaymentNotificationCreateRequest request, AppUser user) {
        PaymentNotification notification = PaymentNotification.builder()
                .user(user)
                .amount(request.getAmount())
                .description(request.getDescription())
                .notificationDate(LocalDateTime.now())
                .status("Bekliyor")
                .build();
        return paymentNotificationRepository.save(notification);
    }

    @Transactional
    public PaymentNotification updateStatus(Long id, String newStatus, AppUser user) {
        currentUserService.requireAdmin(user);
        PaymentNotification notification = paymentNotificationRepository.findById(id)
                .orElseThrow(() -> new com.example.ays.exception.ResourceNotFoundException("Bildirim bulunamadı: " + id));
        notification.setStatus(newStatus);
        return paymentNotificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<PaymentNotification> findAll(AppUser user) {
        if (currentUserService.isAdmin(user)) {
            return paymentNotificationRepository.findAllByOrderByNotificationDateDesc();
        }
        return paymentNotificationRepository.findAllByUserIdOrderByNotificationDateDesc(user.getId());
    }
}
