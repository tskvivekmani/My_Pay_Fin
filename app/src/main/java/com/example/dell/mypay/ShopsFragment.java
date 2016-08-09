package com.example.dell.mypay;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ShopsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    public interface ShopIdNotifier{
        public void getShopId(int shopid);
    }

    private static final String[] C_SHOP_COLUMNS = {
            MyPayContract.CurrentShopsEntry.TABLE_NAME+"."+MyPayContract.CurrentShopsEntry._ID,
            MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_ID,
            MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_NAME,
            MyPayContract.CurrentShopsEntry.COLUMN_C_ADDRESS
    };
    private BroadcastReceiver broadcastReceiver;
    private String loc="",latitude="",longitude="";
    private static final int SHOP_LOADER = 0;
    private ShopAdapter mShopAdapter;

    public ShopsFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        //runtimr_permissions();
        Intent intent =new Intent(getActivity(),GPSService.class);
        //Log.e("ShopsFragment","starting gps service");
        getActivity().startService(intent);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_shop_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //if(id == R.id.action_cart){
        //    new CartAccess(getActivity()).execute();
        //   return true;
        //}
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.e("ShopsFragment","on resume of shopsfragment");
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.e("ShopsFragment","inside onreceive:"+intent.getAction());
                    if (intent.getAction().equals("location_update")) {
                        //Log.e("ShopsFragment","inside onreceive:intent matched");
                        loc += intent.getExtras().get("location");
                        //Toast.makeText(getActivity(),"" + loc,Toast.LENGTH_LONG).show();
                        latitude += loc.split(" ")[0];
                        longitude += loc.split(" ")[1];
                        if(loc.length()>0){
                            Intent i =new Intent(getActivity(),GPSService.class);
                            getActivity().stopService(i);
                        }
                        //Log.e("ShopsFragment","location:"+loc);
                        Intent shopIntent = new Intent(getActivity(),ShopService.class);
                        shopIntent.putExtra(ShopService.SHOP_EXTRA,loc);
                        getActivity().startService(shopIntent);
                    }
                }
            };
            //IntentFilter filter = new IntentFilter();
            //filter.addAction(GPSService.GPS_FILTER);
            getActivity().registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver!=null){
            getActivity().unregisterReceiver(broadcastReceiver);
        }
        int del = getActivity().getContentResolver().delete(
                MyPayContract.CurrentShopsEntry.CONTENT_URI,
                null,null
        );
    }

    private boolean runtimr_permissions() {
        //if(Build.VERSION.SDK_INT >=23 && ContextCompat.checkSelfPermission(this,Manifest.))
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView shops = (ListView)rootView.findViewById(R.id.shops_list);
        mShopAdapter = new ShopAdapter(getActivity(),null,0);
        shops.setAdapter(mShopAdapter);
        shops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cur = (Cursor)adapterView.getItemAtPosition(i);
                if (cur != null) {
                    ((ShopIdNotifier)getActivity()).getShopId(cur.getInt(cur.getColumnIndex(
                            MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_ID)));
                    Log.e("ShopFragment","onclick shop id:"+cur.getInt(cur.getColumnIndex(
                            MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_ID)));
                    Intent intent = new Intent(getActivity(), ScanActivity.class)
                            .setData(MyPayContract.ShopsEntry.buildShopWithShopId(
                                    cur.getInt(cur.getColumnIndex(
                                            MyPayContract.CurrentShopsEntry.COLUMN_C_SHOP_ID ))
                            ));
                    startActivity(intent);
                }
            }
        });



        return rootView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(SHOP_LOADER, savedInstanceState, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Log.e("ShopsFragment","inside oncreate loader");
        return new CursorLoader(getActivity(),
                MyPayContract.CurrentShopsEntry.CONTENT_URI,
                C_SHOP_COLUMNS,
                null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.e("ShopsFragment","inside onloadfinished");
        mShopAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mShopAdapter.swapCursor(null);
    }

}
