package com.cmpe.healthcareai;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class RespiratoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respiratory);
        Toast.makeText(RespiratoryActivity.this, "In Respiratory activity",
                Toast.LENGTH_LONG).show();
    }
}