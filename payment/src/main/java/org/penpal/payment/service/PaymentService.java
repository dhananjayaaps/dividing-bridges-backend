package org.penpal.payment.service;

import org.penpal.payment.model.Payment;
import org.penpal.payment.repository.PaymentRepository;
import org.penpal.shared.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public ResponseEntity<?> addPayment(Payment payment) {
        Payment newPayment = new Payment();
        newPayment.setId(UUID.randomUUID().toString());
        newPayment.setName(payment.getName());
        newPayment.setEmail(payment.getEmail());
        newPayment.setBranch(payment.getBranch());
        newPayment.setAccountNumber(payment.getAccountNumber());
        newPayment.setBankName(payment.getBankName());
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Payment> httpEntity = new HttpEntity<Payment>(newPayment, httpHeaders);
            return new ResponseEntity<Payment>(paymentRepository.save(newPayment), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getPayments() {
        List<Payment> payments = paymentRepository.findAll();
        try {
            return new ResponseEntity<List<Payment>>(payments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getPayment(String id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        if (payment.isPresent()) {
            return new ResponseEntity<>(payment, HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("No payment record found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> filterPayments(String email) {
        List<Payment> payments = paymentRepository.findAll();
        List<Payment> filteredPayments = payments.stream().filter(payment ->
                                (Objects.equals(payment.getEmail(), email))).toList();
        try {
            return new ResponseEntity<List<Payment>>(filteredPayments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updatePayment(String id, Payment payment) {
        Optional<Payment> existingPayment = paymentRepository.findById(id);
        if (existingPayment.isPresent()) {
            if (payment.getAccountNumber()!= null) {
                existingPayment.get().setAccountNumber(payment.getAccountNumber());
            }
            if (payment.getBankName()!= null) {
                existingPayment.get().setBankName(payment.getBankName());
            }
            if (payment.getBranch()!= null) {
                existingPayment.get().setBranch(payment.getBranch());
            }
            if(payment.getName()!=null) {
                existingPayment.get().setName(payment.getName());
            }
            return new ResponseEntity<>(paymentRepository.save(existingPayment.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>(new APIResponse("No payment record found!"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deletePayment(String id) {
        Optional<Payment> existingPayment = paymentRepository.findById(id);
        if (existingPayment.isPresent()) {
            paymentRepository.delete(existingPayment.get());
            return new ResponseEntity<>(new APIResponse("Payment record deleted"), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new APIResponse("No payment record found!"), HttpStatus.NOT_FOUND);
    }

}
