package com.jin.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;


import com.jin.browser.activity.JinActivity;
import com.jin.browser.activity.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;


/**
 * Created by kwy on 2017-12-27.
 */

public class JinDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "JIN_TALK.db";


    public static class Talk{
        public static String TALK_TABLE_NAME = "TALK";

        public static String ID_FIELD = "ID";
        public static String TALK_ID_FIELD = "TALK_ID";
        public static String TALK_GROUP_ID_FIELD = "TALK_GROUP";
        public static String FROM_ID_FIELD = "FROM_ID";
        public static String TO_ID_FIELD = "TO_ID";
        public static String MESSAGE_FIELD = "MESSAGE";
        public static String ENC_FLAG_FIELD = "ENC_FLAG";
        public static String STATUS_FIELD = "STATUS";
        public static String DATE_FIELD = "DATE_TIME";
        public static String AGREEMENT_KEY_FIELD = "AGREEMENT_KEY";
        public static String TALK_TYPE_FIELD = "TALK_TYPE";
        public static String ATTACH_PATH_FIELD = "ATTACH_PATH";
        public static String SEND_OR_RECV_FIELD = "SEND_OR_RECEIVE";
        public static String NOT_READ_COUNT_FIELD = "SEND_COUNT";
        public static String SEND_RECV_CHECK = "SEND_RECV_CHECK";

        public static int NOT_READ_STATE = 0;
        public static int READ_STATE = 1;

        public static int SEND_RECV_CHECK_SUCCESS = 1;

        public static int RECEIVE = 0;
        public static int SEND = 1;

        public static int MESSAGE_TYPE = 0;
        public static int ATTACH_TYPE=1;
        public static int REQUEST_VIDEO_CHAT_TYPE=2;
        public static int CANCEL_VIDEO_CHAT_TYPE=3;
        public static int ENTER_GROUP_TYPE=4;
        public static int EXIT_GROUP_TYPE=5;
        public static int SECURITY_SIGN_MESSAGE_TYPE=6;

        public int id;
        public String talk_id;
        public String talk_group;
        public String from_id;
        public String to_id;
        public String message;
        public int enc_flag;
        public int status;
        public String date_time;
        public String agreement_key;
        public int talk_type;
        public String attach_path;
        public int send_or_receive;
        public int not_read_count;
        public int send_recv_check;

        public Talk(int id, String talk_id, String talk_group, String from_id, String my_id,
                    String agreement_key, String message, int enc_flag,
                    int status, String date_time, int talk_type, String attach_path,
                    int send_or_receive, int not_read_count, int send_recv_check){
            this.id = id;
            this.talk_id = talk_id;
            this.talk_group = talk_group;
            this.from_id = from_id;
            this.to_id  = my_id;
            this.message = message;
            this.enc_flag = enc_flag;
            this.status = status;
            this.date_time = date_time;
            this.agreement_key = agreement_key;
            this.talk_type = talk_type;
            this.attach_path = attach_path;
            this.send_or_receive = send_or_receive;
            this.not_read_count = not_read_count;
            this.send_recv_check = send_recv_check;
        }
    }


    public static class TalkAddress extends TalkGroup{

        public static String TALK_ADDRESS_TABLE_NAME = "TALK_ADDRESS";

        public static String ID_FIELD = "ID";
        public static String DEVICE_ID_FIELD = "DEVICE_ID";
        public static String NAME_FIELD = "NAME";
        public static String NICKNAME_FIELD = "NICKNAME";
        public static String PHONE_NUM_FIELD = "PHONE_NUM";
        public static String HASH_PHONE_NUM_FIELD = "HASH_PHONE_NUM";
        public static String BASE64_ICON_FIELD = "BASE64_ICON";
        public static String EMAIL = "EMAIL";
        public static String IS_FRIEND = "IS_FRIEND";
        public static String PUB_KEY = "PUB_KEY";
        public static String RSA_PUB_KEY = "RSA_PUB_KEY";

        public static int FRIEND_BLOCK = -1;
        public static int FRIEND_NO = 0;
        public static int FRIEND_OK = 1;

        public int id;
        public String device_id;
        public String name;
        public String nickname;
        public String phone_num;
        public String hash_phone_num;
        public String base64_icon;
        public String email;
        public int is_friend;
        public String pub_key;
        public String rsa_pub_key;
        public int message_cnt = 0;

        public boolean isInTalkGroup = false;

        public TalkAddress(){}

        public TalkAddress(int i, String d, String n, String n2, String p, String h_p, String b, String e, int is_friend, String pub_key, String rsa_pub_key){
            this.id = i;
            this.device_id = d;
            this.name = n;
            this.nickname = n2;
            this.phone_num = p;
            this.hash_phone_num = h_p;
            this.base64_icon = b;
            this.email = e;
            this.is_friend = is_friend;
            this.pub_key = pub_key;
            this.rsa_pub_key = rsa_pub_key;
        }

    }



    public static class TalkGroup{

        public static String TALK_GROUP_TABLE_NAME = "TALK_GROUP";

        public static String ID_FIELD = "ID";
        public static String GROUP_ID_FIELD = "GROUP_ID";
        public static String GROUP_NUM_FIELD = "GROUP_NUM";
        public static String GROUP_NAME_FIELD = "GROUP_NAME";
        public static String OWNER_D_ID_FIELD = "OWNER_D_ID";
        public static String GROUP_DESCRIPTION_FIELD = "GROUP_DESCRIPTION";

        public int id;
        public String group_id;
        public int group_num;
        public String group_name;
        public String owner_d_id;
        public String group_description;
        public int message_cnt = 0;
        public TalkGroup(){}

        public TalkGroup(int i, String g_id, int g_num, String g_name, String owner_d_id, String g_description){
            this.id = i;
            this.group_id = g_id;
            this.group_num = g_num;
            this.group_name = g_name;
            this.owner_d_id = owner_d_id;
            this.group_description = g_description;
        }
    }

    public static class Bookmark{
        public static String BOOKMARK_TABLE_NAME = "JIN_BOOKMARK";
        public static String ID_FIELD = "ID";
        public static String TITLE_FIELD = "TITLE";
        public static String URL_FIELD = "URL";
        public static String OPTION_FIELD= "OPTION";
        public static String DATE_FIELD = "DATE_TIME";
        public static String IMG_FIELD = "IMG";


        final public static int HISTORY_URL = 0;
        final public static int BOOKMAKR_URL = 1;
        final public static int BLOCK_URL = 2;


        public int id;
        public String title;
        public String url;
        public int opt = 0;
        public String date_time;
        public Bitmap img;

        public Bookmark(int i, String t, String u, int o, String d, Bitmap img){
            this.id = i;
            this.title = t;
            this.url = u;
            this.opt = o;
            this.date_time = d;
            this.img = img;
        }
    }




    private final String bookmark_table_create = "CREATE TABLE "+ Bookmark.BOOKMARK_TABLE_NAME+" (ID INTEGER PRIMARY KEY," +
            Bookmark.TITLE_FIELD+" TEXT," +
            Bookmark.URL_FIELD+" TEXT," +
            Bookmark.OPTION_FIELD+" INTEGER DEFAULT 0, " +
            //Bookmark.DATE_FIELD+" TEXT DEFAULT (datetime('now','localtime'))" +
            Bookmark.DATE_FIELD+" DATETIME DEFAULT (datetime('now','localtime'))," +
            Bookmark.IMG_FIELD+" TEXT"+
            //Bookmark.IMG_FIELD+" BLOB"+
            ")";

    private static JinDbHelper instance = null;
    private JinActivity jinActivity;
    private Context context;

    public static JinDbHelper getInstance(Context context){
        if(instance == null){
            instance = new JinDbHelper(context);
            //mMainActivity = (MainActivity)context;


        }

        return instance;
    }



    private JinDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(bookmark_table_create);
    }

    public void setMainActivity(Context c){
        this.jinActivity = (JinActivity) c;
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        //onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db, oldVersion, newVersion);
    }

    public int countBookmark(Context context, String where, String[] args){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int result = 0;
        try {
            db = JinDbHelper.getInstance(context).getReadableDatabase();
            String query = "select count(*) as cnt from "+ Bookmark.BOOKMARK_TABLE_NAME;

            if(where != null){
                query += where;
            }
            cursor = db.rawQuery(query, args);

            cursor.moveToFirst();

            result = cursor.getInt(0);;
        }
        catch (Exception e){
        }
        finally {
            if(cursor != null) cursor.close();
            //if(db != null) db.close();
        }

        return result;
    }

    public byte[] getImgBytes(Bitmap img){
        byte[] result = null;

        ByteArrayOutputStream stream = null;

        try {
            stream = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.PNG, 5, stream);
            result = stream.toByteArray();
            stream.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

    //public

    public Bitmap getImg(String img_name) {
        Bitmap result = null;
        File f = new File(jinActivity.getFilesDir(), img_name);

        if(f.exists() && f.isFile()){
            FileInputStream f_in = null;
            try {
                f_in = new FileInputStream(f);

                byte[] read_buf = new byte[f_in.available()];
                f_in.read(read_buf, 0, read_buf.length);

                result =  getImg(read_buf);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                try{
                    if(f_in != null) f_in.close();
                }
                catch (Exception e){}
            }
        }

        return result;
    }

    public static Bitmap getImg(byte[] data) {
        Bitmap result = null;

        if(data != null){
            //result = BitmapFactory.decodeByteArray(data, 0, data.length);
            System.out.println("img size : "+data.length);

            result = Utils.decodeSampledBitmapFromResource(data, 48, 48);
        }
        return result;
    }


    public Bookmark getBookmark(Context context, Bookmark b){
        Bookmark result = null;
        String where = Bookmark.OPTION_FIELD+"=? and "+Bookmark.URL_FIELD + "=?";
        String[] selectorArgs = {""+Bookmark.BOOKMAKR_URL, b.url};

        ArrayList<Bookmark> list = selectBookmark(context, where, selectorArgs, true);

        if(list.size() > 0){
            result = list.get(0);
        }

        return result;
    }

    public void insertBookmark(Context context, Bookmark a){
        // Gets the data repository in write mode
        SQLiteDatabase db = JinDbHelper.getInstance(context).getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Bookmark.TITLE_FIELD, a.title);
        values.put(Bookmark.URL_FIELD, a.url);
        values.put(Bookmark.OPTION_FIELD, a.opt);

        if(a.img != null) {
            String f_name = ""+System.nanoTime();
            File file = new File(jinActivity.getFilesDir(), f_name);
            FileOutputStream fout = null;

            try {
                fout = new FileOutputStream(file);
                //fout.write(getImgBytes(a.img));

                int quality = 50;
                a.img.compress(Bitmap.CompressFormat.JPEG, quality, fout);
                fout.flush();
                fout.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                try {
                    if (fout != null) fout.close();
                }
                catch(Exception e){}

                a.img.recycle();
            }

            values.put(Bookmark.IMG_FIELD, f_name);
        }
        else{
            values.put(Bookmark.IMG_FIELD, "");
        }


        db.insert(Bookmark.BOOKMARK_TABLE_NAME, null, values);
    }



    public void updateBookmark(Context context, Bookmark a){
        // Gets the data repository in write mode
        SQLiteDatabase db = JinDbHelper.getInstance(context).getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Bookmark.TITLE_FIELD, a.title);
        values.put(Bookmark.URL_FIELD, a.url);

        if(a.img != null) {
            String f_name = ""+System.nanoTime();
            File file = new File(jinActivity.getFilesDir(), f_name);
            FileOutputStream fout = null;

            try {
                fout = new FileOutputStream(file);
                //fout.write(getImgBytes(a.img));

                int quality = 50;
                a.img.compress(Bitmap.CompressFormat.JPEG, quality, fout);
                fout.flush();
                fout.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                try {
                    if (fout != null) fout.close();
                }
                catch(Exception e){}

                a.img.recycle();
            }

            values.put(Bookmark.IMG_FIELD, f_name);
        }
        else{
            values.put(Bookmark.IMG_FIELD, "");
        }


        String selection = Bookmark.OPTION_FIELD + " = ? and "+Bookmark.URL_FIELD+" = ?";
        String[] selectionArgs = { ""+Bookmark.BOOKMAKR_URL, a.url};

        int updateCnt = db.update(Bookmark.BOOKMARK_TABLE_NAME, values, selection, selectionArgs);
    }

    public ArrayList<Bookmark> selectBookmark(Context context, String select, String[] selectorArgs, boolean load_bookmark_img){
        ArrayList<Bookmark> result = new ArrayList<Bookmark>();

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = JinDbHelper.getInstance(context).getReadableDatabase();

            String query = "select " + Bookmark.ID_FIELD+", "+ Bookmark.TITLE_FIELD + ","
                    + Bookmark.URL_FIELD + ","+Bookmark.OPTION_FIELD +", "+Bookmark.DATE_FIELD+", "+Bookmark.IMG_FIELD+", " +
                    " (datetime('now','localtime')), (datetime('now','localtime', '-30 seconds'))  "
                    +" from " + Bookmark.BOOKMARK_TABLE_NAME;
            //+" from " + Bookmark.BOOKMARK_TABLE_NAME +" group by "+Bookmark.URL_FIELD;

            //String query = "select * from "+TalkAddress.TALK_ADDRESS_TABLE_NAME;

            if(select != null){
                query += " where "+select;
            }

            query +=  " group by "+Bookmark.URL_FIELD;
            //String order_by = " order by "+ Bookmark.ID_FIELD +" ASC, "+Bookmark.OPTION_FIELD + " DESC ";
            //String order_by = " order by "+Bookmark.ID_FIELD + " DESC, "+Bookmark.OPTION_FIELD +" DESC ";
            //String order_by = " order by "+Bookmark.OPTION_FIELD + " DESC, "+Bookmark.ID_FIELD +" DESC ";
            String order_by = " order by "+Bookmark.ID_FIELD +" DESC ";
            query += order_by;



            cursor = db.rawQuery(query, selectorArgs);

            while(cursor.moveToNext()){
                //cursor.moveToNext();
                int i = 0;
                int id = cursor.getInt(i++);
                //System.out.println("id : "+id);
                String title = cursor.getString(i++);
                String url = cursor.getString(i++);
                int opt = cursor.getInt(i++);
                String date_time = cursor.getString(i++);
                //byte[] img_bytes = cursor.getBlob(i++);
                String img_name = cursor.getString(i++);

                String old_date_time_1 = cursor.getString(i++);
                String old_date_time_2 = cursor.getString(i++);

                //System.out.println("DateStr : "+old_date_time_1);
                //System.out.println("DateStr : "+old_date_time_2);

                Bitmap bookmark_img = null;

                if(load_bookmark_img){
                    bookmark_img = getImg(img_name);
                }

                Bookmark bookmark = new Bookmark(
                        id,
                        title,
                        url,
                        opt,
                        date_time,
                        bookmark_img);

                result.add(bookmark);
            }
            //c.close();
            //db.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(cursor != null) cursor.close();
            //if(db != null) db.close();
        }

        return result;
    }


    public int deleteBookmark(Context context, String where, String[] selectionArgs){
        SQLiteDatabase db = null;
        int res = 0;

        try {
            db = JinDbHelper.getInstance(context).getReadableDatabase();

            res = db.delete(Bookmark.BOOKMARK_TABLE_NAME, where, selectionArgs);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            //if(db != null) db.close();
        }

        return res;
    }

}
