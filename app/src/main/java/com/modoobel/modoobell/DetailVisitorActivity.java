package com.modoobel.modoobell;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.modoobel.modoobell.adapter.CommAdapter;
import com.modoobel.modoobell.custom_obj.Common;

import static com.modoobel.modoobell.custom_obj.Common.DIALOG_ID;

public class DetailVisitorActivity extends AppCompatActivity {

    CommAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_detail_visitor);

        getSupportActionBar().setTitle("상세보기");
        getSupportActionBar().setSubtitle(getIntent().getStringExtra("sub_title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //*********************************************************************
        // 비디오뷰 16:9로 설정

        ImageView videoLayout = (ImageView)findViewById(R.id.image);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        float imageWidth = metrics.widthPixels;
        float imageHeight = imageWidth/16 * 9;

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
        lp.width = (int) imageWidth;
        lp.height = (int) imageHeight;
        videoLayout.setLayoutParams(lp);

        //*********************************************************************


        Glide.with(this)
                .load(Common.getSavedFile(this,String.valueOf(getIntent().getLongExtra(DIALOG_ID,0)),false))
                .asBitmap()
                .placeholder(R.drawable.avata)
                .override(lp.width,lp.height)
                .thumbnail(0.2f)
                .centerCrop()
                .into(videoLayout);


        adapter = new CommAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);


//        Log.d("Tag","" + getIntent().getLongExtra(DIALOG_ID,0));

        adapter.reloadAdapter(getIntent().getLongExtra(DIALOG_ID,0));
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
}
