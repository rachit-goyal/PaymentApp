package com.learn.paymentapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class PaymentViewModel extends ViewModel {
    
    private final MutableLiveData<List<Payment>> payments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> totalAmount = new MutableLiveData<>(0);

    public LiveData<List<Payment>> getPayments() {
        return payments;
    }

    public LiveData<Integer> getTotalAmount() {
        return totalAmount;
    }

    public void addPayment(Payment payment) {
        List<Payment> currentPayments = payments.getValue();
        assert currentPayments != null;
        currentPayments.add(payment);
        payments.setValue(currentPayments);

        totalAmount.setValue(totalAmount.getValue() + payment.getAmount());
    }

    public void removePayment(Payment payment) {
        List<Payment> currentPayments = payments.getValue();
        assert currentPayments != null;
        currentPayments.remove(payment);
        payments.setValue(currentPayments);

        totalAmount.setValue(totalAmount.getValue() - payment.getAmount());
    }

    public boolean isPaymentTypeAdded(String type) {
        for (Payment payment : payments.getValue()) {
            if (payment.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}