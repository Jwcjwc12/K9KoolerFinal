package com.example.layout.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.layout.R;

import java.text.DecimalFormat;


public class TempControl extends AppCompatActivity {
    //Init local values
    int tar_temp;
    Context context;
    CharSequence decreasetext;
    CharSequence increasetext;
    int duration;
    SharedPreferences SP;
    boolean isCelcius;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_control);

        tar_temp = 20;
        context = getApplicationContext();
        decreasetext= "Lower Limit Reached";
        increasetext="Upper Limit Reached";
        duration=Toast.LENGTH_SHORT;
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle extras = getIntent().getExtras();
        int value1 = 0;
        if (extras == null) {
            return;
        }
        value1 = extras.getInt("Tar_Temp");
        if (value1 != 0) {
            tar_temp=value1;
        }


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.75), (int)(height*.75));

        TextView tempdisplay = (TextView) findViewById(R.id.tempview);
        isCelcius = !SP.getBoolean("display_setting", false);
        if (isCelcius) {
            tempdisplay.setText(getString(R.string.update_temp_C, Integer.toString(tar_temp)));
        } else {
            tempdisplay.setText(getString(R.string.update_temp_F, CtoF(Integer.toString(tar_temp))));
        }
    }
    public void cancel(View view){
        super.finish();
    }
    public void display(int number) {
        number = tar_temp;
        TextView displayInteger = (TextView) findViewById(
                R.id.tempview);
        isCelcius = !SP.getBoolean("display_setting", false);
        if (isCelcius) {
            displayInteger.setText(getString(R.string.update_temp_C, Integer.toString(tar_temp)));
        } else {
            displayInteger.setText(getString(R.string.update_temp_F, CtoF(Integer.toString(tar_temp))));
        }

    }
    private String CtoF(String string) {

        return new DecimalFormat("##.#").format((Double.valueOf(string))*(1.8)+32);

    }
    public void increaseTemp(View view) {
        tar_temp = tar_temp + 1;

        if (tar_temp > 25) {
            tar_temp-=1;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, increasetext, duration);
            toast.show();
        } else{
        display(tar_temp);
    }}
    public void decreaseTemp(View view) {
        tar_temp = tar_temp - 1;

        if (tar_temp < 20) {
            tar_temp+=1;
            Toast toast = Toast.makeText(context, decreasetext, duration);
            toast.show();
        } else {
        display(tar_temp);
    }
    }
    public void setTargetTemp (View view) {
        Toast toast;
        if(isCelcius) {
            toast = Toast.makeText(context, "Temperature set to " + tar_temp + " °C", duration);
        } else {
            toast = Toast.makeText(context, "Temperature set to " + CtoF(Integer.toString(tar_temp)) + " °F", duration);
        }
        toast.show();
        Intent i = new Intent();
        String tar_temp_string= Integer.toString(tar_temp);
        i.putExtra("returnKey1", tar_temp_string);
        setResult(RESULT_OK, i);
        super.finish();
    }


}
