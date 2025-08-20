package com.wastesmart.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUploadUtil {
    
    private static final String TAG = "ImageUploadUtil";
    // Free ImgBB API key - get yours from https://api.imgbb.com/
    private static final String IMGBB_API_KEY = "d919e432d9b9735ba23b2ecffbe75852"; // Your ImgBB API key
    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Convert URI to Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                
                // Compress bitmap
                bitmap = compressBitmap(bitmap);
                
                // Convert to Base64
                String base64Image = bitmapToBase64(bitmap);
                
                // Upload to ImgBB
                String response = uploadToImgBB(base64Image);
                
                // Parse response
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    String imageUrl = jsonResponse.getJSONObject("data").getString("url");
                    Log.d(TAG, "Image uploaded successfully");
                    callback.onSuccess(imageUrl);
                } else {
                    String error = jsonResponse.optString("error", "Unknown error");
                    Log.e(TAG, "ImgBB API error: " + error);
                    callback.onError("Upload failed: " + error);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                callback.onError("Upload error: " + e.getMessage());
            }
        });
    }

    private static Bitmap compressBitmap(Bitmap bitmap) {
        // Compress to max 800x800 pixels to reduce file size
        int maxSize = 800;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        
        if (ratio < 1) {
            width = Math.round(ratio * width);
            height = Math.round(ratio * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        
        return bitmap;
    }

    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private static String uploadToImgBB(String base64Image) throws IOException {
        URL url = new URL(IMGBB_UPLOAD_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Set up POST request
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(30000); // 30 seconds
            
            // Prepare data
            String postData = "key=" + URLEncoder.encode(IMGBB_API_KEY, "UTF-8") +
                             "&image=" + URLEncoder.encode(base64Image, "UTF-8");
            
            // Send request
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(postData);
            writer.close();
            
            // Read response
            int responseCode = connection.getResponseCode();
            
            StringBuilder response = new StringBuilder();
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            responseCode >= 200 && responseCode < 300 
                                ? connection.getInputStream() 
                                : connection.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            
            if (responseCode >= 200 && responseCode < 300) {
                return response.toString();
            } else {
                throw new IOException("HTTP " + responseCode + ": " + response.toString());
            }
        } finally {
            connection.disconnect();
        }
    }
}
