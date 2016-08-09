package com.example.dell.mypay;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ShopService extends IntentService {

    public static final String SHOP_FILTER = "fetch_shops";
    public static final String SHOP_EXTRA = "shop";


    public ShopService() {
        super("ShopService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("ShopService","inside service");
        String strJson=
                "{"+
                        "\"Shops\" :["+
                        "{"+
                        "\"shop_id\":\"100\","+
                        "\"sname\":\"Turtle\","+
                        "\"saddress\":\"22,Central Park,Kolkata\","+
                        "\"slat\":\"22.31\","+
                        "\"slong\":\"23.67\","+
                        "\"api\":\"www.turtle.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"200\","+
                        "\"sname\":\"Reebok\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"300\","+
                        "\"sname\":\"Nike\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"400\","+
                        "\"sname\":\"Spencers\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"500\","+
                        "\"sname\":\"Pantaloons\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"600\","+
                        "\"sname\":\"Raymond\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"700\","+
                        "\"sname\":\"John Players\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"800\","+
                        "\"sname\":\"Samsung\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"900\","+
                        "\"sname\":\"Titan Watches\","+
                        "\"saddress\":\"32,Park Street,Kolkata\","+
                        "\"slat\":\"22.37\","+
                        "\"slong\":\"23.77\","+
                        "\"api\":\"www.reebok.com/inventory\""+
                        "},"+
                        "{"+
                        "\"shop_id\":\"1000\","+
                        "\"sname\":\"Adidas\","+
                        "\"saddress\":\"20,New Market,Kolkata\","+
                        "\"slat\":\"22.81\","+
                        "\"slong\":\"23.47\","+
                        "\"api\":\"www.adidas.com/inventory\""+
                        "}"+"]"+
                        "}";
        Log.e("ShopService","inside service:"+strJson);
        String data = "";
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);

            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = jsonRootObject.optJSONArray("Shops");

            //Iterate the jsonArray and print the info of JSONObjects
            for(int i=0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = Integer.parseInt(jsonObject.optString("shop_id"));
                String name = jsonObject.optString("sname");
                String address =  jsonObject.optString("saddress");
                float lat = Float.parseFloat(jsonObject.optString("slat"));
                float longt = Float.parseFloat(jsonObject.optString("slong"));
                String api =  jsonObject.optString("api");
                data=data+id+name+address+lat+longt+api;
                Log.e("ShopService","data:"+data);
                Cursor cur =getContentResolver().query(
                        MyPayContract.CurrentShopsEntry.buildCurShopWithShopId(id),
                        new String[]{MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_ID},
                        null,null,null
                );
                if(!cur.moveToFirst()){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_ID,id);
                    contentValues.put(MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_NAME,name);
                    contentValues.put(MyPayContract.CurrentShopsEntry.COLUMN_C_ADDRESS,address);
                    Uri iu=getContentResolver().insert(
                            MyPayContract.CurrentShopsEntry.CONTENT_URI,contentValues);
                }
                Cursor cursor=getContentResolver().query(
                        MyPayContract.ShopsEntry.buildShopWithShopId(id),
                        new String[]{MyPayContract.ShopsEntry.COLUMN_SHOP_ID},
                        null,null,null
                );
                if(!cursor.moveToFirst()){
                    Log.e("ShopService","first time insert");
                    ContentValues values = new ContentValues();
                    values.put(MyPayContract.ShopsEntry.COLUMN_SHOP_ID,id);
                    values.put(MyPayContract.ShopsEntry.COLUMN_SHOP_NAME,name);
                    values.put(MyPayContract.ShopsEntry.COLUMN_ADDRESS,address);
                    values.put(MyPayContract.ShopsEntry.COLUMN_LATITUDE,lat);
                    values.put(MyPayContract.ShopsEntry.COLUMN_LONGITUDE,longt);
                    values.put(MyPayContract.ShopsEntry.COLUMN_SHOP_API,api);
                    Uri iuri=getContentResolver().insert(
                            MyPayContract.ShopsEntry.CONTENT_URI,values);
                    Log.e("ShopService","insert:"+iuri);
                }

                //data += "Node"+i+" : \n id= "+ id +" \n Name= "+ name +" \n Salary= "+ salary +" \n ";
            }
        } catch (JSONException e) {
            Log.e("ERROR","Json error");
            e.printStackTrace();
        }



    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

}
