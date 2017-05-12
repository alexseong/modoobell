package com.modoobel.modoobell.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.modoobel.modoobell.DetailVisitorActivity;
import com.modoobel.modoobell.R;
import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.MBDialogData;
import com.modoobel.modoobell.custom_obj.MBResponseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.modoobel.modoobell.R.id.btn_delete;
import static com.modoobel.modoobell.custom_obj.Common.DIALOG_ID;
import static com.modoobel.modoobell.custom_obj.Common.IS_AUTO;
import static com.modoobel.modoobell.custom_obj.Common.RESPONSE_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class VisitHistoryFragment extends Fragment {

    public int page_position;
    ArrayList<Bundle> arrayList = new ArrayList<>();
    RecyclerView recyclerView;
    MBResponseArray mbResponseArray;
    VisitorListAdapter adapter;

    public interface onDeleteVisitor{
        void onDelete();
    }

    public onDeleteVisitor mDelete;

    public void setOnDeleteVisitor(onDeleteVisitor listener){
        mDelete = listener;
    }


    public VisitHistoryFragment() {
        // Required empty public constructor
    }

    public void reload()
    {
        setArrayList();
        adapter.notifyDataSetChanged();
    }

    public void setArrayList()
    {
        //******************************************************************************
        // 날짜별로 데이터를 구분한다.

        arrayList.clear();

        SharedPreferences pref = getContext().getSharedPreferences("visitor", Activity.MODE_PRIVATE);

        final String DAY_DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DAY_DATE_FORMAT);
        Date compareDate;

        Date today = new Date();
        if (page_position == 0) {
            compareDate = today;
        }else {

            Calendar cal = new GregorianCalendar();
            cal.setTime(today);

            if (page_position == 1) {
                cal.add(Calendar.DATE, -7);

            }else if (page_position == 2) {
                cal.add(Calendar.MONTH, -1);
            }else {
                cal.add(Calendar.YEAR, -1);
            }

            compareDate = cal.getTime();
        }


        Gson gson = new Gson();
        Type type = new TypeToken<List<MBDialogData>>() {}.getType();

        for (String key : pref.getAll().keySet())
        {
            Date date = new Date(Long.parseLong(key));
            boolean add = false;

            if (page_position == 0) {
                String dateA = simpleDateFormat.format(date);
                String dateB = simpleDateFormat.format(compareDate);

                if (dateA.compareTo(dateB) == 0)
                {
                    add = true;
                }
            }else {


                if (date.after(compareDate)) {
                    add = true;
                }
            }

            if (add)
            {
                Bundle bundle = new Bundle();
                try {

                    JSONObject jObj = new JSONObject(pref.getString(key,""));
                    bundle.putString(RESPONSE_ID,jObj.getString(RESPONSE_ID));
                    bundle.putString("dialod_data",jObj.getString("dialod_data"));
                    bundle.putBoolean(IS_AUTO,jObj.getBoolean(IS_AUTO));
                    bundle.putString(DIALOG_ID,key);


                    ArrayList<MBDialogData> arr = gson.fromJson(jObj.getString("dialod_data"),type);

                    if (arr.size() > 0) {
                        try {
                            bundle.putString("fir_msg",arr.get(1).getContent());
                        }catch (IndexOutOfBoundsException e)
                        {
                            bundle.putString("fir_msg",arr.get(0).getContent());
                        }
                    }else {
                        bundle.putString("fir_msg","");
                    }

                    arrayList.add(bundle);

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        Collections.sort(arrayList,new VisitComparator());

        //******************************************************************************
    }

    public static VisitHistoryFragment newInstance(int position, MBResponseArray mbResponseArray) {
        VisitHistoryFragment fragment = new VisitHistoryFragment();
        fragment.page_position = position;
        fragment.mbResponseArray = mbResponseArray;

        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_visit_history, container, false);

        recyclerView = (RecyclerView)v.findViewById(R.id.list_view);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        setArrayList();

        adapter = new VisitorListAdapter(getContext(),arrayList);
        recyclerView.setAdapter(adapter);


        return v;
    }

    public class VisitComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Bundle bun1 = (Bundle)o1;
            Bundle bun2 = (Bundle)o2;


            long date1 = Long.parseLong(bun1.getString(DIALOG_ID));
            long date2 = Long.parseLong(bun2.getString(DIALOG_ID));

            return (date1 > date2) ? -1: (date1 < date2) ? 1:0 ;
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int itemPosition = recyclerView.getChildLayoutPosition(v);

            Bundle bundle = arrayList.get(itemPosition);
            String strDate = bundle.getString(DIALOG_ID);

            String DAY_DATE_FORMAT = "yy.MM.dd  HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DAY_DATE_FORMAT);
            Date date = new Date(Long.parseLong(strDate));


            String subTitle;
            String id = bundle.getString(RESPONSE_ID);

            if (mbResponseArray.getMBResponse(id) == null) {

                String content = bundle.getString("fir_msg");
                subTitle = simpleDateFormat.format(date) + " " + content;

            }else {
                subTitle = simpleDateFormat.format(date) + " " + mbResponseArray.getResponseTitle(bundle.getString(RESPONSE_ID));
            }

            Intent i = new Intent(getActivity(),DetailVisitorActivity.class);
            i.putExtra(DIALOG_ID,Long.parseLong(strDate));
            i.putExtra("sub_title",subTitle);
            startActivity(i);
        }
    };


    private final View.OnClickListener mOnClickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int itemPosition = (int)v.getTag();
            Bundle bundle = arrayList.get(itemPosition);
            SharedPreferences pref = getContext().getSharedPreferences("visitor", Activity.MODE_PRIVATE);
            SharedPreferences.Editor ePref = pref.edit();

            ePref.remove(bundle.getString(DIALOG_ID));
            ePref.commit();

            mDelete.onDelete();

        }
    };


    public class VisitorListAdapter extends RecyclerView.Adapter {

        private ArrayList<Bundle> arrayList;
        private Context mContext;

        public VisitorListAdapter(Context context,ArrayList<Bundle> arrayList)
        {
            this.arrayList = arrayList;
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_visitor, parent, false);
            view.setOnClickListener(mOnClickListener);
            return new ViewHolderVisitor(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            final ViewHolderVisitor visitorView = (ViewHolderVisitor)holder;

            visitorView.btnDelete.setTag(position);

            Bundle bundle = arrayList.get(position);
            String strDate = bundle.getString(DIALOG_ID);

            String DAY_DATE_FORMAT = "yy.MM.dd  HH:mm";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DAY_DATE_FORMAT);
            Date date = new Date(Long.parseLong(strDate));


            visitorView.tvDate.setText(simpleDateFormat.format(date));

            String id = bundle.getString(RESPONSE_ID);

            if (mbResponseArray.getMBResponse(id) == null) {

                String contetn = bundle.getString("fir_msg");
                visitorView.tvType.setText(contetn);

            }else {
                visitorView.tvType.setText(mbResponseArray.getResponseTitle(bundle.getString(RESPONSE_ID)));
            }


            if (bundle.getBoolean(IS_AUTO,false))
            {
                visitorView.tvAuto.setText("자동");
            }else {
                visitorView.tvAuto.setText("수동");
            }


            int faceSize = (int) mContext.getResources().getDimension(R.dimen.visitor_face_size);
            Glide.with(mContext)
                    .load(Common.getSavedFile(mContext,strDate,true))
                    .asBitmap()
                    .placeholder(R.drawable.avata)
                    .override(faceSize,faceSize)
                    .thumbnail(0.2f)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(visitorView.ivFace) {

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            super.onResourceReady(resource, glideAnimation);

                            RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create( mContext.getResources(), resource);
                            bitmapDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.visitor_face_size)/2);
                            bitmapDrawable.setAntiAlias(true);
                            visitorView.ivFace.setImageDrawable(bitmapDrawable);
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return this.arrayList.size();
        }
    }


    public class ViewHolderVisitor extends RecyclerView.ViewHolder {

        ImageView ivFace;
        TextView tvDate;
        TextView tvAuto;
        TextView tvType;
        Button btnDelete;

        public ViewHolderVisitor(View itemView) {
            super(itemView);

            ivFace = (ImageView) itemView.findViewById(R.id.image);
            tvDate = (TextView)itemView.findViewById(R.id.tv_date);
            tvAuto = (TextView)itemView.findViewById(R.id.tv_auto);
            tvType = (TextView)itemView.findViewById(R.id.tv_type);
            btnDelete = (Button) itemView.findViewById(btn_delete);
            btnDelete.setOnClickListener(mOnClickDelete);
        }

    }

}
