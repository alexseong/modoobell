package com.modoobel.modoobell.custom_obj;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.modoobel.modoobell.R;
import com.modoobel.modoobell.fcm.MyFirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * Created by luckyleeis on 2017. 1. 6..
 */

public class Common {

    public static int MAIN_BUTTON_COUNT = 6; // 메인화면에서 한페이지에 나오는 응대버튼 갯수

    public static String RESPONSE_ID = "response_id";
    public static String MESSAGE_ID = "msg_id";
    public static String DIALOG_ID = "dialog_id";
    public static String IS_ID_DATA = "is_id_data";
    public static String IS_START = "is_start";
    public static String FROM_BELL = "from_bell";
    public static String NAME = "from_name";
    public static String IS_AUTO = "is_auto";
    public static String IS_CLOSE_DIALOG = "is_close_dialog";
    public static String IS_SEND_DEVICE = "is_send_device"; // 푸시메시지에서 디바이스 등록 메시지 일때 적용
    public static String DEVICE_REG_OK = "device_reg_ok";
    public static String VIDEO_CALL_CHANEL = "video_call_chanel";
    public static String SEND_AUTO_RESPONSE_DATA = "send_auto_response_data";
    public static String REQUEST_AUTO_RESPONSE_DATA = "request_auto_response_data";
    public static String DEVICE_TOKEN = "device_token";
    public static String FROM = "from_token";

    public static String OPERATION_CREATE = "create";
    public static String OPERATION_ADD = "add";
    public static String OPERATION_REMOVE = "remove";
    public static String NOTIFICATION_KEY = "notification_key";

    public static String fcbUrl = "https://fcm.googleapis.com/fcm/send";
    public static String fcbNotiUrl = "https://android.googleapis.com/gcm/notification";
    public static String fcbKey = "AAAAx-bZZFs:APA91bG5pYvmAupfm5NLsr_RWlFA7H9xU5YdV7E_SqM29HkfPRStlJE5fJMXSNPUL40dyKUfsT140wvvHsRTv_51r5vAOD6KtiPcqxVVcg8vnELVTzYl3UjdVr83oLV9nWEkDqe77uyM";
    public static String senderId = "858571498587";
    public static String playRtcKey = "60ba608a-e228-4530-8711-fa38004719c1";

    public static String BUNDLE_DEVICE_TOKEN = "token";
    public static String BUNDLE_DEVICE_NAME = "name";
    public static String BUNDLE_KEY_NAME = "key";

    public static int PASS_CODE_CHECK_OK = 1001;
    public static int PASS_CODE_CHECK_WRONG = 1002;

    public static String getRawDataToString(Context context, int raw)
    {
        InputStream inputStream = context.getResources().openRawResource(raw);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            int i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }

