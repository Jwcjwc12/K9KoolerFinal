package com.example.layout.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.layout.R;

/**
 * Created by jwcam on 1/9/2017.
 */

public class VideoStream extends AppCompatActivity {
    //Declare URL target
    ProgressDialog pDialog;
    VideoView videoview;
    String VideoURL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
    //Launch Videoview activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);
        Intent intent = getIntent();
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_video_stream);

        videoview = (VideoView) findViewById(R.id.videoView);

        pDialog = new ProgressDialog(VideoStream.this);
        pDialog.setTitle("Video Stream");
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);

        pDialog.show();

        try{
            MediaController mediaController = new MediaController(VideoStream.this);

            mediaController.setAnchorView(videoview);
            Uri video = Uri.parse(VideoURL);
            videoview.setMediaController(mediaController);
            videoview.setVideoURI(video);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        videoview.requestFocus();
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                videoview.start();

            }
        });
}}
