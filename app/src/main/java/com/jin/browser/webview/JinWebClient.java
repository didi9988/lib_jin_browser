package com.jin.browser.webview;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


import com.jin.browser.activity.JinActivity;
import com.jin.browser.config.AppDefine;
import com.jin.browser.db.JinDbHelper;
import com.jin.browser.activity.util.JinPreferenceUtil;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.jin.browser.R;

public class JinWebClient extends WebViewClient {

    private static final String TAG = "LightningWebClient";
    private ProgressBar mProgressBar;

    @NonNull
    private JinActivity jinActivity;
    private JinWebView mWebView;
    private JinDbHelper dbHelper;
    private Context context;

    JinWebClient(@NonNull Context context, @NonNull JinWebView webview, ProgressBar progress) {
        if(context != null && context instanceof JinActivity) {
            jinActivity = (JinActivity) context;
        }

        this.context = context;
        mProgressBar = progress;
        mWebView = webview;
        dbHelper = JinDbHelper.getInstance(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, @NonNull WebResourceRequest request) {

        String interceptUrl = request.getUrl().toString();
        if(JinPreferenceUtil.getBoolean(context, JinPreferenceUtil.SETTING_ACTIVATE_ADD_BLOCK, false)) {
            if (jinActivity!=null && jinActivity.isBlockUrl(interceptUrl)) {
                ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
                return new WebResourceResponse("text/plain", "utf-8", EMPTY);
            }
        }

        return super.shouldInterceptRequest(view, request);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        /*
        if (mAdBlock.isAd(url)) {
            ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
            return new WebResourceResponse("text/plain", "utf-8", EMPTY);
        }
        */

        //System.out.println("dkdkdkdkd");
        return null;
    }



    class BackgroundPageFinish extends AsyncTask<JinDbHelper.Bookmark, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(JinDbHelper.Bookmark... history) {
            try {
                if(jinActivity != null && jinActivity.isIncognito() == false) {
                    JinDbHelper dbhelper = JinDbHelper.getInstance(context);
                    dbhelper.insertBookmark(jinActivity, history[0]);

                    JinPreferenceUtil.putString(jinActivity, AppDefine.RECENT_VISIT_URL_NAME, history[0].url);
                }

                return null;
            }
            catch (Exception e){}
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            if(result != null) {
                mWebView.setCurrentCaptureImage(result);
            }
        }
    }


