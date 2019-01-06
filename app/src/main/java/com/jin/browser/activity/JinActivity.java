package com.jin.browser.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jin.browser.R;
import com.jin.browser.adapter.JinBookmarkListViewAdapter;
import com.jin.browser.adapter.JinHistoryListViewAdapter;
import com.jin.browser.adapter.JinTabListViewAdapter;
import com.jin.browser.config.AppDefine;
import com.jin.browser.db.JinDbHelper;
import com.jin.browser.activity.util.DrawableUtils;
import com.jin.browser.activity.util.JinPermissionUtil;
import com.jin.browser.activity.util.JinPreferenceUtil;
import com.jin.browser.activity.util.Utils;
import com.jin.browser.webview.JinWebView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.jin.browser.activity.util.Utils.dpToPx;

/**
 * Created by kwy on 2018-03-16.
 */

public abstract class JinActivity extends AppCompatActivity implements View.OnKeyListener {
    private int currentActivityState = 0;
    private byte[] handshake_key;
    private long session_timeout;
    private String session_id;
    private long handshake_time;
    private boolean isConnectReady = true;

    private boolean isPrivacyActivity = false;


    private int app_height = 0;
    private int app_action_bar_height = 0;
    private int app_status_bar_height = 0;

    private GestureOverlayView mGestureLayout;
    protected DrawerLayout mDrawerLayout;
    private ProgressDialog progressDialog;
    private ImageView tab_img;
    private EditText search_edit_text = null;

    private LinearLayout tab_list_view_layout;
    private LinearLayout setting_list_view_layout;
    private LinearLayout bookmark_list_view_layout;

    private ListView history_list_view;
    private JinHistoryListViewAdapter historyListViewAdapter;

    private ListView bookmark_list_view;
    private JinBookmarkListViewAdapter bookmarkListViewAdapter;

    private ListView tab_list_view;
    private JinTabListViewAdapter tabListViewAdapter;


    private android.support.v4.widget.SwipeRefreshLayout webview_swipe_layout;
    private FrameLayout webview_frame;
    private FrameLayout jin_search_bar;

    protected JinDbHelper talkDbHelper;

    public ValueCallback<Uri> WebViewOldUploadMessage;
    public ValueCallback<Uri[]> WebViewLollipopUploadMessage;

    private LinearLayout bottom_menu_bar_layout;


