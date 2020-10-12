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
import org.tensorflow.lite.Interpreter;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;
import com.jlibrosa.audio.wavFile.WavFileException;

import java.io.IOException;

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
                try {
                    predictWithTFLite(uri);
                } catch (FileFormatNotSupportedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WavFileException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void predictWithTFLite(Uri uri) throws FileFormatNotSupportedException, IOException, WavFileException {
        selectAudioBtn.setText("Processing Audio file...");
        // code to predict goes here
        String src = uri.getPath();
        String[] path = src.split(":");

        Log.d("audioPath", src);


       if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("featureExtraction");
        PyObject obj = pyobj.callAttr("build_feat","/mnt/sdcard/Audiodata/107_2b4_Pl_mc_AKGC417L_0.wav", "lstm_5s_21.h5");
        Log.d("out:", obj.toString());
        if(obj.toString().equals("0")){
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
        }

    }
}