package com.jin.browser.activity.util;

/**
 * Created by KWY on 2016-12-01.
 */

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class DeviceUuidFactory {


    protected static final String PREFS_DEVICE_ID = "device_id";
    protected volatile static UUID uuid;

    public DeviceUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final String androidId = Secure.getString(
                            context.getContentResolver(), Secure.ANDROID_ID);
                    // Use the Android ID unless it's broken, in which case
                    // fallback on deviceId,
                    // unless it's not available, then fallback on a random
                    // number which we store to a prefs file
                    try {
                        if (!"9774d56d682e549c".equals(androidId)) {
                            uuid = UUID.nameUUIDFromBytes(androidId
                                    .getBytes("utf8"));
                        } else {
                            final String deviceId = (
                                    (TelephonyManager) context
                                            .getSystemService(Context.TELEPHONY_SERVICE))
                                    .getDeviceId();
                            uuid = deviceId != null ? UUID
                                    .nameUUIDFromBytes(deviceId
                                            .getBytes("utf8")) : UUID
                                    .randomUUID();
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private String getHash(String stringToHash) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] result = null;

        try {
            result = digest.digest(stringToHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        for (byte b : result)
        {
            sb.append(String.format("%02X", b));
        }

        String messageDigest = sb.toString();
        return messageDigest;
    }


    public String getDeviceUuid() {
        return getHash(uuid.toString());
    }
}