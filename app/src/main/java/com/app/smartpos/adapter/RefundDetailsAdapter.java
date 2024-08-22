package com.app.smartpos.adapter;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.Constant;
import com.app.smartpos.R;
import com.app.smartpos.refund.RefundDetails;

import java.util.HashMap;
import java.util.List;

public class RefundDetailsAdapter extends RecyclerView.Adapter<RefundDetailsAdapter.MyViewHolder> {


    RefundDetails refundDetails;
    private List<HashMap<String, String>> orderData;


    public RefundDetailsAdapter(RefundDetails refundDetails, List<HashMap<String, String>> orderData) {
        this.refundDetails = refundDetails;
        this.orderData = orderData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_refund_details_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        double in_tax_total = Double.parseDouble(orderData.get(position).get("in_tax_total"));
        double product_price = Double.parseDouble(orderData.get(position).get("product_price"));
        String product_image = orderData.get(position).get("product_image");
        String product_name_en = orderData.get(position).get("product_name_en");
        String product_name_ar = orderData.get(position).get("product_name_ar");
        String product_uuid = orderData.get(position).get("product_uuid");
        String payment_method = orderData.get(position).get("order_payment_method");
        double product_qty = Double.parseDouble(orderData.get(position).get("product_qty"));
        final double[] refund_qty = {Double.parseDouble(orderData.get(position).get("refund_qty"))};
        String item_checked = orderData.get(position).get("item_checked");
        String order_type = orderData.get(position).get("order_type");
        String orderStatus = orderData.get(position).get(Constant.ORDER_STATUS);

        holder.product_name_tv.setText(product_name_en);

        holder.qty_tv.setText(refundDetails.getString(R.string.total_qty) + " " + product_qty);
        holder.currency_tv.setText(refundDetails.getCurrency());

        holder.line.setAlpha(position == orderData.size() - 1 ? 0f : 1.0f);

        if (item_checked.equals("1")) {
            holder.checkbox_im.setImageResource(R.drawable.ic_box_check);
            holder.refund_qty_ll.setVisibility(View.VISIBLE);
            holder.number_tv.setText(refund_qty[0] + "");
            if (refund_qty[0] > 0) {
                holder.amount_ll.setVisibility(View.VISIBLE);
                holder.number_tv.setText(refund_qty[0] + "");
                holder.amount_tv.setText((product_price * refund_qty[0]) + " " + refundDetails.getCurrency());
            } else {
                holder.amount_ll.setVisibility(View.GONE);
            }
        } else {
            holder.checkbox_im.setImageResource(R.drawable.ic_box_uncheck);
            holder.refund_qty_ll.setVisibility(View.GONE);
        }

        String base64Image = product_image;

        if (product_uuid.equals("PR999999")) {
            holder.product_im.setImageResource(R.drawable.ic_custom_option_gray);
        } else {
            if (base64Image != null) {
                if (base64Image.isEmpty() || base64Image.length() < 6) {
                    holder.product_im.setImageResource(R.drawable.image_placeholder);
                } else {
                    byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                    holder.product_im.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

                }
            }
        }

        holder.plus_im.setOnClickListener(view -> {
            if (refund_qty[0] < product_qty) {
                refund_qty[0] += 1;
                orderData.get(position).put("refund_qty", refund_qty[0] + "");
                notifyItemChanged(position);
                refundDetails.updateTotalAmount();
            }
        });

        holder.minus_im.setOnClickListener(view -> {
            if (refund_qty[0] > 0) {
                refund_qty[0] -= 1;
                orderData.get(position).put("refund_qty", refund_qty[0] + "");
                notifyItemChanged(position);
                refundDetails.updateTotalAmount();
            }
        });

        holder.checkbox_im.setOnClickListener(view -> {
            if (item_checked.equals("1")) {
                orderData.get(position).put("item_checked", "0");
            } else {
                orderData.get(position).put("item_checked", "1");
            }
            refundDetails.updateTotalAmount();
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return orderData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView product_name_tv, qty_tv, number_tv, amount_tv, currency_tv;
        ImageView product_im, plus_im, minus_im, checkbox_im;
        LinearLayout refund_qty_ll, amount_ll;
        View line;

        public MyViewHolder(View itemView) {
            super(itemView);

            product_name_tv = itemView.findViewById(R.id.product_name_tv);
            qty_tv = itemView.findViewById(R.id.qty_tv);
            plus_im = itemView.findViewById(R.id.plus_im);
            minus_im = itemView.findViewById(R.id.minus_im);
            number_tv = itemView.findViewById(R.id.number_tv);
            product_im = itemView.findViewById(R.id.product_im);
            refund_qty_ll = itemView.findViewById(R.id.refund_qty_ll);
            checkbox_im = itemView.findViewById(R.id.checkbox_im);

            amount_ll = itemView.findViewById(R.id.amount_ll);
            amount_tv = itemView.findViewById(R.id.amount_tv);
            currency_tv = itemView.findViewById(R.id.currency_tv);

            line = itemView.findViewById(R.id.line);


        }


    }


}