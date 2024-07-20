package com.example.mymoney.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymoney.R;
import com.example.mymoney.activity.DetailActivity;
import com.example.mymoney.model.MoneyData;

import java.util.List;

public class MoneyAdapter extends RecyclerView.Adapter<MoneyAdapter.MoneyViewHolder> {
    private List<MoneyData> moneyList;
    private Context context;

    public MoneyAdapter(Context context, List<MoneyData> moneyList) {
        this.moneyList = moneyList;
        this.context = context;
    }

    @NonNull
    @Override
    public MoneyAdapter.MoneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_money, parent, false);
        return new MoneyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoneyAdapter.MoneyViewHolder holder, int position) {
        MoneyData moneyData = moneyList.get(position);
        holder.txtItemMoney.setText(moneyData.getMoney());
        holder.txtItemDate.setText(moneyData.getDate());
        holder.txtItemCategory.setText(moneyData.getSpendingItem().getItemName());
        holder.imgItem.setImageResource(moneyData.getSpendingItem().getImgItem());
        holder.txtItemNote.setText(moneyData.getNote());

        int type = moneyData.getSpendingItem().getItemType();
        if(type == 1){
            holder.txtItemPlusOrMinus.setText("+");
            holder.txtItemSpending.setVisibility(View.GONE);
            holder.txtItemIncome.setVisibility(View.VISIBLE);
            holder.txtItemIncome.setText("Thu nhập: +"+moneyData.getMoney());
        }else{
            holder.txtItemPlusOrMinus.setText("-");
            holder.txtItemSpending.setVisibility(View.VISIBLE);
            holder.txtItemIncome.setVisibility(View.GONE);
            holder.txtItemSpending.setText("Chi tiêu: -"+moneyData.getMoney());
        }

        // Su kien click vao item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("moneyData", moneyData);
            context.startActivity(intent);
        });

        // Su kien nhan giu vao item
        holder.itemView.setOnLongClickListener(v -> {

            return true;
        });
    }

    public void refreshData(List<MoneyData> newMoneyList){
        moneyList = newMoneyList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return moneyList.size();
    }

    public static class MoneyViewHolder extends RecyclerView.ViewHolder{

        TextView txtItemMoney, txtItemDate, txtItemCategory, txtItemNote, txtItemPlusOrMinus, txtItemSpending, txtItemIncome;
        ImageView imgItem;
        public MoneyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItemMoney = itemView.findViewById(R.id.txtItemMoney);
            txtItemDate = itemView.findViewById(R.id.txtItemDate);
            txtItemCategory = itemView.findViewById(R.id.txtItemCategory);
            txtItemNote = itemView.findViewById(R.id.txtItemNote);
            txtItemPlusOrMinus = itemView.findViewById(R.id.txtItemPlusOrMinus);
            imgItem = itemView.findViewById(R.id.imgItem);
            txtItemSpending = itemView.findViewById(R.id.txtItemSpending);
            txtItemIncome = itemView.findViewById(R.id.txtItemIncome);
        }
    }
}
