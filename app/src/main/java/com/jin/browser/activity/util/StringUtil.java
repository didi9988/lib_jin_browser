package com.jin.browser.activity.util;

import android.content.Context;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * Created by kwy on 2018-01-18.
 */

public class StringUtil {

    public static String parseOnlyNum(String num_str){
        String reg = "[^0-9]";

        return num_str.replaceAll(reg, "");
    }

    private static final String[] Q = new String[]{"B", "K", "M", "G", "T", "P", "E"};
    public static String getSizeString(long bytes) {
        for (int i = 6; i > 0; i--)
        {
            double step = Math.pow(1024, i);
            if (bytes > step) return String.format("%3.1f %s", bytes / step, Q[i]);
        }
        return Long.toString(bytes)+" "+Q[0];
    }


    public static String getCountryPhoneNumber(Context context, String p_num){
        String phone_num = p_num;
        if(phone_num.startsWith("+") == false){
            if(phone_num.startsWith("0")){
                phone_num = phone_num.substring(1);
            }

            phone_num = JinPermissionUtil.getCountryCode(context)+phone_num;
        }

        return phone_num;
    }


    public static String getHttpURL(String content){
        String result = null;
        try {
            String msg = content.trim();
            int start_idx = msg.indexOf("http");
            int end_idx = msg.indexOf(" ", start_idx);
            if (end_idx == -1) {
                end_idx = start_idx + msg.length();
            }
            int enter_idx = msg.indexOf('\n', start_idx);
            if (enter_idx > start_idx && enter_idx < end_idx) {
                end_idx = enter_idx;
            }

            String url = msg.substring(start_idx);

            if (end_idx > start_idx) {
                url = msg.substring(start_idx, end_idx);
            }

            result = url;
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public static String getDateString(long date){
        String result = null;

        result = new SimpleDateFormat("yyyy.MM.dd E").format(date);

        return result;
    }

    public static String getTimeString(long date){
        String result = null;

        result = new SimpleDateFormat("yyyy.MM.dd E, HH:mm:ss ").format(date);

        return result;
    }

    public static String getDomain(String url){
        String result = null;

        String reg = "(?i)https?:/{2}";
        url = url.replaceAll(reg, "");

        if(url.indexOf("/") > 0) {
            result = url.substring(0, url.indexOf("/"));
        }

        return result;
    }






    public static LinkedList<String> getStreamingUrl(String url) {
        final BufferedReader br;
        String murl = null;
        LinkedList<String> murls = null;
        try {
            URLConnection mUrl = new URL(url).openConnection();
            br = new BufferedReader(
                    new InputStreamReader(mUrl.getInputStream()));
            murls = new LinkedList<String>();
            while (true) {
                try {
                    String line = br.readLine();

                    if (line == null) {
                        break;
                    }
                    murl = parseLine(line);
                    if (murl != null && !murl.equals("")) {
                        murls.add(murl);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Log.i(LOGTAG, "url to stream :" + murl);
        return murls;
    }


    public static LinkedList<String> getStreamingUrl(File file) {
        BufferedReader br = null;
        String murl = null;
        LinkedList<String> murls = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
            murls = new LinkedList<String>();
            while (true) {
                try {
                    String line = br.readLine();

                    if (line == null) {
                        break;
                    }
                    murl = parseLine(line);
                    if (murl != null && !murl.equals("")) {
                        murls.add(murl);
                        //break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null) br.close();
            }
            catch(Exception e){}
        }
        //Log.i(LOGTAG, "url to stream :" + murl);
        return murls;
    }

    public static String parseLine(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.indexOf("http") >= 0) {
            return trimmed.substring(trimmed.indexOf("http"));
        }
        return "";
    }







}
