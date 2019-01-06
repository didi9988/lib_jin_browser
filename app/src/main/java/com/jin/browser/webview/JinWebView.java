package com.jin.browser.webview;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jin.browser.R;
import com.jin.browser.activity.JinActivity;
import com.jin.browser.activity.JinDialogFragment;
import com.jin.browser.config.AppDefine;
import com.jin.browser.activity.util.JinPermissionUtil;
import com.jin.browser.activity.util.JinPreferenceUtil;


import java.io.File;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * Created by kwy on 2018-01-24.
 */

public class JinWebView extends WebView {

    private String mUserAgent;
    private int sMaxFling;
    //private WebViewHandler mWebViewHandler;
    private GestureDetector mGestureDetector;
    private String mTitle;
    private JinActivity jinActivity;
    private Context context;
    private ProgressBar mProgressBar;
    private long mWebViewId;
    //private Bitmap mCurrentCaptureImage;
    boolean isLoading = false;

    private boolean sslError;

    private int mAction;
    private float touchDownPointX;
    private float touchDownPointY;
    private boolean isScrollDown;
    private boolean isActiveSwipeRefesh=false;

    private boolean mPrivacy = false;

    public boolean isReachTopScroll= false;

    CapturedWebview capturedWebView;

    public class CapturedWebview{
        public String url;
        public Bitmap img;

        CapturedWebview(String url, Bitmap img){
            this.url = url;
            this.img = img;
        }

    }

    public JinWebView(Context context, ProgressBar progress, boolean privacy) {
        super(context);

        if(context instanceof JinActivity) {
            jinActivity = (JinActivity) context;
        }
        this.context = context;
        //mWebViewHandler = new WebViewHandler(this);
        mGestureDetector= new GestureDetector(context, new CustomGestureListener());
        sMaxFling = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mProgressBar = progress;
        mWebViewId = System.nanoTime();
        mPrivacy = privacy;
        initWebView();
        initWebViewSetting();
        initCookiePreference();
        //mMainActivity.registerForContextMenu(this);
    }

    public void initCookiePreference(){
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }
        else{
            if(cookieManager != null) {
                cookieManager.setAcceptThirdPartyCookies(this, true);
            }
        }

