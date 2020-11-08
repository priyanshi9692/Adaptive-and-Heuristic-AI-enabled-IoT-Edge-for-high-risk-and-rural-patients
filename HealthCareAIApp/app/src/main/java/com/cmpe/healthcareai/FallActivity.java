package com.cmpe.healthcareai;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FallActivity extends AppCompatActivity {

    SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mGyroscope;
    int sensorDataCount = 500;
    String[] sensorData = new String[sensorDataCount];
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall);

        Toast.makeText(FallActivity.this, "In Fall activity",
                Toast.LENGTH_LONG).show();

        Log.d( "-----","The onCreate() event");

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Log.d( "-----","sensors set");
    }

    public void onResume() {
        Log.d( "-----","onresume -- sensor to be set");
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(sensorEventListener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d( "-----","sensors event list registered");
        count =0;
    }

    public void onStop() {
        Log.d( "-----","onStop event");
        super.onStop();
        mSensorManager.unregisterListener(sensorEventListener);
        Log.d( "-----","sensors unregisterListener");
    }

    public SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }
        public void onSensorChanged(SensorEvent event) {
            if(count ==sensorDataCount){
                mSensorManager.unregisterListener(sensorEventListener);
                Log.d( "-----","sensors unregisterListener as sensorDataCount records read");

                //calculate features, then call model

            }
            else {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                String sensorName = event.sensor.getName();
                if(sensorName.toLowerCase().contains("gyroscope"))
                    sensorName = "gyroscope";
                else
                    sensorName = "accelerometer";
                String sData = sensorName+" "+event.timestamp+" "+x+" "+y+" "+z;
                Log.d("-----", "___"+count+"__"+sData);
                sensorData[count] = sData;
                count=count+1;
            }
        }
    };
}