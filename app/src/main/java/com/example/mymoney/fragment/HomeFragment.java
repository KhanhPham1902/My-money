package com.example.mymoney.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mymoney.R;
import com.example.mymoney.activity.AddSpendingActivity;
import com.example.mymoney.activity.SetBalanceActivity;
import com.example.mymoney.adapter.MoneyAdapter;
import com.example.mymoney.databinding.FragmentHomeBinding;
import com.example.mymoney.model.MoneyData;
import com.example.mymoney.utility.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MoneyAdapter moneyAdapter;
    private List<MoneyData> moneyDataList;
    private DatabaseHelper db;
    private double totalSpending;
    private double totalIncome;
    private double total;
    private double spendingThreshold;
    private int month;
    private int year;

    public static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Lấy tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH trả về từ 0 đến 11, nên cộng thêm 1

        init();

        setListener();

        return binding.getRoot();
    }

    // Khoi tao
    private void init() {
        // Hiển thị tháng hiện tại
        Calendar calendar = Calendar.getInstance();
        String time = new SimpleDateFormat("MMM, yyyy", new Locale("vi")).format(calendar.getTime());
        binding.txtDateHome.setText(time);

        // Khoi tao database
        db = new DatabaseHelper(getActivity());

        // Lấy tổng chi tiêu và thu nhập trong tháng hiện tại
        showSpendingAndIncome(month, year);

        // Lấy ngưỡng chi tiêu và số dư tài khoản
        showBalance();

        // Khoi tao recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvMoney.setLayoutManager(linearLayoutManager);

        // Khoi tao danh sach va adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            moneyDataList = db.getAllSpendingInMonth(month, year);
        }

        moneyAdapter = new MoneyAdapter(getContext(), moneyDataList);
        binding.rvMoney.setAdapter(moneyAdapter);
    }

    // Xu ly su kien
    private void setListener() {

        // Lam moi du lieu
        binding.swipeRefreshHome.setOnRefreshListener(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                moneyAdapter.refreshData(db.getAllSpendingInMonth(month, year));
            }
            showSpendingAndIncome(month, year);
            showBalance();
            binding.swipeRefreshHome.setRefreshing(false);
        });

        // Chuyển sang cửa sổ thêm chi tiêu và thu nhập
        binding.btnAddMoney.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddSpendingActivity.class);
            startActivity(intent);
        });

        // Chuyển sang cửa sổ đặt số dư tài khoản
        binding.cvBalance.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SetBalanceActivity.class);
            intent.putExtra("balance", spendingThreshold);
            startActivity(intent);
        });

        // Chọn tháng và năm
        binding.layoutCalendar.setOnClickListener(v -> {
            showCalendarDialog();
        });
    }

    // Chọn tháng và năm
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
            if (monthStr.isEmpty()) {
                layoutMonth.setError("Chưa chọn tháng");
            } else if (yearStr.isEmpty()) {
                layoutYear.setError("Chưa chọn năm");
            } else if (monthStr.equals("Hiện tại") && yearStr.equals("Hiện tại")) {
                month = today.get(Calendar.MONTH) + 1;
                year = today.get(Calendar.YEAR);
                updateUI(month, year);
                dialog.dismiss();
                // Cập nhật lại dữ liệu sau khi thay đổi tháng và năm
                updateData();
            } else if(monthStr.equals("Hiện tại") && !yearStr.equals("Hiện tại")){
                month = today.get(Calendar.MONTH) + 1;
                year = Integer.parseInt(yearStr);
                updateUI(month, year);
                dialog.dismiss();
                // Cập nhật lại dữ liệu sau khi thay đổi tháng và năm
                updateData();
            }else if(!monthStr.equals("Hiện tại") && yearStr.equals("Hiện tại")){
                month = Integer.parseInt(monthStr);
                year = today.get(Calendar.YEAR);
                updateUI(month, year);
                dialog.dismiss();
                // Cập nhật lại dữ liệu sau khi thay đổi tháng và năm
                updateData();
            }else {
                month = Integer.parseInt(monthStr);
                year = Integer.parseInt(yearStr);
                updateUI(month, year);
                dialog.dismiss();
                // Cập nhật lại dữ liệu sau khi thay đổi tháng và năm
                updateData();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.END;
            params.x = 10;
            params.y = 100;
            window.setAttributes(params);
        }

        dialog.show();
    }

    private void updateUI(int month, int year){
        Calendar today = Calendar.getInstance();
        binding.txtDateHome.setText("thg " + month + ", " + year);
        if (month != (today.get(Calendar.MONTH) + 1) || year != today.get(Calendar.YEAR)) {
            binding.btnAddMoney.setVisibility(View.GONE);
            binding.cvBalance.setVisibility(View.GONE);
        } else {
            binding.btnAddMoney.setVisibility(View.VISIBLE);
            binding.cvBalance.setVisibility(View.VISIBLE);
        }
    }

    // Cập nhật lại dữ liệu sau khi thay đổi tháng và năm
    public void updateData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<MoneyData> newMoneyList = db.getAllSpendingInMonth(month, year);
            if (newMoneyList.isEmpty()) {
                binding.txtCheckData.setVisibility(View.VISIBLE);
                binding.rvMoney.setVisibility(View.GONE);
            } else {
                binding.txtCheckData.setVisibility(View.GONE);
                binding.rvMoney.setVisibility(View.VISIBLE);
                moneyAdapter.refreshData(newMoneyList);
            }
        }
        showSpendingAndIncome(month, year);
        showBalance();
    }

    // Hiển thị tổng chi tiêu và thu nhập trong tháng
    private void showSpendingAndIncome(int month, int year) {
        // Lấy tổng chi tiêu và thu nhập trong tháng
        totalSpending = db.getTotalSpendingByCurrentMonth(month, year);
        Log.d("TotalSpending", "Tổng chi tiêu trong tháng: " + formatCurrency(totalSpending));

        totalIncome = db.getTotalIncomeByCurrentMonth(month, year);
        Log.d("TotalIncome", "Tổng thu nhập trong tháng: " + formatCurrency(totalIncome));

        total = totalSpending + totalIncome;
        Log.d("Total", "Tổng chi tiêu và thu nhập trong tháng: " + formatCurrency(total));

        // Hiển thị tổng chi tiêu và thu nhập trong tháng
        binding.txtSpending.setText(formatCurrency(totalSpending));
        binding.txtIncome.setText(formatCurrency(totalIncome));
        if (total > 0) {
            binding.txtAccountBalance.setText("+ " + formatCurrency(total));
        } else {
            binding.txtAccountBalance.setText(formatCurrency(total));
        }
    }

    // Hiển thị ngưỡng chi tiêu và số dư
    private void showBalance() {
        // Lấy giá trị thiết lập số dư tài khoản
        spendingThreshold = db.getSetupBalance();
        double balance = spendingThreshold + totalSpending;

        if (spendingThreshold != 0) {
            Log.d(TAG, "Số dư tài khoản: " + formatCurrency(balance));
            binding.txtSpendingThreshold.setText(formatCurrency(spendingThreshold));
            binding.txtBalance.setText(formatCurrency(balance));
            binding.pbAccountBalance.setProgress((int) ((balance / spendingThreshold) * 100));

            Drawable progressDrawable = binding.pbAccountBalance.getProgressDrawable().mutate();
            if (balance < 0) {
                progressDrawable.setColorFilter(getResources().getColor(R.color.purple), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.pbAccountBalance.setProgressDrawable(progressDrawable);
                binding.txtWarning.setText(R.string.warning_purple);
                binding.txtWarning.setTextColor(getResources().getColor(R.color.purple));
            } else if (0 < balance && balance < (spendingThreshold / 4)) { // Số dư tài khoản dưới 1/4 ngưỡng
                progressDrawable.setColorFilter(getResources().getColor(R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.pbAccountBalance.setProgressDrawable(progressDrawable);
                binding.txtWarning.setText(R.string.warning_red);
                binding.txtWarning.setTextColor(getResources().getColor(R.color.red));
            } else if ((spendingThreshold / 4) <= balance && balance <= (spendingThreshold / 2)) { // Số dư tài khoản trong khoảng 1/4 - 1/2 ngưỡng
                progressDrawable.setColorFilter(getResources().getColor(R.color.orange), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.pbAccountBalance.setProgressDrawable(progressDrawable);
                binding.txtWarning.setText(R.string.warning_orange);
                binding.txtWarning.setTextColor(getResources().getColor(R.color.orange));
            } else if ((spendingThreshold / 2) < balance && balance <= (3 * spendingThreshold / 4)) { // Số dư tài khoản trong khoảng 1/2 - 3/4 ngưỡng)
                progressDrawable.setColorFilter(getResources().getColor(R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.pbAccountBalance.setProgressDrawable(progressDrawable);
                binding.txtWarning.setText(R.string.warning_yellow);
                binding.txtWarning.setTextColor(getResources().getColor(R.color.yellow));
            } else {  // Số dư tài khoản trên 3/4 ngưỡng
                progressDrawable.setColorFilter(getResources().getColor(R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN);
                binding.pbAccountBalance.setProgressDrawable(progressDrawable);
                binding.txtWarning.setText(R.string.warning_blue);
                binding.txtWarning.setTextColor(getResources().getColor(R.color.blue));
            }
        } else {
            Log.d(TAG, "Số dư tài khoản: 0");
            binding.txtSpendingThreshold.setText("0");
            binding.txtBalance.setText("0");
            binding.pbAccountBalance.setProgress(0);
            binding.txtWarning.setText("Chưa thiết lập ngưỡng chi tiêu");
        }
    }

    // Định dạng số theo dạng phân tách phần nghìn
    private String formatCurrency(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        return numberFormat.format(amount);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            moneyDataList = db.getAllSpendingInMonth(month, year);
            if (moneyDataList.isEmpty()) {
                binding.txtCheckData.setVisibility(View.VISIBLE);
                binding.rvMoney.setVisibility(View.GONE);
            } else {
                binding.txtCheckData.setVisibility(View.GONE);
                binding.rvMoney.setVisibility(View.VISIBLE);
                moneyAdapter.refreshData(moneyDataList);
            }
        }
        showSpendingAndIncome(month, year);
        showBalance();
    }
}
