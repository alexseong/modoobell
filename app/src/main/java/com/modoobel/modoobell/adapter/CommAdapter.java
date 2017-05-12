package com.modoobel.modoobell.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.modoobel.modoobell.R;
import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.MBDialogData;
import com.modoobel.modoobell.custom_obj.MBMessageData;
import com.modoobel.modoobell.custom_obj.MBResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.modoobel.modoobell.custom_obj.Common.RESPONSE_ID;

/**
 * Created by luckyleeis on 2017. 1. 7..
 */

public class CommAdapter extends RecyclerView.Adapter {

    public static int ME_CELL = 1001;
    public static int OPP_CELL = 1002;
    public static int CLOSE_CELL = 1003;

    public long dialogId;
//    private String toId; //받을 사람의 토큰아이디
    public String responseId;

    ArrayList<MBDialogData> arrayList = new ArrayList<>();
    Context mContext;


//    public ArrayList<String> getToIds()
//    {
//        if (toId == null) {
//            return Common.getDeviceTokenList(mContext);
//        }else {
//            ArrayList<String> arr = new ArrayList<>();
//            arr.add(toId);
//            return arr;
//        }
//    }

    public CommAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * 마지막 메시지가 템플릿 대화이면서 답변할 대답이 없으면 대화를 종료 시킨다.
     * @return
     */

    public boolean isClose(MBResponse mbResponse)
    {
        MBDialogData mbDialogData = arrayList.get(arrayList.size()-1);
        MBMessageData mbMessageData = mbResponse.getMessageData(getLastDialogMessageId());

        if (mbDialogData.isIdData && mbMessageData.response_msg == null) {
            return true;
        }

        return false;
    }

    public String getLastDialogMessageId()
    {
        try {
            MBDialogData mbDialogData = arrayList.get(arrayList.size()-1);

            return mbDialogData.getMsgId();

        }catch (Exception e)
        {
            return null;
        }
    }

    public String getLastDialogMessageContent()
    {
        try {
            MBDialogData mbDialogData = arrayList.get(arrayList.size()-1);

            return mbDialogData.getContent();

        }catch (Exception e)
        {
            return null;
        }
    }

    public void reloadAdapter(long dialogId)
    {
        this.dialogId = dialogId;
        Bundle bundle = Common.getDialog(mContext, dialogId);
        this.responseId = bundle.getString(RESPONSE_ID);
//        this.toId = bundle.getString(FROM);

        String data = bundle.getString("dialod_data");

        Gson gson = new Gson();
        Type type = new TypeToken<List<MBDialogData>>() {}.getType();
        this.arrayList = gson.fromJson(data,type);

        Log.d("Tag",data);

        notifyDataSetChanged();
    }

    public void addDialogData(MBDialogData data)
    {
        arrayList.add(data);
        Common.saveDialog(mContext,String.valueOf(dialogId),arrayList,responseId);
    }


    @Override
    public int getItemViewType(int position) {

        if (arrayList.get(position).isCloseMessage) {

            return CLOSE_CELL;

        }else {
            if (arrayList.get(position).isMe)
            {
                return ME_CELL;

            }else {
                return OPP_CELL;

            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ME_CELL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_comm_me, parent, false);
            return new ViewHolderCommMe(view);

        }else if (viewType == OPP_CELL){

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_comm_opp, parent, false);
            return new ViewHolderCommOpp(view);
        }else {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_comm_close, parent, false);
            return new ViewHolderCommClose(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MBDialogData dialogData = arrayList.get(position);

        if (holder.getItemViewType() == CLOSE_CELL)
        {
            ViewHolderCommClose v = (ViewHolderCommClose) holder;
            v.tvDate.setText(dialogData.getDate());

        }else {
            ViewHolderCommMe v = (ViewHolderCommMe)holder;

            v.tvContent.setText(dialogData.getContent());
            v.tvTime.setText(dialogData.getTime());
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolderCommClose extends RecyclerView.ViewHolder {

        TextView tvDate;

        public ViewHolderCommClose(View itemView) {
            super(itemView);
            tvDate = (TextView)itemView.findViewById(R.id.tv_date);

        }
    }


    public class ViewHolderCommOpp extends ViewHolderCommMe {

        public ViewHolderCommOpp(View itemView) {
            super(itemView);
        }
    }


    public class ViewHolderCommMe extends RecyclerView.ViewHolder {

        TextView tvContent;
        TextView tvTime;

        public ViewHolderCommMe(View itemView) {
            super(itemView);

            tvContent = (TextView)itemView.findViewById(R.id.text);
            tvTime = (TextView)itemView.findViewById(R.id.time);

            Log.d("Tag","" + itemView);


            DisplayMetrics dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
            int width = dm.widthPixels;

            float margin = mContext.getResources().getDimension(R.dimen.dialog_me_left_margin) +
                    mContext.getResources().getDimension(R.dimen.activity_horizontal_margin) +
                    mContext.getResources().getDimension(R.dimen.activity_margin_half) +
                    mContext.getResources().getDimension(R.dimen.time_textview_min_width);
            tvContent.setMaxWidth(width - (int)margin);

        }
    }
}