    public static int getResourceId(Context context, String pVariableName, String pResourcename)
    {
        try {
            String pPackageName = context.getPackageName();
            return context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * 비밀번호 설정
     * @param context
     * @param passCode
     */

    public static void setPassCode(Context context, String passCode)
    {
        SharedPreferences pref = context.getSharedPreferences("pass_code", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putString("pass_code",passCode);
        ePref.commit();

    }


    /**
     * 비밀번호 확인
     * @param context
     * @param passCode
     * @return
     */

    public static boolean checkPassCode(Context context, String passCode)
    {
        SharedPreferences pref = context.getSharedPreferences("pass_code", Activity.MODE_PRIVATE);
        String code = pref.getString("pass_code","0000");

        if (code.equals(passCode))
        {
            return true;
        }

        return false;

    }




    /**
     * 대화를 시작한다.
     * @param context
     * @param dialogId 대화 아이디
     */

    public static void startDialog(Context context, long dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putLong("status",dialogId);
        ePref.commit();

        Log.d("Tag","" + dialogId);
    }


    /**
     * 영상통화 채널 아이디를 저장한다.
     * @param context
     * @param chanelId
     */

    public static void setPlayRtcChanelId(Context context, String chanelId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putString(VIDEO_CALL_CHANEL,chanelId);
        ePref.commit();
    }


    /**
     * 영상통화 채널아이디를 가져온다.
     * @param context
     * @return
     */

    public static String getPlayRtcChanelId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        return pref.getString(VIDEO_CALL_CHANEL,"");
    }


    /**
     * 대화를 종료 한다.
     * @param context
     */

    public static void closeDialog(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.remove("status");
        ePref.remove("play_rtc_chanel_id");
        ePref.commit();
    }


    /**
     * 현재 상태를 리턴한다.
     * @return 대화중 - (현재 대화중인 dialogId), 대화중이 아닐때 -1
     */

    public static long getCurrentStatus(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        return pref.getLong("status",-1);
    }

    /**
     * dialogId를 이용하여 대화상대 기기ID를 리턴한다.
     * @param context
     * @param dialogId
     * @return
     */

    public static String getPriviousDialogToID(Context context, String dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        try {
            JSONObject jObj = new JSONObject(pref.getString(dialogId,""));
            return jObj.getString("to_id");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * dialogId를 이용하여 ResposeID(방문타입)를 리턴한다.
     * @param context
     * @param dialogId
     * @return
     */

    public static String getPriviousDialogResponseID(Context context, String dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        try {
            JSONObject jObj = new JSONObject(pref.getString(dialogId,""));
            return jObj.getString("responseId");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * dialogId를 이용하여 대화를 리턴한다
     * @param dialogId
     * @return
     */

    public static ArrayList<MBDialogData> getPriviousDialogData(Context context, String dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        Gson gson = new Gson();

        try {
            JSONObject jObj = new JSONObject(pref.getString(dialogId,""));

            Type type = new TypeToken<List<MBDialogData>>() {}.getType();
            ArrayList<MBDialogData> arrayList = gson.fromJson(jObj.getString("dialod_data"),type);
            return arrayList;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bundle getDialog(Context context, long dialogId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);

        String data = pref.getString(String.valueOf(dialogId),"");
        Bundle bundle = new Bundle();
        try {
            JSONObject jObj = new JSONObject(data);

            bundle.putString(RESPONSE_ID,jObj.getString(RESPONSE_ID));
            bundle.putString("dialod_data",jObj.getString("dialod_data"));

//            if (jObj.has(FROM)) {
//                bundle.putString(FROM,jObj.getString(FROM));
//            }

        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


        return bundle;
    }


    public static void addDialog(Context context, String dialogId, MBDialogData mbDialogData, String responseId, boolean isAuto)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        Gson gson = new Gson();
        ArrayList<MBDialogData> arrayList;
        JSONObject jObj;

        try {
            jObj = new JSONObject(pref.getString(dialogId,""));
            Type type = new TypeToken<List<MBDialogData>>() {}.getType();
            arrayList = gson.fromJson(jObj.getString("dialod_data"),type);
            arrayList.add(mbDialogData);

        } catch (JSONException e) {
            e.printStackTrace();

            arrayList = new ArrayList<>();
            String content = context.getString(R.string.purpose_visit);
            MBDialogData firstDialogData = new MBDialogData(null,content,false,false);
            arrayList.add(firstDialogData);
            arrayList.add(mbDialogData);

        }

        try {

            jObj = new JSONObject();

            if (!jObj.has(RESPONSE_ID)) jObj.put(RESPONSE_ID, responseId);
            if (!jObj.has(IS_AUTO)) jObj.put(IS_AUTO,isAuto);
            jObj.put("dialod_data",gson.toJson(arrayList));

            ePref.putString(dialogId,jObj.toString());
            ePref.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 대화내역 추가
     * @param context
     * @param dialogId 대화 아이디
     * @param mbDialogData
     * @param responseId
     */

    public static void addDialog(Context context, String dialogId, MBDialogData mbDialogData, String responseId)
    {
        addDialog(context,dialogId,mbDialogData,responseId,Common.isAutoResponse(context,responseId));
    }


    /**
     * 대화내역 저장
     * @param context
     * @param dialogId
     * @param arrayList
     */

    public static void saveDialog(Context context, String dialogId, ArrayList<MBDialogData> arrayList, String responseId)
    {
        SharedPreferences pref = context.getSharedPreferences("visitor", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        Gson gson = new Gson();
        try {

            JSONObject jObj = new JSONObject();
            jObj.put(RESPONSE_ID, responseId);
            jObj.put("dialod_data",gson.toJson(arrayList));
//            jObj.put(FROM,toId);

            ePref.putString(dialogId,jObj.toString());
            ePref.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 벨에 등록된 기기 리스트를 반환
     * @param context
     * @return
     */


    public static ArrayList<Bundle> getDeviceList(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);
        ArrayList<Bundle> arr = new ArrayList<>();

        for (String key : pref.getAll().keySet())
        {
            try {
                JSONObject jObj = new JSONObject(pref.getString(key,""));

                Bundle bundle = new Bundle();
                bundle.putString(BUNDLE_DEVICE_NAME,jObj.getString(BUNDLE_DEVICE_NAME));
                bundle.putString(BUNDLE_DEVICE_TOKEN,jObj.getString(BUNDLE_DEVICE_TOKEN));
                bundle.putString(BUNDLE_KEY_NAME,key);
                arr.add(bundle);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return arr;
    }

    /**
     * 벨에 등록된 기기 토큰 리스트를 반환
     * @param context
     * @return
     */

    public static ArrayList<String> getDeviceTokenList(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);

        ArrayList<String> arr = new ArrayList<>();

        for (String key : pref.getAll().keySet())
        {
            try {
                JSONObject jObj = new JSONObject(pref.getString(key,""));
                arr.add(jObj.getString(BUNDLE_DEVICE_TOKEN));
            } catch (JSONException e) {
                continue;
            }
        }

        return arr;
    }

    public static void deleteDevice(Context context, String keyName)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        try {
            JSONObject jObj = new JSONObject(pref.getString(keyName,""));
            String token = jObj.getString(BUNDLE_DEVICE_TOKEN);
            Common.addDeviceToGroup(context,Common.getNotificationName(context),token,OPERATION_REMOVE,getNotificationKey(context));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ePref.remove(keyName);
        ePref.commit();
    }


    /**
     * 벨에 기기를 등록한다.
     * @param context
     * @param deviceToken
     */

    public static boolean saveDevice(Context context, String deviceToken, String name)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);

        JSONObject jObj = new JSONObject();
        try {
            jObj.put(BUNDLE_DEVICE_NAME,name);
            jObj.put(BUNDLE_DEVICE_TOKEN,deviceToken);
        } catch (JSONException e) {
            return false;
        }

        String value = jObj.toString();


        for (String key : pref.getAll().keySet())
        {
            if (pref.getString(key,"").equals(value))
            {
                return false;
            }
        }

        SharedPreferences.Editor ePref = pref.edit();
        String key = "device_" + pref.getAll().size();
        ePref.putString(key,value);
        ePref.commit();

        return true;
    }


    /**
     * 기기에 벨을 등록한다.
     * @param context
     * @param bellKey
     */

    public static void saveBellDevice(Context context, String bellKey)
    {
        SharedPreferences pref = context.getSharedPreferences("device", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putString("bell",bellKey);
        ePref.commit();
    }


    public static void addDeviceToGroup(final Context context, final String notiName, final String token, final String operation, String notiKey)
    {

        final JSONObject json = new JSONObject();
        final JSONArray jarr = new JSONArray();
        try {
            jarr.put(token);
            json.put("operation",operation);
            json.put("notification_key_name",notiName);
            json.put("registration_ids",jarr);

            if (notiKey != null) json.put("notification_key",notiKey);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Thread myThread = new Thread(new Runnable() {
            public void run() {

                try {

                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(mediaType, json.toString());
                    Request request = new Request.Builder()
                            .url(fcbNotiUrl)
                            .post(body)
                            .addHeader("Content-Type","application/json")
                            .addHeader("Authorization","key=" + fcbKey)
                            .addHeader("project_id",senderId)
                            .build();

                    Response response = client.newCall(request).execute();
                    String result = "" + response.body().string();

                    if (response.isSuccessful())
                    {

                        JSONObject jObj = new JSONObject(result);

                        if (operation.equals(OPERATION_CREATE))
                        {
                            Common.saveNotificationKey(context,jObj.getString(NOTIFICATION_KEY),notiName);

                        }else if (operation.equals(OPERATION_ADD))
                        {

                            final JSONObject json = new JSONObject();
                            JSONObject data = new JSONObject();
                            data.put(DEVICE_REG_OK,true);
                            data.put(NOTIFICATION_KEY,jObj.getString(NOTIFICATION_KEY));
                            json.put("data",data);
                            json.put("to",token);

                            send(json,null);


                        }else if (operation.equals(OPERATION_REMOVE))
                        {

                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        myThread.start();
    }



    /**
     * 상대방에게 대화종료 메시지를 보낸다.
     * @param handler
     */

    public static void sendCloseDialogMessage(Context context, long dialogId, String responseId, final Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put(IS_CLOSE_DIALOG,true);
            data.put(DIALOG_ID,dialogId);
            data.put(RESPONSE_ID,responseId);
            data.put(FROM,FirebaseInstanceId.getInstance().getToken());
            json.put("data",data);
            json.put("to",getNotificationKey(context));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);

    }

    /**
     * 대화 내역을 보낸다
     * @param resId MBResponse ID (배달, 택배, 예약 등의 방문 목적)
     * @param msgId MBMessageData ID (대화 내용)
     * @param dialogId 대화의 ID (처음 대화 시작시의 시간)
     * @param isIdData 템플릿 대화일 경우 true, 직접 입력시 false
     * @param isStart 대화의 시작일 경우 true
     * @param handler
     */

    public static void sendToMessage(Context context, String resId, final String msgId, long dialogId, boolean isIdData, boolean isStart, final Handler handler) {
        sendToMessage(context,resId,msgId,dialogId,isIdData,isStart,true,handler);
    }

    public static void sendToMessage(Context context, String resId, final String msgId, long dialogId, boolean isIdData, boolean isStart, boolean fromBell, final Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(RESPONSE_ID,resId);
            data.put(MESSAGE_ID,msgId);
            data.put(DIALOG_ID, dialogId);
            data.put(IS_ID_DATA,isIdData);
            data.put(IS_START,isStart);
            data.put(FROM_BELL, fromBell);
            data.put(FROM,FirebaseInstanceId.getInstance().getToken());

            json.put("data",data);
            json.put("to",getNotificationKey(context));
            json.put("priority","high");
            json.put("time_to_live",0);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Tag",json.toString());
        send(json,handler);
    }


    public static void sendDeviceInfo(String to, Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(IS_SEND_DEVICE,true);
//            data.put(FROM, FirebaseInstanceId.getInstance().getToken());
            json.put("data",data);
            json.put("to",to);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);
    }

    public static void sendAddDeviceOKResponse(String to, Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(DEVICE_REG_OK,true);
//            data.put(FROM, FirebaseInstanceId.getInstance().getToken());
            json.put("data",data);
            json.put("to",to);
            json.put("priority","high");
            json.put("time_to_live",0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);
    }

    public static void sendChanelId(Context context, String chanelId, Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(VIDEO_CALL_CHANEL,chanelId);
            data.put(FROM,FirebaseInstanceId.getInstance().getToken());

            json.put("data",data);
            json.put("to",getNotificationKey(context));
            json.put("priority","high");
            json.put("time_to_live",0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Tag",json.toString());
        send(json,handler);
    }



    public static void send(final JSONObject json, final Handler handler)
    {
        Thread myThread = new Thread(new Runnable() {
            public void run() {

                try {

                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(mediaType, json.toString());
                    Request request = new Request.Builder()
                            .url(fcbUrl)
                            .post(body)
                            .addHeader("Content-Type","application/json")
                            .addHeader("Authorization","key=" + fcbKey)
                            .build();

                    Response response = client.newCall(request).execute();

                    Log.d("Tag","" + response.message() + "" + response);

                    if (handler != null) {
                        handler.sendEmptyMessage(0);
                    }

                    response.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        myThread.start();
    }

    public static void showMessage(Context context, String message)
    {
        LayoutInflater inflater = (LayoutInflater)context.getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_normal, null);
        TextView customTitle = (TextView)view.findViewById(R.id.title);
        customTitle.setText(message);

        view.findViewById(R.id.message).setVisibility(View.GONE);

        final AlertDialog alert = new AlertDialog.Builder(context).setView(view).create();


        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert.dismiss();     //닫기
            }
        });


        view.findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        alert.show();
    }


    /**
     * QR코드를 보여준다.
     * @param context
     * @param mMessageReceiver
     * @param handler
     */

    public static void showQRCode(final Context context, final BroadcastReceiver mMessageReceiver, final Handler handler)
    {
        //*****************************************************
        // QR코드
        //*****************************************************

        context.registerReceiver(mMessageReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER));

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.qrcode_alert);
        dialog.setTitle(context.getString(R.string.add_device_title));

        TextView tv = (TextView) dialog.findViewById(R.id.text);
        tv.setText(context.getString(R.string.add_device_content));

        Button cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (handler != null)
                    handler.sendEmptyMessage(0);
            }
        });


        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                context.unregisterReceiver(mMessageReceiver);
                if (handler != null)
                    handler.sendEmptyMessage(0);
            }
        });

        try {
            Bitmap bm = encodeAsBitmap(FirebaseInstanceId.getInstance().getToken(), 400);
            ImageView iv = (ImageView) dialog.findViewById(R.id.image);
            iv.setImageBitmap(bm);
            dialog.show();

        } catch (WriterException e) {
            e.printStackTrace();
        }

        //*****************************************************
    }

    /**
     * 데이터를 QR코드 비트맵으로 변환
     * @param str
     * @param size
     * @return
     * @throws WriterException
     */

    static Bitmap encodeAsBitmap(String str, int size) throws WriterException {
        BitMatrix result;
        Bitmap bitmap;
        try
        {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, size, size, null);

            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK:WHITE;
                }
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, w, h);
        } catch (Exception iae) {
            iae.printStackTrace();
            return null;
        }
        return bitmap;
    }

    public static void showPassCode(final Context context, final boolean isModify, final Handler handler)
    {

        LayoutInflater inflater = (LayoutInflater)context.getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.alert_pass_word, null);
        TextView customTitle = (TextView)view.findViewById(R.id.title);
        customTitle.setText(context.getString(R.string.input_passcode_title));

        TextView customMessage = (TextView)view.findViewById(R.id.message);
        customMessage.setText(context.getString(R.string.input_passcode_content));

        final EditText input = (EditText)view.findViewById(R.id.et_input);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final AlertDialog alert = new AlertDialog.Builder(context).setView(view).create();

        if (isModify) customMessage.setText(context.getString(R.string.modify_passcode_content));


        final String[] checkStr = {null};

        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String value = input.getText().toString();

                        if (isModify)
                        {
                            if (checkStr[0] == null)
                            {
                                checkStr[0] = value;
                                input.setText("");
                                Toast.makeText(context,context.getString(R.string.one_more_passcode),Toast.LENGTH_LONG).show();

                            }else {

                                if (value.equals(checkStr[0]))
                                {
                                    setPassCode(context,value);
                                    Toast.makeText(context,context.getString(R.string.complete_modify_passcode),Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }else {
                                    Toast.makeText(context,context.getString(R.string.wrong_passcode),Toast.LENGTH_LONG).show();
                                }
                            }


                        }else {

                            if (Common.checkPassCode(context,value))
                            {
                                Message msg = handler.obtainMessage();
                                msg.what = PASS_CODE_CHECK_OK;
                                handler.sendMessage(msg);
                                dialog.dismiss();

                            } else {
                                Toast.makeText(context,context.getString(R.string.wrong_passcode),Toast.LENGTH_LONG).show();
                            }
                        }

//                        alert.dismiss();     //닫기
                    }
                });
            }
        });





        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert.dismiss();
            }
        });


        alert.show();



















