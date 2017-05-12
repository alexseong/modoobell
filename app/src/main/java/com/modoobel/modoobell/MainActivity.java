package com.modoobel.modoobell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.iid.FirebaseInstanceId;
import com.modoobel.modoobell.adapter.BellPagerAdapter;
import com.modoobel.modoobell.adapter.BellPagerAdapter.onClickMainButtonListner;
import com.modoobel.modoobell.custom_lib.PageIndicator;
import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.MBResponse;
import com.modoobel.modoobell.custom_obj.MBResponseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

import static com.modoobel.modoobell.custom_obj.Common.PASS_CODE_CHECK_OK;

public class MainActivity extends AppCompatActivity {

    private ViewPager pager;
    private BellPagerAdapter adapter;
    private PageIndicator indicator;
    private MBResponseArray mbResponseArray;
    private CameraSource mCameraSource = null;
    private Timer mTimer;
    private TimerTask mTask;

    private Timer mTimerScreenStatus;
    private TimerTask mTaskScreenStatus;
    private int crrMenuBtnIdx = 0;
    static private SpeechRecognizer mRecognizer;
    AnimationDrawable inputVoiceAni;
    private ImageView ivInputIcon;
    TextToSpeech tts;

    private boolean isScreenOn()
    {
        try {

            Log.d("Tag","B : " + android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS));

            if(android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS) == 0){

                screenOn(true);
                return false;
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        screenOn(true);
        ((TextView)findViewById(R.id.title)).setText(R.string.welcome);

        try {
            mCameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {

        }
    }

    private void onScreen()
    {

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f;
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(params);

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, 255);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTimerScreenStatus != null){
            mTimerScreenStatus.cancel();
            mTimerScreenStatus = null;
        }
        onScreen();

        Log.d("Tag","onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimerScreenStatus != null){
            mTimerScreenStatus.cancel();
            mTimerScreenStatus = null;
        }

        onScreen();

        Log.d("Tag","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimerScreenStatus != null){
            mTimerScreenStatus.cancel();
            mTimerScreenStatus = null;
        }

        mCameraSource.stop();
        onScreen();

