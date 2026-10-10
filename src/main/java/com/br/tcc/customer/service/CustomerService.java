package com.br.tcc.customer.service;

import com.br.tcc.customer.dto.CreateCustomerRequest;
import com.br.tcc.customer.dto.CustomerResponse;
import com.br.tcc.customer.dto.UpdateCustomerStatusRequest;
import com.br.tcc.customer.entity.Customer;
import com.br.tcc.customer.entity.CustomerStatus;
import com.br.tcc.customer.repository.CustomerRepository;
import com.br.tcc.shared.exception.ResourceConflictException;
import com.br.tcc.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException(
                    "A customer with this email already exists"
            );
        }

        Customer customer = new Customer(
                request.name(),
                request.email(),
                CustomerStatus.ACTIVE
        );

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerResponse.from(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID customerId) {
        Customer customer = findCustomer(customerId);

        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse updateStatus(
            UUID customerId,
            UpdateCustomerStatusRequest request
    ) {
        Customer customer = findCustomer(customerId);

        customer.setStatus(request.status());

        return CustomerResponse.from(customer);
    }

    private Customer findCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + customerId
                ));
    }
}