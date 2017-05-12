package com.modoobel.modoobell.custom_obj;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by luckyleeis on 2017. 1. 6..
 */

public class MBResponse implements Parcelable
{
    public String id;
    public String name;
    public String icon;
    public String auto_response;
    public Bundle messageData;

    public MBResponse (String id, String name)
    {
        this.id = id;
        this.name = name;
        messageData = new Bundle();
    }

    public String getTitle(Context context) {
        try {
            String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier(this.id, "string", packageName);
            return context.getString(resId);

        }catch (Exception e) {
            return this.name;
        }
    }

    public String getId() {
        return id;
    }

    public MBMessageData getMessageData(String msgId)
    {
        return messageData.getParcelable(msgId);
    }

    public void setMessageData(JSONObject joMsg)
    {
        Gson gson = new Gson();
        messageData = new Bundle();

        Iterator i = joMsg.keys();
        while (i.hasNext()) {
            try {
                String key = i.next().toString();
                JSONObject resData = joMsg.getJSONObject(key);

                MBMessageData mbMsg = gson.fromJson(resData.toString(),MBMessageData.class);
                messageData.putParcelable(key,mbMsg);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    protected MBResponse(Parcel in) {
        id = in.readString();
        name = in.readString();
        icon = in.readString();
        messageData = in.readBundle();
    }

    public static final Parcelable.Creator<MBResponse> CREATOR = new Parcelable.Creator<MBResponse>() {
        @Override
        public MBResponse createFromParcel(Parcel in) {
            return new MBResponse(in);
        }

        @Override
        public MBResponse[] newArray(int size) {
            return new MBResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(icon);
        parcel.writeBundle(messageData);

    }
}
