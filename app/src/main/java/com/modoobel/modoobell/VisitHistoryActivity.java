package com.modoobel.modoobell;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.modoobel.modoobell.custom_obj.MBResponseArray;
import com.modoobel.modoobell.fragment.VisitHistoryFragment;

public class VisitHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_visit_history);

        getSupportActionBar().setTitle("방문 기록");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        VisitorHistoryAdapter adapter = new VisitorHistoryAdapter(getSupportFragmentManager(), MBResponseArray.initMBMessageData(this));
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

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

    public class VisitorHistoryAdapter extends FragmentPagerAdapter {

        MBResponseArray mbResponseArray;

        public VisitorHistoryAdapter(FragmentManager fm, MBResponseArray mbResponseArray) {
            super(fm);

            this.mbResponseArray = mbResponseArray;
        }

        @Override
        public Fragment getItem(int position) {
            VisitHistoryFragment fragment = VisitHistoryFragment.newInstance(position, mbResponseArray);
            fragment.setOnDeleteVisitor(new VisitHistoryFragment.onDeleteVisitor() {
                @Override
                public void onDelete() {
                    notifyDataSetChanged();
                }
            });

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            if (position == 0)
            {
              return getString(R.string.visitor_today);

            } else if (position == 1) {
                return getString(R.string.visitor_week);

            } else if (position == 2) {
                return getString(R.string.visitor_month);

            } else {
                return getString(R.string.visitor_year);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }


        @Override
        public int getItemPosition(Object object) {

            if (getCount() == 0) return 0;

            VisitHistoryFragment f = (VisitHistoryFragment) object;
            f.reload();
            return f.page_position;
        }

    }

}
