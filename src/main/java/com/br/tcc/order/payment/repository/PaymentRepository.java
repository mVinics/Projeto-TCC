package com.br.tcc.order.payment.repository;

import com.br.tcc.order.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrder_Id(UUID orderId);

    boolean existsByOrder_Id(UUID orderId);
}