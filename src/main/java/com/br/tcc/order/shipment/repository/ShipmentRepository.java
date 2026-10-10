package com.br.tcc.order.shipment.repository;

import com.br.tcc.order.shipment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrder_Id(UUID orderId);

    boolean existsByOrder_Id(UUID orderId);
}