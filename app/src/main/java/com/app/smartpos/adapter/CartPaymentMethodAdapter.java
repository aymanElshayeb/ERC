package com.app.smartpos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.checkout.NewCheckout;

import java.util.HashMap;
import java.util.List;

public class CartPaymentMethodAdapter extends RecyclerView.Adapter<CartPaymentMethodAdapter.MyViewHolder> {


    private final List<HashMap<String, String>> paymentMethodData;
    private final NewCheckout checkout;
    private int selectedItem = -1;

    public CartPaymentMethodAdapter(NewCheckout checkout, List<HashMap<String, String>> paymentMethodData) {
        this.checkout = checkout;
        this.paymentMethodData = paymentMethodData;

    }


    @NonNull
    @Override
    public CartPaymentMethodAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_cart_payment_method_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final CartPaymentMethodAdapter.MyViewHolder holder, int position) {

        final String payment_method_id = paymentMethodData.get(position).get("payment_method_id");
        String payment_method_name = paymentMethodData.get(position).get("payment_method_name");
        int status = Integer.parseInt(paymentMethodData.get(position).get("payment_method_active"));

        if (payment_method_name.equalsIgnoreCase("cash")) {
            holder.text.setText(checkout.getString(R.string.cash_ar));
        } else {
            holder.text.setText(checkout.getString(R.string.card_ar));
        }
        holder.image.setImageResource(payment_method_name.equals("CASH") ? R.drawable.ic_cash : R.drawable.ic_card);

        holder.itemView.setBackgroundResource(selectedItem == position ? R.drawable.custom_payment_method_selected_item : R.drawable.custom_payment_method_item);

        holder.itemView.setOnClickListener(view -> {
            checkout.setPaymentType(paymentMethodData.get(position).get("payment_method_name"));
            selectedItem = position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethodData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        ImageView image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);

            text = itemView.findViewById(R.id.text);

        }
    }


}
