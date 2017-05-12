package com.modoobel.modoobell.custom_obj;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modoobel.modoobell.R;

import java.util.ArrayList;

/**
 * Created by luckyleeis on 2017. 1. 7..
 */

public class InputButtonSet extends LinearLayout {

    public static int INPUT_BUTTON_COUNT = 4;

    public interface onClickInputButtonListner{
        void onClick(String msgId);
        void onClickMore();
        void onClickSendMessage(String message);
    }

    EditText etInput;
    Button   btnOK;
    ImageButton btnClose;
    Context mContext;
    ViewPager pager;
    InputButtonAdapter adapter;
    public boolean isEnable = true;

    public onClickInputButtonListner mClickEventListener;

    public void setOnClickInputButtonListner(onClickInputButtonListner listener){
        mClickEventListener = listener;
    }

    public InputButtonSet(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public InputButtonSet(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public InputButtonSet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }


    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.input_button_set, this, false);
        addView(v);

        etInput = (EditText)findViewById(R.id.et_input);
        btnOK = (Button) findViewById(R.id.btn_ok);

        btnClose = (ImageButton)findViewById(R.id.btn_more);
        btnClose.setOnClickListener(mClkBtnMore);
        btnOK.setOnClickListener(mClkSendMessage);

        pager = (ViewPager)findViewById(R.id.pager_layout);
        adapter = new InputButtonAdapter(((AppCompatActivity)mContext).getSupportFragmentManager());
        pager.setAdapter(adapter);
    }

    public void setInputButton(MBResponse mbResponse, String msgId) {

        adapter.setAdapterData(mbResponse,msgId);
        adapter.notifyDataSetChanged();
        pager.setVisibility(VISIBLE);
        etInput.setEnabled(true);
        btnOK.setEnabled(true);
        btnClose.setEnabled(true);
    }

    public void setEmptyInputButton()
    {
        pager.setVisibility(INVISIBLE);
        isEnable = true;

        etInput.setEnabled(true);
        btnOK.setEnabled(true);
        btnClose.setEnabled(true);
    }

    public void setDisEnable()
    {
        setEmptyInputButton();
        etInput.setEnabled(false);
        btnOK.setEnabled(false);
        btnClose.setEnabled(false);
    }

    public void setEnable(boolean isEnable)
    {
        this.isEnable = isEnable;
    }

    private OnClickListener mClkBtn = new OnClickListener() {

        @Override
        public void onClick(View view) {

            if (isEnable) {

                mClickEventListener.onClick((String) view.getTag());
                setEnable(false);
            }
        }
    };

    private OnClickListener mClkBtnMore = new OnClickListener() {
        @Override
        public void onClick(View view) {

            mClickEventListener.onClickMore();
        }
    };

    private OnClickListener mClkSendMessage = new OnClickListener() {
        @Override
        public void onClick(View v) {

            InputMethodManager imm= (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
            mClickEventListener.onClickSendMessage(etInput.getText().toString());

            etInput.setText("");
        }
    };



    //**********************************************************************************************
    //**********************************************************************************************

    // InputButtonAdapter Class

    //**********************************************************************************************
    //**********************************************************************************************


    public class InputButtonAdapter extends FragmentStatePagerAdapter {

        private MBResponse mbResponse;
        private String msgId;

        public InputButtonAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setAdapterData(MBResponse mbResponse, String msgId)
        {
            this.mbResponse = mbResponse;
            this.msgId = msgId;
        }

        @Override
        public Fragment getItem(int position) {

            InputButtonFragment fragment = InputButtonFragment.newInstance(position, mbResponse, msgId);
            fragment.mClkBtn = mClkBtn;
            return fragment;
        }

        @Override
        public int getCount() {


            try {
                if (mbResponse.getMessageData(msgId).response_msg.length == 0) return 0;
                float f = (float)mbResponse.getMessageData(msgId).response_msg.length / (float)INPUT_BUTTON_COUNT;

                return (int)Math.ceil(f);

            }catch (NullPointerException e)
            {
                return 0;
            }
        }

        @Override
        public int getItemPosition(Object object) {

            if (getCount() == 0) return 0;

            InputButtonFragment f = (InputButtonFragment)object;
            f.setResponseMessageID(mbResponse,msgId);
            f.setInputButtonFragment();
            return f.position;
        }
    }





    //**********************************************************************************************
    //**********************************************************************************************

    // InputButtonFragment Class

    //**********************************************************************************************
    //**********************************************************************************************

    public static class InputButtonFragment extends Fragment {

        public OnClickListener mClkBtn;
        int position;
        MBResponse mbResponse;
        String msgId;
        ArrayList<TextView> arrButton;


        public InputButtonFragment() {
            // Required empty public constructor
        }

        public static InputButtonFragment newInstance(int positions, MBResponse mbResponse, String msgId) {

            InputButtonFragment fragment = new InputButtonFragment();
            fragment.position = positions;
            fragment.mbResponse = mbResponse;
            fragment.msgId = msgId;
            fragment.arrButton = new ArrayList<>();
            return fragment;
        }


        public void setResponseMessageID(MBResponse mbResponse, String msgId)
        {
            this.mbResponse = mbResponse;
            this.msgId = msgId;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment

            View v = inflater.inflate(R.layout.fragment_input_button, container, false);

            arrButton.add((TextView)v.findViewById(R.id.btn_response_0));
            arrButton.add((TextView)v.findViewById(R.id.btn_response_1));
            arrButton.add((TextView)v.findViewById(R.id.btn_response_2));
            arrButton.add((TextView)v.findViewById(R.id.btn_response_3));

            for (TextView tv : arrButton)
            {
                tv.setOnClickListener(mClkBtn);
            }


            setInputButtonFragment();


            return v;
        }


        public void setInputButtonFragment()
        {
            MBMessageData messageData = mbResponse.getMessageData(msgId);
            if (messageData == null) return;

            int start = position * INPUT_BUTTON_COUNT;

            for (int i = 0; i < arrButton.size(); i++)
            {
                TextView btn = arrButton.get(i);
                if (start + i >= messageData.response_msg.length) {
                    btn.setVisibility(GONE);
                    continue;
                }

                btn.setVisibility(VISIBLE);
                String response_msg_id = messageData.response_msg[start + i];
                MBMessageData responseMessage = mbResponse.getMessageData(response_msg_id);

                btn.setText(responseMessage.msg_short);
                btn.setTag(response_msg_id);
            }

        }
    }
}