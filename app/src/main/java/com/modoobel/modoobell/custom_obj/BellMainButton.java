package com.modoobel.modoobell.custom_obj;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modoobel.modoobell.R;

/**
 * Created by luckyleeis on 2017. 1. 5..
 */

public class BellMainButton extends LinearLayout {

    public ImageView btnIcon;
    public TextView btnText;

    Context mContext;
    MBResponse mbResponse;


    public BellMainButton(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public BellMainButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public BellMainButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }


    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.bell_main_button, this, false);
        btnIcon = (ImageView)v.findViewById(R.id.image);
        btnText = (TextView) v.findViewById(R.id.text);

        addView(v);

    }

    public void setResponseData(MBResponse mbResponse) {
        this.mbResponse = mbResponse;

        int imgResId = Common.getResourceId(mContext,mbResponse.icon,"drawable");
        btnIcon.setImageResource(imgResId);
        btnText.setText(mbResponse.getTitle(mContext));

    }

    public MBResponse getResponseData()
    {
        return this.mbResponse;
    }

}
