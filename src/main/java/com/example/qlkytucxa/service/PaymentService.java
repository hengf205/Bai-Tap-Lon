package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Payment;

public interface PaymentService extends CrudService<Payment> {
    Payment pay(Payment payment);
}
