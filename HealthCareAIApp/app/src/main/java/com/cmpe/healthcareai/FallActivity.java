package com.cmpe.healthcareai;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
              List<List<Double>> result = new ArrayList<>();

                result = predictFallOrNormal(sensorData);
                int count=0;
                String str = new String();
                for( List<Double> feature: result){
                    for(double f: feature){
                        str += f+" ";
                        count++;
                    }
                }
                Log.d("Features: ","Count: "+count+" "+ str);

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

    public List<List<Double>> predictFallOrNormal(String[] sensorData){

        List<List<String>> output = new ArrayList<>();
        int j = 0;
        Log.d("Predicting:", "Total Sample Size:" + sensorData.length);
        for(int i=0; i<sensorData.length; i++){
            String[] out = new String[5];
            out = sensorData[i].split(" ");
            if(out[0].toLowerCase().contains("accelerometer")){
                output.add(Arrays.asList(sensorData[i].split(" ")));
            }
        }
        double [] X = new double[output.size()];
        double [] Y = new double[output.size()];
        double [] Z = new double[output.size()];
        double[] magnitude = new double[output.size()];
        double [] ymag = new double[output.size()];
        double [] TA = new double[output.size()];
        //ArrayList<Double> Zi = new ArrayList<>();
        int k = 0;
        for(int i =0; i<output.size(); i++){

                X[k] = Double.parseDouble(output.get(i).get(2));
                Y[k] = Double.parseDouble(output.get(i).get(3));
                Z[k] = Double.parseDouble(output.get(i).get(4));
                k++;
        }
        Log.d("----", "Predicting: "+ "Xlength "+X.length+ " Ylength"+ Y.length+" Zlength"+Z.length);
        for(int i =0; i<output.size(); i++){
            double x = X[i];
            double y = Y[i];
            double z = Z[i];
            magnitude[i] = magnitude(x,y,z);
        }
        for(int i = 0; i<output.size(); i++){
            ymag[i] = (Y[i])/Math.sqrt(magnitude[i]);
        }
        for(int i=0; i<output.size(); i++){
            TA[i] = Math.asin(Math.toRadians(ymag[i]));
        }
        double averageX = calMean(X);
        double averageY = calMean(Y);
        double averageZ = calMean(Z);
        double medianX = calMedian(X.length, X);
        double medianY = calMedian(Y.length, Y);
        double medianZ = calMedian(Z.length, Z);
        double stdX = calStd(X);
        double stdY = calStd(Y);
        double stdZ = calStd(Z);
        double skewX = calSkew(X, X.length);
        double skewY = calSkew(Y, Y.length);
        double skewZ = calSkew(Z, Z.length);
        double kurtX = calKurtosis(X,X.length);
        double kurtY = calKurtosis(Y,Y.length);
        double kurtZ = calKurtosis(Z,Z.length);
        double minX = minValue(X);
        double minY = minValue(Y);
        double minZ = minValue(Z);
        double maxX = maxValue(X);
        double maxY = maxValue(Y);
        double maxZ = maxValue(Z);
        double slope = calSlope(minX, maxX,minY,maxY, minZ, maxZ);
        double meanTA = calMean(TA);
        double stdTA = calStd(TA);
        double skewTA = calSkew(TA, TA.length);
        double kurtosisTA = calKurtosis(TA, TA.length);
        double absX = absValue(X);
        double absY = absValue(Y);
        double absZ = absValue(Z);
        double abs_meanX =meanAbs(X);
        double abs_meanY =meanAbs(Y);
        double abs_meanZ =meanAbs(Z);
        double abs_medianX = medianAbs(X.length, X);
        double abs_medianY = medianAbs(Y.length, Y);
        double abs_medianZ = medianAbs(Z.length, Z);
        double abs_stdX = stdAbs(X);
        double abs_stdY = stdAbs(Y);
        double abs_stdZ = stdAbs(Z);
        double abs_skewX = skewAbs(X, X.length);
        double abs_skewY = skewAbs(Y, Y.length);
        double abs_skewZ = skewAbs(Z, Z.length);
        double abs_kurtX = kurtAbs(X, X.length);
        double abs_kurtY = kurtAbs(Y, Y.length);
        double abs_kurtZ = kurtAbs(Z, Z.length);
        double abs_minX = absMinValue(X);
        double abs_minY = absMinValue(Y);
        double abs_minZ = absMinValue(Z);
        double abs_maxX = absMaxValue(X);
        double abs_maxY = absMaxValue(Y);
        double abs_maxZ = absMaxValue(Z);
        double abs_slope = calSlope(abs_minX, abs_maxX, abs_minY, abs_maxY, abs_minZ, abs_maxZ);
        double mean_magnitude = calMean(magnitude);
        double std_magnitude = calStd(magnitude);
        double min_mag = minValue(magnitude);
        double max_mag = maxValue(magnitude);
        double diffMinMaxMag = max_mag - min_mag;
        double zcr_Mag = 0;
        double avgResAcc = (1/magnitude.length)*(calSum(magnitude));
        List<Double> feature = new ArrayList<>();
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

        List<List<Double>> features = new ArrayList<>();
         features.add(feature);
         return features;
    }
    // add array elements
    public double calSum(double[] arr){
        double sum =0;
        for(int i =0; i<arr.length; i++){
            sum+=arr[i];
        }
        return sum;
    }
    // Calculate Average of array
    public  double calMean(double[] arr){
        double average = 0;
        double sum = 0;
        int length = arr.length;
        for(int i =0; i<arr.length; i++){
            sum+=arr[i];
        }
        average = sum/length;
        return average;
    }

    // Calculate median of array
    public double calMedian(int n, double[] arr){
        double median=0;
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

    public  double calStd(double[] arr){
        double standardDeviation = 0.0;
        int length = arr.length;
        double mean = calMean(arr);

        for(double num: arr) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
    }

    //calculate skew of an array
    public double calSkew(double [] arr, int n)
    {
        // Find skewness using above formula
        double sum = 0;
        for (int i = 0; i < n; i++){
            sum = ((arr[i] - calMean(arr)) * (arr[i] - calMean(arr)) * (arr[i] - calMean(arr)));
        }
        return sum / (n * calStd(arr) *
                calStd(arr) *
                calStd(arr) *
                calStd(arr));
    }

    //calculate kurtosis of an array
    public double calKurtosis(double[] arr, int n){
        double secondMoment = 0;
        double fourthMoment = 0;
        double kurt =0;
        float secSum =0;
        float forSum = 0;
        double mean = calMean(arr);
        for(int i = 0; i<n; i++){
            secSum += Math.pow(arr[i]-mean,2);
        }
        secondMoment = secSum/n;
        for(int i =0; i<n; i++){
            forSum+= Math.pow(arr[i]-mean,4);
        }
        fourthMoment = forSum/n;
        kurt = fourthMoment/Math.pow(secondMoment,2);
        return kurt;
    }
    // calculation magnitude of an array
    public double magnitude(double x, double y, double z){
        return (Math.sqrt(x*x+y*y+z*z));
    }
    // calculate minimum value in array
    public double minValue(double[] arr){
        double min = arr[0];
        for(int i =1; i<arr.length; i++){
            if(arr[i]<min){
                min = arr[i];
            }
        }
        return min;
    }
    // calculate maximum value in array
    public double maxValue(double[] arr){
        double max = arr[0];
        for(int i =1; i<arr.length; i++){
            if(arr[i]>max){
                max = arr[i];
            }
        }
        return max;
    }

    // calculate absolute value
    public double absValue(double[] arr){
        double sum =0;
        for(int i =0; i<arr.length; i++){
            double num = Math.abs(arr[i]-calMean(arr));
            sum +=num;
        }
        return sum/arr.length;
    }
    // Absolute mean value
    public double meanAbs(double[] arr){
        double sum =0;
        for(int i =0; i<arr.length; i++){
            sum+=Math.abs(arr[i]);
        }
        return sum/arr.length;
    }

    // Absolute median value
    public double medianAbs(int n, double[] arr) {
        double median = 0;
        int m = 0;
        if (n % 2 == 1) {
            m = (int) ((n + 1 / 2) - 1);

        } else {
            m = (int) (((int) (n / 2 - 1) + (int) (n / 2)) / 2);

        }

        return Math.abs(arr[m]);

    }
    // Absolute standard deviation value
    public double stdAbs(double[] arr){
        double standardDeviation = 0.0;
        int length = arr.length;
        double mean = meanAbs(arr);

        for(double num: arr) {
            standardDeviation += Math.pow(Math.abs(num) - mean, 2);
        }

        double std = Math.sqrt(standardDeviation/length);
        return std;
    }
    // Absolute skew value
    public double skewAbs(double [] arr, int n) {
        // Find skewness using above formula
        double sum = 0;
        for (int i = 0; i < n; i++){
            sum = ((Math.abs(arr[i]) - meanAbs(arr)) * (Math.abs(arr[i]) - meanAbs(arr)) * (Math.abs(arr[i]) - meanAbs(arr)));
        }
        return sum / (n * stdAbs(arr) *
                stdAbs(arr) *
                stdAbs(arr) *
                stdAbs(arr));

    }
    // Absolute kurtosis value
    public double kurtAbs(double[] arr, int n){
        double secondMoment = 0;
        double fourthMoment = 0;
        double kurt =0;
        float secSum =0;
        float forSum = 0;
        double mean = meanAbs(arr);
        for(int i = 0; i<n; i++){
            secSum += Math.pow(Math.abs(arr[i])-mean,2);
        }
        secondMoment = secSum/n;
        for(int i =0; i<n; i++){
            forSum+= Math.pow(Math.abs(arr[i])-mean,4);
        }
        fourthMoment = forSum/n;
        kurt = fourthMoment/Math.pow(secondMoment,2);
        return kurt;
    }

    // calculate absolute minimum value in array
    public double absMinValue(double[] arr){
        double min = Math.abs(arr[0]);
        for(int i =1; i<arr.length; i++){
            if(Math.abs(arr[i])<min){
                min = Math.abs(arr[i]);
            }
        }
        return min;
    }
    // calculate absolute maximum value in array
    public  double absMaxValue(double[] arr){
        double max = Math.abs(arr[0]);
        for(int i =1; i<arr.length; i++){
            if(Math.abs(arr[i])>max){
                max = Math.abs(arr[i]);
            }
        }
        return max;
    }

    // calculating slope
    public  double calSlope(double xmin, double xmax,double ymin,double ymax, double zmin, double zmax){
        return Math.sqrt(Math.pow(xmax-xmin,2 )+Math.pow(ymax-ymin,2 )+Math.pow(zmax-zmin,2 ));
    }

//    private double doInference(float age, float gender, float bmi, float children, float smoker, float region_val) {
//        float[][] inputVal = {{age},{gender},{bmi},{children},{smoker},{region_val}};
//        float[][] output= new float[1][1];
//        // tflite.run(inputVal,output);
//        float inferredValue= (float) (output[0][0] * sqrt(1.43451672 * pow(10,8)) + 13092.01280714);
//        return  inferredValue;
//    }

}