package com.example.mymoney.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mymoney.R;
import com.example.mymoney.adapter.SpendingAdapter;
import com.example.mymoney.databinding.ActivityEditSpendingBinding;
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

public class EditSpendingActivity extends AppCompatActivity implements OnItemClickListener {

    private ActivityEditSpendingBinding binding;
    private SpendingAdapter spendingAdapter;
    private List<SpendingItem> spendingItemList;
    private DatabaseHelper db;
    private Integer itemId = -1;
    private String category = "";
    private String money = "";
    private String note = "";
    private String date = "";
    private Integer type = 0;
    private Integer img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditSpendingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        handleEdittext(binding.edtMoneyEdit);

        setListener();
    }

    private void init() {
        // Khoi tao database
        db = new DatabaseHelper(this);

        // Lay du lieu tu DetailActivity
        MoneyData moneyData = (MoneyData) getIntent().getSerializableExtra("moneyData");
        if (moneyData != null) {
            money = moneyData.getMoney();
            date = moneyData.getDate();
            note = moneyData.getNote();

            category = moneyData.getSpendingItem().getItemName();
            type = moneyData.getSpendingItem().getItemType();
            if (type == 1) {
                binding.txtTypeEdit.setText(getString(R.string.income));
            } else {
                binding.txtTypeEdit.setText(getString(R.string.spending));
            }
            img = moneyData.getSpendingItem().getImgItem();

            binding.imgItemEdit.setImageResource(img);
            binding.txtCategoryEdit.setText(category);
            binding.edtMoneyEdit.setText(money);
            binding.txtDateEdit.setText(date);
            binding.edtNoteEdit.setText(note);

            Integer id = moneyData.getId();
            if (id == -1) {
                finish();
                Log.d("TAG", "onCreate: id = -1");
            } else {
                itemId = id;
            }
        }
    }

    // Xu ly cac su kien
    private void setListener() {
        // Luu cac thay doi
        binding.btnSave.setOnClickListener(v -> {
            String newMoney = binding.edtMoneyEdit.getText().toString().trim();
            String newNote = binding.edtNoteEdit.getText().toString().trim();
            if (!newMoney.equals(money)) {
                money = newMoney;
            } else if (!newNote.equals(note)) {
                note = newNote;
            }

            SpendingItem newSpendingItem = new SpendingItem(type, category, img);
            MoneyData newMoneyData = new MoneyData(itemId, money, newNote, newSpendingItem, date);
            db.updateSpending(newMoneyData);
            Toast.makeText(this, "Đã lưu những thay đổi", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        });

        // Mo giao dien chon danh muc
        binding.imgItemEdit.setOnClickListener(v -> {
            binding.editContainer.setVisibility(View.VISIBLE);
            setSelectItemUI();
        });

        // Chon ngay
        binding.txtDateEdit.setOnClickListener(v -> {
            selectDate();
        });

        // Thoat
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        // Nhấn vào vị trí bất kì để ẩn bàn phím ảo
        binding.main.setOnTouchListener((v, event) -> {
            hideKeyboard();
            binding.editContainer.setVisibility(View.INVISIBLE);
            return false;
        });
    }

    // Thiet lap giao dien chon danh muc
    private void setSelectItemUI() {
        // Khoi tao UI
        binding.txtSpendingEdit.setTextColor(getResources().getColor(R.color.blue));
        binding.txtIncomeEdit.setTextColor(getResources().getColor(R.color.gray));

        binding.rvItem.setLayoutManager(new LinearLayoutManager(this));
        spendingItemList = new ArrayList<>();
        initSpendingItems();
        spendingAdapter = new SpendingAdapter(this, spendingItemList, this);
        binding.rvItem.setAdapter(spendingAdapter);

        // Xu ly su kien
        // Sự kiện nhấn vào mục chi tiêu để hiển thị các danh mục chi tiêu
        binding.txtSpendingEdit.setOnClickListener(v -> {
            binding.txtSpendingEdit.setTextColor(getResources().getColor(R.color.blue));
            binding.txtIncomeEdit.setTextColor(getResources().getColor(R.color.gray));

            binding.rvItem.setLayoutManager(new LinearLayoutManager(this));
            spendingItemList = new ArrayList<>();
            initSpendingItems();
            spendingAdapter = new SpendingAdapter(this, spendingItemList, this);
            binding.rvItem.setAdapter(spendingAdapter);
        });

        // Sự kiện nhấn vào mục thu nhập để hiển thị các danh mục thu nhập
        binding.txtIncomeEdit.setOnClickListener(v -> {
            binding.txtSpendingEdit.setTextColor(getResources().getColor(R.color.gray));
            binding.txtIncomeEdit.setTextColor(getResources().getColor(R.color.blue));

            binding.rvItem.setLayoutManager(new LinearLayoutManager(this));
            spendingItemList = new ArrayList<>();
            initIncome();
            spendingAdapter = new SpendingAdapter(this, spendingItemList, this);
            binding.rvItem.setAdapter(spendingAdapter);
        });
    }

    // Chọn ngày
    private void selectDate() {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.now());
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        // Sự kiện nhấn nút chọn ngày
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            date = new SimpleDateFormat("dd 'thg' MM, yyyy", new Locale("vi")).format(new Date(selection));
            binding.txtDateEdit.setText(date);
        });

        // show DatePicker
        materialDatePicker.show(getSupportFragmentManager(), "tag");
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

    // ẩn bàn phím ảo
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    // Click vao item danh muc
    @Override
    public void onItemClick(SpendingItem spendingItem) {
        binding.editContainer.setVisibility(View.INVISIBLE);
        type = spendingItem.getItemType();
        category = spendingItem.getItemName();
        img = spendingItem.getImgItem();

        if (type == 1) {
            binding.txtTypeEdit.setText(getString(R.string.income));
        } else {
            binding.txtTypeEdit.setText(getString(R.string.spending));
        }
        binding.imgItemEdit.setImageResource(img);
        binding.txtCategoryEdit.setText(category);
    }
}