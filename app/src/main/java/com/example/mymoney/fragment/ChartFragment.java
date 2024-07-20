package com.example.mymoney.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mymoney.R;
import com.example.mymoney.databinding.FragmentChartBinding;
import com.example.mymoney.model.MoneyData;
import com.example.mymoney.utility.DatabaseHelper;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ChartFragment extends Fragment {

    private FragmentChartBinding binding;
    private DatabaseHelper db;
    private List<PieEntry> spendingEntries = new ArrayList<>();
    private List<PieEntry> incomeEntries = new ArrayList<>();
    private PieDataSet spendingDataSet;
    private PieDataSet incomeDataSet;
    private int month;
    private int year;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChartBinding.inflate(inflater, container, false);

        // Lấy tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH trả về từ 0 đến 11, nên cộng thêm 1

        db = new DatabaseHelper(getContext());

        loadSpendingData();
        loadIncomeData();

        setupPieChart();

        // Chọn tháng và năm
        binding.layoutCalendarChart.setOnClickListener(v -> {
            showCalendarDialog();
        });

        return binding.getRoot();
    }

    private void showCalendarDialog() {
        Calendar today = Calendar.getInstance();

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.month_year_dialog);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnSelect = dialog.findViewById(R.id.btnSelect);
        TextInputLayout layoutMonth = dialog.findViewById(R.id.layoutMonth);
        TextInputLayout layoutYear = dialog.findViewById(R.id.layoutYear);
        MaterialAutoCompleteTextView inputMonth = dialog.findViewById(R.id.inputMonth);
        MaterialAutoCompleteTextView inputYear = dialog.findViewById(R.id.inputYear);

        btnSelect.setOnClickListener(v -> {
            String monthStr = inputMonth.getText().toString().trim();
            String yearStr = inputYear.getText().toString().trim();
            if(monthStr.isEmpty()){
                layoutMonth.setError("Chưa chọn tháng");
            }else if(yearStr.isEmpty()){
                layoutYear.setError("Chưa chọn năm");
            } else if (monthStr.equals("Hiện tại") && yearStr.equals("Hiện tại")) {
                month = today.get(Calendar.MONTH) + 1;
                year = today.get(Calendar.YEAR);
                updateData(month, year);
                dialog.dismiss();
            }else if(monthStr.equals("Hiện tại") && !yearStr.equals("Hiện tại")){
                month = today.get(Calendar.MONTH) + 1;
                year = Integer.parseInt(yearStr);
                updateData(month, year);
                dialog.dismiss();
            }else if(!monthStr.equals("Hiện tại") && yearStr.equals("Hiện tại")){
                month = Integer.parseInt(monthStr);
                year = today.get(Calendar.YEAR);
                updateData(month, year);
                dialog.dismiss();
            }else{
                month = Integer.parseInt(monthStr);
                year = Integer.parseInt(yearStr);
                updateData(month, year);
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.END;
            params.x = 50;
            params.y = 100;
            window.setAttributes(params);
        }

        dialog.show();
    }

    // Cập nhật lại dữ liệu sau khi thay đổi tháng và năm
    private void updateData(int month, int year){
        binding.txtDateChart.setText("thg " + month + ", " + year);
        loadSpendingData();
        loadIncomeData();
        setupPieChart();
    }

    // Lay du lieu chi tieu trong thang
    private void loadSpendingData() {
        spendingEntries.clear(); // Xóa dữ liệu cũ
        List<MoneyData> spendingList = db.getSpendingInMonth(month, year);
        if (spendingList.isEmpty()) {
            binding.txtNoSpending.setVisibility(View.VISIBLE);
            binding.chartSpending.setVisibility(View.INVISIBLE);
        } else {
            for (MoneyData moneyData : spendingList) {
                float amount = Float.parseFloat(moneyData.getMoney().replace(".", ""));
                spendingEntries.add(new PieEntry(amount, moneyData.getSpendingItem().getItemName()));
            }
            binding.txtNoSpending.setVisibility(View.GONE);
            binding.chartSpending.setVisibility(View.VISIBLE);
        }
    }

    // Lay du lieu thu nhap trong thang
    private void loadIncomeData() {
        incomeEntries.clear(); // Xóa dữ liệu cũ
        List<MoneyData> incomeList = db.getIncomeInMonth(month, year);
        if (incomeList.isEmpty()) {
            binding.txtNoIncome.setVisibility(View.VISIBLE);
            binding.chartIncome.setVisibility(View.INVISIBLE);
        } else {
            for (MoneyData moneyData : incomeList) {
                float amount = Float.parseFloat(moneyData.getMoney().replace(".", ""));
                incomeEntries.add(new PieEntry(amount, moneyData.getSpendingItem().getItemName()));
            }
            binding.txtNoIncome.setVisibility(View.GONE);
            binding.chartIncome.setVisibility(View.VISIBLE);
        }
    }

    // Thiet lap bieu do tron
    private void setupPieChart() {
        if (!spendingEntries.isEmpty()) {
            spendingDataSet = new PieDataSet(spendingEntries, "Chi tiêu");
            spendingDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            spendingDataSet.setValueTextSize(12f);
            PieData spendingData = new PieData(spendingDataSet);
            binding.chartSpending.setData(spendingData);

            // Tắt mô tả
            Description spendingDescription = new Description();
            spendingDescription.setText("");
            binding.chartSpending.setDescription(spendingDescription);

            binding.chartSpending.invalidate(); // refresh
        }

        if (!incomeEntries.isEmpty()) {
            incomeDataSet = new PieDataSet(incomeEntries, "Thu nhập");
            incomeDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            incomeDataSet.setValueTextSize(12f);
            PieData incomeData = new PieData(incomeDataSet);
            binding.chartIncome.setData(incomeData);

            // Tắt mô tả
            Description incomeDescription = new Description();
            incomeDescription.setText("");
            binding.chartIncome.setDescription(incomeDescription);

            binding.chartIncome.invalidate(); // refresh
        }
    }
}
