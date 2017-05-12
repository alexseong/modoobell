package com.modoobel.modoobell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.modoobel.modoobell.adapter.CommAdapter;
import com.modoobel.modoobell.custom_obj.Common;
import com.modoobel.modoobell.custom_obj.InputButtonSet;
import com.modoobel.modoobell.custom_obj.MBDialogData;
import com.modoobel.modoobell.custom_obj.MBResponse;
import com.modoobel.modoobell.custom_obj.MBResponseArray;
import com.modoobel.modoobell.fcm.MyFirebaseMessagingService;
import com.sktelecom.playrtc.PlayRTC;
import com.sktelecom.playrtc.PlayRTCFactory;
import com.sktelecom.playrtc.config.PlayRTCConfig;
import com.sktelecom.playrtc.config.PlayRTCVideoConfig;
import com.sktelecom.playrtc.exception.RequiredConfigMissingException;
import com.sktelecom.playrtc.exception.RequiredParameterMissingException;
import com.sktelecom.playrtc.exception.UnsupportedPlatformVersionException;
import com.sktelecom.playrtc.observer.PlayRTCObserver;
import com.sktelecom.playrtc.stream.PlayRTCMedia;
import com.sktelecom.playrtc.util.ui.PlayRTCVideoView;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.modoobel.modoobell.custom_obj.Common.DIALOG_ID;
import static com.modoobel.modoobell.custom_obj.Common.FROM_BELL;
import static com.modoobel.modoobell.custom_obj.Common.IS_ID_DATA;

public class DialogActivity extends AppCompatActivity {


