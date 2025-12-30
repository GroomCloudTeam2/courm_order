package com.groom.e_commerce.order.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groom.e_commerce.order.domain.entity.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
