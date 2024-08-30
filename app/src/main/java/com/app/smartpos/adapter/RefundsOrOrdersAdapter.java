package com.app.smartpos.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.refund.RefundOrOrderDetails;
import com.app.smartpos.refund.RefundOrOrderList;

import java.util.HashMap;
import java.util.List;

public class RefundsOrOrdersAdapter extends RecyclerView.Adapter<RefundsOrOrdersAdapter.MyViewHolder> {


    RefundOrOrderList refundOrOrderList;
    private List<HashMap<String, String>> orderData;


    public RefundsOrOrdersAdapter(RefundOrOrderList refundOrOrderList, List<HashMap<String, String>> orderData) {
        this.refundOrOrderList = refundOrOrderList;
        this.orderData = orderData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_refund_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {


        String in_tax_total=orderData.get(position).get("in_tax_total");
        String invoice_id=orderData.get(position).get("invoice_id");
        String operation_type=orderData.get(position).get("operation_type");
        String payment_method=orderData.get(position).get("order_payment_method");
        String order_type=orderData.get(position).get("order_type");
        String orderStatus = orderData.get(position).get(Constant.ORDER_STATUS);

        Log.i("datadata",orderData.get(position).toString());

        holder.amount_tv.setText(in_tax_total);
        holder.receipt_number_tv.setText(invoice_id);
        holder.currency_tv.setText(refundOrOrderList.getCurrency());
        holder.card_tv.setVisibility(payment_method.equals("CARD")?View.VISIBLE:View.GONE);
        holder.cash_tv.setVisibility(payment_method.equals("CASH")?View.VISIBLE:View.GONE);
        if(refundOrOrderList.isRefund()) {

        }
        holder.refunded_tv.setVisibility(operation_type.equals("refund") ? View.VISIBLE : View.GONE);
        if(position==orderData.size()-1){
            refundOrOrderList.loadMore();
        }
    }

    @Override
    public int getItemCount() {
        return orderData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView amount_tv,currency_tv,receipt_number_tv,card_tv, cash_tv,refunded_tv,view_tv;


        public MyViewHolder(View itemView) {
            super(itemView);

            amount_tv = itemView.findViewById(R.id.amount_tv);
            currency_tv = itemView.findViewById(R.id.currency_tv);
            receipt_number_tv= itemView.findViewById(R.id.receipt_number_tv);
            card_tv = itemView.findViewById(R.id.card_tv);
            cash_tv= itemView.findViewById(R.id.cash_tv);
            refunded_tv= itemView.findViewById(R.id.refunded_tv);
            view_tv =itemView.findViewById(R.id.view_tv);

            view_tv.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(refundOrOrderList, RefundOrOrderDetails.class).putExtra("isRefund",refundOrOrderList.isRefund());
            i.putExtra("order_id",orderData.get(getAdapterPosition()).get("invoice_id"));
            i.putExtra("order_payment_method",orderData.get(getAdapterPosition()).get("order_payment_method"));
            i.putExtra("operation_type",orderData.get(getAdapterPosition()).get("operation_type"));
            if(refundOrOrderList.isRefund()) {
                refundOrOrderList.finish();
            }
            refundOrOrderList.startActivity(i);
        }
    }




}