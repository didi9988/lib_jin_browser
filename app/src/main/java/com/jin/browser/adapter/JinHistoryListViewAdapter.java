package com.jin.browser.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.jin.browser.R;
import com.jin.browser.activity.JinActivity;
import com.jin.browser.db.JinDbHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kwy on 2018-02-01.
 */

public class JinHistoryListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    private HashMap<String,String> ItemList = new HashMap<String,String>() ;

    private JinActivity jinActivity;
    private JinDbHelper talkDbHelper = null;

    public class ListViewItem {
        private Drawable iconDrawable ;
        private String titleStr ;
        private String descStr ;

        private String date_str;
        private String id;

        public void setIcon(Drawable icon) {
            iconDrawable = icon ;
        }
        public void setTitle(String title) {
            titleStr = title ;
        }
        public void setDesc(String desc) {
            descStr = desc ;
        }
        public void setId(String id){
            this.id = id;
        }
        public void setDate(String date){
            this.date_str = date;
        }

        public String getId(){
            return this.id;
        }

        public String getDate(){
            return this.date_str;
        }

        public Drawable getIcon() {
            return this.iconDrawable ;
        }
        public String getTitle() {
            return this.titleStr ;
        }
        public String getDesc() {
            return this.descStr ;
        }
    }

    // ListViewAdapter의 생성자
    public JinHistoryListViewAdapter(Context context) {
        jinActivity = (JinActivity)context;
        talkDbHelper = JinDbHelper.getInstance(jinActivity);
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.jin_adapter_history_list_view, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;
        CheckBox list_check_box_layout = (CheckBox) convertView.findViewById(R.id.list_check_box) ;
        list_check_box_layout.setVisibility(View.GONE);

        iconImageView.setMaxWidth(32);
        iconImageView.setMaxHeight(32);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        titleTextView.setText(listViewItem.getTitle());
        descTextView.setText(listViewItem.getDesc());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Drawable icon, String title, String url, String id ){
        ListViewItem item = new ListViewItem();

        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(url);
        item.setId(id);

        if(ItemList.containsKey(id) == false) {
            listViewItemList.add(item);
            ItemList.put(id, null);
        }
    }

    public void addItem(Drawable icon, JinDbHelper.Bookmark bookmark){
        ListViewItem item = new ListViewItem();


        System.out.println("OPT : "+bookmark.opt);
        if(bookmark.opt == JinDbHelper.Bookmark.BOOKMAKR_URL){
            icon = jinActivity.getResources().getDrawable(R.drawable.ic_menu_star);
        }
        else{
            icon = jinActivity.getResources().getDrawable(R.drawable.ic_menu_recent_history);
        }

        item.setIcon(icon);
        item.setTitle(bookmark.title + "("+bookmark.date_time+")");
        //item.setDesc(address.phone_num+"||"+address.hash_phone_num);
        item.setDesc(bookmark.url);
        item.setId(""+bookmark.id);
        item.setDate(bookmark.date_time);

        //String d_id = (String) info.get("d_id");

        String itemKey = ""+bookmark.id;



        if(ItemList.containsKey(itemKey) == false) {
            listViewItemList.add(item);
            ItemList.put(itemKey,null);
        }
    }

    public void deleteItem(int position, String name, String p_num){


        String itemKey = p_num+"||"+p_num;

        if(ItemList.containsKey(itemKey) == true) {
            listViewItemList.remove(position);
            ItemList.remove(itemKey);
        }
    }

    public void initListView() {
        try {

            String where = JinDbHelper.Bookmark.OPTION_FIELD + "=?";
            String[] selector = {""+JinDbHelper.Bookmark.BOOKMAKR_URL};

            ArrayList<JinDbHelper.Bookmark> bookmark_list = talkDbHelper.selectBookmark(jinActivity, where, selector, false);

            for(int i=0; i<bookmark_list.size(); i++) {
                addItem(null, bookmark_list.get(i));
            }

            String where2 = JinDbHelper.Bookmark.OPTION_FIELD + "<>?";
            String[] selector2 = {""+JinDbHelper.Bookmark.BLOCK_URL};

            ArrayList<JinDbHelper.Bookmark> list = talkDbHelper.selectBookmark(jinActivity, null, null, false);

            for(int i=0; i<list.size(); i++) {
                addItem(null, list.get(i));
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

    public void searchListView(String url){
        try {
            String where = JinDbHelper.Bookmark.OPTION_FIELD+"=? and " + JinDbHelper.Bookmark.URL_FIELD + " like ? ";
            String[] selector = {""+JinDbHelper.Bookmark.BOOKMAKR_URL, "%" + url + "%"};

            ArrayList<JinDbHelper.Bookmark> list = talkDbHelper.selectBookmark(jinActivity, where, selector, false);

            for (int i = 0; i < list.size(); i++) {
                addItem(null, list.get(i));
            }


            String where2 = JinDbHelper.Bookmark.OPTION_FIELD+"=? and " + JinDbHelper.Bookmark.URL_FIELD + " like ? ";
            String[] selector2 = {""+JinDbHelper.Bookmark.HISTORY_URL, "%" + url + "%"};

            ArrayList<JinDbHelper.Bookmark> list2 = talkDbHelper.selectBookmark(jinActivity, where2, selector2, false);

            for (int i = 0; i < list2.size(); i++) {
                addItem(null, list2.get(i));
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

    public void updateItem(int position, Drawable icon, JinDbHelper.Bookmark bookmark) {
        ListViewItem item = listViewItemList.get(position);
        item.setIcon(icon);
        item.setId(""+bookmark.id);
        //item.setAddressId(address.id);
    }


    public void clearListView(){

        listViewItemList.clear();
        ItemList.clear();
    }
}
