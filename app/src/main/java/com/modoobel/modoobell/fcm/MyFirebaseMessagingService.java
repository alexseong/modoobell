/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.modoobel.modoobell.fcm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.MBDialogData;
import com.modoobel.modoobell.custom_obj.MBResponseArray;

import static com.modoobel.modoobell.custom_obj.Common.DEVICE_TOKEN;
import static com.modoobel.modoobell.custom_obj.Common.DIALOG_ID;
import static com.modoobel.modoobell.custom_obj.Common.FROM;
import static com.modoobel.modoobell.custom_obj.Common.FROM_BELL;
import static com.modoobel.modoobell.custom_obj.Common.IS_CLOSE_DIALOG;
import static com.modoobel.modoobell.custom_obj.Common.IS_ID_DATA;
import static com.modoobel.modoobell.custom_obj.Common.IS_SEND_DEVICE;
import static com.modoobel.modoobell.custom_obj.Common.MESSAGE_ID;
import static com.modoobel.modoobell.custom_obj.Common.NAME;
import static com.modoobel.modoobell.custom_obj.Common.REQUEST_AUTO_RESPONSE_DATA;
import static com.modoobel.modoobell.custom_obj.Common.RESPONSE_ID;
import static com.modoobel.modoobell.custom_obj.Common.SEND_AUTO_RESPONSE_DATA;
import static com.modoobel.modoobell.custom_obj.Common.VIDEO_CALL_CHANEL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Tag";
    public static final String INTENT_FILTER = "INTENT_FILTER";
    public static final String INTENT_FILTER_SEND_AUTO_RESPONSE = "send_auto_reponse";
    public static final String INTENT_FILTER_REQUEST_AUTO_RESPONSE = "request_auto_response";


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // 내가보낸 메시지의 경우 리턴한다.

        if (remoteMessage.getData().containsKey(FROM)) {
            if (remoteMessage.getData().get(FROM).equals(FirebaseInstanceId.getInstance().getToken()))
            {
                return;
            }
        }

        // 영상통화 채널 아이디를 받을때는 리턴한다.
        if (remoteMessage.getData().containsKey(VIDEO_CALL_CHANEL)) {
            return;
        }


        //자동응답 세팅데이터를 요청 받았을때.

        if (remoteMessage.getData().containsKey(REQUEST_AUTO_RESPONSE_DATA))
        {
            String token = remoteMessage.getFrom();
            Common.sendAutoResponseSettingData(getBaseContext(),token,null);
            return;
        }

        //자동응답 세팅데이터를 받았을때.

        if (remoteMessage.getData().containsKey(SEND_AUTO_RESPONSE_DATA))
        {
            Bundle bundle = new Bundle();

            for (String key : remoteMessage.getData().keySet())
            {
                bundle.putBoolean(key,Boolean.parseBoolean(remoteMessage.getData().get(key)));
            }

            Common.setAutoResponseSetting(getBaseContext(),bundle);

            return;
        }



        Intent intent = new Intent(INTENT_FILTER);
        long dialogId = 0;

        if (remoteMessage.getData().containsKey(DIALOG_ID)) {
            dialogId = Long.parseLong(remoteMessage.getData().get(DIALOG_ID));
        }

        if (remoteMessage.getData().containsKey(IS_CLOSE_DIALOG))
        {

            MBDialogData mbDialogData = new MBDialogData();
            Common.addDialog(getBaseContext(),String.valueOf(dialogId),mbDialogData,remoteMessage.getData().get(RESPONSE_ID));
            intent.putExtra(IS_CLOSE_DIALOG,remoteMessage.getData().get(IS_CLOSE_DIALOG));


        } else if (remoteMessage.getData().containsKey(IS_SEND_DEVICE))
        {
            intent.putExtra(IS_SEND_DEVICE,remoteMessage.getData().get(IS_SEND_DEVICE));
            intent.putExtra(DEVICE_TOKEN,remoteMessage.getData().get(DEVICE_TOKEN));
            intent.putExtra(NAME,remoteMessage.getData().get(NAME));

        } else {

            String id = remoteMessage.getData().get(RESPONSE_ID);
            String msgId = remoteMessage.getData().get(MESSAGE_ID);

            boolean isIdData = Boolean.parseBoolean(remoteMessage.getData().get(IS_ID_DATA));
            boolean isFromBell = Boolean.parseBoolean(remoteMessage.getData().get(FROM_BELL));

            MBResponseArray arr = MBResponseArray.initMBMessageData(this);
            String content;

            if (isIdData) {
                content = arr.getMBResponse(id).getMessageData(msgId).msg;
            }else {
                content = msgId;
            }


            MBDialogData mbDialogData = new MBDialogData(msgId,content,isFromBell,isIdData);
            Common.addDialog(getBaseContext(),String.valueOf(dialogId),mbDialogData,id);

            intent.putExtra(FROM_BELL,isFromBell);
            intent.putExtra(IS_ID_DATA,isIdData);
        }

        intent.putExtra(DIALOG_ID,dialogId);

        sendBroadcast(intent);

















        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


}