        Log.d("Tag","onStop");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        FirebaseInstanceId.getInstance().getToken();
        initUI();

    }

    void initUI()
    {

        mbResponseArray = MBResponseArray.initMBMessageData(this);

        pager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new BellPagerAdapter(getSupportFragmentManager(),mbResponseArray);
        adapter.setOnMainButtonClickListner(mainButtonListner);
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(final int position) {
                indicator.selectDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        indicator = (PageIndicator) findViewById(R.id.indicator);
        indicator.setItemMargin(20);
        indicator.setAnimDuration(300);
        indicator.createDotPanel(adapter.getCount(), R.drawable.indicator_unselected , R.drawable.indicator_selected);



        inputVoiceAni = (AnimationDrawable) findViewById(R.id.input_voice).getBackground();
        ivInputIcon = (ImageView) findViewById(R.id.input_icon);


        setScreenBright();
        createCameraSource();


        ArrayList<String> arrayList = Common.getDeviceTokenList(getBaseContext());
        if (arrayList.size() == 0)
        {
            showQRCode();
        }

        tts = new TextToSpeech(this,null);
    }

    public void clkMenu(View v)
    {
        isScreenOn();

        int id = v.getId();

        switch (crrMenuBtnIdx)
        {
            case 0 :
                if (id == R.id.btn_menu_1) crrMenuBtnIdx = 1;
                else crrMenuBtnIdx = 0;
                break;

            case 1 :
                if (id == R.id.btn_menu_2) crrMenuBtnIdx = 2;
                else crrMenuBtnIdx = 0;
                break;

            case 2 :
                if (id == R.id.btn_menu_3) crrMenuBtnIdx = 3;
                else crrMenuBtnIdx = 0;
                break;

            case 3 :
                if (id == R.id.btn_menu_4) {
                    crrMenuBtnIdx = 0;
                    showPassCode();

                    if (mTimerScreenStatus != null) {
                        mTimerScreenStatus.cancel();
                    }
                }
                break;
        }




    }

    public void showPassCode()
    {
        Common.showPassCode(this,false,new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == PASS_CODE_CHECK_OK)
                {
                    Intent intent = new Intent(MainActivity.this,MenuActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });
    }


    public void showQRCode()
    {

        if (mTimerScreenStatus != null) {
            mTimerScreenStatus.cancel();
            mTimerScreenStatus = null;
        }


        if (FirebaseInstanceId.getInstance().getToken() == null)
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   showQRCode();
                }
            },200);

            return;
        }


        //*****************************************************
        // QR코드
        //*****************************************************

        Common.showQRCode(this, mMessageReceiver, new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                screenOn(true);
            }
        });

    }


    private void setScreenBright()
    {
        try{

            if(android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == 1){

                android.provider.Settings.System.putInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            }

            screenOn(true);

        }catch(Exception e){}

    }

    private void screenOn(final boolean isOn)
    {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float appBright = 0.0f;
                int systemBright = 0;

                if (isOn) {
                    appBright = 1.0f;
                    systemBright = 255;


                    try {
                        if(Settings.System.getInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS) == 0){
                            tts.speak("안녕하세요. 모두벨입니다. 방문목적을 선택하세요.", TextToSpeech.QUEUE_FLUSH, null);

                            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {

                                }

                                @Override
                                public void onDone(String utteranceId) {

                                }

                                @Override
                                public void onError(String utteranceId) {

                                }
                            });

                        }
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (mTimerScreenStatus != null) {
                        mTimerScreenStatus.cancel();
                    }

                    mTimerScreenStatus = new Timer();
                    prepareScreenStatusTask();
                    mTimerScreenStatus.schedule(mTaskScreenStatus, 5000);


                }

                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = appBright;
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                getWindow().setAttributes(params);

                android.provider.Settings.System.putInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS, systemBright);

            }
        });
    }



    private void prepareTask()
    {

        mTask = new TimerTask() {
            @Override
            public void run() {
                screenOn(true);
            }
        };
    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {}

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .build();

        try {
            mCameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker();
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {

        GraphicFaceTracker() {}

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            Log.d("Tag", "Id : " + faceId);

            if (mTimer == null) {
                mTimer = new Timer();
                prepareTask();
                mTimer.schedule(mTask, 1);
            }
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            Log.d("Tag", "onMissing");

        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {

            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

        }
    }

    private void prepareScreenStatusTask()
    {
        mTaskScreenStatus = new TimerTask() {
            @Override
            public void run() {
                screenOn(false);
                mTimerScreenStatus = null;
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Your code here

//        if (ev.getAction() == MotionEvent.ACTION_DOWN)
//        {
//            screenOn(true);
//        }

        return super.dispatchTouchEvent(ev);
    }


    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(Common.IS_SEND_DEVICE))
            {
                String from = intent.getStringExtra(Common.DEVICE_TOKEN);
                String name = intent.getStringExtra(Common.NAME);

                Common.saveDevice(getBaseContext(),from, name);
                Common.sendAddDeviceOKResponse(from,new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                    }
                });
            }
        }
    };

    /**
     * 응대버튼 클릭시
     */

    private onClickMainButtonListner mainButtonListner = new onClickMainButtonListner() {
        @Override
        public void onClick(final MBResponse mbResponse) {

            if (!isScreenOn()) return;

            ArrayList<String> arrayList = Common.getDeviceTokenList(getBaseContext());

            if (arrayList.size() == 0) {
                Common.showMessage(MainActivity.this,getString(R.string.no_reg_device));
                return;
            }

            if (mbResponse.getId().equals("more")) {
                int item = pager.getCurrentItem() + 1;
                if (item == adapter.getCount()) item = 0;
                pager.setCurrentItem(item,true);

                ((TextView)findViewById(R.id.title)).setText("방문목적을 직접 입력하시거나,\n" + "마이크를 누른 후 직접 말씀하세요");

                return;
            }

            if (mbResponse.messageData.size() == 0)
            {
                Common.showMessage(MainActivity.this,"메시지 데이터가 없습니다.");
                return;
            }

            sendMessage(mbResponse.id, true);

        }
    };


    public boolean onKeyDown(int keycode, KeyEvent event)
    {
        switch(keycode)
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                screenOn(true);
                break;
        }
        return true;
    }


    public void clkStartSTT(View v)
    {
        if (!isScreenOn()) return;

        tts.stop();

        if (mRecognizer == null) {
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
        }

        if (inputVoiceAni.isRunning()) {
            mRecognizer.cancel();
            stopSTT();
            return;
        }


        inputVoiceAni.start();
        ivInputIcon.setImageResource(R.drawable.microphone_in);





        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        mRecognizer.startListening(i);

    }

    //음성인식 리스너
    private RecognitionListener listener = new RecognitionListener() {
        //입력 소리 변경 시
        @Override public void onRmsChanged(float rmsdB) {}
        //음성 인식 결과 받음
        @Override public void onResults(Bundle results) {
            ArrayList<String> arrResult = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (arrResult.size() > 0) {
                final String message = arrResult.get(0);

                ArrayList<String> arrayList = Common.getDeviceTokenList(getBaseContext());

                if (arrayList.size() == 0) {
                    Common.showMessage(MainActivity.this,getString(R.string.no_reg_device));
                    return;
                }

                sendMessage(message, false);
            }

            inputVoiceAni.stop();

            View v = findViewById(R.id.input_voice);
            v.setBackground(null);
            v.setBackgroundResource(R.drawable.input_voice_ani);
            inputVoiceAni = (AnimationDrawable) v.getBackground();
            ivInputIcon.setImageResource(R.drawable.microphone);

            mRecognizer.cancel();
        }

        @Override public void onBeginningOfSpeech() {}
        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onReadyForSpeech(Bundle params) {}
        @Override public void onEndOfSpeech() {}
        @Override public void onError(int error) {

            mRecognizer.cancel();
        }
    };

    public void stopSTT()
    {
        inputVoiceAni.stop();
        View v = findViewById(R.id.input_voice);
        v.setBackground(null);
        v.setBackgroundResource(R.drawable.input_voice_ani);
        inputVoiceAni = (AnimationDrawable) v.getBackground();
        ivInputIcon.setImageResource(R.drawable.microphone);
    }


    public void clkDirectMsg (View v)
    {
        if (!isScreenOn()) return;

        if (mTimerScreenStatus != null) {
            mTimerScreenStatus.cancel();
        }


        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.alert_pass_word, null);
        TextView customTitle = (TextView)view.findViewById(R.id.title);
        customTitle.setText(getString(R.string.purpose_visit_title));

        TextView customMessage = (TextView)view.findViewById(R.id.message);
        customMessage.setText(getString(R.string.purpose_visit));


        final AlertDialog alert = new AlertDialog.Builder(this).setView(view).create();


        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String value = ((EditText)view.findViewById(R.id.et_input)).getText().toString();
                sendMessage(value, false);
                alert.dismiss();     //닫기
            }
        });


        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenOn(true);
                alert.dismiss();
            }
        });

        alert.show();


//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInputFromInputMethod (input .getApplicationWindowToken(),InputMethodManager.SHOW_FORCED);

    }

    public void sendMessage(final String id, final boolean isIdData)
    {

        final String content;
        if (isIdData) content = "msg1";
        else content = id;

        final long dialogId = System.currentTimeMillis();

        Intent intent = new Intent(MainActivity.this,DialogActivity.class);
        intent.putExtra(Common.RESPONSE_ID,id);
        intent.putExtra(Common.MESSAGE_ID,content);
        intent.putExtra(Common.DIALOG_ID,dialogId);
        intent.putExtra(Common.IS_ID_DATA, isIdData);

        startActivity(intent);


    }

}
