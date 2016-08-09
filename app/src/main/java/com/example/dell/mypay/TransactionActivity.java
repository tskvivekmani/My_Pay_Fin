package com.example.dell.mypay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class TransactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#068FC4")));
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String[] SHOPS_COLUMN_TRANSACTION = {
                MyPayContract.ShopsEntry.TABLE_NAME+"."+MyPayContract.ShopsEntry._ID,
                MyPayContract.ShopsEntry.COLUMN_SHOP_ID,
                MyPayContract.ShopsEntry.COLUMN_SHOP_NAME,
                MyPayContract.ShopsEntry.COLUMN_ADDRESS,
                MyPayContract.ShopsEntry.COLUMN_SHOP_API
        };

        private static final String[] CART_COLUMNS_TRANSACTION = {
                MyPayContract.CartEntry.TABLE_NAME+"."+MyPayContract.CartEntry._ID,
                MyPayContract.CartEntry.COLUMN_CART_ID,
                MyPayContract.CartEntry.COLUMN_C_ITEM_NAME,
                MyPayContract.CartEntry.COLUMN_C_ITEM_DESC,
                MyPayContract.CartEntry.COLUMN_C_ITEM_LOGO,
                MyPayContract.CartEntry.COLUMN_C_ITEM_PRICE,
                MyPayContract.CartEntry.COLUMN_C_ITEM_PRICE_UNIT,
                MyPayContract.CartEntry.COLUMN_C_QUANTITY
        };
        private Button skip,send,dismiss;
        private int shop_id;
        private String s_address,s_name,total_price;
        private static final int DATA_LOADER = 70;
        private String invoice;
        private boolean select;
        private ImageView tick,error;
        private EditText email,sms;
        private TextView cancel_cap;
        private RelativeLayout rel_success;
        private LinearLayout lin_button;


        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_transaction, container, false);
            tick = (ImageView)rootView.findViewById(R.id.success_image);
            error = (ImageView)rootView.findViewById(R.id.failure_image);
            email = (EditText)rootView.findViewById(R.id.email_edittext);
            sms = (EditText)rootView.findViewById(R.id.sms_edittext);
            cancel_cap = (TextView)rootView.findViewById(R.id.cancel_caption);
            rel_success = (RelativeLayout)rootView.findViewById(R.id.success_receipt_layout);
            dismiss = (Button)rootView.findViewById(R.id.dismiss_button);
            skip = (Button)rootView.findViewById(R.id.skip_button);
            send = (Button)rootView.findViewById(R.id.send_button);
            lin_button = (LinearLayout)rootView.findViewById(R.id.receipt_success_button);

            return rootView;
        }

        public boolean isEmailValid(CharSequence email) {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(DATA_LOADER,savedInstanceState,this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            Intent intent=getActivity().getIntent();
            if (intent==null)return null;
            Loader<Cursor> cursorLoader=new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    SHOPS_COLUMN_TRANSACTION,
                    null,null,null
            );
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            if(cursor.moveToFirst()){
                shop_id = cursor.getInt(cursor.getColumnIndex(
                        MyPayContract.ShopsEntry.COLUMN_SHOP_ID
                ));
                s_address = cursor.getString(cursor.getColumnIndex(
                        MyPayContract.ShopsEntry.COLUMN_ADDRESS
                ));
                s_name = cursor.getString(cursor.getColumnIndex(
                        MyPayContract.ShopsEntry.COLUMN_SHOP_NAME
                ));
                Log.e("Transaction","data loader:"+shop_id+" "+s_address+" "+s_name);
                SharedPreferences pref_value=getActivity().getSharedPreferences("SuccessFail", MODE_PRIVATE);
                select = pref_value.getBoolean("_val",false);
                if(select){
                    tick.setVisibility(View.VISIBLE);
                    error.setVisibility(View.GONE);
                    rel_success.setVisibility(View.VISIBLE);
                    cancel_cap.setVisibility(View.GONE);
                    lin_button.setVisibility(View.VISIBLE);
                    dismiss.setVisibility(View.GONE);
                    skip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(getActivity(),InvoiceAckActivity.class).putExtra("Invoice",false));
                            int del=getActivity().getContentResolver().delete(
                                    MyPayContract.CartEntry.CONTENT_URI,
                                    null,null
                            );

                        }
                    });
                    SharedPreferences prefs=getActivity().getSharedPreferences("T_price", MODE_PRIVATE);
                    total_price=prefs.getString("t_price","0");
                    invoice="             "+s_name+"                \n"+
                            "          "+s_address+"               \n"+
                            "              ITEMS                   \n";

                    Cursor cart_cur=getActivity().getContentResolver().query(
                            MyPayContract.CartEntry.CONTENT_URI,
                            CART_COLUMNS_TRANSACTION,
                            null,null,null
                    );
                    String i_name,i_desc,price,p_unit="Rs";
                    if(cart_cur.moveToFirst()){
                        for (cart_cur.moveToFirst(); !cart_cur.isAfterLast(); cart_cur.moveToNext()) {
                            i_name=cart_cur.getString(cart_cur.getColumnIndex(
                                    MyPayContract.CartEntry.COLUMN_C_ITEM_NAME));
                            i_desc=cart_cur.getString(cart_cur.getColumnIndex(
                                    MyPayContract.CartEntry.COLUMN_C_ITEM_DESC));
                            price=cart_cur.getString(cart_cur.getColumnIndex(
                                    MyPayContract.CartEntry.COLUMN_C_ITEM_PRICE));
                            p_unit=cart_cur.getString(cart_cur.getColumnIndex(
                                    MyPayContract.CartEntry.COLUMN_C_ITEM_PRICE_UNIT));
                            invoice+=" "+i_name+" \n "+i_desc+"                           "+p_unit+"."+price+"\n";

                        }
                    }
                    invoice+="Total Price:                          "+p_unit+"."+total_price;


                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String em = email.getText().toString();
                            String sm = sms.getText().toString();
                            if (em.length() == 0 && sm.length() == 0) {
                                Toast.makeText(getActivity(),
                                        "Please enter email id or sms", Toast.LENGTH_LONG).show();
                            } else if (em.length()>0 && !isEmailValid(em))
                                Toast.makeText(getActivity(),
                                        "Improper email id format", Toast.LENGTH_LONG).show();
                            else if (sm.length()>0 && !PhoneNumberUtils.isGlobalPhoneNumber(sm))
                                Toast.makeText(getActivity(),
                                        "Invalid mobile number",Toast.LENGTH_LONG).show();
                            else{
                                if(em.length()>0){
                                    Mail m=new Mail("MyPayQueueless@gmail.com","queuelesspayment");
                                    String[] toArr = {em};
                                    m.set_to(toArr);
                                    m.set_from("MyPayQueueless@gmail.com");
                                    m.set_subject("MyPay:Invoice");
                                    m.setBody(invoice);

                                    try {
                                        //m.addAttachment("/sdcard/filelocation");

                                        if(m.send()) {
                                            Toast.makeText(getActivity(), "Email was sent successfully.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Email was not sent.", Toast.LENGTH_LONG).show();
                                        }
                                    } catch(Exception e) {
                                        //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                                        Log.e("Transaction", "Could not send email", e);
                                    }
                                }
                                if(sm.length()>0){
                                    try {
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(sm.substring(sm.length()-10), null,invoice, null, null);
                                        Toast.makeText(getActivity(), "SMS Sent!",
                                                Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(),
                                                "SMS failed, please try again later!",
                                                Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                                startActivity(new Intent(getActivity(),InvoiceAckActivity.class).putExtra("Invoice",true));
                            }
                            int del=getActivity().getContentResolver().delete(
                                    MyPayContract.CartEntry.CONTENT_URI,
                                    null,null
                            );

                        }
                    });
                }
                else {
                    tick.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);
                    rel_success.setVisibility(View.GONE);
                    cancel_cap.setVisibility(View.VISIBLE);
                    lin_button.setVisibility(View.GONE);
                    dismiss.setVisibility(View.VISIBLE);
                    dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            startActivity(new Intent(getActivity(),CartActivity.class)
                                    .setData(MyPayContract.ShopsEntry.buildShopWithShopId(shop_id)));
                        }
                    });
                }

            }

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    }
}
