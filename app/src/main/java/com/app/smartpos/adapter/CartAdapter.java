package com.app.smartpos.adapter;

import static com.app.smartpos.common.Utils.trimLongDouble;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.smartpos.R;
import com.app.smartpos.cart.Cart;
import com.app.smartpos.database.DatabaseAccess;
import com.app.smartpos.pos.ProductCart;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {

    MediaPlayer player;
    private List<HashMap<String, String>> cart_product;
    private Cart productCart;


    public CartAdapter(Cart productCart, List<HashMap<String, String>> cart_product) {
        this.productCart = productCart;
        this.cart_product = cart_product;
        player = MediaPlayer.create(productCart, R.raw.delete_sound);

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(productCart).inflate(R.layout.new_cart_product_item, parent, false);
        return new MyViewHolder(view);

    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(productCart);
        databaseAccess.open();

        final String cart_id = cart_product.get(position).get("cart_id");
        String product_id = cart_product.get(position).get("product_id");
        String product_name = databaseAccess.getProductName(product_id);


        final String price = cart_product.get(position).get("product_price");
//        final String weight_unit_id = cart_product.get(position).get("product_weight_unit");
        final String weight = cart_product.get(position).get("product_weight");
        final String qty = cart_product.get(position).get("product_qty");
        final String uuid = cart_product.get(position).get("product_uuid");
        final String stock = cart_product.get(position).get("stock");

        int getStock=(stock==null || stock.isEmpty())?
                Integer.MAX_VALUE:Integer.parseInt(stock);

        //  Log.d("unit_ID ", weight_unit_id);

        databaseAccess.open();
        String base64Image = databaseAccess.getProductImage(product_id);

//        databaseAccess.open();
//        String weight_unit_name = databaseAccess.getWeightUnitName(weight_unit_id);


        databaseAccess.open();
        String currency = databaseAccess.getCurrency();

       //productCart.updateTotalPrice();
        holder.txtPlus.setImageResource(uuid.equals("CUSTOM_ITEM") ? R.drawable.ic_plus_gray : R.drawable.ic_plus);
        holder.txtPlus.setEnabled(!uuid.equals("CUSTOM_ITEM"));
        holder.txtMinus.setImageResource(uuid.equals("CUSTOM_ITEM") ? R.drawable.ic_minus_gray : R.drawable.ic_minus);
        holder.txtMinus.setEnabled(!uuid.equals("CUSTOM_ITEM"));

        if(uuid.equals("CUSTOM_ITEM")){
            holder.imgProduct.setImageResource(R.drawable.ic_custom_option_gray);
        }else {
            if (base64Image != null) {
                if (base64Image.isEmpty() || base64Image.length() < 6) {
                    holder.imgProduct.setImageResource(R.drawable.image_placeholder);
                } else {


                    byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                    holder.imgProduct.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

                }
            }
        }

        final double getPrice = Double.parseDouble(price) * Integer.parseInt(qty);


        holder.txtItemName.setText(product_name);
        holder.txtPrice.setText(trimLongDouble(getPrice)+" "+currency);
//        holder.txtWeight.setText(weight + " " + weight_unit_name);
        holder.txtQtyNumber.setText(qty);

        holder.deleteIm.setOnClickListener(v -> {
            DatabaseAccess databaseAccess1 = DatabaseAccess.getInstance(productCart);
            databaseAccess1.open();
            boolean deleteProduct = databaseAccess1.deleteProductFromCart(cart_id);

            if (deleteProduct) {
                Toasty.success(productCart, productCart.getString(R.string.product_removed_from_cart), Toast.LENGTH_SHORT).show();

                // Calculate Cart's Total Price Again
                //  setCartTotal();

                productCart.updateTotalPrice(cart_product);

                player.start();

                //for delete cart item dynamically
                // Remove CartItem from Cart List
                cart_product.remove(holder.getAdapterPosition());

                // Notify that item at position has been removed
                notifyItemRemoved(holder.getAdapterPosition());


                productCart.updateTotalPrice(cart_product);
            } else {
                Toasty.error(productCart, productCart.getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }


            databaseAccess1.open();
            int itemCount = databaseAccess1.getCartItemCount();
            Log.d("itemCount", "" + itemCount);
//                if (itemCount <= 0) {
//                    txt_total_price.setVisibility(View.GONE);
//                    btnSubmitOrder.setVisibility(View.GONE);
//
//                    imgNoProduct.setVisibility(View.VISIBLE);
//                    txt_no_product.setVisibility(View.VISIBLE);
//                }

        });


        holder.txtPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String qty1 = holder.txtQtyNumber.getText().toString();
                int get_qty = Integer.parseInt(qty1);

                if (get_qty >= getStock) {
                    Toasty.error(productCart, productCart.getString(R.string.available_stock) + " " + getStock, Toast.LENGTH_SHORT).show();
                }else if(productCart.checkCartTotalPrice(position)){
                    Toast.makeText(productCart, R.string.total_price_cannot_exceed, Toast.LENGTH_SHORT).show();
                }else {
                    get_qty++;


                    double cost = Double.parseDouble(price) * get_qty;


                    holder.txtPrice.setText(currency + trimLongDouble(cost));
                    holder.txtQtyNumber.setText("" + get_qty);

                    cart_product.get(position).put("product_qty",get_qty+"");
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(productCart);
                    databaseAccess.open();
                    databaseAccess.updateProductQty(cart_id, "" + get_qty);
                    notifyItemChanged(position);
                    productCart.updateTotalPrice(cart_product);
                }
            }
        });


        holder.txtMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String qty = holder.txtQtyNumber.getText().toString();
                int get_qty = Integer.parseInt(qty);


                if (get_qty >= 2) {
                    get_qty--;

                    double cost = Double.parseDouble(price) * get_qty;

                    holder.txtPrice.setText(currency + trimLongDouble(cost));
                    holder.txtQtyNumber.setText("" + get_qty);

                    cart_product.get(position).put("product_qty",get_qty+"");
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(productCart);
                    databaseAccess.open();
                    databaseAccess.updateProductQty(cart_id, "" + get_qty);
                    notifyItemChanged(position);
                    productCart.updateTotalPrice(cart_product);

                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return cart_product.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtItemName, txtPrice, txtQtyNumber;
        ImageView txtPlus, txtMinus;
        ImageView imgProduct,deleteIm;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtItemName = itemView.findViewById(R.id.txt_item_name);
            txtPrice = itemView.findViewById(R.id.txt_price);
//            txtWeight = itemView.findViewById(R.id.txt_weight);
            txtQtyNumber = itemView.findViewById(R.id.txt_number);
            imgProduct = itemView.findViewById(R.id.product_im);
            txtMinus = itemView.findViewById(R.id.txt_minus);
            txtPlus = itemView.findViewById(R.id.txt_plus);
            deleteIm = itemView.findViewById(R.id.delete_im);

        }


    }


}
