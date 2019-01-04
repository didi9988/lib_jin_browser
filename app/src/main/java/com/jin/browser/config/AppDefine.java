package com.jin.browser.config;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.jin.browser.db.JinDbHelper;


/**
 * Created by KWY on 2016-12-06.
 */

public class AppDefine {
    //public static final int APP_VERSION = 1;
    //public static final String APP_VERSION_NAME = "0.4.8.0.2";

    public static final int DEMO_PRODUCT_TYPE = 0;
    public static final int FREE_PRODUCT_TYPE = 1;
    public static final int PAID_PRODUCT_TYPE = 2;
    public static final int DEVELOP_PRODUCT_TYPE = 3;



    public static final boolean IS_SSL = true;
    public static final String HTTP_HOST = "https://jintalk.net:8080/entity";
    public static final String TALK_HOST = "wss://jintalk.net:8080/websocket";
    public static final String VIDEO_TALK_HOST = "wss://jintalk.net:7474/websocket";


    /*
    public static final boolean IS_SSL = true;
    public static final String HTTP_HOST = "https://192.168.1.77:8080/entity";
    public static final String TALK_HOST = "wss://192.168.1.77:8080/websocket";
    public static final String VIDEO_TALK_HOST= "wss://192.168.1.77:9090/websocket";
    */


