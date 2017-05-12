package com.modoobel.modoobell.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.MBResponse;
import com.modoobel.modoobell.custom_obj.MBResponseArray;
import com.modoobel.modoobell.fragment.BellMainFragment;

/**
 * Created by luckyleeis on 2017. 1. 5..
 */

public class BellPagerAdapter extends FragmentPagerAdapter {

    public interface onClickMainButtonListner{
        void onClick(MBResponse mbResponse);
    }

    MBResponseArray mbResponseArray;
    private onClickMainButtonListner mClickEventListener;

    public void setOnMainButtonClickListner(onClickMainButtonListner listener){
        mClickEventListener = listener;
    }

    public BellPagerAdapter(FragmentManager fm, MBResponseArray mbResponseArray) {
        super(fm);
        this.mbResponseArray = mbResponseArray;
    }

    @Override
    public Fragment getItem(int position) {

        BellMainFragment fragment = BellMainFragment.newInstance(position, mbResponseArray);

        fragment.setOnMainButtonClickListner(new BellMainFragment.onClickMainButtonListner() {
            @Override
            public void onClick(MBResponse mbResponse) {
                mClickEventListener.onClick(mbResponse);
            }
        });

        return fragment;
    }

    @Override
    public int getCount() {

        float f = (float)mbResponseArray.getSize() / (float)Common.MAIN_BUTTON_COUNT;
        return (int)Math.ceil(f);
    }
}
