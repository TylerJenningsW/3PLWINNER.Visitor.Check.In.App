package com.example.a3plwinnervisitorcheckinapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class VisitorsCheckout extends AppCompatActivity {

    private String nextDocumentId;
    private ArrayList<Visitor> mVisitorArrayList;
    private RecyclerView mVisitorsRecyclerView;
    private VisitorRecyclerViewAdapter mVisitorRecyclerViewAdapter;

    private FirebaseFirestore db;

    private static Visitor mVisitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors_checkout);

        // prevents users from rotating screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().hide();

        mVisitorsRecyclerView = findViewById(R.id.recyclerViewVisitors);

        db = FirebaseFirestore.getInstance();

        mVisitorArrayList = new ArrayList<>();
        mVisitorsRecyclerView.setHasFixedSize(true);
        mVisitorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mVisitorRecyclerViewAdapter = new VisitorRecyclerViewAdapter(mVisitorArrayList, this);
        mVisitorsRecyclerView.setAdapter(mVisitorRecyclerViewAdapter);

        db.collection("Visitors").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot d : list) {
                    Visitor v = d.toObject(Visitor.class);
                    if (v.getCheckedIn() == true) {

                        mVisitorArrayList.add(v);
                    }

                }
                int id = Integer.parseInt(list.get(list.size() - 1).getId());
                id++;
                nextDocumentId = String.valueOf(id);

                mVisitorRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        mVisitorRecyclerViewAdapter.setOnItemClickListener(new VisitorRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                mVisitor = mVisitorArrayList.get(position);
                mVisitor.setCheckedIn(false);
                mVisitor.setCheckOutTime(getCurrentTime());
                CollectionReference d = db.collection("Visitors");

                d.document(mVisitorArrayList.get(position).getDocumentId()).delete();

                addDataToFirebase(mVisitor);
                sendEmail(mVisitor);

                Intent i = new Intent(VisitorsCheckout.this, CheckoutDelayScreen.class);
                startActivity(i);
                finish();
            }
        });

    }

    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("MM/dd/yyyy HH:mm:ss"));
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private void addDataToFirebase(Visitor _visitor) {

        _visitor.setDocumentId(String.valueOf(nextDocumentId));

        db.collection("Visitors").document(String.valueOf(nextDocumentId)).set(_visitor).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });

    }

    private void sendEmail(Visitor _visitor) {
        final String username = "3plwinnerwms@gmail.com";
        final String password = "cjptjqoojmkrpdql";
        final String recipient = "brian@3plwinner.com";
        String messageToSend = "<br><b>Visitor name: </b>" + _visitor.getFirstName() + " " + _visitor.getLastName() +
                "<br><b>Company: </b>" + _visitor.getCompany() +
                "<br><b>Who is visitor seeing: </b>" + _visitor.getWhoAreYouVisiting() +
                "<br><b>Reason of visit: </b>" + _visitor.getReason() +
                "<br><b>Emergency contact name: </b>" + _visitor.getEmergencyContact()+
                "<br><b>Emergency contact phone number: </b>" + _visitor.getEmergencyPhone() +
                "<br><b>Checked in at: </b>" + _visitor.getCheckInTime() +
                "<br><b>Checked out at: </b>" + _visitor.getCheckOutTime();
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));


            message.setSubject("3PLWINNER Visitor Check-Out - " + getCurrentTime());

            MimeMultipart multipart = new MimeMultipart();

//            MimeBodyPart attachment = new MimeBodyPart();

//            filePath = getPath(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.normalizeScheme());
//            assert filePath != null;
//            DataSource source = new FileDataSource(filePath);

//            attachment.setDataHandler(new DataHandler(source));

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(messageToSend, "text/html");
            multipart.addBodyPart(messageBodyPart);
//            multipart.addBodyPart(attachment);

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e.toString());
        }

    }
}
