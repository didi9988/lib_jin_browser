package com.jin.browser.activity.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kwy on 2018-01-15.
 */

public class JinPreferenceUtil {
    public static final String MY_DEVICE_ID = "MY_DEVICE_ID";
    public static final String ALLOW_READ_PHONE_STATE = "ALLOW_READ_PHONE_STATE";
    public static final String IS_REGISTER_CLIENT = "IS_REGISTER_CLIENT";
    public static final String IS_JOIN_CLIENT = "IS_JOIN_CLIENT";
    public static final String IS_CONFIRM_AUTHENTICATE = "IS_CONFIRM_AUTHENTICATE";
    public static final String MIGRATIONED_CONTACTS = "MIGRATIONED_CONTACTS";
    public static final String NOTI_ALARM_ACTIVATE = "NOTI_ALARM_ACTIVATE";
    public static final String DELETE_TALK_AFTER_READ = "DELETE_TALK_AFTER_READ";
    public static final String RSA_ENCRYPT_WITH_SERVER = "ENCRYPT_WITH_SERVER";
    public static final String PRESET_BOOKMARK = "PRESET_BOOKMARK";
    public static final String TUTORIAL_EXE = "TUTORIAL_EXE";
    public static final String IS_AGREEMENT_PERMISSION = "IS_AGREEMENT_PERMISSION";

    public static final String MY_NICKNAME = "MY_NICKNAME";

    public static final String BADGE_COUNT_NAME = "BADGE_COUNT_NAME";

    public static final String VIDEO_CHAT_FRONT_RESOLUTION = "VIDEO_CHAT_FRONT_RESOLUTION";
    public static final String VIDEO_CHAT_BACK_RESOLUTION = "VIDEO_CHAT_BACK_RESOLUTION";
    public static final String VIDEO_CHAT_AUDIO_BITRATE= "VIDEO_CHAT_AUDIO_BITRATE";
    public static final String VIDEO_CHAT_AUDIO_VOLUME= "VIDEO_CHAT_AUDIO_VOLUME";
    public static final String VIDEO_CHAT_FRAME_RATE= "VIDEO_CHAT_FRAME_RATE";
    public static final String VIDEO_CHAT_QUALITY= "VIDEO_CHAT_QUALITY";
    public static final String VIDEO_CHAT_ENCODE_VELOCITY= "VIDEO_CHAT_ENCODE_VELOCITY";
    public static final String VIDEO_CHAT_TRANSFER_VELOCITY= "VIDEO_CHAT_TRANSFER_VELOCITY";
    public static final String VIDEO_CHAT_PLAY_VELOCITY= "VIDEO_CHAT_PLAY_VELOCITY";


    public static final String GESTURE_ENABLE = "GESTURE_ENABLE";
    public static final String GESTURE_VISIBLE = "GESTURE_VISIBLE";
    public static final String GESTURE_BOOKMARK_ACTIVE = "GESTURE_BOOKMARK_ACTIVE";
    public static final String GESTURE_REMOVE_BOOKMARK_ACTIVE = "GESTURE_REMOVE_BOOKMARK_ACTIVE";
    public static final String GESTURE_CAPTURE_SCREEN_ACTIVE = "GESTURE_CAPTURE_SCREEN_ACTIVE";

    public static final String SETTING_FULL_SCREEN = "SETTING_FULL_SCREEN";
    public static final String SETTING_VISIBLE_BOTTOM_MENU_BAR = "SETTING_VISIBLE_BOTTOM_MENU_BAR";
    public static final String SETTING_ACTIVATE_ADD_BLOCK = "SETTING_ACTIVATE_ADD_BLOCK";
    public static final String SETTING_NO_USE_CACHE = "SETTING_NO_USE_CACHE";
    public static final String SETTING_WEBVIEW_FONT_SIZE = "SETTING_WEBVIEW_FONT_SIZE";
    public static final String SETTING_PROFILE_IMAGE = "SETTING_PROFILE_IMAGE";

    public static final String PLAY_ALL_FILE = "PLAY_ALL_FILE";

    public static final String DH_PUB_KEY = "DH_PUB_KEY";
    public static final String DH_PRI_KEY = "DH_PRI_KEY";

