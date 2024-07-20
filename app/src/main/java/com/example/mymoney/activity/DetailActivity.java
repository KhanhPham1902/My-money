package com.example.mymoney.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mymoney.R;
import com.example.mymoney.databinding.ActivityDetailBinding;
import com.example.mymoney.model.MoneyData;
import com.example.mymoney.utility.DatabaseHelper;

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private DatabaseHelper db;
    private Integer itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khoi tao database
        db = new DatabaseHelper(this);

        // Lay du lieu tu intent
        Intent intent = getIntent();
        MoneyData moneyData = (MoneyData) intent.getSerializableExtra("moneyData");
        if (moneyData != null) {
            binding.imgItemDetail.setImageResource(moneyData.getSpendingItem().getImgItem());
            binding.txtCategoryDetail.setText(moneyData.getSpendingItem().getItemName());
            binding.txtMoneyDetail.setText(moneyData.getMoney());
            binding.txtDateDetail.setText(moneyData.getDate());
            binding.txtNoteDetail.setText(moneyData.getNote());

            Integer type = moneyData.getSpendingItem().getItemType();
            if (type == 1) {
                binding.txtTypeDetail.setText(getString(R.string.income));
            } else {
                binding.txtTypeDetail.setText(getString(R.string.spending));
            }

            Integer id = moneyData.getId();
            if (id == -1) {
                finish();
                Log.d("TAG", "onCreate: id = -1");
            } else {
                itemId = id;
            }
        }

        // Chinh sua
        binding.btnEdit.setOnClickListener(v -> {
            if (moneyData != null) {
                Intent sendData = new Intent(this, EditSpendingActivity.class);
                sendData.putExtra("moneyData", moneyData);
                startActivity(sendData);
            }else{
                Toast.makeText(this, "Không có dữ liệu để gửi!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xoa
        binding.btnDelete.setOnClickListener(v -> {
            showDeleteDialog();
        });

        // Thoat
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    // Hien thi dialog xoa item
    private void showDeleteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_dialog);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            db.deleteSpendingById(itemId);
            dialog.dismiss();
            finish();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}