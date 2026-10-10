package com.br.tcc.customer.controller;

import com.br.tcc.customer.dto.CreateCustomerRequest;
import com.br.tcc.customer.dto.CustomerResponse;
import com.br.tcc.customer.dto.UpdateCustomerStatusRequest;
import com.br.tcc.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return customerService.create(request);
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getById(
            @PathVariable UUID customerId
    ) {
        return customerService.getById(customerId);
    }

    @PatchMapping("/{customerId}/status")
    public CustomerResponse updateStatus(
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerStatusRequest request
    ) {
        return customerService.updateStatus(customerId, request);
    }
}