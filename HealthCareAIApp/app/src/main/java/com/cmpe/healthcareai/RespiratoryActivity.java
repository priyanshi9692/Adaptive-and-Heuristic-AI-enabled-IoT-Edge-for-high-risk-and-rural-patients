package com.cmpe.healthcareai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
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

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RespiratoryActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    Button selectAudioBtn;
    Interpreter tflite;

    TextView cvTitle;
    TextView cvResult;
    ProgressBar progressBar;
    CardView card;

    Python py = null;
    PyObject pyobj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory);
        selectAudioBtn = findViewById(R.id.selectAudioBtn);
        cvTitle = findViewById(R.id.card_view_title);
        cvResult = findViewById(R.id.card_view_result);
        progressBar = findViewById(R.id.progress_bar);
        card = findViewById(R.id.cardView);
        card.setVisibility(View.INVISIBLE);
        cvResult.setVisibility(View.INVISIBLE);
        //verifyStoragePermissions(RespiratoryActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

//        Toast.makeText(RespiratoryActivity.this, "In Respiratory activity",
//                Toast.LENGTH_LONG).show();

        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }


    public void onSelectAudioBtnClicked(View view) {
//        Toast.makeText(RespiratoryActivity.this, "Btn clicked",
//                Toast.LENGTH_SHORT).show();
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
                card.setVisibility(View.VISIBLE);
                cvTitle.setText("Predicting...");
                cvResult.setText("");
                progressBar.setVisibility(View.VISIBLE);
                //predictWithTFLite(uri);
                new DoMlInBackground().execute(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private void predictWithTFLite(Uri uri) {
//        selectAudioBtn.setText("Processing Audio file...");
//        // code to predict goes here
//        String src = uri.getPath();
//        String[] path = src.split(":");
//
//        Log.d("audioPath", src);
//
//
//        Python py = Python.getInstance();
//        PyObject pyobj = py.getModule("featureExtraction");
//        PyObject obj = pyobj.callAttr("build_feat",path[1]);
//        //Log.d("out:", obj.toString());
//
//
//        //DataType inpType = tflite.getInputTensor(0).dataType();
//       // Log.d("inpType",inpType.toString());
//        float[][][] feat = obj.toJava(float[][][].class);
//
//        //DataType outType = tflite.getOutputTensor(0).dataType();
//       // Log.d("outType",outType.toString());
//
//        float[][] outprob= new float[1][4];
//        tflite.run(feat,outprob);
//        int predictedClass = findMaximumIndex(outprob);
//        Log.d("Class", Integer.toString(predictedClass));
//        Log.d("outprob0",String.format("%.8f",outprob[0][0]));
//        Log.d("outprob1",String.format("%.8f",outprob[0][1]));
//        Log.d("outprob2",String.format("%.8f",outprob[0][2]));
//        Log.d("outprob3",String.format("%.8f",outprob[0][3]));
//
//
//        if(obj.toString().equals("0")){
//            Log.d("Diagnosis:", "No abnormalities were detected");
//            cvResult.setText("No abnormalities were detected");
//        }
//        else if(obj.toString().equals("1")){
//            Log.d("Diagnosis:", "Contains crackles");
//            cvResult.setText("Contains crackles");
//        }
//        else if(obj.toString().equals("2")){
//            Log.d("Diagnosis:", "Contains wheeze");
//            cvResult.setText("Contains wheeze");
//        }
//        else{
//            Log.d("Diagnosis:", "contains both crackle and wheeze");
//            cvResult.setText("Contains both crackle and wheeze");
//        }
//        cvTitle.setText("Result");
//        cvResult.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.INVISIBLE);
//
//    }

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

    private class DoMlInBackground extends AsyncTask<Uri, Integer, String> {
        protected String doInBackground(Uri... urls) {
            if (py == null){
                py = Python.getInstance();
                pyobj = py.getModule("featureExtraction");
            }


            String src = urls[0].getPath();
            String[] path = src.split(":");
            PyObject obj = pyobj.callAttr("build_feat",path[1]);
            Log.d("audioPath", src);



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
            return Integer.toString(predictedClass);
        }



        protected void onPostExecute(String result) {
            String dataToInsert = null;
            if(result.equals("0")){
                dataToInsert = "No abnormalities were detected";
                Log.d("Diagnosis:", dataToInsert);
                cvResult.setText(dataToInsert);
            }
            else if(result.equals("1")){
                dataToInsert = "Contains crackles";
                Log.d("Diagnosis:", dataToInsert);
                cvResult.setText(dataToInsert);
            }
            else if(result.equals("2")){
                dataToInsert = "Contains wheeze";
                Log.d("Diagnosis:", dataToInsert);
                cvResult.setText(dataToInsert);
            }
            else{
                dataToInsert = "Contains both crackle and wheeze";
                Log.d("Diagnosis:", dataToInsert);
                cvResult.setText(dataToInsert);
            }
            cvTitle.setText("Result");
            cvResult.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            selectAudioBtn.setText("Select another audio file");
            selectAudioBtn.setEnabled(true);
            addDataToFirestore(dataToInsert);
        }
    }

    void addDataToFirestore(String result){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String,Object> mapToAdd = new HashMap<>();
        mapToAdd.put("email",user.getEmail());
        mapToAdd.put("result",result);
        mapToAdd.put("time", FieldValue.serverTimestamp());

        db.collection("respiratory")
                .add(mapToAdd)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(RespiratoryActivity.this, "Checkup result synced to cloud",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                        Toast.makeText(RespiratoryActivity.this, "Error syncing checkup result to cloud",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

