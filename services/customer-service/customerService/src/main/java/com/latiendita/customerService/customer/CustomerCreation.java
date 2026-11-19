package com.latiendita.customerService.customer;

import com.latiendita.customerService.customer.dto.CustomerDto;

/** Internal result that lets the controller answer 201 on create and 200 on idempotent replay. */
public record CustomerCreation(CustomerDto customer, boolean created) {
}
