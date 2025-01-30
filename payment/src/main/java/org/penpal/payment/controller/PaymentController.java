package org.penpal.payment.controller;

import lombok.RequiredArgsConstructor;
import org.penpal.payment.model.Payment;
import org.penpal.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping(path ="")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> addPayment(@RequestBody Payment payment) {
        return paymentService.addPayment(payment);
    }

    @GetMapping(path ="")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPayments() {
        return paymentService.getPayments();
    }

    @GetMapping(path ="/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPayment(@PathVariable("id") String id) {
        return paymentService.getPayment(id);
    }

    @GetMapping(path ="/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> filterPayments(@RequestParam("email") String email) {
        return paymentService.filterPayments(email);
    }

    @PatchMapping(path ="/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updatePayment(@PathVariable("id") String id, @RequestBody Payment payment) {
        return paymentService.updatePayment(id, payment);
    }

    @DeleteMapping(path ="/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deletePayment(@PathVariable("id") String id) {
        return paymentService.deletePayment(id);
    }
}
