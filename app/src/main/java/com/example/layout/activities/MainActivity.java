package com.example.layout.activities;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.layout.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //Global Values to be changed by user/unit
    int tar_temp = 22; // Default Target Temperature
    int recording = 0;
    int REQUEST_CODE;
    boolean isSuccess = true;
    boolean isCelsius = true;
    boolean showNotifications = true;
    boolean refreshing = false;
    String temp_a = "";
    String temp_b = "";
    String temp_c = "";
    String temp_d = "";
    String temp_e = "";
    String temp_f = "";
    String error_1 = "";
    String error_2 = "";
    String error_3 = "";
    String error_4 = "";
    String error_5 = "";

    TextView tartemp;

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    SharedPreferences SP;
    Handler handler;
    TimerTask task;
    Timer timer;
    RequestQueue queue;
    AlertDialog alertDialog;
    StringBuilder sb;
    final String url = "http://k9base.ddns.net/";
    String record_url = "http://k9base.ddns.net/record.php";
    //Temp Keys
    static final String KEY_TEMP_A = "temp_a";
    static final String KEY_TEMP_B = "temp_b";
    static final String KEY_TEMP_C = "temp_c";
    static final String KEY_TEMP_D = "temp_d";
    static final String KEY_TEMP_E = "temp_e";
    static final String KEY_TEMP_F = "temp_f";


    static final String JSON_ARRAY = "result";
    //Error Keys
    static final String KEY_ERROR_1 = "error_1";
    static final String KEY_ERROR_2 = "error_2";
    static final String KEY_ERROR_3 = "error_3";
    static final String KEY_ERROR_4 = "error_4";
    static final String KEY_ERROR_5 = "error_5";

    //make sliding mail that switches between gps location weather pane and temp control pane
    //Launches Main Screen
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //Init Preferences
        SP = PreferenceManager.getDefaultSharedPreferences(this);
        showNotifications = SP.getBoolean("error_notifications", true);
        //Init Elements
        handler = new Handler();
        timer = new Timer();
        queue = Volley.newRequestQueue(this);
        //Init Buttons and Views
        tartemp = (TextView) findViewById(R.id.tartempview);
        Button toTempControl = (Button) findViewById(R.id.tempchangebutton);
        Button toVideoStream = (Button) findViewById(R.id.imageButton2);
        final Button refresh = (Button) findViewById(R.id.imageButton3);
        ImageButton toSettings = (ImageButton) findViewById(R.id.Settings);
        final TextView temp_1 = (TextView) findViewById(R.id.sensor_value_1);
        final TextView temp_2 = (TextView) findViewById(R.id.sensor_value_2);
        final TextView temp_3 = (TextView) findViewById(R.id.sensor_value_3);
        final TextView temp_4 = (TextView) findViewById(R.id.sensor_value_4);
        final TextView temp_5 = (TextView) findViewById(R.id.sensor_value_5);
        final TextView temp_6 = (TextView) findViewById(R.id.sensor_value_6);


        //cast tartemp to textview
        isCelsius = !SP.getBoolean("display_setting", false);
        if (isCelsius) {
            tartemp.setText(getString(R.string.update_temp_C, Integer.toString(tar_temp)));
        } else {
            tartemp.setText(getString(R.string.update_temp_F, CtoF(Integer.toString(tar_temp))));
        }
        if (SP.getBoolean("video_stream", true)) {
            recording = 1;
        } else {
            recording = 0;
        }
        //Listener for Temp Control
        toTempControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Launch Temp Control
                Intent intent = new Intent(MainActivity.this, TempControl.class);
                REQUEST_CODE = 123;
                intent.putExtra("Tar_Temp", tar_temp);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //Listener for Video Stream
        toVideoStream.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VideoStream.class);
                startActivity(intent);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (refreshing) {
                    task.cancel();
                    refresh.getBackground().clearColorFilter();
                    refresh.clearAnimation();
                    refreshing = false;
                    sendNotification(view);
                    //TODO isresponding value in mysql for video
                    return;
                }
                final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                animation.setDuration(1000); // duration - one second
                animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
                refresh.getBackground().setColorFilter(Color.rgb(164, 198, 57), PorterDuff.Mode.MULTIPLY);
                refresh.startAnimation(animation);
                refreshing = true;

                //check if value has changed
                if (recording==1 && !SP.getBoolean("video_stream", true)) {
                    stopRecording();
                    recording = 0;
                }
                if (recording==0 && SP.getBoolean("video_stream", true)) {
                    startRecording();
                    recording = 1;
                }

                //TODO isresponding value in mysql for video
                sendNotification(view);

                task = new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (recording == 0) {
                                    StringRequest recordRequest = new StringRequest(com.android.volley.Request.Method.GET, record_url + "t=" + recording,
                                            new com.android.volley.Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                }
                                            },
                                            new com.android.volley.Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    // error
                                                    Log.d("Error.Response", error.toString());
                                                }
                                            }
                                    );
                                    queue.add(recordRequest);
                                }
                                StringRequest valuerequest = new StringRequest(url + "Get_Values.php", new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        sensorJSON(response);
                                        isCelsius = !SP.getBoolean("display_setting", false);
                                        //Convert to F if specified in settings
                                        if (isCelsius) {
                                            temp_1.setText(getString(R.string.update_temp_C, temp_a));
                                            temp_2.setText(getString(R.string.update_temp_C, temp_b));
                                            temp_3.setText(getString(R.string.update_temp_C, temp_c));
                                            temp_4.setText(getString(R.string.update_temp_C, temp_d));
                                            temp_5.setText(getString(R.string.update_temp_C, temp_e));
                                            temp_6.setText(getString(R.string.update_temp_C, temp_f));
                                            tartemp.setText(getString(R.string.update_temp_C, Integer.toString(tar_temp)));
                                        } else {
                                            temp_1.setText(getString(R.string.update_temp_F, CtoF(temp_a)));
                                            temp_2.setText(getString(R.string.update_temp_F, CtoF(temp_b)));
                                            temp_3.setText(getString(R.string.update_temp_F, CtoF(temp_c)));
                                            temp_4.setText(getString(R.string.update_temp_F, CtoF(temp_d)));
                                            temp_5.setText(getString(R.string.update_temp_F, CtoF(temp_e)));
                                            temp_6.setText(getString(R.string.update_temp_F, CtoF(temp_f)));
                                            tartemp.setText(getString(R.string.update_temp_F, CtoF(Integer.toString(tar_temp))));

                                        }

                                    }
                                },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.d("connection error", error.getMessage());
                                            }
                                        });
                                queue.add(valuerequest);

                                StringRequest errorrequest = new StringRequest(url + "Get_Errors.php", new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        errorJSON(response);
                                        if (SP.getBoolean("error_notifications", true)) {
                                            //TODO Update error codes
                                            if ((Integer.parseInt(error_1) == 1 || Integer.parseInt(error_2) == 1 || Integer.parseInt(error_3) == 1 || Integer.parseInt(error_4) == 1 || Integer.parseInt(error_5) == 1)) {
                                                sb = new StringBuilder();
                                                if (Integer.parseInt(error_1) == 1) {
                                                    AlertMessageBuilder("Fan 1 Failed, check unit");
                                                }
                                                if (Integer.parseInt(error_2) == 1) {
                                                    AlertMessageBuilder("Fan 2 Failed, check unit");
                                                }
                                                if (Integer.parseInt(error_3) == 1) {
                                                    AlertMessageBuilder("Fan 3 Failed, check unit");
                                                }
                                                if (Integer.parseInt(error_4) == 1) {
                                                    AlertMessageBuilder("Fan 4 Failed, check unit");
                                                }
                                                if (Integer.parseInt(error_5) == 1) {
                                                    AlertMessageBuilder("Compressor Failed, check unit");
                                                }
                                                AlertShow();
                                            }
                                        }
                                    }
                                },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.d("connection error", error.getMessage());
                                            }
                                        });
                                queue.add(errorrequest);
                            }
                        });
                    }
                };

                timer.schedule(task,0,1000);
                }
            });


        toSettings.setOnClickListener(new View.OnClickListener()

    {
        public void onClick (View view){
        Intent intent = new Intent(MainActivity.this, MyPreferenceActivity.class);
        intent.putExtra("isCelsius", isCelsius);
        REQUEST_CODE = 321;
        startActivityForResult(intent, REQUEST_CODE);
    }
    });
}

    // Recieve tartemp setting from user and change value
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data.hasExtra("returnKey1")) {
                tar_temp = Integer.parseInt(data.getExtras().getString("returnKey1"));
                //   [SEND REQUEST TO SQL TO REWRITE TEMP ON SERVER SIDE ]

                StringRequest getRequest = new StringRequest(com.android.volley.Request.Method.GET, url + "New_Temp.php?target=" + tar_temp,
                        new com.android.volley.Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // response
                                isCelsius = !SP.getBoolean("display_setting", false);
                                if (isCelsius) {
                                    tartemp.setText(getString(R.string.update_temp_C, Integer.toString(tar_temp)));
                                } else {
                                    tartemp.setText(getString(R.string.update_temp_F, CtoF(Integer.toString(tar_temp))));
                                }
                                Log.d("Response", response);
                            }
                        },
                        new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                isSuccess = false;
                                Log.d("Error.Response", error.toString());
                            }
                        }
                );
                queue.add(getRequest);
            }
        }
    }

    public void sendNotification(View view) {
        if (refreshing) {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.k9kooler_logo)
                            .setContentTitle("K9KOOLER")
                            .setContentText("Monitoring Unit")
                            .setContentIntent(contentIntent);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        } else {
            mNotificationManager.cancel(1);
        }
    }

    //build notification
    protected void AlertMessageBuilder(String error_text) {
        //Build Error Message
        sb.append(error_text + "\n");
    }

    // show notification
    protected void AlertShow() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Attention:");

        // set dialog message
        alertDialogBuilder
                .setMessage(sb.toString())
                .setCancelable(false)
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private String CtoF(String string) {

        return new DecimalFormat("##.#").format((Double.valueOf(string)) * (1.8) + 32);

    }

    private void sensorJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(JSON_ARRAY);
            JSONObject sensorData = result.getJSONObject(0);
            temp_a = sensorData.getString(KEY_TEMP_A);
            temp_b = sensorData.getString(KEY_TEMP_B);
            temp_c = sensorData.getString(KEY_TEMP_C);
            temp_d = sensorData.getString(KEY_TEMP_D);
            temp_e = sensorData.getString(KEY_TEMP_E);
            temp_f = sensorData.getString(KEY_TEMP_F);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void errorJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(JSON_ARRAY);
            JSONObject errorData = result.getJSONObject(0);
            error_1 = errorData.getString(KEY_ERROR_1);
            error_2 = errorData.getString(KEY_ERROR_2);
            error_3 = errorData.getString(KEY_ERROR_3);
            error_4 = errorData.getString(KEY_ERROR_4);
            error_5 = errorData.getString(KEY_ERROR_5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void startRecording() {
        StringRequest recordRequest = new StringRequest(com.android.volley.Request.Method.GET, record_url + "t=" + 1,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("recording", "started");
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        queue.add(recordRequest);
    }
    private void stopRecording() {
        StringRequest recordRequest = new StringRequest(com.android.volley.Request.Method.GET, record_url + "t=" + 0,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("recording", "stopped");
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        queue.add(recordRequest);
    }
}