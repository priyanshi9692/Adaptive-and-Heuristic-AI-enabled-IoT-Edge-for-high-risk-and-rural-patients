package com.cmpe.healthcareai;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.tensorflow.lite.Interpreter;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;


public class RespiratoryActivity extends AppCompatActivity {

    Button selectAudioBtn;
    TextView predTV;
    CardView resultCV;
    ProgressBar progressBar;
    Interpreter tflite;
    PyObject pyobj;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory);



        selectAudioBtn = findViewById(R.id.selectAudioBtn);
        predTV = findViewById(R.id.predTV);
        resultCV = findViewById(R.id.card_view);
        progressBar = findViewById(R.id.progressBar);
        //verifyStoragePermissions(RespiratoryActivity.this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
//            }
//        }

        Toast.makeText(RespiratoryActivity.this, "In Respiratory activity",
                Toast.LENGTH_LONG).show();

//        File[] testFile = new File("src/main/python/").listFiles();
//        String[] names = new String[testFile.length];
//        for (int i = 0; i < testFile.length; i++) {
//            names[i] = testFile[i].getName();
//        }
//        Log.v("FileNames",String.join(",", names));
//        Log.v("FileNames",String.join(",", getApplicationInfo().dataDir));


        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py = Python.getInstance();
        pyobj = py.getModule("featureExtraction");
        selectAudioBtn.setEnabled(true);

    }


    public void onSelectAudioBtnClicked(View view) {
        Toast.makeText(RespiratoryActivity.this, "Btn clicked",
                Toast.LENGTH_SHORT).show();
//        Intent intent_upload = new Intent();
//        intent_upload.setType("*/*");
//        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
//        intent_upload = Intent.createChooser(intent_upload,"Select a file");
//        startActivityForResult(intent_upload,1);
        new PredictAsyncTask(this).execute("dummyFileName");
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

    private void predictWithTFLite(Uri uri)  {
        selectAudioBtn.setText("Processing Audio file...");
        // code to predict goes here
        String src = uri.getPath();
        String[] path = src.split(":");

        Log.d("audioPath", src);



        PyObject obj = pyobj.callAttr("build_feat","107_2b4_Pl_mc_AKGC417L_0.wav", "lstm_5s_21.h5");
        Log.d("out:", obj.toString());
        if(obj.toString().equals("0")){
            Log.d("Diagnosis:", "No abnormalities were detected");
            updateCard("No abnormalities detected");
        }
        else if(obj.toString().equals("1")){
            Log.d("Diagnosis:", "Contains crackles");
            updateCard("Crackles detected");
        }
        else if(obj.toString().equals("2")){
            Log.d("Diagnosis:", "Wheeze detected");
            updateCard("Wheeze detected");
        }
        else{
            Log.d("Diagnosis:", "Both crackle and wheeze detected");
            updateCard("Both Crackle and Wheeze detected");
        }

    }

    private void updateCard(String strToDisplay) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultCV.setVisibility(View.VISIBLE);
                predTV.setText(strToDisplay);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initCard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultCV.setVisibility(View.VISIBLE);
                predTV.setText("Predicting...");
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private class PredictAsyncTask extends AsyncTask<String, Integer, String> {
        private Context mContext;

        public PredictAsyncTask (Context context){
            mContext = context;
        }
        protected String doInBackground(String... fileName) {
            //updateCard("Predicting...");
            initCard();
            if(!Python.isStarted())
                Python.start(new AndroidPlatform(mContext));
            Python py = Python.getInstance();
            PyObject pyobj = py.getModule("featureExtraction");
            PyObject obj = pyobj.callAttr("build_feat","107_2b4_Pl_mc_AKGC417L_0.wav", "lstm_5s_21.h5");
            Log.d("out:", obj.toString());
            return obj.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            if(result.equals("0")){
                Log.d("Diagnosis:", "No abnormalities were detected");
                updateCard("No abnormalities detected");
            }
            else if(result.equals("1")){
                Log.d("Diagnosis:", "Contains crackles");
                updateCard("Crackles detected");
            }
            else if(result.equals("2")){
                Log.d("Diagnosis:", "Wheeze detected");
                updateCard("Wheeze detected");
            }
            else{
                Log.d("Diagnosis:", "Both crackle and wheeze detected");
                updateCard("Both Crackle and Wheeze detected");
            }
        }
    }
}