        if(cookieManager != null) {
            cookieManager.setAcceptCookie(true);
        }
    }

    public void goBack(){
        stopLoading();
        super.goBack();
    }

    public void destroy(){
        try{
            stopLoading();
            ViewParent parent = this.getParent();
            if(parent instanceof ViewGroup){
                ((ViewGroup) parent).removeView(this);
            }

            if(mPrivacy){
                clearCache(true);
                clearHistory();
            }

            if(mProgressBar != null) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            removeAllViews();

            jinActivity.removeWebView(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        super.destroy();
    }

    public JinActivity getActivity(){
        return this.jinActivity;
    }


    public boolean isSslError(){
        return sslError;
    }

    public void setSslError(boolean b){
        this.sslError = b;
    }

    public void stopLoading(){
        super.stopLoading();
        setLoading(false);
    }

    public void setLoading(boolean b){
        this.isLoading = b;

        if(jinActivity != null) {
            jinActivity.setReloadImageView(b);
        }
    }

    public boolean isLoading(){
        return this.isLoading;
    }


    public void initWebView(){

        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    jinActivity.setActiveSwipe(isActiveSwipeRefesh);
                }
            }
        });

        this.setDrawingCacheBackgroundColor(Color.WHITE);
        this.setFocusableInTouchMode(true);
        this.setFocusable(true);
        this.setDrawingCacheEnabled(false);
        this.setWillNotCacheDrawing(true);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            this.setAnimationCacheEnabled(false);
            //noinspection deprecation
            this.setAlwaysDrawnWithCacheEnabled(false);
        }
        this.setBackgroundColor(Color.WHITE);

        this.setScrollbarFadingEnabled(true);
        this.setSaveEnabled(true);
        this.setNetworkAvailable(true);
        this.setWebChromeClient(new JinWebChromeClient(context, this, mProgressBar));
        this.setWebViewClient(new JinWebClient(context, this, mProgressBar));
        this.setDownloadListener(new JinDownloadListener());
        mGestureDetector = new GestureDetector(context, new CustomGestureListener());
        this.setOnTouchListener(new TouchListener());
        mUserAgent = this.getSettings().getUserAgentString();


        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(jinActivity == null) return true;

                Message msg = v.getHandler().obtainMessage();
                requestFocusNodeHref(msg);

                final String href_url = msg.getData().getString("url");

                final HitTestResult webViewHitTestResult = getHitTestResult();

                int type = webViewHitTestResult.getType();
                if (type == HitTestResult.SRC_ANCHOR_TYPE ||
                        webViewHitTestResult.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

                    final String url = webViewHitTestResult.getExtra();

                    if(URLUtil.isValidUrl(url)) {
                        FragmentManager fm = jinActivity.getSupportFragmentManager();
                        final JinDialogFragment dialogFragment = new JinDialogFragment();
                        dialogFragment.setTitle((href_url!=null)?href_url:url);
                        dialogFragment.setPosition((int) touchDownPointX, (int) touchDownPointY);

                        dialogFragment.addContextListItem(
                                new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_load_url)) {
                                    public void click() {
                                        jinActivity.loadUrl((href_url!=null)?href_url:url);
                                    }
                                });

                        dialogFragment.addContextListItem(
                                new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_load_new_tab)) {
                                    public void click() {
                                        jinActivity.newWebView((href_url!=null)?href_url:url);
                                    }
                                });
                        dialogFragment.addContextListItem(
                                new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_copy_url)) {
                                    public void click() {
                                        final ClipboardManager clipboardManager = (ClipboardManager) jinActivity.getSystemService(CLIPBOARD_SERVICE);
                                        ClipData clipData = ClipData.newPlainText("label", (href_url!=null)?href_url:url);
                                        clipboardManager.setPrimaryClip(clipData);
                                    }
                                });
                        dialogFragment.addContextListItem(
                                new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_share)) {
                                    public void click() {
                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_TEXT, (href_url!=null)?href_url:url);
                                        jinActivity.startActivity(Intent.createChooser(intent, jinActivity.getString(R.string.webview_share)));
                                        //mMainActivity.startActivityForResult(Intent.createChooser(intent, mMainActivity.getString(R.string.webview_share)), ON_ACTIVITY_RESULT_REQUEST_AUDIO_FOCUS);
                                    }
                                });

                        if(type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE){
                            dialogFragment.addContextListItem(
                                    new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_download_image)) {
                                        public void click() {
                                            /*
                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                            request.allowScanningByMediaScanner();
                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            DownloadManager downloadManager = (DownloadManager) mMainActivity.getSystemService(DOWNLOAD_SERVICE);
                                            downloadManager.enqueue(request);

                                            Toast.makeText(mMainActivity,"Image Downloaded Successfully.",Toast.LENGTH_LONG).show();
                                            */

                                            checkPermissionAndDownloadFile(JinWebView.this, url, null, null, null, null);
                                        }
                                    });
                        }
                        dialogFragment.setShadowVisible(false);
                        dialogFragment.show(fm, "fragment_dialog_test");

                        return true;
                    }
                }
                else if (type == HitTestResult.IMAGE_TYPE) {

                    final String url = webViewHitTestResult.getExtra();

                    FragmentManager fm = jinActivity.getSupportFragmentManager();
                    final JinDialogFragment dialogFragment = new JinDialogFragment();
                    dialogFragment.setTitle((href_url!=null)?href_url:url);
                    dialogFragment.setPosition((int)touchDownPointX, (int)touchDownPointY);


                    dialogFragment.setContextListItem(
                            new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_download_image)){
                                public void click(){

                                    if(URLUtil.isValidUrl(url)){
                                        /*
                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                                        request.allowScanningByMediaScanner();
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        DownloadManager downloadManager = (DownloadManager) mMainActivity.getSystemService(DOWNLOAD_SERVICE);
                                        downloadManager.enqueue(request);

                                        Toast.makeText(mMainActivity,"Image Downloaded Successfully.",Toast.LENGTH_LONG).show();
                                        */

                                        /*
                                        String file_name = null;

                                        int idx = url.lastIndexOf('/');
                                        if(idx > 0) {
                                            file_name = url.substring(idx);
                                        }
                                        */

                                        checkPermissionAndDownloadFile(JinWebView.this, url, null, null, null, null);
                                    }
                                }
                            }
                            ,new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_load_new_tab)){
                                public void click(){

                                    if(URLUtil.isValidUrl(url)){
                                        jinActivity.newWebView(url);
                                    }

                                }
                            }
                            ,new JinDialogFragment.ListItem(jinActivity.getString(R.string.webview_share)) {
                                        public void click() {
                                            Intent intent = new Intent(Intent.ACTION_SEND);
                                            //intent.setType("image/jpg");
                                            //intent.putExtra(Intent.EXTRA_STREAM, url);
                                            //intent.setType("text/plain");
                                            intent.setType("image/*");
                                            intent.putExtra(Intent.EXTRA_TEXT, (href_url!=null)?href_url:url);
                                            jinActivity.startActivity(Intent.createChooser(intent, jinActivity.getString(R.string.webview_share)));
                                            //mMainActivity.startActivityForResult(Intent.createChooser(intent, mMainActivity.getString(R.string.webview_share)), ON_ACTIVITY_RESULT_REQUEST_AUDIO_FOCUS);
                                        }
                                    }
                    );
                    dialogFragment.setShadowVisible(false);
                    dialogFragment.show(fm, "fragment_dialog_test");

                    return true;
                }



                return false;
            }
        });
    }

    public boolean getPrivacy(){
        return this.mPrivacy;
    }

    public void checkPermissionAndDownloadFile(final WebView webview, final String url, final String mimeType, final String userAgent, final String contentDisposition, final String file_name){
        boolean res = JinPermissionUtil.checkPermission(context, AppDefine.PERMISSION_WRITE_EXTERNAL_STORAGE, new JinPermissionUtil.CallBackListener() {
            @Override
            public void execute(boolean allow_permission) throws Exception {
                if(allow_permission){
                    downloadFile(webview, url, mimeType, userAgent, contentDisposition, file_name);
                }
            }
        });

        if(res) downloadFile(webview, url, mimeType, userAgent, contentDisposition, file_name);
    }


    public void downloadFile(WebView webview, String url, String mimeType, String userAgent, String contentDisposition, String file_name){
        try {
            if (file_name == null) {
                file_name = URLUtil.guessFileName(url, contentDisposition, mimeType);

                if(file_name.endsWith(".bin")){
                    int idx = url.lastIndexOf('/');
                    if(idx > 0) {
                        file_name = url.substring(idx+1);
                    }
                }
            }

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(url));

            if (mimeType != null) {
                request.setMimeType(mimeType);
            }
            String cookies = CookieManager.getInstance().getCookie(url);

            request.addRequestHeader("cookie", cookies);

            if (userAgent != null) {
                request.addRequestHeader("User-Agent", userAgent);
            }

            request.setDescription("Downloading file...");

            /*
            String file_name = null;

            if (mimeType != null && contentDisposition != null) {
                String[] strArray = contentDisposition.split(";");

                for (int i = 0; i < strArray.length; i++) {
                    if (strArray[i].toLowerCase().trim().startsWith("filename")) {
                        String[] nameArray = strArray[i].split("=");

                        file_name = nameArray[1];
                        file_name = file_name.replaceAll("[\"]", "");
                        break;
                    }
                }
            }

            if (file_name == null) {
                file_name = URLUtil.guessFileName(url, contentDisposition, mimeType);
            }
            */



            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


            File folder = new File(Environment.getExternalStorageDirectory().toString() + "/" + AppDefine.WEBVIEW_DOWNLOAD_DIR);
            if (!folder.exists()) {
                boolean res = folder.mkdirs();

                if (res == false) {
                    folder = new File(context.getFilesDir().getAbsolutePath() + "/" + AppDefine.WEBVIEW_DOWNLOAD_DIR);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                }
            }

            String ff_path = null;
            if (folder.exists()) {
                ff_path = folder.getAbsolutePath() + "/" + file_name;

                File downloadFile = new File(ff_path);

                if(downloadFile.exists()){
                    file_name = System.currentTimeMillis()+"_"+file_name;
                }

                request.setTitle(file_name);
                request.setDestinationInExternalPublicDir(AppDefine.WEBVIEW_DOWNLOAD_DIR, file_name);

                DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

                Toast.makeText(context.getApplicationContext(), "Downloading File : " + ff_path,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context.getApplicationContext(), "Download Fail : " + ff_path,
                        Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    public long getmWebViewId(){
        return this.mWebViewId;
    }


    public void initWebViewSetting(){
        WebSettings settings = this.getSettings();

        //String appCachePath = context.getApplicationContext().getCacheDir().getAbsolutePath();

        String appCachePath = getCachePath(mPrivacy);

        if(appCachePath != null) {
            settings.setAppCachePath(appCachePath);
        }

        if (mPrivacy == false) {
            settings.setGeolocationEnabled(true);
        } else {
            settings.setGeolocationEnabled(false);
        }
        if (Build.VERSION.SDK_INT < KITKAT) {
            switch (1) {
                case 0:
                    //noinspection deprecation
                    settings.setPluginState(WebSettings.PluginState.OFF);
                    break;
                case 1:
                    //noinspection deprecation
                    settings.setPluginState(WebSettings.PluginState.ON_DEMAND);
                    break;
                case 2:
                    //noinspection deprecation
                    settings.setPluginState(WebSettings.PluginState.ON);
                    break;
                default:
                    break;
            }
        }


        int font_size = JinPreferenceUtil.getInt(getContext(), JinPreferenceUtil.SETTING_WEBVIEW_FONT_SIZE, 100);
        settings.setTextZoom(font_size);

        //setUserAgent(context, mPreferences.getUserAgentChoice());

        if (mPrivacy == false) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //noinspection deprecation
                settings.setSavePassword(true);
            }
            settings.setSaveFormData(true);
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //noinspection deprecation
                settings.setSavePassword(false);
            }
            settings.setSaveFormData(false);
        }

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        /*
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else{
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        */

        if (true) {
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
        } else {
            settings.setJavaScriptEnabled(false);
            settings.setJavaScriptCanOpenWindowsAutomatically(false);
        }

        settings.setBlockNetworkImage(false);


        if(mPrivacy == false) {
            settings.setSupportMultipleWindows(true);
        } else {
            settings.setSupportMultipleWindows(false);
        }

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this,
                    true);
        }

        if (Build.VERSION.SDK_INT  < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            settings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        if (Build.VERSION.SDK_INT  < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection deprecation
            settings.setEnableSmoothTransition(true);
        }
        if (Build.VERSION.SDK_INT  > Build.VERSION_CODES.JELLY_BEAN) {
            settings.setMediaPlaybackRequiresUserGesture(true);
        }

        if (mPrivacy==false && Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE); //MIXED_CONTENT_ALWAYS_ALLOW
        } else if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
            // We're in Incognito mode, reject
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }


        settings.setDefaultTextEncodingName("UTF-8");

        if (mPrivacy == false) {
            settings.setDomStorageEnabled(true);

            if(JinPreferenceUtil.getBoolean(context, JinPreferenceUtil.SETTING_NO_USE_CACHE, false)){
                settings.setAppCacheEnabled(false);
                settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            }
            else{
                settings.setAppCacheEnabled(true);
                settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                //settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }

            settings.setDatabaseEnabled(true);
        } else {
            //Edit by miku77
            //settings.setDomStorageEnabled(false);
            settings.setDomStorageEnabled(false);

            settings.setAppCacheEnabled(false);
            settings.setDatabaseEnabled(false);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }
    }


    public String getCachePath(boolean secure){
        String result = null;

        String dir_name = AppDefine.WEBVIEW_CACHE_DIR;
        if(secure){
            dir_name = AppDefine.WEBVIEW_SECURE_CACHE_DIR;
        }

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/" + dir_name);
        if (!folder.exists()) {
            boolean res = folder.mkdirs();

            if (res == false) {
                folder = new File(context.getFilesDir().getAbsolutePath() + "/" + dir_name);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
            }
        }

        if(folder.exists()){
            result = folder.getAbsolutePath();
        }

        return result;
    }

    public void setTitle( String title) {
        if (title == null) {
            mTitle = "";
        } else {
            mTitle = title;
        }
    }

    public void textSmaller(TextView view) {
        WebSettings settings = this.getSettings();
        settings.setTextZoom(settings.getTextZoom() - 10);

        JinPreferenceUtil.putInt(getContext(), JinPreferenceUtil.SETTING_WEBVIEW_FONT_SIZE, settings.getTextZoom());

        view.setText(""+settings.getTextZoom());
    }

    public void textBigger(TextView view) {

        WebSettings settings = this.getSettings();
        settings.setTextZoom(settings.getTextZoom() + 10);

        JinPreferenceUtil.putInt(getContext(), JinPreferenceUtil.SETTING_WEBVIEW_FONT_SIZE, settings.getTextZoom());

        view.setText(""+settings.getTextZoom());
    }

    public void setCurrentCaptureImage(Bitmap img){
        this.capturedWebView = new CapturedWebview(getUrl(), img);
    }

    public CapturedWebview getCurrentCaptureImage(){
        return this.capturedWebView;
    }

    private class TouchListener implements OnTouchListener {

        float mPointY;


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(@Nullable View view, @NonNull MotionEvent arg1) {
            if(jinActivity == null ){
                return false;
            }

            if (view == null)
                return false;

            mAction = arg1.getAction();
            mPointY = arg1.getY();

            if (mAction == MotionEvent.ACTION_DOWN) {
                touchDownPointY = arg1.getY();
            }
            else if(mAction == MotionEvent.ACTION_UP){
                if((touchDownPointY-mPointY) < 0){
                    isScrollDown = true;

                    if(isReachTopScroll) {
                        jinActivity.setActionView(false, false);
                    }
                }
                else{
                    isScrollDown = false;
                    isActiveSwipeRefesh = false;
                    jinActivity.setActiveSwipe(false);
                }
            }

            mGestureDetector.onTouchEvent(arg1);
            return false;
        }
    }

    @Override
    protected void onScrollChanged(int newLeft, int newTop, int oldLeft, int oldTop) {
        if(getTop()==newTop){
            //if(newTop == 0){
            // reaches the top end
            this.isReachTopScroll = true;
        }
        else{
            this.isReachTopScroll = false;
        }
        super.onScrollChanged(newLeft, newTop, oldLeft, oldTop);
    }


    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if(clampedY){
            if(isScrollDown && scrollY==0){
                isActiveSwipeRefesh = true;
                jinActivity.setActiveSwipe(true);
            }
            else{//bottom

            }
        }

        super.onOverScrolled(scrollX, scrollY,clampedX,clampedY);
        //requestDisallowInterceptTouchEvent(true);
    }




    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if(jinActivity != null) {
                float power = (velocityY * 100 / sMaxFling);
                if (power < -5) {
                    jinActivity.setActionView(true, false);
                    //mUIController.hideActionBar();
                } else if (power > 10) {
                    //mUIController.showActionBar();
                    jinActivity.setActionView(false, false);
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /*
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
            final int SLIDE_THRESHOLD = 100;
            try {
                float deltaY = e2.getY() - e1.getY();
                float deltaX = e2.getX() - e1.getX();

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > SLIDE_THRESHOLD) {
                        if (deltaX > 0) {
                            // the user made a sliding right gesture
                            //return onSlideRight();
                        } else {
                            // the user made a sliding left gesture
                            //return onSlideLeft();
                        }
                    }
                } else {
                    if (Math.abs(deltaY) > SLIDE_THRESHOLD) {
                        if (deltaY > 0) {
                            // the user made a sliding down gesture
                            //return onSlideDown();
                            if(isOverScrolled) {
                                mMainActivity.setActionView(false, false);
                            }

                        } else {
                            // the user made a sliding up gesture
                            //return onSlideUp();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        */


        /**
         * Without this, onLongPress is not called when user is zooming using
         * two fingers, but is when using only one.
         * <p/>
         * The required behaviour is to not trigger this when the user is
         * zooming, it shouldn't matter how much fingers the user's using.
         */
        private boolean mCanTriggerLongPress = true;

        @Override
        public void onLongPress(MotionEvent e) {
            if (mCanTriggerLongPress) {
                /*
                Message msg = mWebViewHandler.obtainMessage();
                if (msg != null) {
                    msg.setTarget(mWebViewHandler);

                    requestFocusNodeHref(msg);
                }
                */
            }
        }

        /**
         * Is called when the user is swiping after the doubletap, which in our
         * case means that he is zooming.
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            mCanTriggerLongPress = false;
            return false;
        }

        /**
         * Is called when something is starting being pressed, always before
         * onLongPress.
         */
        @Override
        public void onShowPress(MotionEvent e) {
            mCanTriggerLongPress = true;
        }
    }



    public class JinDownloadListener implements DownloadListener{
        //private WebView webview;
        //public JinDownloadListener(WebView w){
           // webview = w;
        //}
        public void onDownloadStart(final String url, final String userAgent,
                                    final String contentDisposition, final String mimeType,
                                    long contentLength) {

            String file_name = null;

            if (mimeType != null && contentDisposition != null) {
                String[] strArray = contentDisposition.split(";");

                for (int i = 0; i < strArray.length; i++) {
                    if (strArray[i].toLowerCase().trim().startsWith("filename")) {
                        String[] nameArray = strArray[i].split("=");

                        file_name = nameArray[1];
                        file_name = file_name.replaceAll("[\"]", "");
                        break;
                    }
                }
            }

            if (file_name == null) {
                file_name = URLUtil.guessFileName(url, contentDisposition, mimeType);
            }

            final String download_name = file_name;

            jinActivity.confirmDialog("[" + download_name + "] " + jinActivity.getString(R.string.webview_download_confirm),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            checkPermissionAndDownloadFile(JinWebView.this, url, mimeType, userAgent, contentDisposition, download_name);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        }
    }


    public static int dpToPx(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (dp * metrics.density + 0.5f);
    }
}
