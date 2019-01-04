# lib_jin_browser

This library is based on JinTalk Browser

https://play.google.com/store/apps/details?id=com.won.android.app.jin

Step 1. Add the JitPack repository to your build file 

Add it in your root build.gradle at the end of repositories:

allprojects {

	repositories {
		...
		maven { url 'https://jitpack.io' }
	}

}



Step 2. Add the dependency

dependencies {

	implementation 'com.github.didi9988:lib_jin_browser:Tag'

}


Step 3. Make MainActivity

package com.your.package;

import android.content.Context;

import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import com.jin.browser.activity.JinActivity;

import com.jin.browser.activity.util.JinPreferenceUtil;

import com.jin.browser.webview.JinWebView;


public class MainActivity extends JinActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionView(true, true);

        JinPreferenceUtil.putBoolean(this, "SETTING_FULL_SCREEN", true);

        /*
        JinPreferenceUtil.putBoolean(this, "SETTING_FULL_SCREEN", false);
        super.lockDrawerLayout();
        */

        newWebView("http://www.google.com");
    }

    protected void onResume(){
        super.onResume();

        setActionView(true, true);
        processIntentMsg();
    }

    public boolean newWebView(String url){
        super.newWebView(url);

        return true;
    }

    public void setActivityForCurrentPage(JinWebView webview, String title, String url){

        super.setActivityForCurrentPage(webview, title, url);
    }

    public void processIntentMsg(){
        Intent intent = getIntent();
        if(intent == null) return;

        try {
            String url = null;
            url = intent.getDataString();
            if (url != null && url.toLowerCase().indexOf("http") > -1) {
                loadUrl(url);
                intent.setData(null);
                return;
            }

            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey("url")) {
                    url = extras.getString("url");
                    loadUrl(url);
                    extras.remove("url");
                }
                extras.clear();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            intent.replaceExtras(new Bundle());
            intent.setAction("");
            intent.setData(null);
            intent.setFlags(0);
        }

    }

}

