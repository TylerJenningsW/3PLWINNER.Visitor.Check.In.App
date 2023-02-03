package com.example.a3plwinnervisitorcheckinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

    private String mFirstName, mLastName, mWhoAreYouVisiting, mReason;
    private EditText edtFirstName, edtLastName, edtWhoAreYouVisiting, edtReason;
    private Button btnCheckIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prevents users from rotating screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().hide();

        edtFirstName = findViewById(R.id.edtMainScreenFirstName);
        edtLastName = findViewById(R.id.edtMainScreenLastName);
        edtWhoAreYouVisiting = findViewById(R.id.edtMainScreenWhoAreYouVisiting);
        edtReason = findViewById(R.id.edtMainScreenReason);
        btnCheckIn = findViewById(R.id.btnMainScreenSubmit);

        btnCheckIn.setOnClickListener(v -> {
            mFirstName = edtFirstName.getText().toString();
            mLastName = edtLastName.getText().toString();
            mWhoAreYouVisiting = edtWhoAreYouVisiting.getText().toString();
            mReason = edtReason.getText().toString();

            if(TextUtils.isEmpty(mFirstName)) {
                edtFirstName.setError("Enter First Name");
            }
            if(TextUtils.isEmpty(mLastName)) {
                edtLastName.setError("Enter Last Name");
            }
            if(TextUtils.isEmpty(mWhoAreYouVisiting)) {
                edtWhoAreYouVisiting.setError("Enter Who Are You Visiting");
            }
            if(TextUtils.isEmpty(mReason)) {
                edtReason.setError("Enter Reason of Visit");
            }
            if(!mFirstName.isEmpty() && !mLastName.isEmpty() &&
                !mWhoAreYouVisiting.isEmpty() && !mReason.isEmpty()) {
                // take photo and send information through email
                sendEmail();
                Intent i = new Intent(MainActivity.this, ConfirmationScreen.class);
                startActivity(i);
                edtFirstName.getText().clear();
                edtLastName.getText().clear();
                edtWhoAreYouVisiting.getText().clear();
                edtReason.getText().clear();
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void sendEmail() {
        final String username="3plwinnerwms@gmail.com";
        final String password="rfemloyjgbiqnsak";
        final String sender = "jorgem@3plwinner.com";
        String messageToSend = "Visitor name: " + mFirstName + " " +mLastName +
                "\nWho is visitor seeing: " + mWhoAreYouVisiting +
                "\nReason of visit: " + mReason +
                "\nChecked in at: " + getCurrentTime();
        Properties props=new Properties();
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.port","587");
        Session session= Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sender));
            message.setSubject("3PLWINNER Visitor Check-In - "+ getCurrentTime());
            message.setText(messageToSend);
            Transport.send(message);
        }catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("MM/dd/yyyy HH:mm:ss"));
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}