    public static final int ON_ACTIVITY_RESULT_UPLOAD_ATTACHMENT = 1;
    public static final int ON_ACTIVITY_RESULT_UPLOAD_PROFILE_IMAGE = 2;
    public static final int ON_ACTIVITY_RESULT_REQUEST_SELECT_FILE = 3;
    public static final int ON_ACTIVITY_RESULT_FILECHOOSER_RESULTCODE = 4;
    public static final int ON_ACTIVITY_RESULT_REQUEST_AUDIO_FOCUS = 5;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jin_activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mGestureLayout = findViewById(R.id.gesture_layout);
        mGestureLayout.setGestureVisible(false);


        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLocker());

        talkDbHelper = JinDbHelper.getInstance(this);
        talkDbHelper.setMainActivity(this);

        webview_swipe_layout = findViewById(R.id.webview_swipe_layout);
        webview_swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getTopWebview() != null) {
                    getTopWebview().reload();
                }
                webview_swipe_layout.setRefreshing(false);
            }
        });

        /*
        webview_swipe_layout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(getTopWebview() != null) {
                    if (getTopWebview().getScrollY() == 0) {
                        webview_swipe_layout.setEnabled(true);
                    } else {
                        webview_swipe_layout.setEnabled(false);
                    }
                }
            }
        });
        */




        webview_frame = findViewById(R.id.webview_frame);
        tab_img =  findViewById(R.id.tab_img);
        search_edit_text = findViewById(R.id.search_edit_text);
        history_list_view = findViewById(R.id.history_list_view);

        jin_search_bar = findViewById(R.id.jin_search_bar);

        tab_list_view_layout = findViewById(R.id.tab_list_view_layout);
        setting_list_view_layout =  findViewById(R.id.setting_list_view_layout);
        bookmark_list_view_layout =  findViewById(R.id.bookmark_list_view_layout);

        bookmark_list_view = findViewById(R.id.bookmark_list_view);
        bookmarkListViewAdapter = new JinBookmarkListViewAdapter(this);
        bookmark_list_view.setAdapter(bookmarkListViewAdapter);

        history_list_view = findViewById(R.id.history_list_view);
        historyListViewAdapter = new JinHistoryListViewAdapter(this);
        history_list_view.setAdapter(historyListViewAdapter);

        tab_list_view = findViewById(R.id.tab_list_view);
        tabListViewAdapter = new JinTabListViewAdapter(this);
        tab_list_view.setAdapter(tabListViewAdapter);

        search_edit_text = findViewById(R.id.search_edit_text);


        bottom_menu_bar_layout = findViewById(R.id.bottom_menu_bar_layout);

        if(JinPreferenceUtil.getBoolean(this, JinPreferenceUtil.SETTING_VISIBLE_BOTTOM_MENU_BAR, false)){
            bottom_menu_bar_layout.setVisibility(VISIBLE);
        }


        /*
        PhoneStateCheckListener phoneCheckListener = new PhoneStateCheckListener(this);

        TelephonyManager telephonyManager  =
                (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneCheckListener,
                PhoneStateListener.LISTEN_CALL_STATE);
        */

        initSearchBar();
        initTabListView();

        initSettingListView();
        initBookmarkListView();
        initHistoryListView();


        final TextView end_draw_header_title = findViewById(R.id.end_draw_header_title);


        final ImageView bookmark_list_header_action_btn = findViewById(R.id.bookmark_list_header_action_btn);
        bookmark_list_header_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if(end_draw_header_title.getText().equals(getString(R.string.gesture_bookmark_txt))){
                    addBookmark(getTopWebview());
                }
                */
            }
        });

        ImageView add_end_draw_btn = findViewById(R.id.add_end_draw_btn);
        add_end_draw_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(end_draw_header_title.getText().equals(getString(R.string.gesture_bookmark_txt))){
                    addBookmark(getTopWebview(), true);
                }
            }
        });

        ImageView delete_end_draw_btn = findViewById(R.id.delete_end_draw_btn);
        delete_end_draw_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bookmarkListViewAdapter.deleteCheckItem();
            }
        });


        ImageView check_all_end_draw_btn = findViewById(R.id.check_all_end_draw_btn);
        check_all_end_draw_btn.setOnClickListener(new View.OnClickListener() {
            boolean toggleCheckAll = true;
            @Override
            public void onClick(View v) {
                bookmarkListViewAdapter.checkAllItem(toggleCheckAll);

                if(toggleCheckAll == true){
                    toggleCheckAll = false;
                }
                else{
                    toggleCheckAll = true;
                }
            }
        });
    }

    public void setActiveSwipe(boolean active){
        webview_swipe_layout.setEnabled(active);
    }

    public void addBookmark(JinWebView webview, final boolean toast){
        //bookmarkListViewAdapter.addItem();

        //talkDbHelper.insertBookmark(this, new JinDbHelper.Bookmark(0, title, url, JinDbHelper.Bookmark.BOOKMAKR_URL, null, img));
        //bookmarkListViewAdapter.updateListView();

        final String url = webview != null ? webview.getUrl() : null;
        final String title = webview != null ? webview.getTitle() : null;
        if (url == null) {
            toastMessage("url is null");
            return;
        }

        //new BookmarkExecute().execute();

        captureWebView(getTopWebview(), 20, new CallBackListener(null) {
            @Override
            public void execute(Object response) throws Exception {
                JinDbHelper.Bookmark b = new JinDbHelper.Bookmark(0,
                        getTopWebview().getTitle(),
                        getTopWebview().getUrl(),
                        JinDbHelper.Bookmark.BOOKMAKR_URL,
                        null,
                        (Bitmap)response);

                JinDbHelper.Bookmark bookmark = talkDbHelper.getBookmark(JinActivity.this, b);
                if(bookmark != null){
                    talkDbHelper.updateBookmark(JinActivity.this, bookmark);
                }
                else{
                    talkDbHelper.insertBookmark(JinActivity.this, b);
                }




                bookmarkListViewAdapter.updateListView();

                if(toast) {
                    toastMessage("[" + getTopWebview().getTitle() + "] " + getString(R.string.added_bookmark));
                }
            }
        });
    }

    public void initTabListView(){
        ImageView tab_list_header_action_btn = findViewById(R.id.tab_list_header_action_btn);
        tab_list_header_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getSupportFragmentManager();
                final JinDialogFragment dialogFragment = new JinDialogFragment();
                dialogFragment.setPosition((int)v.getX(), (int)v.getY());


                dialogFragment.setContextListItem(
                        new JinDialogFragment.ListItem(getString(R.string.tab_setting_open_new)){
                            public void click(){
                                mDrawerLayout.closeDrawer(GravityCompat.START);

                                newWebView((JinPreferenceUtil.getString(JinActivity.this,AppDefine.DEFAULT_URL_NAME) == null)?
                                        AppDefine.DEFAULT_URL:JinPreferenceUtil.getString(JinActivity.this,AppDefine.DEFAULT_URL_NAME));

                                setSearchEditText("");
                            }
                        }
                        ,new JinDialogFragment.ListItem(getString(R.string.tab_setting_close_all)){
                            public void click(){
                                tabListViewAdapter.removeAllTab();
                            }
                        }


                );
                dialogFragment.setShadowVisible(false);
                dialogFragment.show(fm, "fragment_dialog_test");


            }
        });


        tab_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JinTabListViewAdapter.ListViewItem item = (JinTabListViewAdapter.ListViewItem) parent.getItemAtPosition(position) ;

                //FrameLayout webviw_layout = findViewById(R.id.webview_frame);
                JinWebView webview = (JinWebView)item.getWebView();

                setSearchEditText(webview.getUrl());
                setActivityForCurrentPage(webview, webview.getTitle(), webview.getUrl());

                webview.bringToFront();

                //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        ImageView tab_list_add_btn = findViewById(R.id.tab_list_add_btn);
        tab_list_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

                newWebView((JinPreferenceUtil.getString(JinActivity.this,AppDefine.DEFAULT_URL_NAME) == null)?
                        AppDefine.DEFAULT_URL:JinPreferenceUtil.getString(JinActivity.this,AppDefine.DEFAULT_URL_NAME));

                setSearchEditText("");
            }
        });
    }


    public int calculateWebviewHeight(){
        int result = 0 ;

        if(JinPreferenceUtil.getPreferenceFullScreen(this)) {
            result = app_height;
        }
        else{
            result = app_height-app_action_bar_height;
        }

        return result;
    }

    public void initSettingListView(){
        final CheckBox setting_full_screen_ch = findViewById(R.id.setting_full_screen_ch);
        setting_full_screen_ch.setChecked(JinPreferenceUtil.getPreferenceFullScreen(this));
        setting_full_screen_ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout actionBar = (FrameLayout)findViewById(R.id.jin_search_bar);

                if(setting_full_screen_ch.isChecked()){
                    JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_FULL_SCREEN, true);

                    actionBar.getLayoutParams().height = 0;
                    actionBar.requestLayout();

                    webview_frame.setTranslationY(0);
                    webview_frame.getLayoutParams().height = calculateWebviewHeight();
                    webview_frame.requestLayout();
                }
                else{
                    //setActionView(false);
                    JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_FULL_SCREEN, false);

                    actionBar.getLayoutParams().height = app_action_bar_height;
                    actionBar.requestLayout();

                    webview_frame.setTranslationY(app_action_bar_height);
                    webview_frame.getLayoutParams().height = calculateWebviewHeight();
                    webview_frame.requestLayout();
                }
            }
        });



        final CheckBox setting_visible_menu_bar_ch = findViewById(R.id.setting_visible_menu_bar_ch);
        setting_visible_menu_bar_ch.setChecked(JinPreferenceUtil.getBoolean(JinActivity.this, JinPreferenceUtil.SETTING_VISIBLE_BOTTOM_MENU_BAR));
        setting_visible_menu_bar_ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout actionBar = (FrameLayout)findViewById(R.id.jin_search_bar);

                if(setting_visible_menu_bar_ch.isChecked()){
                    JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_VISIBLE_BOTTOM_MENU_BAR, true);

                    bottom_menu_bar_layout.setVisibility(VISIBLE);

                    webview_frame.getLayoutParams().height = calculateWebviewHeight();
                    webview_frame.requestLayout();
                }
                else{
                    JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_VISIBLE_BOTTOM_MENU_BAR, false);

                    bottom_menu_bar_layout.setVisibility(GONE);

                    webview_frame.getLayoutParams().height = calculateWebviewHeight();
                    webview_frame.requestLayout();
                }
            }
        });



        final CheckBox setting_activate_add_block_ch = findViewById(R.id.setting_activate_add_block_ch);
        setting_activate_add_block_ch.setChecked(JinPreferenceUtil.getBoolean(JinActivity.this, JinPreferenceUtil.SETTING_ACTIVATE_ADD_BLOCK, false));
        setting_activate_add_block_ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setting_activate_add_block_ch.isChecked()){
                    JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_ACTIVATE_ADD_BLOCK, true);

                }
                else{
                    JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_ACTIVATE_ADD_BLOCK, false);

                }
            }
        });


        final CheckBox setting_no_use_cache_ch = findViewById(R.id.setting_no_use_cache_ch);
        if(isIncognito()) {
            setting_no_use_cache_ch.setChecked(false);
        }
        else {
            setting_no_use_cache_ch.setChecked(JinPreferenceUtil.getBoolean(JinActivity.this, JinPreferenceUtil.SETTING_NO_USE_CACHE, false));
            setting_no_use_cache_ch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (setting_no_use_cache_ch.isChecked()) {
                        JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_NO_USE_CACHE, true);
                        setWebviewCacheMode(true);
                    } else {
                        JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.SETTING_NO_USE_CACHE, false);
                        setWebviewCacheMode(false);
                    }
                }
            });
        }



        final TextView setting_clear_add_block_tv = findViewById(R.id.setting_clear_add_block_tv);
        setting_clear_add_block_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String where = JinDbHelper.Bookmark.OPTION_FIELD+"=?";
                String[] selector = {""+JinDbHelper.Bookmark.BLOCK_URL};
                talkDbHelper.deleteBookmark(JinActivity.this, where, selector);

                toastMessage(getString(R.string.clear_block_url));
            }
        });


        final TextView setting_open_privacy_tab_tv = findViewById(R.id.setting_open_privacy_tab_tv);
        setting_open_privacy_tab_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.END);

                /*
                Intent intent = new Intent(JinActivity.this, PrivacyActivity.class);
                //intent.putExtra("start_talk_with_client", clientInfo.toJSONString());
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);
                */

                isPrivacyActivity = true;
            }
        });



        final TextView setting_gesture_tv = findViewById(R.id.setting_gesture_tv);
        setting_gesture_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.END);

                FragmentManager fm = getSupportFragmentManager();
                JinDialogFragment dialogFragment = new JinDialogFragment();
                dialogFragment.setDialogOption(JinDialogFragment.GESTURE_SETTING_DIALOG);
                dialogFragment.show(fm, "fragment_dialog_test");
            }
        });

        final TextView setting_bookmark_tv = findViewById(R.id.setting_bookmark_tv);
        setting_bookmark_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting_list_view_layout.setVisibility(GONE);
                bookmark_list_view_layout.setVisibility(VISIBLE);
            }
        });


        final TextView setting_clear_cookie = findViewById(R.id.setting_clear_cookie);
        setting_clear_cookie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CookieManager c = CookieManager.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    c.removeAllCookies(null);
                } else {
                    //noinspection deprecation
                    CookieSyncManager.createInstance(JinActivity.this);
                    //noinspection deprecation
                    c.removeAllCookie();
                }

                toastMessage( getString(R.string.clear_cookie_complete));
            }
        });


        final TextView setting_clear_cache = findViewById(R.id.setting_clear_cache);
        setting_clear_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getTopWebview() != null) {
                    getTopWebview().clearCache(true);

                    toastMessage(getString(R.string.clear_cache_complete));
                }
            }
        });


        ImageButton setting_font_size_smaller = findViewById(R.id.setting_font_size_smaller);
        setting_font_size_smaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView setting_current_font_size = findViewById(R.id.setting_current_font_size);
                getTopWebview().textSmaller(setting_current_font_size);
            }
        });


        ImageButton setting_font_size_bigger = findViewById(R.id.setting_font_size_bigger);
        setting_font_size_bigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView setting_current_font_size = findViewById(R.id.setting_current_font_size);
                getTopWebview().textBigger(setting_current_font_size);
            }
        });
    }

    public void initHistoryListView(){
        String deleteWhere = JinDbHelper.Bookmark.OPTION_FIELD+"=? and "+JinDbHelper.Bookmark.DATE_FIELD +" < (datetime('now','localtime','-7 days'))";
        String[] selectors = {""+JinDbHelper.Bookmark.HISTORY_URL};
        //String[] selectors = {""+JinDbHelper.Bookmark.HISTORY_URL, "(datetime('now','localtime'))"};
        talkDbHelper.deleteBookmark(this, deleteWhere, selectors);

        history_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                JinHistoryListViewAdapter.ListViewItem item = (JinHistoryListViewAdapter.ListViewItem) parent.getItemAtPosition(position) ;

                String titleStr = item.getTitle() ;
                String url = item.getDesc();
                String link_id = item.getId();
                Drawable iconDrawable = item.getIcon() ;

                loadUrl(url);
                history_list_view.setVisibility(GONE);
            }
        }) ;
    }

    public void initBookmarkListView(){

        if(JinPreferenceUtil.getBoolean(this, JinPreferenceUtil.PRESET_BOOKMARK, false)== false){
            for(int i=0; i<AppDefine.preBookMark.length; i++){
                talkDbHelper.insertBookmark(this, AppDefine.preBookMark[i]);
            }

            JinPreferenceUtil.putBoolean(this, JinPreferenceUtil.PRESET_BOOKMARK, true);
        }


        ImageView bookmark_list_header_action_btn = findViewById(R.id.bookmark_list_header_action_btn);
        bookmark_list_header_action_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = JinActivity.this.getSupportFragmentManager();
                final JinDialogFragment dialogFragment = new JinDialogFragment();
                dialogFragment.setPosition((int)v.getX(), (int)v.getY());

                dialogFragment.setContextListItem(
                        new JinDialogFragment.ListItem("Export Bookmark"){
                            public void click(){

                            }
                        },new JinDialogFragment.ListItem("Import Bookmark"){
                            public void click(){

                            }
                        }
                );
                dialogFragment.setShadowVisible(false);
                dialogFragment.show(fm, "fragment_dialog_test");
            }
        });

        bookmarkListViewAdapter.initListView();
        bookmark_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                JinBookmarkListViewAdapter.ListViewItem item = (JinBookmarkListViewAdapter.ListViewItem) parent.getItemAtPosition(position) ;

                String titleStr = item.getTitle() ;
                String url = item.getDesc();
                String link_id = item.getId();
                Bitmap iconBitmap = item.getIcon() ;

                loadUrl(url);

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    //drawer.closeDrawer(GravityCompat.START);
                }
                else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                }
            }
        }) ;

        bookmark_list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                JinBookmarkListViewAdapter.ListViewItem item = (JinBookmarkListViewAdapter.ListViewItem) parent.getItemAtPosition(position) ;
                return false;
            }
        });
    }

    public void setWebviewCacheMode(boolean no_use){
        FrameLayout webviw_layout = findViewById(R.id.webview_frame);
        int cnt = webviw_layout.getChildCount();
        for(int i=(cnt-1); i>=0; i--) {
            JinWebView webview = (JinWebView) webviw_layout.getChildAt(i);
            if(webview != null) {
                if(no_use){
                    webview.getSettings().setAppCacheEnabled(false);
                    webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                }
                else{
                    webview.getSettings().setAppCacheEnabled(true);
                    webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                    //webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                }

            }
        }
    }
    public void setSessionTimeout(String s_id, long time){
        this.session_id = s_id;
        this.session_timeout = time;
    }

    public void alertDialog(final String msg, final DialogInterface.OnClickListener listener){

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = null;

                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(JinActivity.this);
                    alt_bld.setMessage(msg).setCancelable(true)
                            .setPositiveButton(getString(R.string.jin_confirm_txt), listener);

                    alertDialog = alt_bld.create();
                    alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    alertDialog.show();
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static abstract class CallBackListener{
        public CallBackListener(JSONObject o){
            //this.callback_param = o;
        }

        public abstract void execute(Object response) throws Exception;
    }

    public Bitmap captureWebView(JinWebView webview, int scale, CallBackListener callback){
        Bitmap result = null;

        try {
            //Bitmap bitmap = Bitmap.createBitmap(webview.getWidth(), webview.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap bitmap = Bitmap.createBitmap(webview.getWidth(), webview.getHeight(), Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(bitmap);
            webview.draw(canvas);

            if (scale > 1) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        bitmap.getWidth() / scale,
                        bitmap.getHeight() / scale,
                        false);
                bitmap.recycle();

                result = scaledBitmap;
            } else {
                result = bitmap;
            }

            //result.getByteCount();

            //Toast.makeText(MainActivity.this, "caputureWebView() byte length : " + result.getByteCount(), Toast.LENGTH_SHORT).show();

            if(callback != null){
                callback.execute(result);
            }
        }
        catch(final Exception e){
            e.printStackTrace();

            toastMessage("caputureWebView() error : " + e.getMessage());
        }

        return result;
    }

    public void setSslIcon(boolean visibile, boolean isSecure){
        FrameLayout f = findViewById(R.id.layout_ssl);
        if(visibile){
            ImageView ic_ssl = findViewById(R.id.ic_ssl);
            if(isSecure){
                //ic_ssl.setImageResource(R.drawable.ic_menu_login);
                ic_ssl.setImageResource(R.drawable.ic_lock_outline);
                ic_ssl.setBackgroundResource(R.drawable.ic_green_rect_no_border);
            }
            else{
                ic_ssl.setImageResource(R.drawable.ic_menu_emoticons);
                ic_ssl.setBackgroundResource(R.drawable.ic_red_rect_no_border);
            }
            f.setVisibility(View.VISIBLE);
        }
        else{
            f.setVisibility(GONE);
        }

        //f.notifyDataSetChanged();
    }


    private class DrawerLocker implements DrawerLayout.DrawerListener {

        @Override
        public void onDrawerClosed(View v) {

            bookmark_list_view_layout.setVisibility(View.VISIBLE);
            setting_list_view_layout.setVisibility(GONE);
        }

        @Override
        public void onDrawerOpened(View v) {

            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                //drawer.closeDrawer(GravityCompat.START);
                //contactListViewAdapter.initContactListView(null);

                if(tab_list_view_layout.getVisibility() == VISIBLE){
                    tabListViewAdapter.updateListView();
                }

            }
            else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                if(bookmark_list_view_layout.getVisibility() == VISIBLE){


                }
            }
        }

        @Override
        public void onDrawerSlide(View v, float arg) {}

        @Override
        public void onDrawerStateChanged(int arg) {}
    }



    public void openStartDrawer(final int opt){
        mDrawerLayout.openDrawer(GravityCompat.START);
        boolean res = false;

        tabListViewAdapter.updateListView();
    }

    public void initSearchBar(){
        FrameLayout tab_img_layout = findViewById(R.id.tab_img_layout);
        tab_img_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStartDrawer(0);
                JinPreferenceUtil.putBoolean(JinActivity.this, JinPreferenceUtil.TUTORIAL_EXE, true);
            }
        });


        final FrameLayout search_reload_layout = findViewById(R.id.search_reload_layout);
        search_reload_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTopWebview().isLoading()){
                    getTopWebview().stopLoading();
                }
                else {
                    //getTopWebview().reload();
                    loadUrl(search_edit_text.getText().toString());
                }

                //Toast.makeText(MainActivity.this, "Webpage loading : "+getTopWebview().isLoading(), Toast.LENGTH_SHORT).show();
            }
        });


        FrameLayout setting_more_layout = findViewById(R.id.setting_more_layout);
        setting_more_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmark_list_view_layout.setVisibility(GONE);
                setting_list_view_layout.setVisibility(VISIBLE);

                TextView setting_current_font_size = findViewById(R.id.setting_current_font_size);
                setting_current_font_size.setText(""+getTopWebview().getSettings().getTextZoom());

                mDrawerLayout.openDrawer(GravityCompat.END);
            }
        });


        search_edit_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    history_list_view.setVisibility(View.VISIBLE);

                    historyListViewAdapter.initListView();
                }
                else{
                    history_list_view.setVisibility(GONE);
                    //search_edit_start = false;
                }
            }
        });

        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(getTopWebview() != null && search_edit_text.getText().toString().equals(getTopWebview().getUrl())){
                    return;
                }

                //if(search_edit_start == false) return;

                history_list_view.setVisibility(View.VISIBLE);
                historyListViewAdapter.clearListView();

                setSslIcon(false, false);
                String url = search_edit_text.getText().toString();
                String reg = "(?i)https?:/{2}";
                url = url.replaceAll(reg, "");

                //String where = " where "+ JinDbHelper.Bookmark.URL_FIELD+" like ? ";
                //long weekTime = new Date((System.currentTimeMillis() - (1000*3600*24*7))).getTime();
                long weekTime = Calendar.getInstance().getTime().getTime()-(1000*10);

                historyListViewAdapter.searchListView(url);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        search_edit_text.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    search_edit_text.clearFocus();

                    loadUrl(search_edit_text.getText().toString());
                    return true;
                }

                return false;
            }
        });
    }

    public void setActivityForCurrentPage(JinWebView webview, String title, String url){
        if(url != null && url.toLowerCase().startsWith("https://")){
            if(webview.isSslError()) {
                setSslIcon(true, false);
            }
            else{
                setSslIcon(true, true);
            }
        }
        else{
            setSslIcon(false, false);
        }

        /*
        if(isIncognito() == false) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //noinspection deprecation
                CookieSyncManager.getInstance().sync();
            } else {
                CookieManager.getInstance().flush();
            }
        }
        */

        String where = " where "+JinDbHelper.Bookmark.OPTION_FIELD+"=? and "+JinDbHelper.Bookmark.URL_FIELD+"=? ";
        String[] selectors = {""+JinDbHelper.Bookmark.BLOCK_URL, (url==null)?"":url};
        int cnt = talkDbHelper.countBookmark(JinActivity.this, where, selectors);

        if(cnt > 0) {
            ImageView webview_block_img_view = findViewById(R.id.webview_block_img_view);
            webview_block_img_view.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));
        }
        else{
            ImageView webview_block_img_view = findViewById(R.id.webview_block_img_view);
            webview_block_img_view.setColorFilter(null);
        }

        tabListViewAdapter.updateWebviewTab(webview);

        search_edit_text.clearFocus();
        webview.requestFocus();
    }

    public void removeWebView(JinWebView webview){
        //this.mWebviewList.remove(webview);

        this.webview_frame.removeView(webview);
        //this.tabListViewAdapter.re

        JinWebView curWebview = getTopWebview();
        if(curWebview != null){
            setSearchEditText(curWebview.getUrl());
            updateTabNumber(tab_img, webview_frame.getChildCount());

            curWebview.requestFocus();
        }
        else{
            super.onBackPressed();
        }

        tabListViewAdapter.removeWebviewTab(webview);
    }


    public void setSearchEditText(String url){
        this.search_edit_text.setText(url);
        this.history_list_view.setVisibility(GONE);
    }


    public JinWebView getTopWebview(){
        JinWebView result = null;

        if(webview_frame.getChildCount() > 0) {
            result = (JinWebView) webview_frame.getChildAt(webview_frame.getChildCount() - 1);
        }

        return result;
    }


    public void toastMessage(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(JinActivity.this, msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void collapse(final View v, int duration, final int targetHeight, final int position) {

        final int prevHeight = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            int value = 0;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                value = (int) animation.getAnimatedValue();

                v.getLayoutParams().height = value;
                v.requestLayout();

                webview_frame.setTranslationY(value);
                webview_frame.requestLayout();
            }
        });
        //valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.clearAnimation();
            }
        });

    }

    public void setActionView(boolean hide, boolean force){
        if(JinPreferenceUtil.getPreferenceFullScreen(this) == false && force==false){

            return;
        }


        if(hide){
            if(jin_search_bar.getHeight() <= 0 && force==false){
                System.out.println("9999999999999999 : ddddddddddddddd");
                return;
            }

            /*
            webview_frame.getLayoutParams().height = app_height;
            webview_frame.requestLayout();
            webview_frame.invalidate();
            */

            collapse(jin_search_bar, 250, 0, 0);
        }
        else{
            if(jin_search_bar.getHeight() > 0 && force==false){
                System.out.println("8888888888888888 : ddddddddddddddd");
                return;
            }
            /*
            webview_frame.getLayoutParams().height = app_height-app_action_bar_height;
            webview_frame.requestLayout();
            webview_frame.invalidate();
            */

            collapse(jin_search_bar, 250, getActionBarHeight(), 0);
        }
    }

    public int getActionBarHeight(){
        final TypedArray ta = getTheme().obtainStyledAttributes(
                new int[] {android.R.attr.actionBarSize});
        int actionBarHeight = (int) ta.getDimension(0, 0);
        return actionBarHeight;
    }

    public void setReloadImageView(boolean loading){
        final ImageView reloadImg = findViewById(R.id.search_reload_img);

        if (loading){
            reloadImg.setImageResource(R.drawable.ic_clear_search_api_holo_light);
        }
        else {
            reloadImg.setImageResource(R.drawable.ic_go_search_api_holo_light);
        }
    }

    public void loadUrl(String url){
        if(url.toLowerCase().startsWith("http") == false){
            url = "http://"+url;
        }

        if(getTopWebview() == null){
            newWebView(url);
            getTopWebview().requestFocus();
        }
        else {
            getTopWebview().requestFocus();
            getTopWebview().loadUrl(url);
        }
    }

    public void lockDrawerLayout(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void confirmDialog(final String msg, final DialogInterface.OnClickListener pos_listener, final DialogInterface.OnClickListener neg_listener){

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = null;

                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(JinActivity.this);
                    alt_bld.setMessage(msg).setCancelable(true)
                            .setPositiveButton(getString(R.string.jin_confirm_txt), pos_listener)
                            .setNegativeButton(getString(R.string.jin_no_txt), neg_listener);

                    alertDialog = alt_bld.create();
                    alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                    alertDialog.show();

                }
            });

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public ProgressDialog getProgressDialog(){
        return this.progressDialog;
    }

    public abstract class ProgressOnShow{
        public abstract void onShow();
    }

    public void showProgressDialog(String message, final ProgressOnShow progressOnShow, DialogInterface.OnCancelListener onCancel){

        if(this.progressDialog != null && this.progressDialog.isShowing()){
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }

        this.progressDialog = new ProgressDialog(this);//Assuming that you are using fragments.
        this.progressDialog.setTitle("Please wait");
        this.progressDialog.setMessage(message);
        this.progressDialog.setCancelable(true);
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                /*
                progressHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressOnShow.onShow();
                    }
                }, 20);
                */

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        progressOnShow.onShow();
                    }
                }).start();
            }
        });

        if(onCancel != null){
            this.progressDialog.setOnCancelListener(onCancel);
        }

        this.progressDialog.show();
    }

    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }
        else if(history_list_view.getVisibility() == View.VISIBLE){
            history_list_view.setVisibility(GONE);
        }
        else {
            JinWebView currentWebView = this.getTopWebview();

            if(currentWebView != null){
                if(currentWebView.canGoBack()){
                    currentWebView.goBack();
                }
                else{
                    currentWebView.destroy();
                    currentWebView = null;

                    if(this.webview_frame.getChildCount() == 0){
                        super.onBackPressed();
                    }
                }
            }
            else {
                super.onBackPressed();
            }
        }
    }

    public String getFilePathFromUri(Uri uri){
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        for(int i=0; i<cursor.getColumnCount(); i++){
            System.out.println("getImagePath() 11111 >>> columnInfo : " + cursor.getColumnName(i) + "= "+cursor.getString(i));
        }

        if(cursor.getColumnIndex(MediaStore.Images.Media.DATA) != -1){
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            return path;
        }

        String document_id = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
        String display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        long size = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
        cursor.close();

        if(document_id.indexOf(":") == -1) {
            //String select = MediaStore.Images.Media.DISPLAY_NAME+"=? and "+MediaStore.Images.Media.DATE_TAKEN +"=?";
            //String[] args = {display_name, date_token};

            String select = MediaStore.Images.Media.DISPLAY_NAME+"=? and " + MediaStore.Images.Media.SIZE+"=?";
            String[] args = {display_name, ""+size};
            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, select, args, null);
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getColumnCount(); i++) {
                System.out.println("getImagePath() 2222 >>> columnInfo : " + cursor.getColumnName(i) + "= " + cursor.getString(i));
            }

            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        else{
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();

            //for (int i = 0; i < cursor.getColumnCount(); i++) {
            //    System.out.println("getImagePath() 2222 >>> columnInfo : " + cursor.getColumnName(i) + "= " + cursor.getString(i));
            //}

            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }

        return path;
    }


    protected void onResume(){
        super.onResume();

        this.currentActivityState = AppDefine.ON_RESUME_STATE;
    }

    protected void onPause(){
        super.onPause();

        this.currentActivityState = AppDefine.ON_PAUSE_STATE;
    }

    protected void onStop(){
        super.onStop();

        this.currentActivityState = AppDefine.ON_STOP_STATE;
    }

    protected void onDestroy(){
        super.onDestroy();

        this.currentActivityState = AppDefine.ON_DESTROY_STATE;
    }

    public boolean isExpiredSessionTime(){
        long currentTime = System.currentTimeMillis();

        long expire = (currentTime-handshake_time)/1000;

        System.out.println("isExpiredSessionTime() >>> expiref : "+expire);
        System.out.println("isExpiredSessionTime() >>> session_timeout : "+session_timeout);

        if(expire >= session_timeout ){
            return true;
        }

        return false;
    }

    public void setHandshakeKey(byte[] handshake_key) {
        this.handshake_key = handshake_key;

        if(handshake_key == null){
            setSessionTimeout(null,0);
        }
        else{
            setHandshakeTime(System.currentTimeMillis());
        }
    }

    public void setMaxWidthDrawerLayout(FrameLayout layout){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) layout.getLayoutParams();
        params.width = metrics.widthPixels;
        layout.setLayoutParams(params);
    }

    public void setHandshakeTime(long time){
        this.handshake_time = time;
    }

    public String getSessionId(){
        return this.session_id;
    }

    public boolean isConnectReady(){
        return this.isConnectReady;
    }

    public void setConnectReady(boolean b){
        this.isConnectReady = b;
    }

    public byte[] getHandshakeKey(){
        return this.handshake_key;
    }

    public JSONObject generateParam(JSONObject param, JSONObject body){
        if(JinPreferenceUtil.getRsaEncryptWithServer(this)
                && getHandshakeKey() != null){

            param.put("encrypt", true);

            int msg_length = body.toJSONString().getBytes().length;
            //byte[] enc_data = secHandler.encryptRsa(Base64.decode(server_pub_key, 0), body.toJSONString().getBytes());
            try {
                //byte[] enc_data = mSecurityHandler.encrypt(body.toJSONString().getBytes(), getHandshakeKey());
                //param.put("body", Base64.encodeToString(enc_data, 0));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else {
            param.put("body", body);
        }

        return param;
    }


    public String generateEncBody(JSONObject body){
        String result = null;
        if(getHandshakeKey() != null){
            int msg_length = body.toJSONString().getBytes().length;
            //byte[] enc_data = secHandler.encryptRsa(Base64.decode(server_pub_key, 0), body.toJSONString().getBytes());
            try {
                //byte[] enc_data = mSecurityHandler.encrypt(body.toJSONString().getBytes(), getHandshakeKey());
                //byte[] dec_data = mSecurityHandler.decrypt(enc_data, getHandshakeKey());

                //result =  Base64.encodeToString(enc_data, 0);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        return result;
    }


    public int getCurrentActivityState(){
        return this.currentActivityState;
    }


    public boolean isIncognito(){
        return this.isPrivacyActivity;
    }

    public abstract void processIntentMsg();

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void onRequestPermissionsResult(int request_id, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(request_id, permissions, grantResults);
        //Toast.makeText(getActivity(), "["+requestCode +"]Permission request result : "+grantResults[0], Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= 23) {
            JinPermissionUtil.requestPermissionCallback(request_id, permissions, grantResults);
        }
    }

    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event){
        return super.onKeyDown(keyCode, event);
    }

    public boolean isBlockUrl(String url){
        boolean result = false;

        //String domain = StringUtil.getDomain(url);
        //String where = " where "+JinDbHelper.Bookmark.OPTION_FIELD+"=? and "+JinDbHelper.Bookmark.URL_FIELD+" like ? ";
        //String[] selectors = {""+JinDbHelper.Bookmark.BLOCK_URL, "%"+domain+"%"};

        String where = " where "+JinDbHelper.Bookmark.OPTION_FIELD+"=? and "+JinDbHelper.Bookmark.URL_FIELD+" = ? ";
        String[] selectors = {""+JinDbHelper.Bookmark.BLOCK_URL, url};
        int cnt = talkDbHelper.countBookmark(this, where, selectors);

        if(cnt > 0) result=true;

        return result;
    }


    public void updateBookmark(JinWebView webview, JinDbHelper.Bookmark b){
        JinDbHelper.Bookmark bookmark = talkDbHelper.getBookmark(this, b);
        if(bookmark != null){
            if(webview.getCurrentCaptureImage().img != null) {
                //bookmark.img = webview.getCurrentCaptureImage().img;
                bookmark.img = DrawableUtils.cloneBitmap(webview.getCurrentCaptureImage().img);
            }

            talkDbHelper.updateBookmark(this, bookmark);
            //bookmarkListViewAdapter.notifyDataSetChanged();

            bookmarkListViewAdapter.updateListView();
        }
    }

    public boolean newWebView(String url){
        final ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.search_progress);

        JinWebView webview = new JinWebView(this, progressBar, isIncognito());

        if(url != null) {
            webview.loadUrl(url);
        }
        webview_frame.addView(webview);
        updateTabNumber(tab_img, webview_frame.getChildCount());

        tabListViewAdapter.updateListView();
        return true;
    }

    public void updateTabNumber(ImageView img_view, int number) {
        if (img_view != null) {
            img_view.setImageBitmap(DrawableUtils.getRoundedNumberImage(number, dpToPx(24),
                    Utils.dpToPx(24), Color.GRAY, dpToPx(1.5f)));
        }
    }

    public void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Log.d(C.TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        }
        else {
            //Log.d(C.TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    /*
    public abstract void onCallStateChanged(int state, String incomingNumber);


    public class PhoneStateCheckListener extends PhoneStateListener {
        JinActivity mainActivity;
        PhoneStateCheckListener(JinActivity _main){
            mainActivity = _main;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {


            mainActivity.onCallStateChanged(state, incomingNumber);
        }
    }
    */

}
