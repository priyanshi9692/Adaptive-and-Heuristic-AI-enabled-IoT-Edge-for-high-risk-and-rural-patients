package com.cmpe.healthcareai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class RespiratoryActivity extends AppCompatActivity {

    Button selectAudioBtn;
    Interpreter tflite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory);
        selectAudioBtn = findViewById(R.id.selectAudioBtn);
        //verifyStoragePermissions(RespiratoryActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        Toast.makeText(RespiratoryActivity.this, "In Respiratory activity",
                Toast.LENGTH_LONG).show();

        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }


    public void onSelectAudioBtnClicked(View view) {
        Toast.makeText(RespiratoryActivity.this, "Btn clicked",
                Toast.LENGTH_SHORT).show();
        Intent intent_upload = new Intent();
        intent_upload.setType("*/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        intent_upload = Intent.createChooser(intent_upload,"Select a file");
        startActivityForResult(intent_upload,1);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if(requestCode == 1){

            if(resultCode == Activity.RESULT_OK){

                //the selected audio.
                Uri uri = data.getData();
                selectAudioBtn.setEnabled(false);
                selectAudioBtn.setText("Loading Audio file...");
                predictWithTFLite(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void predictWithTFLite(Uri uri) {
        selectAudioBtn.setText("Processing Audio file...");
        // code to predict goes here
        String src = uri.getPath();
        String[] path = src.split(":");

        Log.d("audioPath", src);


        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("featureExtraction");
        PyObject obj = pyobj.callAttr("build_feat",path[1]);
        //Log.d("out:", obj.toString());


        //DataType inpType = tflite.getInputTensor(0).dataType();
       // Log.d("inpType",inpType.toString());
        float[][][] feat = obj.toJava(float[][][].class);

        //DataType outType = tflite.getOutputTensor(0).dataType();
       // Log.d("outType",outType.toString());

        float[][] outprob= new float[1][4];
        tflite.run(feat,outprob);
        int predictedClass = findMaximumIndex(outprob);
        Log.d("Class", Integer.toString(predictedClass));
        Log.d("outprob0",String.format("%.8f",outprob[0][0]));
        Log.d("outprob1",String.format("%.8f",outprob[0][1]));
        Log.d("outprob2",String.format("%.8f",outprob[0][2]));
        Log.d("outprob3",String.format("%.8f",outprob[0][3]));


        /*if(obj.toString().equals("0")){
            Log.d("Diagnosis:", "No abnormalities were detected");
        }
        else if(obj.toString().equals("1")){
            Log.d("Diagnosis:", "Contains crackles");
        }
        else if(obj.toString().equals("2")){
            Log.d("Diagnosis:", "Contains wheeze");
        }
        else{
            Log.d("Diagnosis:", "contains both crackle and wheeze");
        }*/

    }

    private static int findMaximumIndex(float[][] a)
    {
        float minval = Float.MIN_VALUE;
        int idx = 10; //initialize with random index
        for(int row = 0; row < a.length; row++)
        {
            for(int col = 0; col < a[row].length; col++)
            {
                if(a[row][col] > minval)
                {
                    minval = a[row][col];
                    idx= col;
                }
            }
        }
        return idx;
    }
    //Code for loading tflite model as a mappedbytebuffer.
    // Code taken from https://blog.tensorflow.org/2018/03/using-tensorflow-lite-on-android.html
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("converted_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declareLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
    }
}