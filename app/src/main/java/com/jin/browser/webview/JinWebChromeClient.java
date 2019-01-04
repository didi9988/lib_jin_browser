package com.jin.browser.webview;

/**
 * Created by kwy on 2018-01-24.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jin.browser.R;
import com.jin.browser.activity.JinActivity;
import com.jin.browser.config.AppDefine;
import com.jin.browser.activity.util.JinPermissionUtil;


public class JinWebChromeClient extends WebChromeClient {

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    @NonNull
    private JinActivity jinActivity;
    private Context context;
    private JinWebView mWebView;
    private ProgressBar mProgressBar;

    private FrameLayout mFullscreenContainer;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;



    private int toggleOrientation;


    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


    public JinWebChromeClient(Context context, JinWebView webView, ProgressBar progressBar) {
        if (context instanceof JinActivity){
            jinActivity = (JinActivity) context;
        }

        this.context = context;
        mWebView = webView;
        mProgressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (mWebView != null && mWebView.isShown()) {
            if(mProgressBar != null) {
                mProgressBar.setProgress(newProgress);
            }
        }
    }

    @Override
    public void onReceivedIcon(@NonNull WebView view, Bitmap icon) {
        //mLightningView.getTitleInfo().setFavicon(icon);
        //mUIController.tabChanged(mLightningView);
        //cacheFavicon(view.getUrl(), icon);
    }




    @Override
    public void onReceivedTitle(@Nullable WebView view, @Nullable String title) {
        if (title != null) {
            mWebView.setTitle(title);
        }
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(@NonNull final String origin,
                                                   @NonNull final GeolocationPermissions.Callback callback) {

        boolean res = JinPermissionUtil.checkPermission(context, AppDefine.PERMISSION_ACCESS_FINE_LOCATION, new JinPermissionUtil.CallBackListener() {
            @Override
            public void execute(boolean allow_permission) throws Exception {
                if(allow_permission){
                    callback.invoke(origin, true, false);
                }
            }
        });

        if(res) callback.invoke(origin, true, false);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                                  Message resultMsg) {

        if (resultMsg == null || jinActivity==null) {
            return true;
        }

        //String curUrl = mWebView.getUrl();

        if (mWebView.getActivity().newWebView(null)) {
            final JinWebView newWebView = jinActivity.getTopWebview();

            if (newWebView != null) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
            }


            /*
            if (newWebView != null) {
                WebView.HitTestResult result = view.getHitTestResult();
                //int type = result.getType();
                //String url = result.getExtra();

                final WebView dummyWebView = new WebView(context);

                WebView.WebViewTransport transport2 = (WebView.WebViewTransport) resultMsg.obj;
                transport2.setWebView(dummyWebView);
                resultMsg.sendToTarget();

                dummyWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        newWebView.loadUrl(url);
                        dummyWebView.destroy();

                        return true;
                    }
                });
            }
            */

        }

        return true;
    }

    @Override
    public void onCloseWindow(WebView window) {
        if(jinActivity != null) {
            jinActivity.removeWebView(mWebView);
        }
    }

    @Override
    // For Lollipop 5.0+ Devices
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        if(jinActivity == null){
            return false;
        }

        // make sure there is no existing message
        if (jinActivity.WebViewLollipopUploadMessage != null) {
            jinActivity.WebViewLollipopUploadMessage.onReceiveValue(null);
            jinActivity.WebViewLollipopUploadMessage = null;
        }

        jinActivity.WebViewLollipopUploadMessage = filePathCallback;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = fileChooserParams.createIntent();
            try {
                jinActivity.startActivityForResult(intent, JinActivity.ON_ACTIVITY_RESULT_REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                jinActivity.WebViewLollipopUploadMessage = null;
                //Toast.makeText(myActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    //For Android 4.1 only
    protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        if(jinActivity == null){
            return;
        }
        jinActivity.WebViewOldUploadMessage = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        jinActivity.startActivityForResult(Intent.createChooser(intent, "File Browser"), JinActivity.ON_ACTIVITY_RESULT_FILECHOOSER_RESULTCODE);
    }

    // For 3.0+ Devices (Start)
    protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        if(jinActivity == null){
            return;
        }
        jinActivity.WebViewOldUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        jinActivity.startActivityForResult(Intent.createChooser(i, "File Browser"), JinActivity.ON_ACTIVITY_RESULT_FILECHOOSER_RESULTCODE);
    }


    protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
        if(jinActivity == null){
            return;
        }
        jinActivity.WebViewOldUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        jinActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), JinActivity.ON_ACTIVITY_RESULT_FILECHOOSER_RESULTCODE);
    }


    /**
     * Obtain an image that is displayed as a placeholder on a video until the video has initialized
     * and can begin loading.
     *
     * @return a Bitmap that can be used as a place holder for videos.
     */
    @Nullable
    @Override
    public Bitmap getDefaultVideoPoster() {
        final Resources resources = context.getResources();
        return BitmapFactory.decodeResource(resources, android.R.drawable.spinner_background);
    }

    /**
     * Inflate a view to send to a LightningView when it needs to display a video and has to
     * show a loading dialog. Inflates a progress view and returns it.
     *
     * @return A view that should be used to display the state
     * of a video's loading progress.
     */
    /*
    @Override
    public View getVideoLoadingProgressView() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        return inflater.inflate(R.layout.video_loading_progress, null);
    }
    */

    private void setFullscreen(boolean enabled) {
        Window win = ((Activity)context).getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (enabled) {
            winParams.flags |= bits;

            win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            winParams.flags &= ~bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

            win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //win.setAttributes(winParams);
    }


    @Override
    public void onHideCustomView() {
        super.onHideCustomView();

        if (mCustomView == null) {
            return;
        }

        setFullscreen(false);
        //FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        FrameLayout decor = ((Activity)context).findViewById(R.id.content_webview_layout);
        //FrameLayout decor = ((Activity)context).findViewById(R.id.webview_frame_full);
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer.clearFocus();
        mFullscreenContainer.removeAllViews();
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;
        ((Activity)context).setRequestedOrientation(mOriginalOrientation);

       // ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        ImageView webview_video_full_screen_btn = ((Activity)context).findViewById(R.id.webview_video_full_screen_btn);
        if(webview_video_full_screen_btn != null) {
            webview_video_full_screen_btn.setVisibility(View.GONE);
        }

        mWebView.clearFocus();
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        //mUIController.onShowCustomView(view, callback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }




            mOriginalOrientation = ((Activity)context).getRequestedOrientation();
            //FrameLayout decor = (FrameLayout) mMainActivity.getWindow().getDecorView();
            FrameLayout decor = ((Activity)context).findViewById(R.id.content_webview_layout);
            //FrameLayout decor = ((Activity)context).findViewById(R.id.webview_frame_full);
            mFullscreenContainer = new FullscreenHolder(context);
            mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
            decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
            mCustomView = view;
            setFullscreen(true);
            mCustomViewCallback = callback;
            //((Activity)context).setRequestedOrientation(mOriginalOrientation);

            //int requestOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; //SCREEN_ORIENTATION_UNSPECIFIED

            if(mOriginalOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                toggleOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }

            //int requestOrientation = ((Activity)context).getRequestedOrientation();

            //((Activity)context).setRequestedOrientation(requestOrientation);


            ImageView webview_video_full_screen_btn = ((Activity)context).findViewById(R.id.webview_video_full_screen_btn);
            if(webview_video_full_screen_btn != null) {
                webview_video_full_screen_btn.setVisibility(View.VISIBLE);
                webview_video_full_screen_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (toggleOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            toggleOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        }
                        else if(toggleOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            toggleOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        }
                        else {
                            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            toggleOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        }
                    }
                });
            }
        }

        super.onShowCustomView(view, callback);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onShowCustomView(View view, int requestedOrientation,
                                 CustomViewCallback callback) {
        this.onShowCustomView(view, callback);
    }


    private static class FullscreenHolder extends FrameLayout {
        private View video_view;
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.black));
        }

        public void addView(View child, ViewGroup.LayoutParams params) {
            super.addView(child, -1, params);
            video_view = child;
        }

        public void removeAllViews(){
            super.removeAllViews();
            video_view = null;
        }

        public void clearFocus(){
            super.clearFocus();
            if(video_view != null) video_view.clearFocus();
        }

       // @Override
        //public boolean onTouchEvent(MotionEvent evt) {
        //    return true;
        //}
    }
}
