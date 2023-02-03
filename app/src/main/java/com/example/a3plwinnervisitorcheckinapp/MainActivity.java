package com.example.a3plwinnervisitorcheckinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private String mFirstName, mLastName;
    private EditText edtFirstName, edtLastName;
    private Button btnCheckIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prevents users from rotating screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        edtFirstName = findViewById(R.id.edtMainScreenFirstName);
        edtLastName = findViewById(R.id.edtMainScreenLastName);
        btnCheckIn = findViewById(R.id.btnMainScreenSubmit);

        btnCheckIn.setOnClickListener(v -> {
            mFirstName = edtFirstName.getText().toString();
            mLastName = edtLastName.getText().toString();

            if(TextUtils.isEmpty(mFirstName)) {
                edtFirstName.setText("Enter First Name");
            }
            if(TextUtils.isEmpty(mLastName)) {
                edtLastName.setText("Enter Last Name");
            }
            if(!mFirstName.isEmpty() && !mLastName.isEmpty()) {
                // take photo and send information through email
            }
        });
    }
}