package com.modoobel.modoobell;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.modoobel.modoobell.custom_obj.Common;

import java.util.ArrayList;

import static com.modoobel.modoobell.custom_obj.Common.BUNDLE_DEVICE_NAME;
import static com.modoobel.modoobell.custom_obj.Common.BUNDLE_KEY_NAME;

public class DeviceListActivity extends AppCompatActivity {

    RecyclerView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_device_list);

        getSupportActionBar().setTitle("기기 목록");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = (RecyclerView)findViewById(R.id.list_view);
        listView.setVerticalScrollBarEnabled(true);
        listView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        listView.setAdapter(new DeviceListAdapter(this,Common.getDeviceList(this)));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon action bar is clicked; go to parent activity
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class DeviceListAdapter extends RecyclerView.Adapter {

        ArrayList<Bundle> arrayList;
        Context mContext;

        public DeviceListAdapter(Context context, ArrayList<Bundle> arr)
        {
            this.arrayList = arr;
            this.mContext = context;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_device_list, parent, false);
            return new ViewHolderDevice(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Bundle bunDevice = arrayList.get(position);
            ViewHolderDevice v = (ViewHolderDevice)holder;

            v.tvName.setText(bunDevice.getString(BUNDLE_DEVICE_NAME));
            v.btnDelete.setTag(position);
            v.btnDelete.setOnClickListener(clkListenerDelete);

        }

        private View.OnClickListener clkListenerDelete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)v.getTag();
                Bundle bunDevice = arrayList.get(position);
                Common.deleteDevice(mContext,bunDevice.getString(BUNDLE_KEY_NAME));

                arrayList.remove(position);
                notifyDataSetChanged();

            }
        };


        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    public class ViewHolderDevice extends RecyclerView.ViewHolder {

        TextView tvName;
        Button btnDelete;

        public ViewHolderDevice(View itemView) {
            super(itemView);

            tvName = (TextView)itemView.findViewById(R.id.name);
            btnDelete = (Button) itemView.findViewById(R.id.btn_delete);
        }
    }

}
