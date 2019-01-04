package com.jin.browser.activity.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;


import com.jin.browser.config.AppDefine;

import java.util.HashMap;

/**
 * Created by kwy on 2018-01-23.
 */

public class JinPermissionUtil {


    public static HashMap<Integer,  PermissionCallback> requestPermissionList  = new HashMap<Integer,  PermissionCallback>();


    public static HashMap<String, String> countryCodeMap = new HashMap<String, String>();
    static {
        countryCodeMap.put("KR","+82");
        countryCodeMap.put("RU","+7");
        countryCodeMap.put("RW","+250");
        countryCodeMap.put("BL","+590");
        countryCodeMap.put("WS","+685");
        countryCodeMap.put("SM","+378");
        countryCodeMap.put("ST","+239");
        countryCodeMap.put("SA","+966");
        countryCodeMap.put("SN","+221");
        countryCodeMap.put("RS","+381");
        countryCodeMap.put("SC","+248");
        countryCodeMap.put("SL","+232");
        countryCodeMap.put("SG","+65");
        countryCodeMap.put("SK","+421");
        countryCodeMap.put("SI","+386");
        countryCodeMap.put("SB","+677");
        countryCodeMap.put("SO","+252");
        countryCodeMap.put("ZA","+27");
        countryCodeMap.put("ES","+32");
        countryCodeMap.put("LK","+94");
        countryCodeMap.put("SH","+290");
        countryCodeMap.put("PM","+508");
        countryCodeMap.put("SD","+249");
        countryCodeMap.put("SR","+597");
        countryCodeMap.put("SZ","+268");
        countryCodeMap.put("SE","+46");
        countryCodeMap.put("CH","+41");
        countryCodeMap.put("SY","+963");
        countryCodeMap.put("TW","+886");
        countryCodeMap.put("TJ", "+992");
        countryCodeMap.put("TZ", "+255");
        countryCodeMap.put("TH", "+66");
        countryCodeMap.put("TG", "+228");
        countryCodeMap.put("TK", "+690");
        countryCodeMap.put("TO", "+676");
        countryCodeMap.put("TN", "+216");
        countryCodeMap.put("TR", "+90");
        countryCodeMap.put("TM", "+993");
        countryCodeMap.put("TV", "+688");
        countryCodeMap.put("AE", "+971");
        countryCodeMap.put("UG", "+256");
        countryCodeMap.put("GB", "+44");
        countryCodeMap.put("UA", "+380");
        countryCodeMap.put("UY", "+598");
        countryCodeMap.put("US", "+1");
        countryCodeMap.put("UZ", "+998");
        countryCodeMap.put("VU", "+678");
        countryCodeMap.put("VA", "+39");
        countryCodeMap.put("VE", "+58");
        countryCodeMap.put("VN", "+84");
        countryCodeMap.put("WF", "+681");
        countryCodeMap.put("YE", "+967");
        countryCodeMap.put("ZM", "+260");
        countryCodeMap.put("ZW", "+263");
        countryCodeMap.put( "MY","+60");
        countryCodeMap.put("PK","+92");

        System.out.println("Init Country code !");
    }

    public static abstract class PermissionCallback{

        public abstract void callback(String[] permissions, int[] grantResults);
    };

