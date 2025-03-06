package com.learn.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PaymentViewModel paymentViewModel;
    private ChipGroup chipGroupPayments;
    private TextView tvTotalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        paymentViewModel = new ViewModelProvider(this).get(PaymentViewModel.class);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        chipGroupPayments = findViewById(R.id.chipGroupPayments);

        findViewById(R.id.btnAddPayment).setOnClickListener(v -> showAddPaymentDialog());
        chipGroupPayments = findViewById(R.id.chipGroupPayments);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        findViewById(R.id.btnAddPayment).setOnClickListener(v -> showAddPaymentDialog());

        paymentViewModel.getPayments().observe(this, payments -> updateChips(payments));

        paymentViewModel.getTotalAmount().observe(this, amount -> tvTotalAmount.setText("Total Amount: Rs " + amount));
        findViewById(R.id.btnSave).setOnClickListener(v -> savePaymentDetailsToFile());
    }

    private void savePaymentDetailsToFile() {
        Gson gson = new Gson();
        String json = gson.toJson(paymentViewModel.getPayments().getValue());

        File file = new File(getFilesDir(), "LastPayment.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
            Toast.makeText(this, "Payments saved to file!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save payments.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddPaymentDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_payment, null);

        Spinner spinnerPaymentType = dialogView.findViewById(R.id.spinnerPaymentType);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        EditText editTextProvider = dialogView.findViewById(R.id.editTextProvider);
        EditText editTextTransactionRef = dialogView.findViewById(R.id.editTextTransactionRef);

        spinnerPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String) parent.getItemAtPosition(position);
                if (selectedType.equals("Bank Transfer") || selectedType.equals("Credit Card")) {
                    editTextProvider.setVisibility(View.VISIBLE);
                    editTextTransactionRef.setVisibility(View.VISIBLE);
                } else {
                    editTextProvider.setVisibility(View.GONE);
                    editTextTransactionRef.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Payment")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String selectedType = spinnerPaymentType.getSelectedItem().toString();
                    String amountStr = editTextAmount.getText().toString();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Amount is required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int amount = Integer.parseInt(amountStr);

                    String provider = null;
                    String transactionRef = null;

                    if (selectedType.equals("Bank Transfer") || selectedType.equals("Credit Card")) {
                        provider = editTextProvider.getText().toString();
                        transactionRef = editTextTransactionRef.getText().toString();

                        if (provider.isEmpty() || transactionRef.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Provider and Transaction Reference are required for Bank Transfer or Credit Card.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!paymentViewModel.isPaymentTypeAdded(selectedType)) {
                        Payment payment = new Payment(selectedType, amount, provider, transactionRef);
                        paymentViewModel.addPayment(payment);
                    } else {
                        Toast.makeText(MainActivity.this, "Payment type already added!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    private void updateChips(List<Payment> payments) {
        chipGroupPayments.removeAllViews();
        for (Payment payment : payments) {
            Chip chip = new Chip(this);
            chip.setText(payment.getType() + ": Rs " + payment.getAmount());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> paymentViewModel.removePayment(payment));
            chipGroupPayments.addView(chip);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPaymentDetailsFromFile();
    }

    private void loadPaymentDetailsFromFile() {
        File file = new File(getFilesDir(), "LastPayment.txt");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Payment[] savedPayments = gson.fromJson(reader, Payment[].class);
                if (savedPayments != null) {
                    for (Payment payment : savedPayments) {
                        paymentViewModel.addPayment(payment);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}