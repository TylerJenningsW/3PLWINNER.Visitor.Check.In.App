package com.example.a3plwinnervisitorcheckinapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
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

public class MainActivity extends AppCompatActivity {
    private String nextDocumentId;
    private static final String FILENAME_FORMAT = "dd-M-yyyy hh:mm:ss";
    public static final int REQUEST_CODE_PERMISSIONS = 10;
    public static final String TAG = "CameraXApp";
    private ExecutorService cameraExecutor;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        List<String> requiredPermissions = new ArrayList<>(Arrays.asList(android.Manifest.permission.CAMERA));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            requiredPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        REQUIRED_PERMISSIONS = requiredPermissions.toArray(new String[0]);
    }
    private String filePath;
    private String mFirstName, mLastName, mWhoAreYouVisiting, mReason, mCompany, mCheckInTime, mCheckOutTime, mEmergencyContactName, mEmergencyContactPhone;
    private EditText edtFirstName, edtLastName, edtCompany, edtEmergencyContactName, edtEmergencyContactPhone;
    private ProgressBar pbLoading;
    private Button btnCheckIn, btnCheckOut;
    private Spinner spinnerWhoAreYouVisiting, spinnerReason;
    private ImageCapture imageCapture = null;
    private CustomSpinnerAdapter adapterOne, adapterTwo;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prevents users from rotating screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        getSupportActionBar().hide();

        edtFirstName = findViewById(R.id.edtMainScreenFirstName);
        edtLastName = findViewById(R.id.edtMainScreenLastName);
        edtCompany = findViewById(R.id.edtMainScreenCompany);
        edtEmergencyContactName = findViewById(R.id.edtMainScreenEmergencyContactName);
        edtEmergencyContactPhone = findViewById(R.id.edtMainScreenEmergencyContactPhoneNumber);

        spinnerWhoAreYouVisiting = findViewById(R.id.spinnerWhoAreYouVisiting);
        spinnerReason = findViewById(R.id.spinnerReason);

        btnCheckIn = findViewById(R.id.btnMainScreenSubmit);
        btnCheckOut = findViewById(R.id.btnMainScreenCheckout);
        pbLoading = findViewById(R.id.pbLoading);

        String defaultTextForSpinnerOne = "Who Are You Visiting?";
        String defaultTextForSpinnerTwo = "Reason";
        String[] arrayForSpinnerOne = {"Ashley Raschick", "Brian Rodriguez", "Jorge Monraz", "Mark Vanderwarf", "Erik Figueroa", "Maintenance/Service Call", "Other"};
        String[] arrayForSpinnerTwo = {"Meeting", "Warehouse Tour", "Maintenance/Service Call", "Service Quote", "Other"};

        adapterOne = new CustomSpinnerAdapter(this, R.layout.spinner_row, arrayForSpinnerOne, defaultTextForSpinnerOne);
        adapterTwo = new CustomSpinnerAdapter(this, R.layout.spinner_row, arrayForSpinnerTwo, defaultTextForSpinnerTwo);

        spinnerWhoAreYouVisiting.setAdapter(adapterOne);
        spinnerReason.setAdapter(adapterTwo);

        db = FirebaseFirestore.getInstance();

        pbLoading.setVisibility(View.GONE);
        imageCapture = new ImageCapture.Builder().build();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        btnCheckIn.setOnClickListener(v -> {
            Toast.makeText(this, "Smile! Image is being taken", Toast.LENGTH_SHORT).show();
            pbLoading.setVisibility(View.VISIBLE);
            mFirstName = edtFirstName.getText().toString();
            mLastName = edtLastName.getText().toString();
            mCompany = edtCompany.getText().toString();
            mWhoAreYouVisiting = spinnerWhoAreYouVisiting.getSelectedItem().toString();
            mEmergencyContactName = edtEmergencyContactName.getText().toString();
            mEmergencyContactPhone = edtEmergencyContactPhone.getText().toString();
            mReason = spinnerReason.getSelectedItem().toString();
            mCheckInTime = getCurrentTime();


            if(TextUtils.isEmpty(mFirstName)) {
                edtFirstName.setError("Enter First Name");
            }
            if(TextUtils.isEmpty(mLastName)) {
                edtLastName.setError("Enter Last Name");
            }
            if(!mFirstName.isEmpty() && !mLastName.isEmpty() &&
                    !mWhoAreYouVisiting.isEmpty() && !mReason.isEmpty()) {
                // take photo and send information through email
                Visitor visitor = new Visitor();
                visitor.setFirstName(mFirstName);
                visitor.setLastName(mLastName);
                visitor.setCompany(mCompany);
                visitor.setWhoAreYouVisiting(mWhoAreYouVisiting);
                visitor.setReason(mReason);
                visitor.setCheckInTime(mCheckInTime);
                visitor.setCheckedIn(true);
                visitor.setCheckOutTime("none");
                addDataToFirebase(visitor);

                takePhoto();
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {

                    sendEmail();

                    Intent i = new Intent(MainActivity.this, ConfirmationScreen.class);
                    startActivity(i);
                    finish();

                }, 2000);

//                handler.postDelayed(() -> {
//                    try {
//                        deletePhoto();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }, 2000);


            }

        });

        btnCheckOut.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, VisitorsCheckout.class);
            startActivity(i);
            finish();
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        cameraExecutor = Executors.newSingleThreadExecutor();

    }

    private void sendEmail() {
        final String username="3plwinnerwms@gmail.com";
        final String password="cjptjqoojmkrpdql";
        final String recipient = "jorgem@3plwinner.com";
        String messageToSend = "<br><b>Visitor name: </b>" + mFirstName + " " +mLastName +
                "<br><b>Company: </b>" + mCompany +
                "<br><b>Who is visitor seeing: </b>" + mWhoAreYouVisiting +
                "<br><b>Reason of visit: </b>" + mReason +
                "<br><b>Emergency contact name: </b>" + mEmergencyContactName+
                "<br><b>Emergency contact phone number: </b>" + mEmergencyContactPhone +
                "<br><b>Checked in at: </b>" + mCheckInTime;
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));


            message.setSubject("3PLWINNER Visitor Check-In - "+ getCurrentTime());

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart attachment = new MimeBodyPart();

            filePath = getPath(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.normalizeScheme());
            assert filePath != null;
            DataSource source = new FileDataSource(filePath);

            attachment.setDataHandler(new DataHandler(source));
            Log.d(TAG, filePath);
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(messageToSend, "text/html");
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachment);

            message.setContent(multipart);

            Transport.send(message);

        }catch (MessagingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(("MM/dd/yyyy HH:mm:ss"));
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    private void takePhoto() {
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) return;

        SimpleDateFormat nameFormat = new SimpleDateFormat(FILENAME_FORMAT, Locale.US);
        String fileName = nameFormat.format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        imageCapture.takePicture(
                outputOptions,
                Executors.newSingleThreadExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String msg = "Photo capture succeeded: " + outputFileResults.getSavedUri();
                        Log.d(TAG, msg);
                    }
                });
    }
    private void deletePhoto() throws IOException {
        File file = new File(filePath);
        file.delete();

    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, imageCapture).getCameraInfo();
                } catch (Exception exception) {
                    Log.e(TAG, "Use case binding failed", exception);
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "CameraProviderFuture exception", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");

                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.d(TAG, "------------------------------------------------------------------");

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {

            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            cursor.moveToLast();
            if (cursor != null) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        pbLoading.setVisibility(View.GONE);

    }

    private void addDataToFirebase(Visitor _visitor) {

        db.collection("Visitors").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                int id = Integer.parseInt(list.get(list.size() - 1).getId());
                id++;
                nextDocumentId = String.valueOf(id);

                _visitor.setDocumentId(String.valueOf(nextDocumentId));
                db.collection("Visitors").document(String.valueOf(nextDocumentId)).set(_visitor).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });

    }
}