    public static void addPermissionCallback(int request, PermissionCallback callback){
        requestPermissionList.put(request, callback);
    }

    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase();
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase();
                }
            }
        }
        catch (Exception e) { }
        return "KR";
    }

    private static String getCountryCodeFromMap(String countryCode) {

        return countryCodeMap.get(countryCode);
    }

    public static int getCountryCodeIdx(String countryCode){
        int result = 0;
        Object[] array = countryCodeMap.values().toArray();

        for(int i=0; i<array.length; i++){
            if(countryCode.equals((String)array[i])){
                return i;
            }
        }

        return result;
    }


    public static String getDevicePhoneNum(Context a) {
        String u_pn = null;

        TelephonyManager telManager = (TelephonyManager) a.getSystemService(a.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= 23){
            if (a.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                u_pn = telManager.getLine1Number();
            }
            else{
                try{
                    Toast.makeText(a, "Exist user same phone number", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        else{
            u_pn = telManager.getLine1Number();
        }

        if (u_pn == null) u_pn = "00000000000";

        u_pn = StringUtil.parseOnlyNum(u_pn);

        //u_pn = u_pn.replaceAll("[+]82", "0");


        String country = getUserCountry(a.getApplicationContext());
        String countryCode = StringUtil.parseOnlyNum(getCountryCodeFromMap(country));
        if(countryCode != null) {
            if (u_pn.startsWith(countryCode) == false){
                if(u_pn.startsWith("0")){
                    u_pn = u_pn.substring(1);
                }

                u_pn = "+"+countryCode+u_pn;
            }
            else{
                u_pn = "+"+u_pn;
            }
        }

        return u_pn;
    }


    public static String getCountryCode(Context context){
        String country = getUserCountry(context);
        return getCountryCodeFromMap(country);
    }

    public static String getDeviceId(Context a){
        String d_id = null;

        if(JinPreferenceUtil.getString(a, JinPreferenceUtil.MY_DEVICE_ID, null) == null) {
            DeviceUuidFactory df = new DeviceUuidFactory(a);
            d_id = df.getDeviceUuid();

            JinPreferenceUtil.putString(a, JinPreferenceUtil.MY_DEVICE_ID, d_id);
        }
        else{
            d_id = JinPreferenceUtil.getString(a, JinPreferenceUtil.MY_DEVICE_ID, null);
        }

        return d_id;
    }


    public static void requestPermissionCallback(Integer request_id, String[] permissions, int[] grantResults){
        if(requestPermissionList.containsKey(request_id)){
            PermissionCallback listener = requestPermissionList.get(request_id);
            listener.callback(permissions, grantResults);

            requestPermissionList.remove(request_id);
        }
    }


    private static boolean checkPermissionAndRequest(Context context, int request_id, String[] permissions, PermissionCallback callback){
        boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {

            for(int i=0; i<permissions.length; i++){
                if(context.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED){
                    result = false;
                    break;
                }
            }

            if(result == false){
                ActivityCompat.requestPermissions((Activity)context, permissions, request_id);
            }

            addPermissionCallback(request_id, callback);
        }

        return result;
    }




    public static abstract class CallBackListener{
        public abstract void execute(boolean allow_permission) throws Exception;
    }

    public static boolean checkPermission(Context context, int permission, final CallBackListener callback) {
        String[] req_talk_permission = {
                Manifest.permission.READ_PHONE_STATE
                ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,Manifest.permission.CAMERA
                //,Manifest.permission.MODIFY_AUDIO_SETTINGS
                ,Manifest.permission.RECORD_AUDIO
                //,Manifest.permission.CALL_PHONE
                //,Manifest.permission.VIBRATE
                //,Manifest.permission.READ_CONTACTS
        };

        String[] req_write_external_storage_permission = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        String[] req_location_permission = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        String[] req_read_contacts_permission = {
                Manifest.permission.READ_CONTACTS
        };

        String[] reqPermission = null;

        if(permission == AppDefine.PERMISSION_TALK){
            reqPermission = req_talk_permission;
        }
        else if(permission == AppDefine.PERMISSION_MUSIC){
            reqPermission = req_write_external_storage_permission;
        }
        else if(permission == AppDefine.PERMISSION_WRITE_EXTERNAL_STORAGE){
            reqPermission = req_write_external_storage_permission;
        }
        else if(permission == AppDefine.PERMISSION_ACCESS_FINE_LOCATION){
            reqPermission = req_location_permission;
        }
        else if(permission == AppDefine.PERMISSION_READ_CONTACTS){
            reqPermission = req_read_contacts_permission;
        }

        if(reqPermission == null) return false;


        if (Build.VERSION.SDK_INT >= 23) {
            boolean res = JinPermissionUtil.checkPermissionAndRequest(context,
                    AppDefine.PERMISSION_TALK,
                    reqPermission,
                    new PermissionCallback() {
                        public void callback(String[] permissions, int[] grantResults) {

                            boolean allow_permission = true;
                            for (int i = 0; i < grantResults.length; i++) {
                                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                                    allow_permission = false;
                                    break;
                                }
                            }

                            try {
                                callback.execute(allow_permission);
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

            return res;
        }

        //Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "External Storage Permission is Grant ");
        return true;
    }
}
