package com.jin.browser.activity.util;

/**
 * Created by kwy on 2018-01-24.
 */

/*
 * Copyright 2014 A.C.R. Development
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class Utils {

    private static final String TAG = "Utils";

    public static boolean doesSupportHeaders() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * Creates a new intent that can launch the email
     * app with a subject, address, body, and cc. It
     * is used to handle mail:to links.
     *
     * @param address the address to send the email to.
     * @param subject the subject of the email.
     * @param body    the body of the email.
     * @param cc      extra addresses to CC.
     * @return a valid intent.
     */
    @NonNull
    public static Intent newEmailIntent(String address, String subject,
                                        String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }




    /**
     * Shows a toast to the user.
     * Should only be used if an activity is
     * not available to show a snackbar.
     *
     * @param context  the context needed to show the toast.
     * @param resource the string shown by the toast to the user.
     */
    public static void showToast(@NonNull Context context, @StringRes int resource) {
        Toast.makeText(context, resource, Toast.LENGTH_SHORT).show();
    }

    /**
     * Converts Density Pixels (DP) to Pixels (PX).
     *
     * @param dp the number of density pixels to convert.
     * @return the number of pixels that the conversion generates.
     */
    public static int dpToPx(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (dp * metrics.density + 0.5f);
    }




    public static void trimCache(@NonNull Context context) {
        try {
            File dir = context.getCacheDir();

            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception ignored) {

        }
    }

    private static boolean deleteDir(@Nullable File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir != null && dir.delete();
    }

    /**
     * Creates and returns a new favicon which is the same as the provided
     * favicon but with horizontal or vertical padding of 4dp
     *
     * @param bitmap is the bitmap to pad.
     * @return the padded bitmap.
     */
    @NonNull
    public static Bitmap padFavicon(@NonNull Bitmap bitmap) {
        int padding = Utils.dpToPx(4);

        Bitmap paddedBitmap = Bitmap.createBitmap(bitmap.getWidth() + padding, bitmap.getHeight()
                + padding, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0x00, 0x00, 0x00, 0x00); // this represents white color
        canvas.drawBitmap(bitmap, padding / 2, padding / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return paddedBitmap;
    }

    public static boolean isColorTooDark(int color) {
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        //final byte BLUE_CHANNEL = 0;

        int r = ((int) ((float) (color >> RED_CHANNEL & 0xff) * 0.3f)) & 0xff;
        int g = ((int) ((float) (color >> GREEN_CHANNEL & 0xff) * 0.59)) & 0xff;
        int b = ((int) ((float) (color /* >> BLUE_CHANNEL */ & 0xff) * 0.11)) & 0xff;
        int gr = (r + g + b) & 0xff;
        int gray = gr /* << BLUE_CHANNEL */ + (gr << GREEN_CHANNEL) + (gr << RED_CHANNEL);

        return gray < 0x727272;
    }

    public static int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        //final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) + ((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) + ((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) + ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return 0xff << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b;
    }

    @SuppressLint("SimpleDateFormat")
    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + '_';
        File storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
    }

    /**
     * Checks if flash player is installed
     *
     * @param context the context needed to obtain the PackageManager
     * @return true if flash is installed, false otherwise
     */
    public static boolean isFlashInstalled(@NonNull Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo("com.adobe.flashplayer", 0);
            if (ai != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * Quietly closes a closeable object like an InputStream or OutputStream without
     * throwing any errors or requiring you do do any checks.
     *
     * @param closeable the object to close
     */
    public static void close(@Nullable Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to close cursors. Cursor did not
     * implement Closeable until API 16, so using this
     * method for when we want to close a cursor.
     *
     * @param cursor the cursor to close
     */
    public static void close(@Nullable Cursor cursor) {
        if (cursor == null) {
            return;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws the trapezoid background for the horizontal tabs on a canvas object using
     * the specified color.
     *
     * @param canvas the canvas to draw upon
     * @param color  the color to use to draw the tab
     */
    public static void drawTrapezoid(@NonNull Canvas canvas, int color, boolean withShader) {

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
//        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);
        paint.setDither(true);
        if (withShader) {
            paint.setShader(new LinearGradient(0, 0.9f * canvas.getHeight(),
                    0, canvas.getHeight(),
                    color, mixTwoColors(Color.BLACK, color, 0.5f),
                    Shader.TileMode.CLAMP));
        } else {
            paint.setShader(null);
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        double radians = Math.PI / 3;
        int base = (int) (height / Math.tan(radians));

        Path wallpath = new Path();
        wallpath.reset();
        wallpath.moveTo(0, height);
        wallpath.lineTo(width, height);
        wallpath.lineTo(width - base, 0);
        wallpath.lineTo(base, 0);
        wallpath.close();

        canvas.drawPath(wallpath, paint);
    }


    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Nullable
    public static String guessFileExtension(@NonNull String filename) {
        int lastIndex = filename.lastIndexOf('.') + 1;
        if (lastIndex > 0 && filename.length() > lastIndex) {
            return filename.substring(lastIndex, filename.length());
        }
        return null;
    }



    public static Bitmap decodeSampledBitmapFromResource(byte[] res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(res, 0, res.length);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(res, 0, res.length);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static int getSoundMinBufSize(int sample_rate, int min_buffer_size){

        if(min_buffer_size < 0) {
        //if(false){
            int minBufSize = 640;
            if(sample_rate == 8000){//8000
                minBufSize = 640;
            }
            else if(sample_rate == 16000){//16000
                minBufSize = 1280;
            }
            else if(sample_rate == 44100){//44100
                minBufSize = 3584;
            }
            return (minBufSize*2);
        }

        /*
        int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
        int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

        int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
        if (buffer_size < min_buffer_size) {
            buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;
        }

        return buffer_size;
        */

        return (min_buffer_size*2);
    }


    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

}
