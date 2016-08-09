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
public class ItemService extends IntentService {

    public static final String ITEM_EXTRA = "ITEMS";


    public ItemService() {
        super("ItemService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("ItemService","inside service");
        String strJson=
                "{"+
                        "\"Items\" :["+
                        "{"+
                        "\"item_id\":\"10\","+
                        "\"sid_ref\":\"100\","+
                        "\"iname\":\"Jeans\","+
                        "\"idesc\":\"Slim fit jeans\","+
                        "\"ilogo\":\"logo\","+
                        "\"iprice\":\"11.00\","+
                        "\"ipunit\":\"Rs\","+
                        "\"ibar\":\"036000291452\""+
                        "},"+
                        "{"+
                        "\"item_id\":\"11\","+
                        "\"sid_ref\":\"100\","+
                        "\"iname\":\"Trousers\","+
                        "\"idesc\":\"Slim fit trousers\","+
                        "\"ilogo\":\"logo\","+
                        "\"iprice\":\"2500.00\","+
                        "\"ipunit\":\"Rs\","+
                        "\"ibar\":\"3245456345344\""+
                        "}"+"]"+
                        "}";
        String data = "";
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);

            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = jsonRootObject.optJSONArray("Items");

            //Iterate the jsonArray and print the info of JSONObjects
            for(int i=0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = Integer.parseInt(jsonObject.optString("item_id"));
                int sid = Integer.parseInt(jsonObject.optString("sid_ref"));
                String name = jsonObject.optString("iname");
                String desc =  jsonObject.optString("idesc");
                String logo =  jsonObject.optString("ilogo");
                float price = Float.parseFloat(jsonObject.optString("iprice"));
                String punit =  jsonObject.optString("ipunit");
                String bar =  jsonObject.optString("ibar");
                Cursor cursor=getContentResolver().query(
                        MyPayContract.ItemsEntry.buildItemWithShopIdAndBarcode(sid,bar),
                        new String[]{MyPayContract.ItemsEntry.COLUMN_ITEM_ID},
                        null,new String[]{Integer.toString(sid),bar},null
                );
                if(!cursor.moveToFirst()){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_ID,id);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_SHOP_ID_REF,sid);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_NAME,name);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_DESC,desc);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_LOGO,logo);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_PRICE,price);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_PRICE_UNIT,punit);
                    contentValues.put(MyPayContract.ItemsEntry.COLUMN_ITEM_BARCODE,bar);
                    Uri u = getContentResolver().insert(MyPayContract.ItemsEntry.CONTENT_URI,contentValues);

                }

                //data += "Node"+i+" : \n id= "+ id +" \n Name= "+ name +" \n Salary= "+ salary +" \n ";
            }
        } catch (JSONException e) {e.printStackTrace();}

    }

}
