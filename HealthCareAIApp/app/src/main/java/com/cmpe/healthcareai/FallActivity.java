package com.cmpe.healthcareai;

import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FallActivity extends AppCompatActivity {

    SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mGyroscope;
    int sensorDataCount =100;
    String[] sensorData = new String[sensorDataCount];
    int count = 0;
    Interpreter tflite;
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

//        if(!Python.isStarted())
//            Python.start(new AndroidPlatform(this));

        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }
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
              List<List<Float>> result = new ArrayList<>();

                result = calculateFeatures(sensorData);
                int count=0;
                String str = new String();
                for( List<Float> feature: result){
                    for(float f: feature){
                        str += f+" ";
                        count++;
                    }
                }
                Log.d("features:",str);
                doInference(result);

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


    public int doInference(List<List<Float>> features) {
        float[][] inputFeatures = new float[1][features.get(0).size()];
        Log.d("Predicting: ", "Fall or No-Fall");

        for (int j = 0; j < features.get(0).size(); j++) {
            inputFeatures[0][j] = features.get(0).get(j);

   }
        float[][] output = new float[1][1];
            tflite.run(inputFeatures, output);
            float inferredValue = output[0][0];
            Log.d("Value: ", String.valueOf(inferredValue));
            int result = Math.round(inferredValue);
            Log.d("Result: ", String.valueOf(result));
            if (inferredValue > 0.5) {
                Log.d("Result: ", " It's Fall");
                return 1;

            } else {
                Log.d("Result: ", "No Fall");
                return 0;
            }
//        return result;
    }

        public List<List<Float>> calculateFeatures (String[]sensorData){

            List<List<String>> output = new ArrayList<>();
            int j = 0;
            Log.d("Predicting:", "Total Sample Size:" + sensorData.length);
            for (int i = 0; i < sensorData.length; i++) {
                String[] out = new String[5];
                out = sensorData[i].split(" ");
                if (out[0].toLowerCase().contains("accelerometer")) {
                    output.add(Arrays.asList(sensorData[i].split(" ")));
                }
            }
            float[] X = new float[output.size()];
            float[] Y = new float[output.size()];
            float[] Z = new float[output.size()];

            float[] magnitude = new float[output.size()];
            float[] ymag = new float[output.size()];
            float[] TA = new float[output.size()];
            int k = 0;
            for (int i = 0; i < output.size(); i++) {

                X[k] = Float.parseFloat(output.get(i).get(2));
                Y[k] = Float.parseFloat(output.get(i).get(3));
                Z[k] = Float.parseFloat(output.get(i).get(4));
                k++;
            }
            Log.d("----", "Predicting: " + "Xlength " + X.length + " Ylength" + Y.length + " Zlength" + Z.length);
            for (int i = 0; i < output.size(); i++) {
                float x = X[i];
                float y = Y[i];
                float z = Z[i];
                magnitude[i] = magnitude(x, y, z);
            }
            for (int i = 0; i < output.size(); i++) {
                ymag[i] = (Y[i]) / (float) Math.sqrt(magnitude[i]);
            }
            for (int i = 0; i < output.size(); i++) {
                TA[i] = (float) Math.asin(Math.toRadians(ymag[i]));
            }
            float averageX = calMean(X);
            float averageY = calMean(Y);
            float averageZ = calMean(Z);
            float medianX = calMedian(X.length, X);
            float medianY = calMedian(Y.length, Y);
            float medianZ = calMedian(Z.length, Z);
            float stdX = calStd(X);
            float stdY = calStd(Y);
            float stdZ = calStd(Z);
            float skewX = calSkew(X, X.length);
            float skewY = calSkew(Y, Y.length);
            float skewZ = calSkew(Z, Z.length);
            float kurtX = calKurtosis(X, X.length);
            float kurtY = calKurtosis(Y, Y.length);
            float kurtZ = calKurtosis(Z, Z.length);
            float minX = minValue(X);
            float minY = minValue(Y);
            float minZ = minValue(Z);
            float maxX = maxValue(X);
            float maxY = maxValue(Y);
            float maxZ = maxValue(Z);
            float slope = calSlope(minX, maxX, minY, maxY, minZ, maxZ);
            float meanTA = calMean(TA);
            float stdTA = calStd(TA);
            float skewTA = calSkew(TA, TA.length);
            float kurtosisTA = calKurtosis(TA, TA.length);
            float absX = absValue(X);
            float absY = absValue(Y);
            float absZ = absValue(Z);
            float abs_meanX = meanAbs(X);
            float abs_meanY = meanAbs(Y);
            float abs_meanZ = meanAbs(Z);
            float abs_medianX = medianAbs(X.length, X);
            float abs_medianY = medianAbs(Y.length, Y);
            float abs_medianZ = medianAbs(Z.length, Z);
            float abs_stdX = stdAbs(X);
            float abs_stdY = stdAbs(Y);
            float abs_stdZ = stdAbs(Z);
            float abs_skewX = skewAbs(X, X.length);
            float abs_skewY = skewAbs(Y, Y.length);
            float abs_skewZ = skewAbs(Z, Z.length);
            float abs_kurtX = kurtAbs(X, X.length);
            float abs_kurtY = kurtAbs(Y, Y.length);
            float abs_kurtZ = kurtAbs(Z, Z.length);
            float abs_minX = absMinValue(X);
            float abs_minY = absMinValue(Y);
            float abs_minZ = absMinValue(Z);
            float abs_maxX = absMaxValue(X);
            float abs_maxY = absMaxValue(Y);
            float abs_maxZ = absMaxValue(Z);
            float abs_slope = calSlope(abs_minX, abs_maxX, abs_minY, abs_maxY, abs_minZ, abs_maxZ);
            float mean_magnitude = calMean(magnitude);
            float std_magnitude = calStd(magnitude);
            float min_mag = minValue(magnitude);
            float max_mag = maxValue(magnitude);
            float diffMinMaxMag = max_mag - min_mag;
            float zcr_Mag = 0;
            float multiplier = (float)1/ magnitude.length;
            float avgResAcc = multiplier * (calSum(magnitude));
            List<Float> feature = new ArrayList<>();
            // Average values
            feature.add(averageX);
            feature.add(averageY);
            feature.add(averageZ);

            // Median values
            feature.add(medianX);
            feature.add(medianY);
            feature.add(medianZ);

            // Standard Deviation values
            feature.add(stdX);
            feature.add(stdY);
            feature.add(stdZ);

            // Skew values
            feature.add(skewX);
            feature.add(skewY);
            feature.add(skewZ);

            // Kurtosis values
            feature.add(kurtX);
            feature.add(kurtY);
            feature.add(kurtZ);

            // min values
            feature.add(minX);
            feature.add(minY);
            feature.add(minZ);

            // max values
            feature.add(maxX);
            feature.add(maxY);
            feature.add(maxZ);

            // slope value
            feature.add(slope);

            // calculations related to TA

            feature.add(meanTA);
            feature.add(stdTA);
            feature.add(skewTA);
            feature.add(kurtosisTA);

            // absolute values

            feature.add(absX);
            feature.add(absY);
            feature.add(absZ);
            feature.add(abs_meanX);
            feature.add(abs_meanY);
            feature.add(abs_meanZ);
            feature.add(abs_medianX);
            feature.add(abs_medianY);
            feature.add(abs_medianZ);
            feature.add(abs_stdX);
            feature.add(abs_stdY);
            feature.add(abs_stdZ);
            feature.add(abs_skewX);
            feature.add(abs_skewY);
            feature.add(abs_skewZ);
            feature.add(abs_kurtX);
            feature.add(abs_kurtY);
            feature.add(abs_kurtZ);
            feature.add(abs_minX);
            feature.add(abs_minY);
            feature.add(abs_minZ);
            feature.add(abs_maxX);
            feature.add(abs_maxY);
            feature.add(abs_maxZ);
            feature.add(abs_slope);

            // magnitude and remaining features
            feature.add(mean_magnitude);
            feature.add(std_magnitude);
            feature.add(min_mag);
            feature.add(max_mag);
            feature.add(diffMinMaxMag);
            feature.add(zcr_Mag);
            feature.add(avgResAcc);

            List<List<Float>> features = new ArrayList<>();
            features.add(feature);
            return features;

    }

    // add array elements
    public float calSum(float[] arr){
        float sum =0;
        for(int i =0; i<arr.length; i++){
            sum+=arr[i];
        }
        return sum;
    }
    // Calculate Average of array
    public  float calMean(float[] arr){
        float average = 0;
        float sum = 0;
        int length = arr.length;
        for(int i =0; i<arr.length; i++){
            sum+=arr[i];
        }
        average = sum/length;
        return average;
    }

    // Calculate median of array
    public float calMedian(int n, float[] arr){
        float median=0;
        int m = 0;
        if(n%2==1)
        {
            m=(int)((n+1/2)-1);

        }
        else
        {
            m=(int)(((int)(n/2-1)+ (int)(n/2))/2);

        }
        median = arr[m];
        return median;
    }

    // calculate standard deviation of array

    public  float calStd(float[] arr){
        float standardDeviation = 0.0f;
        int length = arr.length;
        float mean = calMean(arr);

        for(float num: arr) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return (float)Math.sqrt(standardDeviation/length);
    }

    //calculate skew of an array
    public float calSkew(float [] arr, int n){
        float mean = calMean(arr);
        float sum3 = 0;
        for(int i =0; i<n; i++){
            sum3 += Math.pow(arr[i]-mean,3);
        }
        float div = (float)1/n;
        float m3 = div*sum3;
        float sum2 = 0;
        for(int i =0; i<n; i++) {
            sum2 += (float) Math.pow(arr[i] - mean, 2);
        }
        float m2 = div*sum2;
        float m2E3 = (float)Math.sqrt(Math.pow(m2,3));
        float skewness =  (m3/m2E3);

        return skewness;
    }

    //calculate kurtosis of an array
    public float calKurtosis(float[] arr, int n){

        float mean = calMean(arr);
        float sum4 = 0;
        for(int i =0; i<n; i++){
            sum4+= (float)Math.pow(arr[i]-mean,4);
        }
        float div = (float)1/n;
        float m4 = div*sum4;
        float sum2 = 0;
        for(int i =0; i<n; i++) {
            sum2 += (float) Math.pow(arr[i] - mean, 2);
        }
        float m2 = div*sum2;
        float kurt = m4/(float)Math.pow(m2,2);
        return kurt-3;
    }
    // calculation magnitude of an array
    public float magnitude(float x, float y, float z){
        return (float)(x*x+y*y+z*z);
    }
    // calculate minimum value in array
    public float minValue(float[] arr){
        float min = arr[0];
        for(int i =1; i<arr.length; i++){
            if(arr[i]<min){
                min = arr[i];
            }
        }
        return min;
    }
    // calculate maximum value in array
    public float maxValue(float[] arr){
        float max = arr[0];
        for(int i =1; i<arr.length; i++){
            if(arr[i]>max){
                max = arr[i];
            }
        }
        return max;
    }

    // calculate absolute value
    public float absValue(float[] arr){
        float sum =0;
        for(int i =0; i<arr.length; i++){
            float num = Math.abs(arr[i]-calMean(arr));
            sum +=num;
        }
        return sum/arr.length;
    }
    // Absolute mean value
    public float meanAbs(float[] arr){
        float sum =0;
        for(int i =0; i<arr.length; i++){
            sum+=Math.abs(arr[i]);
        }
        return sum/arr.length;
    }

    // Absolute median value
    public float medianAbs(int n, float[] arr) {
        float median = 0;
        int m = 0;
        if (n % 2 == 1) {
            m = (int) ((n + 1 / 2) - 1);

        } else {
            m = (int) (((int) (n / 2 - 1) + (int) (n / 2)) / 2);

        }

        return Math.abs(arr[m]);

    }
    // Absolute standard deviation value
    public float stdAbs(float[] arr){
        float standardDeviation = 0.0f;
        int length = arr.length;
        float mean = meanAbs(arr);

        for(float num: arr) {
            standardDeviation += Math.pow(Math.abs(num) - mean, 2);
        }

        float std = (float)Math.sqrt(standardDeviation/length);
        return std;
    }
    // Absolute skew value
    public float skewAbs(float [] arr, int n) {
        float mean = meanAbs(arr);
        float sum3 = 0;
        for(int i =0; i<n; i++){
            sum3 += Math.pow(Math.abs(arr[i])-mean,3);
        }
        float div = (float)1/n;
        float m3 = div*sum3;
        float sum2 = 0;
        for(int i =0; i<n; i++) {
            sum2 += (float) Math.pow(Math.abs(arr[i]) - mean, 2);
        }
        float m2 = div*sum2;
        float m2E3 = (float)Math.sqrt(Math.pow(m2,3));
        float skewness =  (m3/m2E3);

        return skewness;
    }
    // Absolute kurtosis value
    public float kurtAbs(float[] arr, int n){
        float mean = meanAbs(arr);
        float sum4 = 0;
        for(int i =0; i<n; i++){
            sum4+= (float)Math.pow(Math.abs(arr[i])-mean,4);
        }
        float div = (float)1/n;
        float m4 = div*sum4;
        float sum2 = 0;
        for(int i =0; i<n; i++) {
            sum2 += (float) Math.pow(Math.abs(arr[i]) - mean, 2);
        }
        float m2 = div*sum2;
        float kurt = m4/(float)Math.pow(m2,2);
        return kurt-3;

    }

    // calculate absolute minimum value in array
    public float absMinValue(float[] arr){
        float min = Math.abs(arr[0]);
        for(int i =1; i<arr.length; i++){
            if(Math.abs(arr[i])<min){
                min = Math.abs(arr[i]);
            }
        }
        return min;
    }
    // calculate absolute maximum value in array
    public  float absMaxValue(float[] arr){
        float max = Math.abs(arr[0]);
        for(int i =1; i<arr.length; i++){
            if(Math.abs(arr[i])>max){
                max = Math.abs(arr[i]);
            }
        }
        return max;
    }

    // calculating slope
    public  float calSlope(float xmin, float xmax,float ymin,float ymax, float zmin, float zmax){
        return (float)Math.sqrt(Math.pow(xmax-xmin,2 )+Math.pow(ymax-ymin,2 )+Math.pow(zmax-zmin,2 ));
    }


private MappedByteBuffer loadModelFile() throws IOException {
    AssetFileDescriptor fileDescriptor = this.getAssets().openFd("ml_fall_new_model.tflite");
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declareLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
}

}

