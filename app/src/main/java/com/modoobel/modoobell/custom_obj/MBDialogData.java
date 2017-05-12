package com.modoobel.modoobell.custom_obj;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luckyleeis on 2017. 1. 7..
 */

public class MBDialogData  {

    private String msgId;
    private String content;
    private long timestamp;
    public boolean isMe;
    public boolean isIdData;
    public boolean isCloseMessage;

    public MBDialogData()
    {
        this.timestamp = System.currentTimeMillis();
        this.isCloseMessage = true;
    }

    public MBDialogData(String msgId, String content, boolean isMe, boolean isIdData) {
        this.msgId = msgId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isMe = isMe;
        this.isIdData = isIdData;
        this.isCloseMessage = false;
//        if (isIdData) {
//            this.content = context.getString(Common.getResourceId(context,responseId + "_" + msgId + "_msg","string"));
//        }
    }

    public String getMsgId()
    {
        return msgId;
    }

    public String getContent()
    {
        return content;
    }

    public String getTime()
    {
        Date date = new Date(timestamp);
        DateFormat sdFormat = new SimpleDateFormat("HH:mm");
        String time = sdFormat.format(date);
        return time;
    }

    public String getDate()
    {
        Date date = new Date(timestamp);
        DateFormat sdFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm");
        String time = sdFormat.format(date);
        return time;
    }

}
