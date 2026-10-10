package com.br.tcc.customer.dto;

import com.br.tcc.customer.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(

        @NotNull
        CustomerStatus status

) {
}