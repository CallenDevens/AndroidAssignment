package com.example.aya.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.IccOpenLogicalChannelResponse;
import android.util.Log;
import android.util.LruCache;

import com.google.android.gms.drive.FileUploadPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aya on 15-9-16.
 */
public class ImageLoader {

    private LruCache<String, Bitmap> mMemoryCache;
    private SDFileUtils sdFileUtils;

    public ImageLoader(Context context){
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

        };
        sdFileUtils = new SDFileUtils(context);
    }

    /**
     *
     * @param url
     * @return
     */
    public Bitmap getUrlBitmap(String url) {
        Bitmap PosterBitmap = null;
        try {
            HttpURLConnection connect =  (HttpURLConnection)new URL(url).openConnection();
            connect.setDoInput(true);
            connect.connect();
            InputStream is = connect.getInputStream();
            PosterBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return PosterBitmap;
    }

    /**
     *
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        key.replaceAll("\\s+","_");
        key +=".jpg";
        if(bitmap != null && getBitmapFromMemoryCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    /**
     * get bitmap from cache
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

    public Bitmap getCacheBitmap(String fileName){

        fileName = fileName.replaceAll("\\s","_");
        fileName +=".jpg";
        if(getBitmapFromMemoryCache(fileName) != null){
            return getBitmapFromMemoryCache(fileName);
        }else if(sdFileUtils.isFileExists(fileName) && sdFileUtils.getFileSize(fileName) != 0){
            Bitmap bitmap = sdFileUtils.getBitmapFromSD(fileName);
            addBitmapToMemoryCache(fileName, bitmap);
            return bitmap;
        }
        return null;
    }

    /**
     *
     * @param fileName
     * @param bitmap
     */
    public void saveBitmapToSD(String fileName, Bitmap bitmap){
        if(sdFileUtils.isExceedLimit()){
            sdFileUtils.deleteFile();
        }
        try {
            fileName = fileName.replaceAll("\\s+","_");
            sdFileUtils.saveBitmapToSD(fileName, bitmap);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}