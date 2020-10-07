package com.cmpe.healthcareai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RespiratoryActivity extends AppCompatActivity {

    Button selectAudioBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory);
        selectAudioBtn = findViewById(R.id.selectAudioBtn);
        Toast.makeText(RespiratoryActivity.this, "In Respiratory activity",
                Toast.LENGTH_LONG).show();

    }

    public void onSelectAudioBtnClicked(View view) {
        Toast.makeText(RespiratoryActivity.this, "Btn clicked",
                Toast.LENGTH_SHORT).show();
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,1);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if(requestCode == 1){

            if(resultCode == RESULT_OK){

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
    }
}