//        final EditText input = new EditText(context);
//        input.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
//
//        final AlertDialog dialog = new AlertDialog.Builder(context)
//                .setView(input)
//                .setTitle(context.getString(R.string.input_passcode_title))
//                .setMessage(context.getString(R.string.input_passcode_content))
//                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
//                .setNegativeButton(android.R.string.cancel, null)
//                .create();
//
//        if (isModify) dialog.setMessage(context.getString(R.string.modify_passcode_content));
//
//        final String[] checkStr = {null};
//
//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//
//            @Override
//            public void onShow(final DialogInterface dialog) {
//
//                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
//                button.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View view) {
//                        // TODO Do something
//                        String value = input.getText().toString();
//
//                        if (isModify)
//                        {
//                            if (checkStr[0] == null)
//                            {
//                                checkStr[0] = value;
//                                input.setText("");
//                                Toast.makeText(context,context.getString(R.string.one_more_passcode),Toast.LENGTH_LONG).show();
//
//                            }else {
//
//                                if (value.equals(checkStr[0]))
//                                {
//                                    setPassCode(context,value);
//                                    Toast.makeText(context,context.getString(R.string.complete_modify_passcode),Toast.LENGTH_LONG).show();
//                                    dialog.dismiss();
//                                }else {
//                                    Toast.makeText(context,context.getString(R.string.wrong_passcode),Toast.LENGTH_LONG).show();
//                                }
//                            }
//
//
//                        }else {
//
//                            if (Common.checkPassCode(context,value))
//                            {
//                                Message msg = handler.obtainMessage();
//                                msg.what = PASS_CODE_CHECK_OK;
//                                handler.sendMessage(msg);
//                                dialog.dismiss();
//
//                            } else {
//                                Toast.makeText(context,context.getString(R.string.wrong_passcode),Toast.LENGTH_LONG).show();
//                            }
//                        }
//
//                    }
//                });
//            }
//        });
//
//        dialog.show();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod (input .getApplicationWindowToken(),InputMethodManager.SHOW_FORCED);
    }

    public static Bitmap cropFaceFromBitmap(Bitmap original, Face face) {

        int x = face.getPosition().x < 0 ? 0:(int)face.getPosition().x;
        int y = face.getPosition().y < 0 ? 0:(int)face.getPosition().y;
        int width = (int)face.getWidth();
        int height = (int)face.getHeight();

        if (width + x > original.getWidth()) {
            width = original.getWidth() - x;
        }

        if (height + y > original.getHeight()) {
            height = original.getHeight() - y;
        }

        Bitmap result = Bitmap.createBitmap(original
                , x
                , y
                , width
                , height);
        return result;
    }

    public static boolean isSaveVisitorPicture(Context context, long dialogId)
    {
        //*************************************************************************************
        // 폴더생성
        File file = new File(context.getApplicationContext().getFilesDir() + "/visitor/" + dialogId);
        if (file.exists()) {
            return true;
        }else {
            return false;
        }
        //*************************************************************************************
    }


    public static void saveVisitorPicture(Context context, Bitmap bitmap, long dialogId, Runnable saveVisitorPicture)
    {
        //*************************************************************************************
        // 폴더생성
        File dirName = new File(context.getApplicationContext().getFilesDir() + "/visitor");
        if (!dirName.exists()) {
            dirName.mkdirs();
        }
        //*************************************************************************************

        if (isSaveVisitorPicture(context,dialogId)) return;

        FaceDetector fd = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> mFaces = fd.detect(frame);
        fd.release();

        Bitmap face;

        String file_name = dialogId+".jpg";
        String file_name_face = dialogId+"_face.jpg";
        String string_path = dirName.getAbsolutePath() + "/" + file_name;
        String string_path_face = dirName.getAbsolutePath() + "/" + file_name_face;

        if (mFaces.size() > 0)
        {
            face = Common.cropFaceFromBitmap(bitmap,mFaces.valueAt(0));
        }else {

            savePicture(string_path,bitmap);
            Handler handler = new Handler();
            handler.postDelayed(saveVisitorPicture,1000);
            return;
        }

        try{
            savePicture(string_path,bitmap);
            FileOutputStream out_face = new FileOutputStream(string_path_face);
            face.compress(Bitmap.CompressFormat.JPEG, 100, out_face);
            out_face.close();

        }catch(FileNotFoundException exception){
            Log.d("Tag", exception.getMessage());
        }catch(IOException exception){
            Log.d("Tag", exception.getMessage());
        }
    }

    public static void savePicture(String path, Bitmap bitmap)
    {
        try{
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        }catch(FileNotFoundException exception){
            Log.d("Tag", exception.getMessage());
        }catch(IOException exception){
            Log.d("Tag", exception.getMessage());
        }

    }

    public static String getSavedFile(Context context, String dialogId, boolean isFace)
    {
        File dirName = new File(context.getApplicationContext().getFilesDir() + "/visitor");
        String file_name;

        if (isFace)
        {
            file_name = dialogId+"_face.jpg";
        }else {

            file_name = dialogId+".jpg";
        }

        return dirName.getAbsolutePath() + "/" + file_name;
    }


    /**
     * 자동응답 모드 세팅 리스트를 불러온다.
     * @param context
     * @return
     */

    public static Bundle getAutoResponseSetting(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        MBResponseArray arr = MBResponseArray.initMBMessageData(context);

        Bundle bundle = new Bundle();
        for (String key : arr.getIdList())
        {
            bundle.putBoolean(key,pref.getBoolean(key,false));
        }

        return bundle;
    }

    /**
     *  자동응답 세팅 온오프
     * @param context
     */

    public static void setAutoResponseSetting(Context context, Bundle bundle)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();

        for (String key : bundle.keySet())
        {
            ePref.putBoolean(key,bundle.getBoolean(key,false));
        }

        ePref.commit();
    }

    /**
     * 자동응답 세팅내역을 전송한다.
     * @param context
     * @param to
     * @param handler
     */

    public static void sendAutoResponseSettingData(Context context, String to, Handler handler)
    {
        final JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        Bundle bunSetting = getAutoResponseSetting(context);

        try {

            for (String key : bunSetting.keySet())
            {
                data.put(key,bunSetting.getBoolean(key,false));
            }
            data.put(SEND_AUTO_RESPONSE_DATA,true);

            json.put("data",data);
            json.put("to",to);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(json,handler);
    }


    public static boolean isAutoResponse(Context context, String resId)
    {
        SharedPreferences pref = context.getSharedPreferences("setting_response", Activity.MODE_PRIVATE);
        return pref.getBoolean(resId,false);
    }



    /**
     * 메시지 키를 저장한다.
     * @param notiKey
     */

    public static void saveNotificationKey(Context context, String notiKey, String notiName)
    {
        SharedPreferences pref = context.getSharedPreferences("notiKey", Activity.MODE_PRIVATE);
        SharedPreferences.Editor ePref = pref.edit();
        ePref.putString("key",notiKey);
        ePref.putString("name",notiName);
        ePref.commit();

        Log.d("Tag",notiKey);
        Log.d("Tag",notiName);
    }


    public static String getNotificationKey(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("notiKey", Activity.MODE_PRIVATE);
        return pref.getString("key","");
    }

    public static String getNotificationName(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("notiKey", Activity.MODE_PRIVATE);
        return pref.getString("name","");
    }

}