    public static final String RSA_PUB_KEY = "RSA_PUB_KEY";
    public static final String RSA_PRI_KEY = "RSA_PRI_KEY";


    public static final String PLAY_MUSIC_ALWAYS_FOREGROUND = "PLAY_MUSIC_ALWAYS_FOREGROUND";
    public static final String PLAY_MUSIC_DIR_PATH = "PLAY_MUSIC_DIR_PATH";
    public static final String PLAY_MUSIC_FILE_PATH = "PLAY_MUSIC_FILE_PATH";
    public static final String PLAY_MUSIC_ITEM_POSITION = "PLAY_MUSIC_ITEM_POSITION";
    public static final String PLAY_MUSIC_SEEK_POSITION = "PLAY_MUSIC_SEEK_POSITION";



    public static SharedPreferences getPref(Activity a){
        return a.getSharedPreferences("pref", MODE_PRIVATE);
    }

    public static SharedPreferences getPref(Context a){
        return a.getSharedPreferences("pref", MODE_PRIVATE);
    }

    public static void putBoolean(Activity a, String id, boolean v){
        SharedPreferences.Editor editor = getPref(a).edit();
        editor.putBoolean(id, v);
        editor.commit();
    }


    public static boolean getBoolean(Activity a, String id){
        return getBoolean(a, id, false);
    }

    public static boolean getBoolean(Activity a, String id, boolean d){
        boolean result = false;
        result = getPref(a).getBoolean(id, d);

        return result;
    }

    public static boolean getBoolean(Context a, String id, boolean d){
        boolean result = false;
        result = getPref(a).getBoolean(id, d);

        return result;
    }


    public static void putInt(Activity a, String id, int v){
        SharedPreferences.Editor editor = getPref(a).edit();
        editor.putInt(id, v);
        editor.commit();
    }

    public static int getInt(Activity a, String id, int v){
        return getPref(a).getInt(id, v);
    }

    public static void putInt(Context a, String id, int v){
        SharedPreferences.Editor editor = getPref(a).edit();
        editor.putInt(id, v);
        editor.commit();
    }

    public static long getLong(Context a, String id, int v){
        return getPref(a).getLong(id, v);
    }

    public static void putLong(Context a, String id, long v){
        SharedPreferences.Editor editor = getPref(a).edit();
        editor.putLong(id, v);
        editor.commit();
    }




    public static int getInt(Context a, String id, int v){
        return getPref(a).getInt(id, v);
    }

    public static void putString(Activity a, String id, String v){
        SharedPreferences.Editor editor = getPref(a).edit();
        editor.putString(id, v);
        editor.commit();
    }

    public static void putString(Context a, String id, String v){
        SharedPreferences.Editor editor = getPref(a).edit();
        editor.putString(id, v);
        editor.commit();
    }



    public static String getString(Activity a, String id){
        return getString( a, id, null);
    }

    public static String getString(Context a, String id){
        return getString( a, id, null);
    }

    public static String getString(Activity a, String id, String v){
        String result = null;
        result = getPref(a).getString(id, v);

        return result;
    }

    public static String getString(Context a, String id, String v){
        String result = null;
        result = getPref(a).getString(id, v);

        return result;
    }


    public static boolean getRsaEncryptWithServer(Context a){
        return JinPreferenceUtil.getBoolean(a, JinPreferenceUtil.RSA_ENCRYPT_WITH_SERVER, false);
    }


    public static boolean isAgreementPermission(Context a){
        return JinPreferenceUtil.getBoolean(a, JinPreferenceUtil.IS_AGREEMENT_PERMISSION, false);
    }

    public static boolean getPlayAllFile(Context a){
        return JinPreferenceUtil.getBoolean(a, JinPreferenceUtil.PLAY_ALL_FILE, true);
    }

    public static boolean getPreferenceFullScreen(Context a){
        return JinPreferenceUtil.getBoolean(a, JinPreferenceUtil.SETTING_FULL_SCREEN, true);
    }

    public static boolean getPreferenceConfirmAuthenticate(Context a){
        return JinPreferenceUtil.getBoolean(a, JinPreferenceUtil.IS_CONFIRM_AUTHENTICATE, false);
    }

}
