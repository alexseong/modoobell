package com.modoobel.modoobell;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.modoobel.modoobell.custom_obj.Common;

import static com.modoobel.modoobell.custom_obj.Common.OPERATION_ADD;
import static com.modoobel.modoobell.custom_obj.Common.getNotificationKey;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_menu);
    }


    public void clkMenuButton(View v)
    {
        int id = v.getId();

        if (id == R.id.btn_close) {

            finish();
            overridePendingTransition(0,0);


        }else if (id == R.id.btn_pass) {

            Common.showPassCode(this,true,new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            });

        }else if (id == R.id.btn_history) {

            Intent i = new Intent(MenuActivity.this,VisitHistoryActivity.class);
            startActivity(i);

        }else if (id == R.id.btn_add_device) {

            Common.showQRCode(this,mMessageReceiver,null);

        }else if (id == R.id.btn_manage_device) {

            Intent intent = new Intent(MenuActivity.this,DeviceListActivity.class);
            startActivity(intent);

        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(Common.IS_SEND_DEVICE))
            {
                String from = intent.getStringExtra(Common.DEVICE_TOKEN);
                String name = intent.getStringExtra(Common.NAME);

                Common.saveDevice(getBaseContext(),from,name);
                Common.addDeviceToGroup(getBaseContext(),Common.getNotificationName(getBaseContext()),from,OPERATION_ADD,getNotificationKey(getBaseContext()));

            }
        }
    };

}
