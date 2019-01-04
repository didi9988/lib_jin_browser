package com.jin.browser.activity.util;

/**
 * Created by kwy on 2018-01-24.
 */

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import com.jin.browser.R;
import com.jin.browser.config.AppDefine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class DrawableUtils {

    @NonNull
    public static Bitmap getRoundedNumberImage(int number, int width, int height, int color, int thickness) {
        String text;

        if (number > 99) {
            text = "\u221E";
        } else {
            text = String.valueOf(number);
        }

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(color);
        Typeface boldText = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        paint.setTypeface(boldText);
        paint.setTextSize(Utils.dpToPx(14));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        int radius = Utils.dpToPx(2);

        RectF outer = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRoundRect(outer, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        radius--;
        RectF inner = new RectF(thickness, thickness, canvas.getWidth() - thickness, canvas.getHeight() - thickness);
        canvas.drawRoundRect(inner, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

        canvas.drawText(String.valueOf(text), xPos, yPos, paint);

        return image;
    }


    public static Animation startAnimation(Context context, View view, Animation.AnimationListener listener){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        animation.setAnimationListener(listener);
        view.setAnimation(animation);

        view.startAnimation(animation);

        return animation;
    }


    public static Bitmap getRoundedLetterImage(@NonNull Character character, int width, int height, String color) {
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(color));
        Typeface boldText = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        paint.setTypeface(boldText);
        paint.setTextSize(Utils.dpToPx(8));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        int radius = Utils.dpToPx(2);

        RectF outer = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRoundRect(outer, radius, radius, paint);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));

        paint.setColor(Color.WHITE);
        canvas.drawText(character.toString(), xPos, yPos, paint);

        return image;
    }

    /**
     * Hashes a character to one of four colors:
     * blue, green, red, or orange.
     *
     * @param character the character to hash.
     * @param app       the application needed to get the color.
     * @return one of the above colors, or black something goes wrong.
     */
    @ColorInt
    public static int characterToColorHash(@NonNull Character character, @NonNull Application app) {
        int smallHash = Character.getNumericValue(character) % 4;

        /*
        switch (Math.abs(smallHash)) {
            case 0:
                return ContextCompat.getColor(app, R.color.bookmark_default_blue);
            case 1:
                return ContextCompat.getColor(app, R.color.bookmark_default_green);
            case 2:
                return ContextCompat.getColor(app, R.color.bookmark_default_red);
            case 3:
                return ContextCompat.getColor(app, R.color.bookmark_default_orange);
            default:
                return Color.BLACK;
        }
        */

        return Color.BLACK;
    }


    public static int mixColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        return (startA + (int) (fraction * (endA - startA))) << 24 |
                (startR + (int) (fraction * (endR - startR))) << 16 |
                (startG + (int) (fraction * (endG - startG))) << 8 |
                (startB + (int) (fraction * (endB - startB)));
    }

    public static void setBackground(@NonNull View view, @Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            //noinspection deprecation
            view.setBackgroundDrawable(drawable);
        }
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = null;

        try {
            int src_left = 0;
            int src_top = 0;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                src_left = (bitmap.getWidth() - bitmap.getHeight()) / 2;
            } else {
                output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
                src_top = (bitmap.getHeight() - bitmap.getWidth()) / 2;
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final Rect src = new Rect(src_left, src_top, bitmap.getWidth(), bitmap.getHeight());

            float r = 0;

            if (bitmap.getWidth() > bitmap.getHeight()) {
                r = bitmap.getHeight() / 2;
            } else {
                r = bitmap.getWidth() / 2;
            }

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(r, r, r, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, rect, paint);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return output;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, int rotate) throws Exception{

        Bitmap resizedBitmap = null;

        int width = bm.getWidth();
        int height = bm.getHeight();

        if(width <= 0 || height <= 0) return null;


        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bit map
        //matrix.postScale(scaleWidth, scaleHeight);
        matrix.postScale(scaleWidth, scaleHeight);

        if(rotate != 0) {
            matrix.postRotate(rotate);
        }

        // Recreate the new Bitmap //RGB_565
        //resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        bm.recycle();

        return resizedBitmap;
    }

    public static Bitmap getGrayscale(Bitmap src){
        float[] matrix = new float[]{
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0,};

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }

    public static int getRotatedAngle(String path){
        int result = 0;

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);


        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                result = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                result = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                result = 270;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }

        return result;
    }

    public static Bitmap rotatedImage(String path) {
        Bitmap bmap;
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        bmap = BitmapFactory.decodeFile(path, options);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bmap = rotateImage(bmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                bmap = rotateImage(bmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                bmap = rotateImage(bmap, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        return bmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }


    public static String toBase64(Bitmap img){
        String result = null;
        ByteArrayOutputStream stream = null;
        try {
            stream = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            result = Base64.encodeToString(byteArray, 0);
            //scaledBitmap.recycle();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            try{
                if(stream != null) stream.close();
            }
            catch (Exception e){}
        }

        return result;
    }

    public static Bitmap getMyProfileImage(String profile_img_path, boolean isThumnail){
        Bitmap scaledBitmap = null;

        if(profile_img_path != null){
            File f = new File(profile_img_path);
            if(f.exists()){
                Bitmap profile_img = DrawableUtils.rotatedImage(f.getAbsolutePath());
                scaledBitmap = getMyProfileImage(profile_img, isThumnail);
            }
        }

        return scaledBitmap;
    }


    public static Bitmap cloneBitmap(Bitmap src){
        Bitmap clone = null;

        if(src != null) {
            clone = src.copy(src.getConfig(), true);
        }

        return clone;
    }

    public static Bitmap getMyProfileImage(Bitmap profile_img, boolean isThumnail){
        Bitmap scaledBitmap = null;
        try{
            if(profile_img != null){
                int width = profile_img.getWidth();
                int height = profile_img.getHeight();

                if(isThumnail){
                    width = (int) ((float) profile_img.getWidth() * AppDefine.PROFILE_THUM_IMG_HEIGHT / (float) profile_img.getHeight());
                    height = AppDefine.PROFILE_THUM_IMG_HEIGHT;
                }
                else {
                    if (width > AppDefine.PROFILE_IMG_MAX_WIDTH) {
                        height = (int) ((float) profile_img.getHeight() * AppDefine.PROFILE_IMG_MAX_WIDTH / (float) profile_img.getWidth());
                        width = AppDefine.PROFILE_IMG_MAX_WIDTH;
                    }
                }

                scaledBitmap = Bitmap.createScaledBitmap(
                        profile_img,
                        width,
                        height,
                        false);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

}
