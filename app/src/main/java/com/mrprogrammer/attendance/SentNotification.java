package com.mrprogrammer.attendance;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;

public class SentNotification extends AsyncTask<String, String, String> {
    String appName, message, to, firebaseKey;
    String endpoint = "https://fcm.googleapis.com/fcm/send";

    public SentNotification(String token, String appName, String message) {
        this.appName = appName;
        this.message = message;
        this.to = "admin";
        this.firebaseKey = "AAAAl8xAsuM:APA91bEjGVILKSxFyO_Vt77LNZHssFAFCY4_RnRZi3t5sepRQt1LNTPyTzp8t0QnQN-ELr2qql_sTqlw0SKe0R8moaf5xkdqq1biqYusbmctqYqhgrL5o_FaDDXLAGwEbFTkjHRTjaOR";
    }


    @Override
    protected String doInBackground(String... strings) {
       try {
           URL url = new URL(endpoint);
           HttpsURLConnection httpsURLConnection  = (HttpsURLConnection) url.openConnection();

           httpsURLConnection.setReadTimeout(10000);
           httpsURLConnection.setConnectTimeout(15000);
           httpsURLConnection.setRequestMethod("POST");
           httpsURLConnection.setDoInput(true);
           httpsURLConnection.setDoOutput(true);

           // Adding the necessary headers
           httpsURLConnection.setRequestProperty("authorization", "key="+firebaseKey);
           httpsURLConnection.setRequestProperty("Content-Type", "application/json");

           // Creating the JSON with post params
           JSONObject body = new JSONObject();
           JSONObject data = new JSONObject();
           data.put("title", appName);
           data.put("body", message);
           body.put("notification",data);
           body.put("to","/topics/"+to);

           OutputStream outputStream =new  BufferedOutputStream(httpsURLConnection.getOutputStream());
           BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
           writer.write(body.toString());
           writer.flush();
           writer.close();
           outputStream.close();

           int responseCode = httpsURLConnection.getResponseCode();
           String responseMessage = httpsURLConnection.getResponseMessage();

           Log.d("Response:", responseCode + " -> "+responseMessage);
           String result = new String();
           InputStream inputStream = null;

           if(responseCode >= 400 && responseCode <=499){
               inputStream = httpsURLConnection.getErrorStream();
           }else{
               inputStream = httpsURLConnection.getInputStream();
           }

       }catch (Exception e){
          e.printStackTrace();
       }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