    public void processObjectTag(final WebView view, final String baseUrl){

        String getMovieUrlFromObjectTag = "(function(){\n" +
                "var objects = document.getElementsByTagName('object');\n" +
                "if(objects.length > 0){\n" +
                "var objectChild = objects[0].childNodes;\n" +
                "for(var i=0; i<objectChild.length; i++){\n" +
                "if(objectChild[i].nodeName.toLowerCase() == \"param\"){\n" +
                "if(objectChild[i].getAttribute(\"name\").toLowerCase() == \"movie\"){\n" +
                "return objectChild[i].getAttribute(\"value\");\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "return null;\n" +
                "})();";


        /*
        String getObjectTagScript = "(function(){\n" +
                "var objects = document.getElementsByTagName('object');\n" +
                "if(objects.length > 0){\n" +
                "return objects[0].outerHTML;\n" +
                "}\n" +
                "return null;\n" +
                "})();";
        */




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(getMovieUrlFromObjectTag, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    System.out.println("getMovieUrlFromObjectTag : " + value);
                    if (value != null && value.length() > 0 && !value.equals("null")) {

                        if(value.indexOf("youtube.com") == -1) return;

                        //value = " <object width=\"560\" height=\"315\"><param name=\"movie\" value=\"http://www.youtube.com/v/O0cAx1jLbJk?controls=1&amp;rel=1&amp;autohide=1&amp;version=3\"><param name=\"allowFullScreen\" value=\"true\"><param name=\"allowscriptaccess\" value=\"never\"><embed allowscriptaccess=\"never\" type=\"application/x-shockwave-flash\" src=\"http://www.youtube.com/v/O0cAx1jLbJk?controls=1&amp;rel=1&amp;autohide=1&amp;version=3\" allowfullscreen=\"true\" width=\"560\" height=\"315\"></object>";

                        value = value.replaceAll("[\"]", "");
                        value = value.substring(0, value.indexOf("?"));

                        value = value.replaceAll("/v/", "/embed/");

                        //value = "https://www.youtube.com/embed/O0cAx1jLbJk";
                        String iframeStr = "<iframe src='"+value+"' frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>";

                        String injectScript = "(function(){var objects = document.getElementsByTagName('object');\n" +
                                "if(objects.length > 0){\n" +
                                "var parent = objects[0].parentNode;\n" +
                                "var value = \"<iframe src='"+value+"' frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe>\";\n" +
                                "parent.insertAdjacentHTML( 'afterbegin', value );\n" +
                                "}})();";

                        System.out.println("injectScript : " + injectScript);

                       //view.loadDataWithBaseURL(baseUrl, iframeStr,
                        //        "text/html", "UTF-8", null);

                        //view.loadUrl(injectScript);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            view.evaluateJavascript(injectScript, null);
                        }
                    }
                }
            });
        }


    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onPageFinished(@NonNull WebView view, String url) {
        super.onPageFinished(view, url);

        String title = view.getTitle();

        if (title == null || title.isEmpty()) {
            title = "untitled";
        } else {
            try {
                title = new String(title.getBytes("UTF-8"));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        try {
            mWebView.setLoading(false);
            mWebView.setTitle(title);

            if(mProgressBar != null) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }



            String getViewPortScript = "(function(){\n" +
                    "           var metas = document.getElementsByTagName('meta');\n" +
                    "           for (var i=0; i<metas.length; i++) {\n" +
                    "               var meta_name = metas[i].getAttribute(\"name\");\n" +
                    "              if (meta_name != null && meta_name.toLowerCase() == \"viewport\") {\n" +
                    "                 return metas[i].getAttribute(\"content\");\n" +
                    "              }\n" +
                    "           }\n" +
                    "           return \"\";\n" +
                    "       })();";


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(getViewPortScript, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        System.out.println("getViewPortScript : " + value);
                        if (value != null) {
                            value = value.replaceAll(" ", "");
                            value = value.toLowerCase();
                            if(value.indexOf("user-scalable=no") > -1) {
                                mWebView.getSettings().setUseWideViewPort(false);
                            }
                        }
                    }
                });
            }

            if(jinActivity != null) {
                JinDbHelper.Bookmark history = new JinDbHelper.Bookmark(0, title, url, JinDbHelper.Bookmark.HISTORY_URL, null, null);

                this.dbHelper.insertBookmark(jinActivity, history);
                JinPreferenceUtil.putString(jinActivity, AppDefine.RECENT_VISIT_URL_NAME, history.url);

                mWebView.setCurrentCaptureImage(jinActivity.captureWebView(mWebView, 20, null));
                this.jinActivity.updateBookmark(mWebView, history);
                this.jinActivity.setActivityForCurrentPage(mWebView, title, url);
            }


            //processObjectTag(view, url);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setLoading(true);
        mWebView.setSslError(false);

        if(jinActivity != null) {
            jinActivity.setSearchEditText(url);
        }

        if(mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(final WebView view, @NonNull final HttpAuthHandler handler,
                                          final String host, final String realm) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
    }

    private volatile boolean mIsRunning = false;
    private float mZoomScale = 0.0f;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onScaleChanged(@NonNull final WebView view, final float oldScale, final float newScale) {

        /*
        if (view.isShown() && mLightningView.mPreferences.getTextReflowEnabled() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mIsRunning)
                return;
            float changeInPercent = Math.abs(100 - 100 / mZoomScale * newScale);
            if (changeInPercent > 2.5f && !mIsRunning) {
                mIsRunning = view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mZoomScale = newScale;
                        view.evaluateJavascript(Constants.JAVASCRIPT_TEXT_REFLOW, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                mIsRunning = false;
                            }
                        });
                    }
                }, 100);
            }

        }
        */
    }

    @NonNull
    private static List<Integer> getAllSslErrorMessageCodes(@NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = new ArrayList<>(1);

        /*
        if (error.hasError(SslError.SSL_DATE_INVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_date_invalid);
        }
        if (error.hasError(SslError.SSL_EXPIRED)) {
            errorCodeMessageCodes.add(R.string.message_certificate_expired);
        }
        if (error.hasError(SslError.SSL_IDMISMATCH)) {
            errorCodeMessageCodes.add(R.string.message_certificate_domain_mismatch);
        }
        if (error.hasError(SslError.SSL_NOTYETVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_not_yet_valid);
        }
        if (error.hasError(SslError.SSL_UNTRUSTED)) {
            errorCodeMessageCodes.add(R.string.message_certificate_untrusted);
        }
        if (error.hasError(SslError.SSL_INVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_invalid);
        }
        */

        return errorCodeMessageCodes;
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull final SslErrorHandler handler, @NonNull SslError error) {

        mWebView.setSslError(true);

        List<Integer> errorCodeMessageCodes = getAllSslErrorMessageCodes(error);

        StringBuilder stringBuilder = new StringBuilder();
        for (Integer messageCode : errorCodeMessageCodes) {
            stringBuilder.append(" - ").append(context.getString(messageCode)).append('\n');
        }
        //String alertMessage = stringBuilder.toString();

        String alertMessage = error.toString();
        alertMessage += "\n\nContinue moving to url?";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Security warning");
        builder.setMessage(alertMessage)
                .setCancelable(true)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.proceed();
                            }
                        })
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.cancel();
                            }
                        });
        Dialog dialog = builder.show();
    }

    @Override
    public void onFormResubmission(WebView view, @NonNull final Message dontResend, @NonNull final Message resend) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("ReSubmit");
        builder.setMessage("ReSubmit")
                .setCancelable(true)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                resend.sendToTarget();
                            }
                        })
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dontResend.sendToTarget();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
        //return shouldOverrideLoading(view, request.getUrl().toString()) || super.shouldOverrideUrlLoading(view, request);
        boolean result = shouldOverrideLoading(view, request.getUrl().toString());
        if(result == false){
            return super.shouldOverrideUrlLoading(view, request);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
        //return shouldOverrideLoading(view, url) || super.shouldOverrideUrlLoading(view, url);
        boolean result = shouldOverrideLoading(view, url);
        if(result == false){
            //return super.shouldOverrideUrlLoading(view, url);
            return super.shouldOverrideUrlLoading(view, url);
        }
        return result;
    }

    private boolean shouldOverrideLoading(@NonNull WebView view, @NonNull String url) {
        if( URLUtil.isNetworkUrl(url)){
            if(url.indexOf("play.google.com") > -1 && url.indexOf("details?id=") > -1) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);

                        return true;
                    }
                } catch (Exception e) {
                }
            }

            return false;
        }

        if (url.startsWith("tel:")) {

            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(tel);

            return true;
        }
        else if(url.startsWith("sms:")
                || url.startsWith(WebView.SCHEME_MAILTO)
                || url.startsWith(WebView.SCHEME_GEO)
                || url.startsWith("maps:")
                ){
            Intent action = new Intent(Intent.ACTION_VIEW);
            action.setData(Uri.parse(url));
            context.startActivity(action);

            return true;
        }
        else if (url.startsWith("intent:")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                PackageManager packageManager = context.getPackageManager();
                ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (info != null) {
                    context.startActivity(intent);
                } else {
                    /*
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                    marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                    context.startActivity(marketIntent);
                    */

                    confirmMoveToMarketUrl(intent);
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(url.startsWith("market://")){
            try{
                Intent intent =Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent != null) {
                    context.startActivity(intent);
                }
                return true;
            } catch (URISyntaxException e){
                e.printStackTrace();
            }
        }
        else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);

                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public void confirmMoveToMarketUrl(final Intent intent){
        final String market_url = "market://details?id=" + intent.getPackage();
        String message = context.getString(R.string.alert_move_to_url)+"\n"+"["+market_url+"]";

        android.app.AlertDialog.Builder alt_bld = new android.app.AlertDialog.Builder(context);
        alt_bld.setMessage(message).setCancelable(true)
                .setPositiveButton(context.getString(R.string.jin_confirm_txt),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                                marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                                context.startActivity(marketIntent);

                                dialog.cancel();
                            }})
                .setNegativeButton(context.getString(R.string.jin_no_txt),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

        android.app.AlertDialog alert_dialog = alt_bld.create();
        alert_dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alert_dialog.show();
    }

}