    public static final JinDbHelper.Bookmark[] preBookMark = {
            new JinDbHelper.Bookmark(0,
                    "NAVER",
                    "https://m.naver.com/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null),
            new JinDbHelper.Bookmark(0,
                    "GOOGLE",
                    "https://www.google.com/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null),
            new JinDbHelper.Bookmark(0,
                    "TWITTER",
                    "https://mobile.twitter.com/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null),
            new JinDbHelper.Bookmark(0,
                    "FACEBOOK",
                    "https://m.facebook.com/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null),
            new JinDbHelper.Bookmark(0,
                    "NATE",
                    "http://m.nate.com/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null),
            new JinDbHelper.Bookmark(0,
                    "YOUTUBE",
                    "https://m.youtube.com/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null),
            new JinDbHelper.Bookmark(0,
                    "Daum",
                    "https://m.daum.net/",
                    JinDbHelper.Bookmark.BOOKMAKR_URL,
                    null,
                    null)

    };

    public static int VIDEO_CHAT_QUALITY = 25;
    public static int VIDEO_CHAT_AUDIO_VOLUME = 50;
    public static int VIDEO_CHAT_FRAME_RATE_PER_SECOND = 15;

    //public static int sampleRate = 8000;
    //public static int sampleRate = 16000;
    public static int sampleRate = 44100;
    public static int encoingBit = AudioFormat.ENCODING_PCM_16BIT;
    public static int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public static float audioVolume = 0.5f;


    public static int LOW_SETTING = 0;
    public static int MIDDLE_SETTING = 1;
    public static int HIGH_SETTING = 2;

    public static int CAMERA_RECORD_WIDTH=480;
    public static int CAMERA_RECORD_HEIGHT=360;
    public static int CAMERA_RECORD_QUALITY=50;


    public static int PROFILE_IMG_MAX_WIDTH = 800;
    public static int PROFILE_THUM_IMG_HEIGHT = 100;

    public static final int MESSAGE_MAX_SIZE = 1024*2;
    public static final int SECURITY_SIGN_MESSAGE_MAX_SIZE = 1024*10;
    public static final int ATTACHMENT_UPLOAD_MAX_SIZE = 1024*1024*5;
    public static final int ATTACHMENT_READ_BUFFER_SIZE = 1024*256;

    public static final int ON_DESTROY_STATE = 3;
    public static final int ON_RESUME_STATE = 2;
    public static final int ON_STOP_STATE = 1;
    public static final int ON_PAUSE_STATE = 0;

    public static final int PERMISSION_ALL = 0;
    public static final int PERMISSION_TALK = 1;
    public static final int PERMISSION_READ_CONTACTS = 2;
    public static final int PERMISSION_WAKE_LOCK = 3;
    public static final int PERMISSION_MUSIC = 4;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 5;
    public static final int PERMISSION_CAMERA = 6;
    public static final int PERMISSION_MODIFY_AUDIO_SETTINGS = 7;
    public static final int PERMISSION_RECORD_AUDIO = 8;
    public static final int PERMISSION_VIBRATE = 9;
    public static final int PERMISSION_CAPTURE_AUDIO_OUTPUT = 10;
    public static final int PERMISSION_CALL_PHONE = 11;
    public static final int PERMISSION_ACCESS_FINE_LOCATION = 12;
    public static final int PERMISSION_VIDEO_CHAT = 13;
    public static final int PERMISSION_ACCESS_WIFI_STATE = 14;



    public static final String CMD_KEY_NAME = "cmd";
    public static final String CHAT_BROADCAST_ACTION_NAME = "CHAT_BROADCAST_ACTION_NAME";
    public static final String MEDIA_BROADCAST_ACTION_NAME = "MEDIA_BROADCAST_ACTION_NAME";
    public static final String MAIN_BROADCAST_ACTION_NAME = "MAIN_BROADCAST_ACTION_NAME";
    public static final String POPUP_BROADCAST_ACTION_NAME = "POPUP_BROADCAST_ACTION_NAME";
    //public static final String MEDIA_RECENT_DIR_NAME = "MEDIA_RECENT_DIR_NAME";
    public static final String RECENT_VISIT_URL_NAME = "RECENT_VISIT_URL_NAME";
    public static final String DEFAULT_URL_NAME = "DEFAULT_URL_NAME";
    public static final String DEFAULT_URL = "http://www.google.com";

    public static final String DEFAULT_APP_DIR = "JinTalkBrowser";
    public static final String SCREEN_CAPTURE_DIR = DEFAULT_APP_DIR+"/"+"capture";
    public static final String WEBVIEW_DOWNLOAD_DIR = DEFAULT_APP_DIR+"/"+"download";
    public static final String TALK_DOWNLOAD_DIR = DEFAULT_APP_DIR+"/"+"talk";

    public static final String WEBVIEW_CACHE_DIR = DEFAULT_APP_DIR+"/"+"cache";
    public static final String WEBVIEW_SECURE_CACHE_DIR = DEFAULT_APP_DIR+"/"+"secure_cache";

    public static final long CMD_LOGIN = 0;
    public static final long CMD_REGISTER_CLIENT = 1;
    public static final long CMD_UNREGISTER_CLIENT = 2;
    public static final long CMD_TRANSFER_TEXT = 3;
    public static final long CMD_TRANSFER_BIN = 4;
    public static final long CMD_RECEIVE_CHECK = 5;
    public static final long CMD_MAKE_GROUP = 6;
    public static final long CMD_INVITE_TO_GROUP = 7;
    public static final long CMD_ENTER_GROUP = 8;
    public static final long CMD_EXIT_GROUP = 9;
    public static final long CMD_BLOCK_CLIENT = 10;
    public static final long CMD_UNBLOCK_CLIENT = 11;
    public static final long CMD_GET_DEVICE_INFO = 12;
    public static final long CMD_TRANSFER_TEXT_TO_GATEWAY = 13;
    public static final long CMD_DEVICE_LOGIN = 14;
    public static final long CMD_GET_GROUP = 15;
    public static final long CMD_GET_CLIENT_IN_GROUP = 16;
    public static final long CMD_GET_CLIENT_BY_FAVORITE = 17;
    public static final long CMD_DUPLICATED_LOGIN = 18;
    public static final long CMD_ACCESS_DENY = 19;
    public static final long CMD_JOIN = 20;

    public static final int LOTTOPOP_SERVICE = 0;
    public static final int CAHT_SERVICE = 1;

    public static final int ANDROID_PLATFORM = 0;
    public static final int IOS_PLATFORM = 1;

    public static final String LOTTOPOT_CONTROLLER_URI = "/lotto/command";
    public static final String LOTTOPOT_DB_KEY_NAME = "lotto";

    public static final String CHAT_CONTROLLER_URI = "/chat/command";
    public static final String CHAT_DB_KEY_NAME = "chat";

    public static final String HTTP_BODY_KEY_NAME = "content_body";
    public static final String MESSAGE_ID_KEY_NAME = "mid";

    public static final String GOOGLE_FCM_RESPONSE_KEY_NAME = "google_response";

    public static final String CALLBACK_RESULT_KEY_NAME = "callback_result";
    public static final int CALLBACK_RESULT_OK = 1;
    public static final int CALLBACK_RESULT_FAIL = 0;


    public static final String CHANNEL_ATTRIBUTE_MY_DEVICE_KEY_NAME = "my_device_id";

    public static final String JDBC_WORKER_NAME = "JDBCWorker";
    public static final String PUSH_WORKER_NAME = "PushWorker";
}