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

    private String mFirstName, mLastName;
    private EditText edtFirstName, edtLastName;
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
                sendEmail();
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void sendEmail() {
        final String username="3plwinnerwms@gmail.com";
        final String password="rfemloyjgbiqnsak";
        final String sender = "jorgem@3plwinner.com";
        String messageToSend = mFirstName + ", " +mLastName + " checked in at: " + getCurrentTime();
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
            message.setSubject("Warehouse Visitor Check-In - "+ getCurrentTime());
            message.setText(messageToSend);
            Transport.send(message);
            Toast.makeText(getApplicationContext(), "email send successfully", Toast.LENGTH_LONG).show();
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