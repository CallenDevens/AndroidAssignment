package com.example.aya.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by aya on 15-9-16.
 */
public class SDFileUtils {

    private static String sdRootPath = Environment.getExternalStorageDirectory().getPath();
    private final static String FOLDER = "/MoivesApp";

    private boolean isExistSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    public SDFileUtils(Context c){
         if(!isExistSDCard()){
             Log.d("SD", "SD dooes not exist!");
         }
    }

    /**
     * get SD card Directory
     * @return
     */
    private String getSDDirectory (){
        return sdRootPath + FOLDER;
    }

    /**
     * write Bit map to SD Directory
     * @param fileName
     * @param bitmap
     * @throws IOException
     */
    public void saveBitmapToSD(String fileName, Bitmap bitmap) throws IOException{
        fileName = fileName.replaceAll("\\s+", "_");
        fileName += ".jpg";
        if(bitmap != null){
            String path = getSDDirectory();
            File file = new File(path);
            if(!file.exists()){
                file.mkdir();
            }
            File f = new File(path + File.separator + fileName);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            /*
            File dirFile = new File(getSDDirectory());
            String []children = dirFile.list();
            for(int i = 0; i < children.length; i++){
                Log.d("FILENAME", children[i]+" "+ children.length);
            }
            */
        }
    }

    /**
     * delete a piciture, making room for new Cache
     */
    public void deleteFile() {
        File dirFile = new File(getSDDirectory());
        if(! dirFile.exists()){
            return;
        }
        if (dirFile.isDirectory()) {
            File [] files = dirFile.listFiles();
            Arrays.sort(files, new Comparator() {

                public int compare(Object o1, Object o2) {
                    if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                        return -1;
                    } else if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                        return +1;
                    } else {
                        return 0;
                    }
                }
            });

//            Log.d("TEST", files[0].getName() + children.length);
            if(files.length >= 1){
                files[0].delete();
            }
        }
    }
    /**
     * delete saved file when picture num exceeds limit
     */
    /*
    public void deleteFiles() {
        File dirFile = new File(getSDDirectory());
        if(! dirFile.exists()){
            return;
        }
        if (dirFile.isDirectory()) {
            String[] children = dirFile.list();
            for (int i = 0; i < children.length; i++) {
                new File(dirFile, children[i]).delete();
            }
        }
        dirFile.delete();
    }
    */

    public boolean isExceedLimit(){
        File dirfile = new File(getSDDirectory());
        if(!dirfile.exists()){
            return true;
        }
        else {
            return dirfile.list().length >=10;
        }
    }
    /**
     * get saved bitmap from SD card
     * @param fileName
     * @return
     */
    public Bitmap getBitmapFromSD(String fileName){
        return BitmapFactory.decodeFile(getSDDirectory() + File.separator + fileName);
    }

    /**
     *
     * @param fileName
     * @return
     */
    public boolean isFileExists(String fileName){
        return new File(getSDDirectory() + File.separator + fileName).exists();
    }

    /**
     *
     * @param fileName
     * @return
     */
    public long getFileSize(String fileName) {
        return new File(getSDDirectory() + File.separator + fileName).length();
    }
}
