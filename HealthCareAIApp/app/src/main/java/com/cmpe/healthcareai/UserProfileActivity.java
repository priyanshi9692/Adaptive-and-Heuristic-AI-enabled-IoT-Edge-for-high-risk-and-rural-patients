package com.cmpe.healthcareai;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    FirebaseUser user;
    EditText userEmailET, heightET, weightET, ageET, docNoET;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        user = FirebaseAuth.getInstance().getCurrentUser();
        toolBarLayout.setTitle(user.getDisplayName());

        userEmailET = findViewById(R.id.user_email_ET);
        userEmailET.setText(user.getEmail());

        heightET = findViewById(R.id.user_height_ET);
        weightET = findViewById(R.id.user_weight_ET);
        ageET = findViewById(R.id.user_age_ET);
        docNoET = findViewById(R.id.user_doc_no_ET);

        loadUserData();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = userEmailET.getText().toString();
                String height = heightET.getText().toString();
                String weight = weightET.getText().toString();
                String age = ageET.getText().toString();
                String docPhNo = docNoET.getText().toString();

                Map<String,Object> mapToAdd = new HashMap<>();
                mapToAdd.put("email",email);
                mapToAdd.put("height",height);
                mapToAdd.put("weight", weight);
                mapToAdd.put("age",age);
                mapToAdd.put("docNo", docPhNo);

                db.collection("profile")
                        .add(mapToAdd)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(UserProfileActivity.this, "Profile data saved",
                                        Toast.LENGTH_SHORT).show();
                                Intent myIntent = new Intent(UserProfileActivity.this, MainActivity.class);
                                UserProfileActivity.this.startActivity(myIntent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error adding document", e);
                                Toast.makeText(UserProfileActivity.this, "Error syncing checkup result to cloud",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


    }

    private void loadUserData() {
        db.collection("profile")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                docNoET.setText((CharSequence) document.get("docNo"));
                                heightET.setText((CharSequence) document.get("height"));
                                weightET.setText((CharSequence) document.get("weight"));
                                ageET.setText((CharSequence) document.get("age"));
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}