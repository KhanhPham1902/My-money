package com.example.mymoney.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymoney.R;
import com.example.mymoney.listener.OnItemClickListener;
import com.example.mymoney.model.SpendingItem;

import java.util.ArrayList;
import java.util.List;

public class SpendingAdapter extends RecyclerView.Adapter<SpendingAdapter.SpendingViewHolder> {
    private List<SpendingItem> spendingItemList;
    private List<SpendingItem> originalList; // Lưu trữ danh sách ban đầu
    private Context context;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Vị trí được chọn ban đầu

    public SpendingAdapter(Context context, List<SpendingItem> spendingItemList, OnItemClickListener listener) {
        this.spendingItemList = spendingItemList;
        this.originalList = new ArrayList<>(spendingItemList); // Khởi tạo danh sách ban đầu
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpendingAdapter.SpendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spending, parent, false);
        return new SpendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpendingViewHolder holder, int position) {
        SpendingItem spendingItem = spendingItemList.get(position);
        holder.txtItem.setText(spendingItem.getItemName());
        holder.imgItemSpending.setImageResource(spendingItem.getImgItem());

        // Hiển thị imgItemCheck nếu item này được chọn
        holder.imgItemCheck.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition == position) {
                // Nếu đã chọn lại item đã chọn rồi, bỏ chọn
                selectedPosition = RecyclerView.NO_POSITION;
                updateList(originalList); // Hiển thị lại danh sách đầy đủ
            } else {
                // Nếu chọn item mới
                selectedPosition = holder.getAdapterPosition();
                // Cập nhật danh sách chỉ với mục đã chọn
                List<SpendingItem> singleItemList = new ArrayList<>();
                singleItemList.add(spendingItemList.get(position));
                updateList(singleItemList);

                // Su kien click vao item
                listener.onItemClick(spendingItem);
            }
            notifyDataSetChanged(); // Cập nhật lại giao diện sau khi thay đổi selectedPosition
        });
    }


    @Override
    public int getItemCount() {
        return spendingItemList.size();
    }

    public static class SpendingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItemSpending, imgItemCheck;
        TextView txtItem;

        public SpendingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItemSpending = itemView.findViewById(R.id.imgItemSpending);
            imgItemCheck = itemView.findViewById(R.id.imgItemCheck);
            txtItem = itemView.findViewById(R.id.txtItem);
        }
    }

    // Phương thức cập nhật danh sách
    public void updateList(List<SpendingItem> newList) {
        spendingItemList = newList;
        notifyDataSetChanged();
    }
}
