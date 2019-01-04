package com.jin.browser.activity;

/**
 * Created by kwy on 2018-01-16.
 */

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.jin.browser.R;
import com.jin.browser.activity.util.DrawableUtils;
import com.jin.browser.activity.util.JinPreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JinDialogFragment extends DialogFragment {

    public static int DEFAULT_DIALOG = 0;
    public static int GESTURE_SETTING_DIALOG = 1;
    public static int SIMPLE_JOIN_DIALOG = 2;
    public static int ADD_CONTACT_DIALOG = 3;
    public static int WEBVIEW_SEARCH_WORD_DIALOG = 4;
    public static int CONTACT_PROFILE_DIALOG = 5;
    public static int ADD_GROUP_DIALOG = 6;
    public static int INPUT_PASSWORD_DIALOG = 7;
    public static int ABOUT_DIALOG = 8;
    public static int PERMISSION_DIALOG=9;

    private ArrayList<ListItem> contextListItem = new ArrayList<ListItem>();

    DialogFragment fragment;

    private int CURRENT_DIALOG = DEFAULT_DIALOG;
    private String default_title;
    private String default_content;

    private ImageView jin_dialog_title_close_btn;

    private View.OnClickListener okBtnOnClickListner;
    private View.OnClickListener imageOnClickListner;
    private String okBtnTxt;

    private boolean shadowVisible = true;
    private boolean fullScreen = false;
    private DialogInterface onDismissListener;

    private ImageView profileImage;
    private Bitmap contactImage;

    private ArrayList<String> group_contacts;

    private View rootView;

    private int position_x = 0;
    private int position_y = 0;

    private int width = 300;
    private int height = 0;

    private Bitmap profile_img;

    private EditText nickname_et;

    private EditText group_name_et;

    private String packageSignature;

    private ImageView contact_profile_talk_image;
    private ImageView contact_profile_video_talk_image;
    private ImageView contact_profile_security_mail_image;

    public void setPackageSignature(String s){
        packageSignature = s;
    }

    public static abstract class ListItem{
        String title;
        long id;

        public ListItem(String name){
            this.title = name;
            this.id = System.nanoTime();
        }

        public abstract void click();
    }

    public JinDialogFragment(){

        fragment = this;
    }

    public void setFullScreen(boolean b){
        this.fullScreen = b;
    }

    public void setTitle(String t){
        this.default_title = t;
    }


    public  void onStart() {
        super.onStart();

        if(this.fullScreen) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(this.onDismissListener != null){
            this.onDismissListener.cancel();
            this.onDismissListener.dismiss();
        }
    }


    public void setOnDismissListener(DialogInterface listener){
        this.onDismissListener = listener;
    }


    public void setPosition(int x, int y){
        /*
        Window window = getDialog().getWindow();
        //window.setGravity(Gravity.TOP|Gravity.LEFT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = x;
        params.y=y;

        window.setAttributes(params);
        */

        this.position_x = x;
        this.position_y = y;
    }


    public void setContent(String c){
        this.default_content = c;
    }


    public void setShadowVisible(boolean b){
        shadowVisible = b;
    }


    public void setProfileImage(File file){
        //profile_img = BitmapFactory.decodeFile(file.getAbsolutePath());
        profile_img = DrawableUtils.rotatedImage(file.getAbsolutePath());
        this.profileImage.setImageBitmap(profile_img);

        JinPreferenceUtil.putString(getActivity(), JinPreferenceUtil.SETTING_PROFILE_IMAGE, file.getAbsolutePath());
    }

    public void setContactProfileImage(Bitmap bitmap){
        //profile_img = BitmapFactory.decodeFile(file.getAbsolutePath());
        //profile_img = DrawableUtils.rotatedImage(file.getAbsolutePath());

        if(bitmap != null) {
            //this.profileImage.setImageBitmap(bitmap);
            this.contactImage = bitmap;

            if(this.profileImage != null) {
                this.profileImage.setImageBitmap(this.contactImage);
            }
        }

        //JinPreferenceUtil.putString(getActivity(), JinPreferenceUtil.SETTING_PROFILE_IMAGE, file.getAbsolutePath());
    }

    public Bitmap getProfileImg(){
        return this.profile_img;
    }

    public Bitmap getContactImage(){
        return this.contactImage;
    }

    public void setDialogOption(int opt){
        CURRENT_DIALOG = opt;
    }

    public void setDialogOption(int opt, String dialog_title){
        CURRENT_DIALOG = opt;
        setTitle(dialog_title);
    }

    public DialogFragment getFragment(){
        return this.fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.jin_fragment_dialog, container,
                false);
        //getDialog().setTitle("DialogFragment Tutorial");
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Do something else

        setLayoutView(rootView, CURRENT_DIALOG);

        jin_dialog_title_close_btn = rootView.findViewById(R.id.jin_dialog_title_close_btn);

        jin_dialog_title_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        if(this.default_title != null){
            TextView title = rootView.findViewById(R.id.jin_dialog_title);
            title.setText(default_title);
        }

        if(CURRENT_DIALOG == GESTURE_SETTING_DIALOG) {
            initGestureDialog(rootView);
        }
        else if(CURRENT_DIALOG == WEBVIEW_SEARCH_WORD_DIALOG){
            initWebviewSearchWordDialog(rootView);
        }
        else if(CURRENT_DIALOG == ADD_GROUP_DIALOG){
            initAddGroupDialog(rootView);
        }
        else if(CURRENT_DIALOG == INPUT_PASSWORD_DIALOG){
            initInputPasswordDialog(rootView);
        }
        else if(CURRENT_DIALOG == ABOUT_DIALOG){
            initAboutDialog(rootView);
        }
        else{
            initDefaultDialog(rootView);
            setCancelable(false);
        }

        if(shadowVisible == false) {
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            setCancelable(true);
        }




        Button btn = rootView.findViewById(R.id.dialog_ok_btn);
        if(this.okBtnOnClickListner != null){
            btn.setOnClickListener(this.okBtnOnClickListner);
        }
        else{
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragment().dismiss();
                }
            });
        }




        if(okBtnTxt != null) btn.setText(this.okBtnTxt);

        /*
        Window window = getDialog().getWindow();
        // set gravity
        window.setGravity(Gravity.BOTTOM| Gravity.CENTER);
        // then set the values to where you want to position it
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 300;
        params.y = 100;
        window.setAttributes(params);
        */

        if(this.contextListItem.size() >0 ){
            btn.setVisibility(View.GONE);
        }

        if(this.position_x > 0 || this.position_y > 0){
            Window window = getDialog().getWindow();
            window.setGravity(Gravity.TOP|Gravity.LEFT);
            WindowManager.LayoutParams params = window.getAttributes();
            params.x = position_x;
            params.y= position_y;

            //params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            //params.height = LinearLayout.LayoutParams.WRAP_CONTENT;

            window.setAttributes(params);
        }


        return rootView;
    }


    public void show(FragmentManager manager, String tag) {
        if(this.isVisible() == false) {
            super.show(manager, tag);
        }
    }

    public void setLayoutView(View view, int opt){
        LinearLayout default_layout = view.findViewById(R.id.default_layout);
        LinearLayout gesture_setting_layout = view.findViewById(R.id.gesture_setting_layout);
        LinearLayout req_video_chat_layout = view.findViewById(R.id.req_video_chat_layout);
        LinearLayout webview_search_word_layout = view.findViewById(R.id.webview_search_word_layout);
        LinearLayout add_group_layout = view.findViewById(R.id.add_group_layout);
        LinearLayout input_password_layout = view.findViewById(R.id.input_password_layout);
        LinearLayout about_layout = view.findViewById(R.id.about_layout);

        if(opt == JinDialogFragment.GESTURE_SETTING_DIALOG){
            gesture_setting_layout.setVisibility(View.VISIBLE);
        }
        else if(opt == JinDialogFragment.WEBVIEW_SEARCH_WORD_DIALOG){
            webview_search_word_layout.setVisibility(View.VISIBLE);
        }
        else if(opt == JinDialogFragment.ADD_GROUP_DIALOG){
            add_group_layout.setVisibility(View.VISIBLE);
        }
        else if(opt == JinDialogFragment.INPUT_PASSWORD_DIALOG){
            input_password_layout.setVisibility(View.VISIBLE);
        }
        else if(opt == JinDialogFragment.ABOUT_DIALOG){
            about_layout.setVisibility(View.VISIBLE);
        }
        else{
            default_layout.setVisibility(View.VISIBLE);
        }
    }

    public void setGroupContacts( ArrayList<String> contacts){
        this.group_contacts = contacts;
    }

    private int dpToPx(int dp) {

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);

    }

    public void initDefaultDialog(final View rootView){

        LinearLayout jin_dialog_title_layout = rootView.findViewById(R.id.jin_dialog_title_layout);
        TextView title = rootView.findViewById(R.id.jin_dialog_title);
        TextView content = rootView.findViewById(R.id.content_tv);

        if(default_title == null) {
            title.setText(R.string.default_dialog_title_txt);
        }
        else{
            title.setText(default_title);
        }

        if(this.default_content != null) {
            content.setText(this.default_content);
        }
        else{
            if(default_title == null) {
                jin_dialog_title_layout.setVisibility(View.GONE);
            }
            content.setVisibility(View.GONE);
        }


        if(contextListItem.size() > 0) {

            ListView listView = rootView.findViewById(R.id.jin_dialog_list_view);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1);

            final List<ListItem> itemList = new ArrayList<>(1);
            for (ListItem it : contextListItem) {
                itemList.add(it);
            }

            for (ListItem it : itemList) {
                adapter.add(it.title);
            }

            listView.setAdapter(adapter);

            listView.setDivider(null);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    itemList.get(position).click();
                    dismiss();
                }
            });
        }
    }


    public void setContextListItem(ListItem... items){
        for(int i=0; i<items.length; i++) {
            this.contextListItem.add(items[i]);
        }
    }

    public void addContextListItem(ListItem item){
        this.contextListItem.add(item);
    }


    public void initWebviewSearchWordDialog(final View rootView){
        TextView title = rootView.findViewById(R.id.jin_dialog_title);
        title.setText(R.string.webview_search_word);
    }



    public void initAboutDialog(final View rootView){
        TextView title = rootView.findViewById(R.id.jin_dialog_title);
        title.setText(this.default_title);

        TextView content = rootView.findViewById(R.id.about_content_tv);
        content.setText(this.default_content);

        EditText about_package_signature_edit = rootView.findViewById(R.id.about_package_signature_edit);
        if(packageSignature != null){
            about_package_signature_edit.setText(packageSignature);
        }
        else{
            about_package_signature_edit.setVisibility(View.GONE);
        }
    }



    public String getNickName(){
        return nickname_et.getText().toString();
    }



    public ImageView getImageViewById(int id){
        return getView().findViewById(id);
    }


    public void initInputPasswordDialog(final View rootView){
        //TextView title = rootView.findViewById(R.id.jin_dialog_title);
        //title.setText(R.string.jin_password_txt);
    }


    public void initAddGroupDialog(final View rootView){
        TextView title = rootView.findViewById(R.id.jin_dialog_title);
        title.setText(R.string.talk_setting_make_group);

        //this.group_contacts
        group_name_et = rootView.findViewById(R.id.talk_group_name);

        TextView list = rootView.findViewById(R.id.talk_group_txt);

        String members = "";

        if(this.group_contacts != null){
            for(int i=0; i<this.group_contacts.size(); i++){
                members += group_contacts.get(i) + ",";
            }
        }

        list.setText(members);
    }


    public String getGroupName(){
        return group_name_et.getText().toString();
    }


    public void setOkButtonOnClickListener( View.OnClickListener listener, String txt){
        this.okBtnOnClickListner = listener;
        this.okBtnTxt = txt;
    }


    public void setImageOnClickListener( View.OnClickListener listener){
        this.imageOnClickListner = listener;
    }


    public void initGestureDialog(final View rootView){
        TextView title = rootView.findViewById(R.id.jin_dialog_title);
        title.setText(R.string.gesture_setting_txt);

        final CheckBox enable_gesture = rootView.findViewById(R.id.enable_gesture_cbx);
        final CheckBox visible_gesture  = rootView.findViewById(R.id.visible_gesture_cbx);
        final CheckBox gesture_bookmark  = rootView.findViewById(R.id.gesture_bookmark_cbx);
        final CheckBox gesture_remove_bookmark  = rootView.findViewById(R.id.gesture_remove_bookmark_cbx);
        final CheckBox gesture_screen_capture  = rootView.findViewById(R.id.gesture_capture_cbx);


        enable_gesture.setChecked(JinPreferenceUtil.getBoolean(getActivity(), JinPreferenceUtil.GESTURE_ENABLE, false));
        visible_gesture.setChecked(JinPreferenceUtil.getBoolean(getActivity(), JinPreferenceUtil.GESTURE_VISIBLE, false));
        gesture_bookmark.setChecked(JinPreferenceUtil.getBoolean(getActivity(), JinPreferenceUtil.GESTURE_BOOKMARK_ACTIVE, true));
        gesture_remove_bookmark.setChecked(JinPreferenceUtil.getBoolean(getActivity(), JinPreferenceUtil.GESTURE_REMOVE_BOOKMARK_ACTIVE, true));
        gesture_screen_capture.setChecked(JinPreferenceUtil.getBoolean(getActivity(), JinPreferenceUtil.GESTURE_CAPTURE_SCREEN_ACTIVE, true));

        enable_gesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enable_gesture.isChecked() == false){

                    /*
                    visible_gesture.setClickable(false);
                    visible_gesture.setBackgroundColor(Color.parseColor("#cccccc"));
                    gesture_bookmark.setClickable(false);
                    gesture_remove_bookmark.setClickable(false);
                    gesture_screen_capture.setClickable(false);
                    */
                }
            }
        });



        setOkButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JinPreferenceUtil.putBoolean(getActivity(), JinPreferenceUtil.GESTURE_ENABLE, enable_gesture.isChecked());
                JinPreferenceUtil.putBoolean(getActivity(), JinPreferenceUtil.GESTURE_VISIBLE, visible_gesture.isChecked());
                JinPreferenceUtil.putBoolean(getActivity(), JinPreferenceUtil.GESTURE_BOOKMARK_ACTIVE, gesture_bookmark.isChecked());
                JinPreferenceUtil.putBoolean(getActivity(), JinPreferenceUtil.GESTURE_REMOVE_BOOKMARK_ACTIVE, gesture_remove_bookmark.isChecked());
                JinPreferenceUtil.putBoolean(getActivity(), JinPreferenceUtil.GESTURE_CAPTURE_SCREEN_ACTIVE, gesture_screen_capture.isChecked());

                getFragment().dismiss();
            }
        }, null);
    }


    /*
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                // Set Dialog Icon
                .setIcon(R.drawable.file_icon)
                // Set Dialog Title
                .setTitle("Alert DialogFragment")
                // Set Dialog Message
                .setMessage("Alert DialogFragment Tutorial")

                // Positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something else
                    }
                })

                // Negative Button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        // Do something else
                    }
                }).create();
    }
    */
}
