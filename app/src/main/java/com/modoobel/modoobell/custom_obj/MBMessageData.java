package com.modoobel.modoobell.custom_obj;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luckyleeis on 2017. 1. 6..
 */

public class MBMessageData implements Parcelable {

    public String msg;
    public String msg_short;
    public String[] response_msg;




    protected MBMessageData(Parcel in) {
        msg = in.readString();
        msg_short = in.readString();
        in.readStringArray(response_msg);

    }


    public static final Parcelable.Creator<MBMessageData> CREATOR = new Parcelable.Creator<MBMessageData>() {
        @Override
        public MBMessageData createFromParcel(Parcel in) {
            return new MBMessageData(in);
        }

        @Override
        public MBMessageData[] newArray(int size) {
            return new MBMessageData[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(msg);
        parcel.writeString(msg_short);
        parcel.writeStringArray(response_msg);
    }
}
