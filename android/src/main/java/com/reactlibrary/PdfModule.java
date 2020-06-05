package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class PdfModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public PdfModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "Pdf";
    }//这个模块的名字

    @ReactMethod//要返回一个方法给RN使用 就要添加@ReactMethod注解
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    private String getFilePathFromContentUri(Uri uri) {
        String filePath;
        if (isExternalStorageDocument(uri)){
            final String docId = DocumentsContract.getDocumentId(uri);
            System.out.println("------docID "+ docId);
            final String[] split = docId.split(":");
            final String type = split[0];
            System.out.println("getPath() docId:" + docId +", split:" + split.length +", type:" + type);
            // This is for checking Main Memory
            if ("primary".equalsIgnoreCase(type)) {
                if (split.length > 1) {
                    filePath =  Environment.getExternalStorageDirectory() +"/" + split[1] +"/";
                } else {
                    filePath =  Environment.getExternalStorageDirectory() +"/";
                }
                // This is for checking SD Card
            } else {
                filePath = "storage" +"/" + docId.replace(":","/");
            }
        } else if (isDownloadsDocument(uri)){
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = reactContext.getContentResolver().query(contentUri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath =  cursor.getString(column_index);
        }else {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            Cursor cursor = reactContext.getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        System.out.println("------path: " + filePath);
        return filePath;
    }

    @ReactMethod
    public void convertPdf(String pdfPath, boolean isUri, Callback callback) throws IOException {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        if (isUri){
            pdfPath = getFilePathFromContentUri(Uri.parse(pdfPath));
        }
        if (pdfPath!=null){
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()){
                try {
                    PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
                    Bitmap bitmap;
                    final int pageCount = renderer.getPageCount();
                    for (int p = 0; p < pageCount; p++){
                        PdfRenderer.Page page = renderer.openPage(p);
                        int width = page.getWidth();
                        int height = page.getHeight();
                        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        Rect r = new Rect(0, 0, width, height);
                        page.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        bitmaps.add(bitmap);
                        // close the page
                        page.close();
                    }
                    renderer.close();
                }catch (Exception e){
                    callback.invoke("error");
                }
            }else {
                callback.invoke("error");
            }
            JSONArray imgs = saveImg(bitmaps, callback);
            if (imgs!=null){
                callback.invoke(imgs.toString());
            }else {
                callback.invoke("error");
            }

        }else {
            callback.invoke("error");
        }
    }

    private JSONArray saveImg(ArrayList<Bitmap> bitmaps, Callback callback){
        File saveDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "snapread_pdf");
        JSONArray Imgarray = new JSONArray();
        for (int i = 0; i<bitmaps.size();i++){
            if (!saveDir.exists()){//文件夹不存在就新建一个
                saveDir.mkdir();
            }
            String pdfImgName = "snapread_" + i + ".jpg";//保存的图片的名称
            File pdfImgFile = new File(saveDir,pdfImgName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(pdfImgFile);
                bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                double imgeH = bitmaps.get(i).getHeight();
                double imgW = bitmaps.get(i).getWidth();
                JSONObject imgInfo = new JSONObject();
                double imgSize = pdfImgFile.length()/1024;
                imgInfo.put("path","file://"+saveDir+ File.separator+pdfImgName);
                imgInfo.put("size",imgSize);
                imgInfo.put("width",imgW);
                imgInfo.put("height",imgeH);
                Imgarray.put(i,imgInfo);
            } catch (FileNotFoundException e) {
                callback.invoke("error");
//                e.printStackTrace();
            } catch (IOException e) {
                callback.invoke("error");
//                e.printStackTrace();
            } catch (JSONException e) {
                callback.invoke("error");
//                e.printStackTrace();
            }

        }
        return Imgarray;
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

}
