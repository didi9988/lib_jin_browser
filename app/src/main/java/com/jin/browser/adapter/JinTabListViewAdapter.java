package com.jin.browser.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.jin.browser.R;
import com.jin.browser.activity.JinActivity;
import com.jin.browser.db.JinDbHelper;
import com.jin.browser.webview.JinWebView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kwy on 2018-02-01.
 */

public class JinTabListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    private HashMap<Long,ListViewItem> ItemList = new HashMap<Long,ListViewItem>() ;

    private JinActivity jinActivity;
    private JinDbHelper talkDbHelper = null;
    private Boolean activeUpdateIconThread = false;
    private  boolean is_closed_draw = false;
    private boolean is_active_async_task = false;

    public class ListViewItem {
        private Bitmap iconBitmap ;
        private String titleStr ;
        private String descStr ;

        private String date_str;
        private long id;

        private JinWebView webview;

        public void setIcon(Bitmap icon) {
            iconBitmap = icon ;
        }
        public void setTitle(String title) {
            titleStr = title ;
        }
        public void setDesc(String desc) {
            descStr = desc ;
        }
        public void setId(long id){
            this.id = id;
        }
        public void setDate(String date){
            this.date_str = date;
        }

        public long getId(){
            return this.id;
        }

        public String getDate(){
            return this.date_str;
        }

        public Bitmap getIcon() {
            return this.iconBitmap ;
        }
        public String getTitle() {
            return this.titleStr ;
        }
        public String getDesc() {
            return this.descStr ;
        }

        public void setWebView(JinWebView w){
            this.webview = w;
        }
        public JinWebView getWebView(){return this.webview;}
    }

    // ListViewAdapter의 생성자
    public JinTabListViewAdapter(Context context) {
        this.jinActivity = (JinActivity) context;
    }

    public boolean isClosedDraw(){
        return this.is_closed_draw;
    }

    public void setIsClosedDraw(boolean b){
        this.is_closed_draw = b;
    }


    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.jin_adapter_history_list_view, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        final ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;

        ImageView tab_item_close_btn = (ImageView) convertView.findViewById(R.id.tab_item_close_btn) ;
        tab_item_close_btn.setVisibility(View.VISIBLE);

        CheckBox list_check_box_layout = (CheckBox) convertView.findViewById(R.id.list_check_box) ;
        list_check_box_layout.setVisibility(View.GONE);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final ListViewItem listViewItem = listViewItemList.get(position);


        tab_item_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jinActivity.removeWebView(listViewItem.webview);
                //deleteCheckItem(position);
            }
        });

        if(listViewItem.iconBitmap == null) {
            //new LoadImage(iconImageView, listViewItem).execute();

            jinActivity.captureWebView(listViewItem.webview, 20, new JinActivity.CallBackListener(null) {
                @Override
                public void execute(Object response) throws Exception {
                    listViewItem.iconBitmap = (Bitmap)response;
                    iconImageView.setImageBitmap(listViewItem.iconBitmap);
                }
            });
        }
        else {
            iconImageView.setImageBitmap(listViewItem.getIcon());
        }

        titleTextView.setText(listViewItem.getTitle());
        descTextView.setText(listViewItem.getDesc());

        return convertView;
    }



    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        ListViewItem item = listViewItemList.get(position);
        return item.getId() ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        final ListViewItem listViewItem = listViewItemList.get(position);


        return listViewItem;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Bitmap icon, final JinWebView webview){
        if(ItemList.containsKey(webview.getmWebViewId()) == false) {
            ListViewItem item = new ListViewItem();
            item.setIcon(icon);
            item.setTitle(webview.getTitle());
            item.setDesc(webview.getUrl());
            item.setId(webview.getmWebViewId());
            item.setWebView(webview);

            listViewItemList.add(0,item);
            ItemList.put(webview.getmWebViewId(), item);
        }
    }

    public void deleteCheckItem(int position){
        if(position < listViewItemList.size() ) {
            ListViewItem listViewItem = listViewItemList.get(position);
            ItemList.remove(listViewItem.getId());
            listViewItemList.remove(position);

            jinActivity.runOnUiThread(
                    new Runnable() {
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
        }
    }


    public void removeAllTab(){
        for(int i=0; i< listViewItemList.size(); i++){
            ListViewItem listViewItem = listViewItemList.get(i);

            //removeWebviewTab(listViewItem.webview);
            listViewItem.webview.destroy();
            i--;
        }
    }

    public void removeWebviewTab(JinWebView webview){
        if(ItemList.containsKey(webview.getmWebViewId())){
            ListViewItem item = ItemList.get(webview.getmWebViewId());
            ItemList.remove(webview.getmWebViewId());
            listViewItemList.remove(item);


            notifyDataSetChanged();
        }
    }


    public void updateWebviewTab(JinWebView webview){
        if(ItemList.containsKey(webview.getmWebViewId())){
            ListViewItem item = ItemList.get(webview.getmWebViewId());
            item.setIcon(webview.getCurrentCaptureImage().img);
            item.setTitle(webview.getTitle());
            item.setDesc(webview.getUrl());
            item.setId(webview.getmWebViewId());

            notifyDataSetChanged();
        }
    }


    public void initListView() {
        try {
            FrameLayout webview_layout = this.jinActivity.findViewById(R.id.webview_frame);


            int cnt = webview_layout.getChildCount();

            for(int i=(cnt-1); i>=0; i--){
                JinWebView webview = (JinWebView)webview_layout.getChildAt(i);

                if(webview.getCurrentCaptureImage() != null) {
                    addItem(webview.getCurrentCaptureImage().img, webview);
                }
                else{
                    addItem(null, webview);
                }


                //addItem(null, webview);
            }

            jinActivity.runOnUiThread(
                    new Runnable() {
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateListView(){
        //clearListView();
        initListView();
    }




    public void clearListView(){

        listViewItemList.clear();
        ItemList.clear();
    }



    class LoadImage extends AsyncTask<Object, Void, Bitmap> {
        private ImageView imv;
        private ListViewItem listViewItem;

        public LoadImage(ImageView imv, ListViewItem listViewItem) {

            this.imv = imv;
            this.listViewItem = listViewItem;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            listViewItem.iconBitmap = jinActivity.captureWebView(listViewItem.webview, 20, null);
            return listViewItem.iconBitmap;
        }

        @Override
        protected void onPostExecute(final Bitmap result) {
            if(result != null && imv != null){

                jinActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imv.setVisibility(View.VISIBLE);
                        imv.setImageBitmap(result);
                    }
                });
            }
        }

    }
}
