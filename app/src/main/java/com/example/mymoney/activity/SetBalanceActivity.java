package com.example.mymoney.activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mymoney.R;
import com.example.mymoney.databinding.ActivitySetBalanceBinding;
import com.example.mymoney.utility.DatabaseHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class SetBalanceActivity extends AppCompatActivity {

    private ActivitySetBalanceBinding binding;
    private DatabaseHelper db;
    private double spendingThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetBalanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);

        spendingThreshold = getIntent().getDoubleExtra("balance", 0);
        binding.txtSpendingThreshold.setText(formatCurrency(spendingThreshold));

        binding.cvSetup.setOnClickListener(v ->{
            showSetupDialog();
        });

        binding.btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void showSetupDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.setup_dialog);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtOK = dialog.findViewById(R.id.txtOK);
        TextView txtCancel = dialog.findViewById(R.id.txtCancel);
        EditText edtBalance = dialog.findViewById(R.id.edtSetup);

        handleEdittext(edtBalance);

        edtBalance.setHint(formatCurrency(spendingThreshold));

        txtOK.setOnClickListener(v -> {
            String input = edtBalance.getText().toString().trim();
            if (input.isEmpty()) {
                edtBalance.setError("Không được để trống");
                return;
            }
            double newThreshold = Double.parseDouble(input.replace(".", ""));
            if (newThreshold > 0) {
                Log.d("New Threshold", formatCurrency(newThreshold));
                spendingThreshold = newThreshold;
                db.insertSetupBalance(formatCurrency(newThreshold));
                binding.txtSpendingThreshold.setText(formatCurrency(newThreshold));
                dialog.dismiss();
            }else{
                edtBalance.setError("Nhập số lớn hơn 0");
            }
        });

        txtCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    // Định dạng số theo dạng phân tách phần nghìn
    private String formatCurrency(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        return numberFormat.format(amount);
    }

    // Tách 3 chữ số bằng dấu chấm
    private void handleEdittext(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[,\\.]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed;
                        try {
                            parsed = Double.parseDouble(cleanString);
                        } catch (NumberFormatException e) {
                            parsed = 0.0;
                        }
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        String formatted = formatter.format(parsed);

                        current = formatted;
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());

                        spendingThreshold = Double.parseDouble(cleanString);
                        Log.d("money", String.valueOf(spendingThreshold));

                        editText.addTextChangedListener(this);
                    }
                }
            }
        });
    }
}