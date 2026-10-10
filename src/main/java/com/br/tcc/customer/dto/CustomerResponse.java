package com.br.tcc.customer.dto;

import com.br.tcc.customer.entity.Customer;
import com.br.tcc.customer.entity.CustomerStatus;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String email,
        CustomerStatus status
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getStatus()
        );
    }
}