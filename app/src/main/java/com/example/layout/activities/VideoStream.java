package com.example.layout.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.layout.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jwcam on 1/9/2017.
 */

public class VideoStream extends AppCompatActivity {
    //Declare URL target
    ProgressDialog pDialog;
    VideoView videoview;
    String get_url = "http://k9base.ddns.net/get_video.php";
    static final String JSON_ARRAY = "result";
    static final String KEY_FILE = "file";
    String blobString = "";
    byte[] videoByte;
    File outputFile;
    String filename;
    String filepath;
    int recordpreference;


    //Launch Videoview activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);
        Intent intent = getIntent();
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_video_stream);
        RequestQueue queue = Volley.newRequestQueue(this);
        videoview = (VideoView) findViewById(R.id.videoView);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        //set progress bar title
        pDialog = new ProgressDialog(VideoStream.this);
        pDialog.setTitle("Loading Video");
        // Set progressbar message
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        // Show progressbar
        pDialog.show();

        StringRequest videorequest = new StringRequest(get_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                videoJSON(response);
                try {

                    MediaController mediaController = new MediaController(VideoStream.this);
                    mediaController.setAnchorView(videoview);
                    Uri video = Uri.parse(filepath);
                    videoview.setVideoURI(Uri.parse(filepath));


                    if (video != null) {
                        pDialog.dismiss();
                        Log.d("Video", "Playing");
                        videoview.start();
                    }
                    Log.d("File Exists", Boolean.toString(outputFile.exists()));
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Error", e.toString());
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       // onDestroy();
                        Log.d("error", "you suck");
                        pDialog.dismiss();
                        cancelVideo();
                    }
                });

        videorequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(videorequest);
        Log.d("Volley", "Added");

        Button cancelButton = (Button) findViewById(R.id.cancel_video);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cancelVideo();
            }
        });
    }

    private void cancelVideo() {
        super.finish();
    }

    private void videoJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(JSON_ARRAY);
            JSONObject videoData = result.getJSONObject(0);
            blobString = videoData.getString(KEY_FILE);
            videoByte = Base64.decode(blobString, Base64.DEFAULT);
            Log.d("Byte array", "constructed");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            outputFile = File.createTempFile("file", "mp4", null);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(videoByte);
            Log.d("File", outputFile.getPath());
            outputFile.deleteOnExit();
            fileOutputStream.close();
            filename = outputFile.getName();
            filepath = outputFile.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

//    protected void onResume() {
//        Log.i("VideoPreview", "Resume");
//        videoview.resume();
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        Log.i("VideoPreview", "Pause");
//        videoview.suspend();
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        Log.i("VideoPreview", "Destroy");
//        videoview.stopPlayback();
//        super.onDestroy();
//    }
}



