package com.example.mymoney.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mymoney.R;
import com.example.mymoney.adapter.SpendingAdapter;
import com.example.mymoney.databinding.ActivityAddSpendingBinding;
import com.example.mymoney.listener.OnItemClickListener;
import com.example.mymoney.model.MoneyData;
import com.example.mymoney.model.SpendingItem;
import com.example.mymoney.utility.DatabaseHelper;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddSpendingActivity extends AppCompatActivity implements OnItemClickListener {

    private ActivityAddSpendingBinding binding;
    private SpendingAdapter spendingAdapter;
    private List<SpendingItem> spendingItemList;
    private DatabaseHelper db;
    private String timeSpending = "";
    private String money = "";
    private SpendingItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddSpendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

        // Khoi tao Database
        db = new DatabaseHelper(this);

        // Sự kiện nhấn nút lưu
        binding.btnSave.setOnClickListener(v -> {
            String money = binding.edtSpending.getText().toString();
            String note = binding.edtNote.getText().toString();
            if (money.isEmpty()) {
                Toast.makeText(this, "Bạn chưa nhập số tiền!", Toast.LENGTH_SHORT).show();
            } else if (timeSpending == null || timeSpending.isEmpty()) {
                Toast.makeText(this, "Bạn chưa chọn ngày!", Toast.LENGTH_SHORT).show();
            } else if (item == null) {
                Toast.makeText(this, "Bạn chưa chọn mục chi tiêu!", Toast.LENGTH_SHORT).show();
            } else {
                MoneyData moneyData = new MoneyData(0, money, note, item, timeSpending);
                db.insertSpending(moneyData);
                finish();
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }
        });

        handleEdittext(binding.edtSpending);

        setListener();
    }

    // Khoi tao giao dien
    private void initView() {

        binding.edtSpending.requestFocus();

        binding.btnSpending.setTextColor(getColor(R.color.blue));
        binding.btnIncome.setTextColor(getColor(R.color.gray));

        binding.rvMoney.setLayoutManager(new LinearLayoutManager(this));
        spendingItemList = new ArrayList<>();
        initSpendingItems();
        spendingAdapter = new SpendingAdapter(this, spendingItemList, this);
        binding.rvMoney.setAdapter(spendingAdapter);
    }

    // Khởi tạo danh sách các mục chi tiêu
    private void initSpendingItems() {
        spendingItemList.add(new SpendingItem(0, getString(R.string.eat), R.drawable.eat));
        spendingItemList.add(new SpendingItem(0, getString(R.string.shopping), R.drawable.shopping));
        spendingItemList.add(new SpendingItem(0, getString(R.string.sport), R.drawable.sports));
        spendingItemList.add(new SpendingItem(0, getString(R.string.book), R.drawable.book));
        spendingItemList.add(new SpendingItem(0, getString(R.string.clothes), R.drawable.clothes));
        spendingItemList.add(new SpendingItem(0, getString(R.string.traffic), R.drawable.car));
        spendingItemList.add(new SpendingItem(0, getString(R.string.tax), R.drawable.tax));
        spendingItemList.add(new SpendingItem(0, getString(R.string.call), R.drawable.call));
        spendingItemList.add(new SpendingItem(0, getString(R.string.pet), R.drawable.pet));
        spendingItemList.add(new SpendingItem(0, getString(R.string.food), R.drawable.vegetable));
        spendingItemList.add(new SpendingItem(0, getString(R.string.water_fee), R.drawable.water));
        spendingItemList.add(new SpendingItem(0, getString(R.string.drink), R.drawable.wine));
        spendingItemList.add(new SpendingItem(0, getString(R.string.gift), R.drawable.gift));
        spendingItemList.add(new SpendingItem(0, getString(R.string.health), R.drawable.health));
        spendingItemList.add(new SpendingItem(0, getString(R.string.edu), R.drawable.edu));
        spendingItemList.add(new SpendingItem(0, getString(R.string.game), R.drawable.game));
        spendingItemList.add(new SpendingItem(0, getString(R.string.insurance), R.drawable.security));
        spendingItemList.add(new SpendingItem(0, getString(R.string.snacks), R.drawable.ice_cream));
        spendingItemList.add(new SpendingItem(0, getString(R.string.electric), R.drawable.electric));
        spendingItemList.add(new SpendingItem(0, getString(R.string.travel), R.drawable.travel));
        spendingItemList.add(new SpendingItem(0, getString(R.string.cosmetics), R.drawable.lipstick));
        spendingItemList.add(new SpendingItem(0, getString(R.string.house), R.drawable.home));
        spendingItemList.add(new SpendingItem(0, getString(R.string.other), R.drawable.more));
    }

    // Khởi tạo danh sách các mục thu nhập
    private void initIncome() {
        spendingItemList.add(new SpendingItem(1, getString(R.string.salary), R.drawable.salary));
        spendingItemList.add(new SpendingItem(1, getString(R.string.refund), R.drawable.refund));
        spendingItemList.add(new SpendingItem(1, getString(R.string.donation), R.drawable.donation));
        spendingItemList.add(new SpendingItem(1, getString(R.string.lease), R.drawable.lease));
        spendingItemList.add(new SpendingItem(1, getString(R.string.stock), R.drawable.stock));
        spendingItemList.add(new SpendingItem(1, getString(R.string.sale), R.drawable.sale));
        spendingItemList.add(new SpendingItem(1, getString(R.string.other), R.drawable.more));
    }

    private void setListener() {
        // Sự kiện nhấn vào mục chi tiêu để hiển thị các danh mục chi tiêu
        binding.btnSpending.setOnClickListener(v -> {
            binding.btnSpending.setTextColor(getColor(R.color.blue));
            binding.btnIncome.setTextColor(getColor(R.color.gray));

            binding.rvMoney.setLayoutManager(new LinearLayoutManager(this));
            spendingItemList = new ArrayList<>();
            initSpendingItems();
            spendingAdapter = new SpendingAdapter(this, spendingItemList, this);
            binding.rvMoney.setAdapter(spendingAdapter);
        });

        // Sự kiện nhấn vào mục thu nhập để hiển thị các danh mục thu nhập
        binding.btnIncome.setOnClickListener(v -> {
            binding.btnSpending.setTextColor(getColor(R.color.gray));
            binding.btnIncome.setTextColor(getColor(R.color.blue));

            binding.rvMoney.setLayoutManager(new LinearLayoutManager(this));
            spendingItemList = new ArrayList<>();
            initIncome();
            spendingAdapter = new SpendingAdapter(this, spendingItemList, this);
            binding.rvMoney.setAdapter(spendingAdapter);
        });

        // Sự kiện nhấn chọn ngày
        binding.layoutTime.setOnClickListener(v -> {
            // Lấy ngày hiện tại làm ngày mặc định
            timeSpending = new SimpleDateFormat("dd 'thg' MM, yyyy", new Locale("vi")).format(new Date());
            binding.txtTimeSpending.setText(timeSpending);

            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointBackward.now());
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            // Sự kiện nhấn nút chọn ngày
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                timeSpending = new SimpleDateFormat("dd 'thg' MM, yyyy", new Locale("vi")).format(new Date(selection));
                binding.txtTimeSpending.setText(timeSpending);
            });

            // Sự kiện nhấn nút Hủy
            materialDatePicker.addOnDismissListener(dialog -> {
                if (timeSpending == null) {
                    timeSpending = new SimpleDateFormat("dd 'thg' MM, yyyy", new Locale("vi")).format(new Date());
                    binding.txtTimeSpending.setText(timeSpending);
                }
            });

            // show DatePicker
            materialDatePicker.show(getSupportFragmentManager(), "tag");
        });

        // Nhấn vào vị trí bất kì để ẩn bàn phím ảo
        binding.layoutSpending.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        // Sự kiện nhấn nút Back
        binding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
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

                    money = cleanString;
                    Log.d("money", money);

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    // Nhấn vào vị trí bất kì để ẩn bàn phím ảo
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onItemClick(SpendingItem spendingItem) {
        item = spendingItem;
    }
}