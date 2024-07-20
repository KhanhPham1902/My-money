package com.example.mymoney.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mymoney.model.MoneyData;
import com.example.mymoney.model.SpendingItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moneyapp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SPENDING_TABLE = "allspendings";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_MONEY = "money";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE = "image";

    private static final String BALANCE_TABLE = "balance_table";
    private static final String COLUMN_BALANCE = "balance";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Tạo database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Thiết lập bảng chi tiêu
        String createTableQuery = "CREATE TABLE " + SPENDING_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_MONEY + " TEXT, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_TYPE + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE + " INTEGER)";
        db.execSQL(createTableQuery);

        // Thiết lập bảng số dư
        String createBalanceTableQuery = "CREATE TABLE " + BALANCE_TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BALANCE + " TEXT)";
        db.execSQL(createBalanceTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + SPENDING_TABLE;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    // Thêm dữ liệu chi tiêu/ thu nhập vào database
    public void insertSpending(MoneyData moneyData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONEY, moneyData.getMoney());
        values.put(COLUMN_NOTE, moneyData.getNote());
        values.put(COLUMN_TYPE, moneyData.getSpendingItem().getItemType());
        values.put(COLUMN_CATEGORY, moneyData.getSpendingItem().getItemName());
        values.put(COLUMN_IMAGE, moneyData.getSpendingItem().getImgItem());
        values.put(COLUMN_DATE, moneyData.getDate());
        db.insert(SPENDING_TABLE, null, values);
        db.close();
    }

    // Thêm dữ liệu thiết lập ngưỡng chi tiêu vào database
    public void insertSetupBalance(String maxMoney) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BALANCE, maxMoney);

        // Kiểm tra xem đã có bản ghi nào trong bảng balance_table chưa
        String query = "SELECT " + COLUMN_ID + " FROM " + BALANCE_TABLE + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            // Đã có bản ghi, cập nhật giá trị balance
            String[] args = {String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)))};
            db.update(BALANCE_TABLE, values, "id=?", args);
        } else {
            // Chưa có bản ghi, chèn bản ghi mới
            db.insert(BALANCE_TABLE, null, values);
        }

        cursor.close();
        db.close();
    }

    // Lấy giá trị thiết lập số dư tài khoản
    public double getSetupBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        double balance = 0.0;
        String query = "SELECT " + COLUMN_BALANCE + " FROM " + BALANCE_TABLE + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            String balanceStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BALANCE));
            if (balanceStr != null) {
                balance = Double.parseDouble(balanceStr.replace(".", "")); // Loại bỏ dấu chấm phân tách hàng nghìn
            }
        }

        cursor.close();
        db.close();
        return balance;
    }

    // Lấy tất cả dữ liệu trong tháng
    public List<MoneyData> getAllSpendingInMonth(int month, int year) {
        List<MoneyData> spendingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Định dạng ngày trong database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' MM, yyyy");

        // Truy vấn để sắp xếp theo cột ngày tháng giảm dần
        String query = "SELECT * FROM " + SPENDING_TABLE + " ORDER BY " + COLUMN_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

            if (dateStr != null) {
                try {
                    Date date = dateFormat.parse(dateStr);
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);

                    int yearDb = dateCalendar.get(Calendar.YEAR);
                    int monthDb = dateCalendar.get(Calendar.MONTH) + 1;

                    if (yearDb == year && monthDb == month) {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String money = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONEY));
                        String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                        int image = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
                        SpendingItem spendingItem = new SpendingItem(type, category, image);
                        MoneyData moneyData = new MoneyData(id, money, note, spendingItem, dateStr);
                        spendingList.add(moneyData);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        cursor.close();
        db.close();
        return spendingList;
    }

    // Phương thức lấy tất cả mục chi tiêu (type = 0) trong tháng hiện tại
    public List<MoneyData> getSpendingInMonth(int month, int year) {
        List<MoneyData> spendingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Định dạng ngày trong database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' MM, yyyy");

        // Truy vấn để chọn các mục chi tiêu với type = 0
        String query = "SELECT * FROM " + SPENDING_TABLE + " WHERE " + COLUMN_TYPE + " = 0";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

            if (dateStr != null) {
                try {
                    Date date = dateFormat.parse(dateStr);
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);

                    int yearDb = dateCalendar.get(Calendar.YEAR);
                    int monthDb = dateCalendar.get(Calendar.MONTH) + 1;

                    if (yearDb == year && monthDb == month) {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String money = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONEY));
                        String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                        int image = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
                        SpendingItem spendingItem = new SpendingItem(type, category, image);
                        MoneyData moneyData = new MoneyData(id, money, note, spendingItem, dateStr);
                        spendingList.add(moneyData);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
        db.close();

        return spendingList;
    }

    // Phương thức lấy tất cả mục thu nhập (type = 1) trong tháng hiện tại
    public List<MoneyData> getIncomeInMonth(int month, int year) {
        List<MoneyData> incomeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Định dạng ngày trong database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' MM, yyyy");

        // Truy vấn để chọn các mục thu nhập với type = 1
        String query = "SELECT * FROM " + SPENDING_TABLE + " WHERE " + COLUMN_TYPE + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

            if (dateStr != null) {
                try {
                    Date date = dateFormat.parse(dateStr);
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);

                    int yearDb = dateCalendar.get(Calendar.YEAR);
                    int monthDb = dateCalendar.get(Calendar.MONTH) + 1;

                    if (yearDb == year && monthDb == month) {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String money = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONEY));
                        String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                        int image = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
                        SpendingItem spendingItem = new SpendingItem(type, category, image);
                        MoneyData moneyData = new MoneyData(id, money, note, spendingItem, dateStr);
                        incomeList.add(moneyData);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
        db.close();

        return incomeList;
    }

    // Phương thức tính tổng chi tiêu trong tháng
    public double getTotalSpendingByCurrentMonth(int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalSpending = 0.0;

        // Định dạng ngày trong database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' MM, yyyy");

        // Truy vấn để chọn các giá trị "money" với type = 0
        String query = "SELECT " + COLUMN_MONEY + ", " + COLUMN_DATE + " FROM " + SPENDING_TABLE + " WHERE " + COLUMN_TYPE + " = 0";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String moneyStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONEY));
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

            if (moneyStr != null && dateStr != null) {
                try {
                    Date date = dateFormat.parse(dateStr);
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);

                    int yearDb = dateCalendar.get(Calendar.YEAR);
                    int monthDb = dateCalendar.get(Calendar.MONTH) + 1;

                    if (yearDb == year && monthDb == month) {
                        double money = Double.parseDouble(moneyStr.replace(".", "")); // Loại bỏ dấu chấm phân tách hàng nghìn
                        totalSpending -= money; // Trừ tiền chi tiêu
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
        db.close();

        return totalSpending;
    }

    // Phương thức tính tổng thu nhập trong tháng
    public double getTotalIncomeByCurrentMonth(int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalIncome = 0.0;

        // Định dạng ngày trong database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' MM, yyyy");

        // Truy vấn để chọn các giá trị "money" với type = 1
        String query = "SELECT " + COLUMN_MONEY + ", " + COLUMN_DATE + " FROM " + SPENDING_TABLE + " WHERE " + COLUMN_TYPE + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String moneyStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONEY));
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

            if (moneyStr != null && dateStr != null) {
                try {
                    Date date = dateFormat.parse(dateStr);
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);

                    int yearDb = dateCalendar.get(Calendar.YEAR);
                    int monthDb = dateCalendar.get(Calendar.MONTH) + 1;

                    if (yearDb == year && monthDb == month) {
                        double money = Double.parseDouble(moneyStr.replace(".", ""));  // Loại bỏ dấu chấm phân tách hàng nghìn
                        totalIncome += money; // Cộng tiền thu nhập
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
        db.close();

        return totalIncome;
    }

    // Cập nhật
    public void updateSpending(MoneyData moneyData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONEY, moneyData.getMoney());
        values.put(COLUMN_NOTE, moneyData.getNote());
        values.put(COLUMN_TYPE, moneyData.getSpendingItem().getItemType());
        values.put(COLUMN_CATEGORY, moneyData.getSpendingItem().getItemName());
        values.put(COLUMN_IMAGE, moneyData.getSpendingItem().getImgItem());
        values.put(COLUMN_DATE, moneyData.getDate());
        String clause = COLUMN_ID + " = ?";
        String[] args = {String.valueOf(moneyData.getId())};
        db.update(SPENDING_TABLE, values, clause, args);
        db.close();
    }

    // Lấy item theo id
    public MoneyData getSpendingById(int spendingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + SPENDING_TABLE + " WHERE " + COLUMN_ID + " = " + spendingId;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String money = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONEY));
        String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
        int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
        int image = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
        SpendingItem spendingItem = new SpendingItem(type, category, image);
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));

        cursor.close();
        db.close();
        return new MoneyData(id, money, note, spendingItem, date);
    }

    // Xóa item theo id
    public void deleteSpendingById(int spendingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String clause = COLUMN_ID + " = ?";
        String[] args = {String.valueOf(spendingId)};
        db.delete(SPENDING_TABLE, clause, args);
        db.close();
    }
}
