package com.jin.browser.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.jin.browser.R;
import com.jin.browser.activity.JinActivity;
import com.jin.browser.activity.util.DrawableUtils;
import com.jin.browser.db.JinDbHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kwy on 2018-02-01.
 */

public class JinBookmarkListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;
    private HashMap<String,String> ItemList = new HashMap<String,String>() ;

    private JinActivity jinActivity;
    private JinDbHelper talkDbHelper = null;

    public class ListViewItem {
        public JinDbHelper.Bookmark bookmark;
        private Bitmap iconBitmap ;
        private String titleStr ;
        private String descStr ;

        private String date_str;
        private String id;

        private boolean checked = false;
        public void setChecked(boolean c){
            checked = c;
        }
        public boolean isChecked(){
            return this.checked;
        }

        public void setIcon(Bitmap icon) {
            iconBitmap = icon ;
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

        public Bitmap getIcon() {
            return this.iconBitmap ;
        }
        public String getTitle() {
            return this.titleStr ;
        }
        public String getDesc() {
            return this.descStr ;
        }
    }

    // ListViewAdapter의 생성자
    public JinBookmarkListViewAdapter(Context context) {
        jinActivity = (JinActivity) context;
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



        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final ListViewItem listViewItem = listViewItemList.get(position);

        final CheckBox list_check_box = (CheckBox) convertView.findViewById(R.id.list_check_box) ;
        list_check_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewItem.setChecked(list_check_box.isChecked());
            }
        });
        list_check_box.setChecked(listViewItem.isChecked());


        // 아이템 내 각 위젯에 데이터 반영
        //iconImageView.setImageBitmap(listViewItem.getIcon());

        if(listViewItem.iconBitmap != null) {
            iconImageView.setImageBitmap(listViewItem.iconBitmap);
        }
        else {
            //if(listViewItem.bookmark.url.equals(mMainActivity.getTopWebview().getUrl())) {
            //    new LoadImage(iconImageView, listViewItem).execute();
           // }

            iconImageView.setImageBitmap(DrawableUtils.getRoundedLetterImage(listViewItem.getTitle().charAt(0), 45, 45, "#6699ff"));
            //getRoundedLetterImage
        }

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

    public void addItem(Bitmap icon, JinDbHelper.Bookmark bookmark){
        ListViewItem item = new ListViewItem();
        item.bookmark = bookmark;

        item.setIcon(icon);
        item.setTitle(bookmark.title);
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


    public ArrayList<ListViewItem> getBookmarkList(){
        return this.listViewItemList;
    }


    public void deleteFromDB(ListViewItem item){
        String where = JinDbHelper.Bookmark.ID_FIELD +"=?";
        String[] selector = {item.getId()};

        talkDbHelper.deleteBookmark(jinActivity, where, selector);
    }

    public void deleteCheckItem(){
        //int cnt = listViewItemList.size();

        for(int i=0; i<listViewItemList.size(); i++){
            ListViewItem item = listViewItemList.get(i);
            if(item.isChecked()){
                listViewItemList.remove(i);
                ItemList.remove(item.getId());
                deleteFromDB(item);

                i--;
            }
            else{
                item.setChecked(false);
            }
        }

        jinActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
    }

    public void checkAllItem(boolean check){
        //int cnt = listViewItemList.size();

        for(int i=0; i<listViewItemList.size(); i++){
            ListViewItem item = listViewItemList.get(i);

            item.setChecked(check);
        }

        jinActivity.runOnUiThread(
                new Runnable() {
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
    }


    public void initListView() {
        try {
            String where = JinDbHelper.Bookmark.OPTION_FIELD + "=?";
            String[] selector = {""+JinDbHelper.Bookmark.BOOKMAKR_URL};
            ArrayList<JinDbHelper.Bookmark> list = talkDbHelper.selectBookmark(jinActivity, where, selector, true);

            for(int i=0; i<list.size(); i++) {
                addItem(list.get(i).img, list.get(i));
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
        clearListView();
        initListView();
    }

    public void clearListView(){

        listViewItemList.clear();
        ItemList.clear();
    }


    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private ImageView imv;
        private ListViewItem listViewItem;
        //private File file;

        public LoadImage(ImageView imv, ListViewItem listViewItem) {

            this.imv = imv;
            this.listViewItem = listViewItem;
            //this.file = listViewItem.getFile();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {


            //return listViewItem.iconBitmap;

            return jinActivity.captureWebView(jinActivity.getTopWebview(), 20, null);
        }
        @Override
        protected void onPostExecute(final Bitmap result) {

            jinActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(result != null && imv != null){
                        imv.setVisibility(View.VISIBLE);
                        imv.setImageBitmap(result);

                        listViewItem.bookmark.img = result;
                        talkDbHelper.insertBookmark(jinActivity, listViewItem.bookmark);
                    }
                }
            });
        }
    }
}
