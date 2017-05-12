package com.modoobel.modoobell.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.modoobel.modoobell.R;
import com.modoobel.modoobell.custom_obj.BellMainButton;
import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.MBResponse;
import com.modoobel.modoobell.custom_obj.MBResponseArray;

/**
 * A simple {@link Fragment} subclass.
 */
public class BellMainFragment extends Fragment {

    public interface onClickMainButtonListner{
        void onClick(MBResponse mbResponse);
    }

    int page_position;
    MBResponseArray mbResponseArray;
    private onClickMainButtonListner mClickEventListener;

    public void setOnMainButtonClickListner(onClickMainButtonListner listener){
        mClickEventListener = listener;
    }

    public BellMainFragment() {
        // Required empty public constructor
    }

    public static BellMainFragment newInstance(int position, MBResponseArray mbResponseArray) {
        BellMainFragment fragment = new BellMainFragment();
        fragment.page_position = position;
        fragment.mbResponseArray = mbResponseArray;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_bell_main, container, false);

        int start = page_position * Common.MAIN_BUTTON_COUNT;
        int end = start + Common.MAIN_BUTTON_COUNT;

        int resourceIdCount = 0;

        for (int i = start; i < end; i++) {

            int resID = Common.getResourceId(getContext(),"btn_bell_main_" + resourceIdCount,"id");
            BellMainButton btn = (BellMainButton)v.findViewById(resID);

            if (mbResponseArray.getMBResponse(i) != null) {

                btn.setResponseData(mbResponseArray.getMBResponse(i));
                btn.setOnClickListener(mainButtonClickListener);

            }else {
                btn.setVisibility(View.GONE);

            }

            resourceIdCount++;
        }

        return v;
    }


    private View.OnClickListener mainButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            BellMainButton btn = (BellMainButton)view;
            mClickEventListener.onClick(btn.getResponseData());
        }
    };

}
