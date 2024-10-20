package com.app.smartpos.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;

import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.MyViewHolder> {


    private final List<HashMap<String, String>> paymentMethodData;
    private final Context context;


    public PaymentMethodAdapter(Context context, List<HashMap<String, String>> paymentMethodData) {
        this.context = context;
        this.paymentMethodData = paymentMethodData;

    }


    @NonNull
    @Override
    public PaymentMethodAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_method_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final PaymentMethodAdapter.MyViewHolder holder, int position) {

        final String payment_method_id = paymentMethodData.get(position).get("payment_method_id");
        String payment_method_name = paymentMethodData.get(position).get("payment_method_name");
        int status = Integer.parseInt(paymentMethodData.get(position).get("payment_method_active"));

        holder.txtPaymentMethodName.setText(payment_method_name);
        holder.imgStatus.setImageResource(status == 1 ? R.drawable.active_oval : R.drawable.disable_oval);

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.want_to_delete)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                                databaseAccess.open();
                                boolean deleteCustomer = databaseAccess.deletePaymentMethod(payment_method_id);

                                if (deleteCustomer) {
                                    Toasty.success(context, R.string.payment_method_deleted, Toast.LENGTH_SHORT).show();

                                    paymentMethodData.remove(holder.getAdapterPosition());

                                    // Notify that item at position has been removed
                                    notifyItemRemoved(holder.getAdapterPosition());

                                } else {
                                    Toasty.error(context, R.string.failed, Toast.LENGTH_SHORT).show();
                                }
                                dialog.cancel();

                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Perform Your Task Here--When No is pressed
                                dialog.cancel();
                            }
                        }).show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return paymentMethodData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtPaymentMethodName;
        ImageView imgDelete, imgStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPaymentMethodName = itemView.findViewById(R.id.txt_payment_method_name);

            imgDelete = itemView.findViewById(R.id.img_delete);

            imgStatus = itemView.findViewById(R.id.img_status);


        }


    }


}
