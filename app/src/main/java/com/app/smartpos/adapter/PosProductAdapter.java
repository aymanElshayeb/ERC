package com.app.smartpos.adapter;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.Items.Items;
import com.app.smartpos.R;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.product.EditProductActivity;

import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PosProductAdapter extends RecyclerView.Adapter<PosProductAdapter.MyViewHolder> {


    private List<HashMap<String, String>> productData;
    private Activity productActivity;
    MediaPlayer player;
    public static int count;
    DatabaseAccess databaseAccess;

    public PosProductAdapter(Activity productActivity, List<HashMap<String, String>> productData) {
        this.productActivity = productActivity;
        this.productData = productData;
        player = MediaPlayer.create(productActivity, R.raw.delete_sound);

    }


    @NonNull
    @Override
    public PosProductAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pos_new_product_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final PosProductAdapter.MyViewHolder holder, int position) {

        databaseAccess = DatabaseAccess.getInstance(productActivity);

        databaseAccess.open();
        String currency = databaseAccess.getCurrency();

        final String product_id = productData.get(position).get("product_id");
        String name = productData.get(position).get("product_name_en");
        final String product_category = productData.get(position).get("product_category");
        final String product_weight = productData.get(position).get("product_weight");
        final String product_count = ((Items)productActivity).checkCount(position);
        final String product_desc = productData.get(position).get("product_description");
//        final double product_stock = Double.parseDouble(productData.get(position).get("product_stock"));
        final String product_price = productData.get(position).get("product_sell_price");
        final String weight_unit_id = productData.get(position).get("product_weight_unit_id");
        String base64Image = productData.get(position).get("product_image");




        databaseAccess.open();
        final String weight_unit_name = databaseAccess.getWeightUnitName(weight_unit_id);

        holder.txtProductName.setText(name);

        databaseAccess.open();
        String categoryName = databaseAccess.getCategoryName(product_category);

        holder.txtCategory.setText(categoryName);
        holder.txtDesc.setText(product_desc);
        holder.txtPrice.setText(trimLongDouble(product_price)+" "+currency);
        holder.txtCount.setText(product_count);

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                player.start();
//                Intent intent=new Intent(productActivity, EditProductActivity.class);
//                intent.putExtra("product_id",product_id);
//                productActivity.startActivity(intent);
//            }
//        });



        if (base64Image != null) {
            if (base64Image.length() < 6) {
                holder.product_image.setImageResource(R.drawable.image_placeholder);
                holder.product_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {


                byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                holder.product_image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

            }
        }


        holder.plusIm.setOnClickListener(v -> {


            if (1==0)
            {

                Toasty.warning(productActivity, R.string.stock_is_low_please_update_stock, Toast.LENGTH_SHORT).show();
            }
            else {
                int product_count_int = Integer.parseInt(product_count)+1;
                productData.get(position).put("product_count",""+product_count_int);
                notifyItemChanged(position);
                ((Items)productActivity).updateCart(productData.get(position),position);
            }
        });

        holder.minusIm.setOnClickListener(v -> {


            if (1==0)
            {

                Toasty.warning(productActivity, R.string.stock_is_low_please_update_stock, Toast.LENGTH_SHORT).show();
            }
            else {
                int product_count_int = Integer.parseInt(product_count)-1;
                if(product_count_int >= 0){
                    productData.get(position).put("product_count",""+product_count_int);
                    notifyItemChanged(position);
                    ((Items)productActivity).updateCart(productData.get(position),position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return productData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardProduct;
        TextView txtProductName,txtDesc, txtCategory, txtPrice,txtCount;
        ImageView product_image;
        ImageView minusIm,plusIm;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtCategory = itemView.findViewById(R.id.category_tv);
            txtPrice = itemView.findViewById(R.id.txt_price);
            txtDesc = itemView.findViewById(R.id.product_desc_tv);
            txtCount = itemView.findViewById(R.id.product_count_tv);
            product_image = itemView.findViewById(R.id.img_product);
            cardProduct=itemView.findViewById(R.id.card_product);


            minusIm=itemView.findViewById(R.id.minus_im);
            plusIm=itemView.findViewById(R.id.plus_im);


        }
    }


}