    CommAdapter adapter;
    MBResponse mbResponse;
    InputButtonSet inputButtonSet;
    RecyclerView recyclerView;
    private TextView tvMain;
    private PlayRTCObserver playrtcObserver;
    private PlayRTC playrtc = null;
    private SpeechRecognizer mRecognizer;
    private PlayRTCMedia localMedia;
    private PlayRTCVideoView localView;
    private TextToSpeech tts;
    private AnimationDrawable inputVoiceAni;
    private ImageView ivInputIcon;
    private Handler closeDialogHandler = new Handler();
    private static int CLOSE_TIME = 60000;
    private static int ALERT_CLOSE_TIME = 60000;
    private AlertDialog closeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_dialog);
        registerReceiver(mMessageReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER));


        initUI();

        if (getIntent().getStringExtra(Common.RESPONSE_ID) != null) {


            //*******************************
            // 메시지 관련 데이터 수신
            //*******************************
            final String id = getIntent().getStringExtra(Common.RESPONSE_ID);
            final String msgId = getIntent().getStringExtra(Common.MESSAGE_ID);
            final boolean isIdData = getIntent().getBooleanExtra(IS_ID_DATA,false);
            final long dialogId = getIntent().getLongExtra(DIALOG_ID,0);

            mbResponse = MBResponseArray.initMBMessageData(this).getMBResponse(id);

            if (mbResponse == null)
            {
                mbResponse = new MBResponse("",msgId);
            }


            //*************************************************************************************
            // 메시지 입력
            //*************************************************************************************

            String content;
            if (isIdData) {
                content = mbResponse.getMessageData(msgId).msg;
            }else {
                content = msgId;
            }

            MBDialogData mbDialogData = new MBDialogData(msgId,content,true,isIdData);
            Common.addDialog(getBaseContext(),String.valueOf(dialogId),mbDialogData,id);
            adapter.reloadAdapter(dialogId);
            //*************************************************************************************

            Common.sendToMessage(getBaseContext(),id,msgId,dialogId,isIdData,true,new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    if (Common.isAutoResponse(getBaseContext(),id))
                    {
                        String autoResMessage = mbResponse.auto_response;
                        Common.sendToMessage(getBaseContext(),id,autoResMessage,dialogId,false,true,false,null);

                        //*************************************************************************************
                        // 메시지 입력
                        //*************************************************************************************
                        MBDialogData mbDialogData = new MBDialogData(msgId,autoResMessage,false,false);
                        Common.addDialog(getBaseContext(),String.valueOf(dialogId),mbDialogData,id);
                        adapter.reloadAdapter(dialogId);
                        //*************************************************************************************


                    }
                }
            });

        }
    }


    private void initUI()
    {
        inputButtonSet = (InputButtonSet) findViewById(R.id.input_btn_set);
        inputButtonSet.setOnClickInputButtonListner(mClickInputButtonListner);
        inputButtonSet.setEmptyInputButton();

        adapter = new CommAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);

        tvMain = (TextView) findViewById(R.id.tv_main);

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);


        inputVoiceAni = (AnimationDrawable) findViewById(R.id.input_voice).getBackground();
        ivInputIcon = (ImageView) findViewById(R.id.input_icon);

        //*************************************************************************
        // 화상통화 관련 설정

        createVideoView();
        createPlayRTCInstance();


        Handler handler = new Handler();
        handler.postDelayed(saveVisitorPicture,500);

        closeDialogHandler.postDelayed(closeDialogRunnable,CLOSE_TIME);
    }


    private Runnable saveVisitorPicture = new Runnable() {
        @Override
        public void run() {

            if (localView == null)
            {
                Handler handler = new Handler();
                handler.postDelayed(saveVisitorPicture,500);
                return;
            }

            localView.snapshot(new PlayRTCVideoView.SnapshotObserver() {
                @Override
                public void onSnapshotImage(Bitmap bitmap) {
                    Common.saveVisitorPicture(DialogActivity.this,bitmap,adapter.dialogId,saveVisitorPicture);
                }
            });
        }
    };


    private InputButtonSet.onClickInputButtonListner mClickInputButtonListner = new InputButtonSet.onClickInputButtonListner() {
        @Override
        public void onClick(final String msgId) {

            inputButtonSet.setEmptyInputButton();

            //*************************************************************************************
            // 메시지 입력
            //*************************************************************************************
            String content = mbResponse.getMessageData(msgId).msg;
            MBDialogData mbDialogData = new MBDialogData(msgId,content,true,true);
            Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,mbResponse.id);
            adapter.reloadAdapter(adapter.dialogId);
            //*************************************************************************************


            Common.sendToMessage(getBaseContext(),mbResponse.id,msgId,adapter.dialogId,true, false, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                }
            });
        }

        @Override
        public void onClickMore() {
            showCloseDialogMessage();
        }

        @Override
        public void onClickSendMessage(final String message) {

            inputButtonSet.setEmptyInputButton();

            //*************************************************************************************
            // 메시지 입력
            //*************************************************************************************
            MBDialogData mbDialogData = new MBDialogData(message,message,true,true);
            Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,mbResponse.id);
            adapter.reloadAdapter(adapter.dialogId);
            //*************************************************************************************

            Common.sendToMessage(getBaseContext(), mbResponse.id,message,adapter.dialogId,false, false, new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            });
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.hasExtra(Common.IS_CLOSE_DIALOG))
            {
                adapter.reloadAdapter(adapter.dialogId);
                Common.closeDialog(getBaseContext());
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                closeDialogHandler.removeCallbacks(closeDialogRunnable);

                inputButtonSet.setDisEnable();
                finish();


                return;
            }


            long dialogId = intent.getLongExtra(DIALOG_ID,0);

            if (dialogId != 0)
            {
                adapter.reloadAdapter(dialogId);
                if (adapter.getItemCount() > 0)
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);


                if (!intent.getBooleanExtra(FROM_BELL,false))
                {
                    tts = new TextToSpeech(DialogActivity.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            String text = adapter.getLastDialogMessageContent();
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });

                    //*******************************
                    // 하단 메시지 선택 구성

                    boolean isIdData = intent.getBooleanExtra(IS_ID_DATA,false);
                    if (adapter.getLastDialogMessageId() == null || mbResponse == null || !isIdData) {
                        inputButtonSet.setEmptyInputButton();
                    }else {
                        inputButtonSet.setInputButton(mbResponse, adapter.getLastDialogMessageId());
                    }
                }


                // 종료시
                if (adapter.isClose(mbResponse))
                {
                    showCloseDialogMessage();
                    return;
                }
            }
        }
    };


    private void showCloseDialogMessage()
    {
        closeDialogHandler.removeCallbacks(closeDialogRunnable);

        AlertDialog.Builder alert = new AlertDialog.Builder(DialogActivity.this);

        closeDialog = alert.create();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                inputButtonSet.setDisEnable();

                MBDialogData mbDialogData = new MBDialogData();
                Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,adapter.responseId);
                Common.sendCloseDialogMessage(getBaseContext(),adapter.dialogId, adapter.responseId,null);

                if (closeDialog != null) {
                    closeDialog.dismiss();
                }

                finish();
            }
        };

        final Handler handler = new Handler();
        handler.postDelayed(runnable, ALERT_CLOSE_TIME);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                handler.removeCallbacks(runnable);
                inputButtonSet.setDisEnable();

                MBDialogData mbDialogData = new MBDialogData();
                Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,adapter.responseId);
                Common.sendCloseDialogMessage(getBaseContext(),adapter.dialogId, adapter.responseId,null);

                dialog.dismiss();     //닫기
                finish();
            }
        });

        alert.setNegativeButton(R.string.cancel,null);
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
                closeDialog = null;
                closeDialogHandler.postDelayed(closeDialogRunnable,CLOSE_TIME);
            }
        });

        alert.setMessage(getString(R.string.close_dialog));
        alert.show();

    }


    private void createVideoView()
    {
        if (localView == null) {
            RelativeLayout videoView = (RelativeLayout) findViewById(R.id.video_view);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

            localView = new PlayRTCVideoView(this);
            localView.setLayoutParams(param);
            localView.initRenderer();

            videoView.addView(localView);

        }
    }


    private void createPlayRTCInstance() {

        createPlayRTCObserverInstance();
        try {
            PlayRTCConfig setting = setPlayRTCConfiguration();
            playrtc = PlayRTCFactory.createPlayRTC(setting, playrtcObserver);
            createChannel();

        } catch (UnsupportedPlatformVersionException e) {
            e.printStackTrace();

        } catch (RequiredParameterMissingException e) {
            e.printStackTrace();
        }
    }

    private PlayRTCConfig setPlayRTCConfiguration() {

        PlayRTCConfig settings = PlayRTCFactory.createConfig();
        // PlayRTC instance have to get the application context.
        settings.setAndroidContext(getApplicationContext());

        // T Developers Project Key.
        settings.setProjectId(Common.playRtcKey);
        // video는 기본 640x480 30 frame
        settings.video.setEnable(true);
        settings.video.setCameraType(PlayRTCVideoConfig.CameraType.Front);
        settings.audio.setEnable(true);
        settings.audio.setAudioManagerEnable(true); //음성 출력 장치 자동 선택 기눙 활성화
        settings.data.setEnable(false);

        return settings;
    }


    private void createChannel() {
        try {
            // createChannel must have a JSON Object
            playrtc.createChannel(new JSONObject());
        } catch (RequiredConfigMissingException e) {
            e.printStackTrace();
        }
    }


    //***************************************************************
    // 화상통화 관련 옵저버

    private void createPlayRTCObserverInstance() {
        playrtcObserver = new PlayRTCObserver() {
            @Override
            public void onConnectChannel(final PlayRTC obj, final String channelId, final String channelCreateReason) {

                Common.sendChanelId(getBaseContext(),channelId,null);

            }

            @Override
            public void onAddLocalStream(final PlayRTC obj, final PlayRTCMedia playRTCMedia) {
                localMedia = playRTCMedia;
                localView.show(0);
                // Link the media stream to the view.
                playRTCMedia.setVideoRenderer(localView.getVideoRenderer());
            }

            @Override
            public void onAddRemoteStream(final PlayRTC obj, final String peerId, final String peerUserId, final PlayRTCMedia playRTCMedia) {
                playRTCMedia.setVideoMute(true);
            }

            @Override
            public void onDisconnectChannel(final PlayRTC obj, final String disconnectReason) {

            }

            @Override
            public void onOtherDisconnectChannel(final PlayRTC obj, final String peerId, final String peerUserId) {

            }
        };
    }


    public void clkStartSTT(View v)
    {
        Log.d("Tag","clkStartSTT");


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



                Common.sendToMessage(getBaseContext(),mbResponse.id,message,adapter.dialogId,false, false, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        //*************************************************************************************
                        // 메시지 입력
                        //*************************************************************************************
                        MBDialogData mbDialogData = new MBDialogData(message,message,true,true);
                        Common.addDialog(getBaseContext(),String.valueOf(adapter.dialogId),mbDialogData,mbResponse.id);
                        adapter.reloadAdapter(adapter.dialogId);
                        //*************************************************************************************

                        inputButtonSet.setEmptyInputButton();
                    }
                });
            }

            localMedia.setAudioMute(false);

            inputVoiceAni.stop();

            View v = findViewById(R.id.input_voice);
            v.setBackground(null);
            v.setBackgroundResource(R.drawable.input_voice_ani);
            inputVoiceAni = (AnimationDrawable) v.getBackground();
            ivInputIcon.setImageResource(R.drawable.microphone);

            mRecognizer.cancel();

        }

        //음성 인식 준비가 되었으면
        @Override public void onReadyForSpeech(Bundle params) {
            Log.d("Tag","onReadyForSpeech : " + params.toString());
            localMedia.setAudioMute(true);
        }

        @Override public void onEndOfSpeech() {
            Log.d("Tag","onEndOfSpeech");
        }
        @Override public void onError(int error) {
            Log.d("Tag","onError : " + error);
            mRecognizer.cancel();
            stopSTT();
        }
        @Override public void onBeginningOfSpeech() {
            Log.d("Tag","onBeginningOfSpeech");
        }
        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
        @Override public void onBufferReceived(byte[] buffer) {}
    };

    public void stopSTT()
    {
        Log.d("Tag","stopSTT");

        inputVoiceAni.stop();

        View v = findViewById(R.id.input_voice);
        v.setBackground(null);
        v.setBackgroundResource(R.drawable.input_voice_ani);
        inputVoiceAni = (AnimationDrawable) v.getBackground();
        ivInputIcon.setImageResource(R.drawable.microphone);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == KeyEvent.ACTION_DOWN)
        {
            closeDialogHandler.removeCallbacks(closeDialogRunnable);
            closeDialogHandler.postDelayed(closeDialogRunnable,CLOSE_TIME);
        }

        return super.dispatchTouchEvent(ev);
    }

    private Runnable closeDialogRunnable = new Runnable() {
        @Override
        public void run() {
            showCloseDialogMessage();
        }
    };




    @Override
    public void onBackPressed() {

//        Log.d("Tag","onBackPressed" + adapter.toId);
//        Common.sendCloseDialogMessage(adapter.toId,new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                Common.closeDialog(getBaseContext());
//            }
//        });

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(playrtc != null) {
            playrtc.close();
        }

        if (closeDialogHandler != null)
        {
            closeDialogHandler.removeCallbacks(closeDialogRunnable);
            closeDialogHandler = null;
        }

        this.unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

}
