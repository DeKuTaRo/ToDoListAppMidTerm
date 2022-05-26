package com.example.todolistapp.PostLoginActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import com.example.todolistapp.Models.NoteItem;
import com.example.todolistapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText label_update, subtitle_update, textContent_update;
    private TextView textDateTime_update, textWebURL_update;
    private View viewSubtitleIndicator_update;
    private ImageView imageBack, imageUpdate, imageSetPassword, imageNote_update, imageNotification;
    private LinearLayout layoutWebURL_update, layoutDeleteVideo_update;
    private VideoView videoView_update;

    private String selectedNoteColor;
    private String selectedImagePath;

    private AlertDialog dialogURL;
    private Uri imageUriUpdate, videoUriUpdate;
    private String imageUriTaskUpdate, videoUriTaskUpdate;


    final Calendar calendar = Calendar.getInstance();

    DatabaseReference reference;
    StorageReference storageImageReference, storageVideoReference;
    private FirebaseDatabase rootNode;
    private FirebaseStorage storage;
    private String userID;

    private String idNote, label, subtitle, textContent, mdate, color, image, video, web;
    private NoteItem noteItem;

    private NotificationCompat managerCompat;
    private boolean passwordVisible;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE = 3;
    private static final int REQUEST_CODE_SELECT_VIDEO = 4;
    private static final int REQUEST_CODE_STORAGE_VIDEO_PERMISSION = 5;


    public static final int UPDATE_NOTE = 8;
    public static final int SET_PASSWORD = 9;
    public static final int REMOVE_PASSWORD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        storage = FirebaseStorage.getInstance();

        layoutWebURL_update = findViewById(R.id.layoutWebURL_update);

        label_update = findViewById(R.id.label_update);
        subtitle_update = findViewById(R.id.subtitle_update);
        textContent_update = findViewById(R.id.textContent_update);
        textDateTime_update = findViewById(R.id.textDateTime_update);
        textWebURL_update = findViewById(R.id.textWebURL_update);

        viewSubtitleIndicator_update = findViewById(R.id.viewSubtitleIndicator_update);

        imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(this);

        imageUpdate = findViewById(R.id.imageUpdate);
        imageUpdate.setOnClickListener(this);

        imageSetPassword = findViewById(R.id.imageSetPassword);
        imageSetPassword.setOnClickListener(this);

        imageNotification = findViewById(R.id.imageNotification);
        imageNotification.setOnClickListener(this);

        imageNote_update = findViewById(R.id.imageNote_update);

        layoutDeleteVideo_update = findViewById(R.id.layoutDeleteVideo_update);
        videoView_update = findViewById(R.id.videoView_update);

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        noteItem = (NoteItem) getIntent().getSerializableExtra("noteItems");

        findViewById(R.id.imageRemoveWebURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebURL_update.setVisibility(View.GONE);
                layoutWebURL_update.setVisibility(View.GONE);
                idNote = noteItem.getLabel();
                textWebURL_update.setText("");

                userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                reference = FirebaseDatabase.getInstance().getReference("Users").
                        child(userID).child("NoteItems");

                reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getRef().child("webLink").setValue("");
                        Toast.makeText(UpdateActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote_update.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                Picasso.get().load((Uri) null).into(imageNote_update);
//                noteItem.setImagePath("");
                idNote = noteItem.getLabel();

                userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                reference = FirebaseDatabase.getInstance().getReference("Users").
                        child(userID).child("NoteItems");

                StorageReference imageReference = storage.getReferenceFromUrl(noteItem.getImagePath());
                imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().child("imagePath").setValue("");
                                Toast.makeText(UpdateActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        });

        findViewById(R.id.imageRemoveVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDeleteVideo_update.setVisibility(View.GONE);
                videoView_update.setVideoURI(null);
                videoView_update.setVisibility(View.GONE);

                idNote = noteItem.getLabel();

                userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                reference = FirebaseDatabase.getInstance().getReference("Users").
                        child(userID).child("NoteItems");

                StorageReference videoReference = storage.getReferenceFromUrl(noteItem.getVideoPath());
                videoReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().child("videoPath").setValue("");
                                Toast.makeText(UpdateActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        });

        setValueIntent();
        initMiscellaneous();
        setSubtitleIndicator();
//        managerCompat = NotificationManagerCompat.from(this);
    }


    private void setValueIntent() {

        viewSubtitleIndicator_update.setBackgroundColor(Color.parseColor(noteItem.getColor()));
        label_update.setText(noteItem.getLabel());
        subtitle_update.setText(noteItem.getSubtitle());
        textContent_update.setText(noteItem.getText_content());
        textDateTime_update.setText(noteItem.getDate());

        if (noteItem.getImagePath() != null && !noteItem.getImagePath().trim().isEmpty()) {
            Picasso.get().load(noteItem.getImagePath())
                    .into(imageNote_update);
            imageNote_update.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath = noteItem.getImagePath();
        }

        if (noteItem.getWebLink() != null && !noteItem.getWebLink().trim().isEmpty()) {
            textWebURL_update.setText(noteItem.getWebLink());
            layoutWebURL_update.setVisibility(View.VISIBLE);
        }

        if (noteItem.getVideoPath() != null && !noteItem.getVideoPath().trim().isEmpty()) {
            layoutDeleteVideo_update.setVisibility(View.VISIBLE);
            videoView_update.setVisibility(View.VISIBLE);
            videoView_update.setVideoURI(Uri.parse(noteItem.getVideoPath()));
            videoView_update.start();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageBack:
                startActivity(new Intent(UpdateActivity.this, NoteActivity.class));
                break;
            case R.id.imageUpdate:
                updateData();
                break;
            case R.id.imageSetPassword :
                setPasswordNote();
                break;
            case R.id.imageNotification:
                setNotification();
                break;
        }
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_check);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_check);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_check);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3A52Fc";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_check);
                imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_check);
                setSubtitleIndicator();
            }
        });

        if (noteItem != null && noteItem.getColor() != null && !noteItem.getColor().trim().isEmpty()) {
            switch (noteItem.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpdateActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION);
                }
                else {
                    selectImage();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpdateActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_VIDEO_PERMISSION);
                }
                else {
                    selectVideo();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddVoice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                addVoice();
            }
        });
    }

    private void setSubtitleIndicator() {
        ColorDrawable colorDrawable = (ColorDrawable) viewSubtitleIndicator_update.getBackground();
        colorDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    private void selectVideo() {
        startActivityForResult(Intent.createChooser(new Intent().
                                setAction(Intent.ACTION_GET_CONTENT).
                                setType("video/mp4"),
                        "Select a video"),
                REQUEST_CODE_SELECT_VIDEO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_CODE_STORAGE_VIDEO_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectVideo();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUriUpdate = data.getData();
                if(imageUriUpdate != null) {
                    try {
                        Picasso.get().load(imageUriUpdate).into(imageNote_update);

                        imageNote_update.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == RESULT_OK) {
            if (data != null) {
                videoUriUpdate = data.getData();
                if (videoUriUpdate != null) {
                    try {
                        layoutDeleteVideo_update.setVisibility(View.VISIBLE);
                        videoView_update.setVisibility(View.VISIBLE);
                        videoView_update.setVideoURI(videoUriUpdate);
                        videoView_update.start();

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


    private void showAddURLDialog() {
        if (dialogURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);
            dialogURL = builder.create();
            if (dialogURL.getWindow() != null) {
                dialogURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(UpdateActivity.this, "Please enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                        Toast.makeText(UpdateActivity.this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebURL_update.setText(inputURL.getText().toString());
                        layoutWebURL_update.setVisibility(View.VISIBLE);
                        dialogURL.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogURL.dismiss();
                }
            });
        }
        dialogURL.show();
    }

    @SuppressLint("ShowToast")
    public void updateData() {
        int id = noteItem.getID();
        idNote = noteItem.getLabel();

        noteItem.setColor(selectedNoteColor);

        label = label_update.getText().toString().trim();
        textContent = textContent_update.getText().toString().trim();
        subtitle = subtitle_update.getText().toString().trim();
        mdate = textDateTime_update.getText().toString().trim();
        color = noteItem.getColor();
        image = noteItem.getImagePath();
        video = noteItem.getVideoPath();
        web = textWebURL_update.getText().toString().trim();


        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").
                child(userID).child("NoteItems");

        if (imageNote_update.getDrawable() == null && videoView_update.getVisibility() == View.GONE) {
            noteItem = new NoteItem(id, label, subtitle, textContent, mdate, color, "", "", web);
            reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    snapshot.getRef().child("id").setValue(id);
                    snapshot.getRef().child("label").setValue(label);
                    snapshot.getRef().child("subtitle").setValue(subtitle);
                    snapshot.getRef().child("date").setValue(mdate);
                    snapshot.getRef().child("color").setValue(color);
                    snapshot.getRef().child("imagePath").setValue("");
                    snapshot.getRef().child("videoPath").setValue("");
                    snapshot.getRef().child("webLink").setValue(web);

                    Toast.makeText(UpdateActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

        }
        else {
            storageImageReference = FirebaseStorage.getInstance().getReference("images");
            StorageReference imageReference = storageImageReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageUriUpdate));

            imageReference.putFile(imageUriUpdate).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        imageUriTaskUpdate = task.getResult().toString();
                        noteItem = new NoteItem(id, label, subtitle, textContent, mdate, color, imageUriTaskUpdate, videoUriTaskUpdate, web);
                        reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                snapshot.getRef().child("id").setValue(id);
                                snapshot.getRef().child("label").setValue(label);
                                snapshot.getRef().child("subtitle").setValue(subtitle);
                                snapshot.getRef().child("date").setValue(mdate);
                                snapshot.getRef().child("color").setValue(color);
                                snapshot.getRef().child("imagePath").setValue(imageUriTaskUpdate);
                                snapshot.getRef().child("videoPath").setValue(videoUriTaskUpdate);
                                snapshot.getRef().child("webLink").setValue(web);

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            storageVideoReference = FirebaseStorage.getInstance().getReference("videos");
            StorageReference videoReference = storageVideoReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(videoUriUpdate));
            videoReference.putFile(videoUriUpdate).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return videoReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    videoUriTaskUpdate = task.getResult().toString();
                    noteItem = new NoteItem(id, label, subtitle, textContent, mdate, color, imageUriTaskUpdate, videoUriTaskUpdate, web);
                    reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            snapshot.getRef().child("id").setValue(id);
                            snapshot.getRef().child("label").setValue(label);
                            snapshot.getRef().child("subtitle").setValue(subtitle);
                            snapshot.getRef().child("date").setValue(mdate);
                            snapshot.getRef().child("color").setValue(color);
                            snapshot.getRef().child("imagePath").setValue(imageUriTaskUpdate);
                            snapshot.getRef().child("videoPath").setValue(videoUriTaskUpdate);
                            snapshot.getRef().child("webLink").setValue(web);

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        Toast.makeText(UpdateActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent();
        intent.putExtra("note", noteItem);
        setResult(UPDATE_NOTE, intent);
        finish();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @SuppressLint("SetTextI18n")
    private void setNotification() {

        final EditText mtimePicker = new EditText(this);
        mtimePicker.setHint("Choose time");
        mtimePicker.setClickable(false);
        mtimePicker.setFocusable(false);
        mtimePicker.setCursorVisible(false);
        mtimePicker.setFocusableInTouchMode(false);

        mtimePicker.setOnClickListener(view -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(UpdateActivity.this, (timePicker, selectedHour, selectedMinute) ->
                    mtimePicker.setText( selectedHour + ":" + selectedMinute), hour, minute, true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });


        final EditText mdatePicker = new EditText(this);
        mdatePicker.setHint("Choose date");
        mdatePicker.setClickable(false);
        mdatePicker.setFocusable(false);
        mdatePicker.setCursorVisible(false);
        mdatePicker.setFocusableInTouchMode(false);

        DatePickerDialog.OnDateSetListener date = (datePicker, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            mdatePicker.setText(sdf.format(calendar.getTime()));
        };

        mdatePicker.setOnClickListener(view ->
                new DatePickerDialog(UpdateActivity.this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.addView(mtimePicker);
        ll.addView(mdatePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Set time and date for note")
                .setView(ll)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
//                        String time = mtimePicker.getText().toString();
//                        String date = mdatePicker.getText().toString();
                    Toast.makeText(UpdateActivity.this, "Get notification successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    Toast.makeText(UpdateActivity.this, "No notification", Toast.LENGTH_SHORT).show();

                    dialogInterface.dismiss();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setPasswordNote() {
        final EditText passwordNote = new EditText(this);
        passwordNote.setHint("Enter password here");
        passwordNote.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);

        passwordNote.setOnTouchListener((View view, @SuppressLint("ClickableViewAccessibility") MotionEvent motionEvent) -> {
            final int Right = 2;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (motionEvent.getRawX() >= passwordNote.getRight() - passwordNote.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = passwordNote.getSelectionEnd();
                    if (passwordVisible) {
                        passwordNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                        passwordNote.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        passwordNote.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                        passwordNote.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }
                    passwordNote.setSelection(selection);
                    return true;
                }
            }
            return false;
        });

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.addView(passwordNote);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Set password for note")
                .setView(ll)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String passwordNoteValue = passwordNote.getText().toString().trim();

                        int id = noteItem.getID();
                        idNote = noteItem.getLabel();

                        noteItem = new NoteItem(id, "");

                        noteItem.setPasswordNote(passwordNoteValue);

                        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        reference = FirebaseDatabase.getInstance().getReference("Users").
                                child(userID).child("NoteItems");

                        reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                snapshot.getRef().child("id").setValue(id);
                                snapshot.getRef().child("passwordNote").setValue(passwordNoteValue);
                                Toast.makeText(UpdateActivity.this, "Set password successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                        Intent intent = new Intent();
                        intent.putExtra("note", noteItem);
                        setResult(SET_PASSWORD, intent);
                        finish();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int id = noteItem.getID();
                        idNote = noteItem.getLabel();

                        noteItem = new NoteItem(id, "");
                        noteItem.setPasswordNote("");

                        userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        reference = FirebaseDatabase.getInstance().getReference("Users").
                                child(userID).child("NoteItems");

                        reference.child(idNote).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                snapshot.getRef().child("id").setValue(id);
                                snapshot.getRef().child("passwordNote").setValue("");
                                Toast.makeText(UpdateActivity.this, "Remove password successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UpdateActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                        Intent intent = new Intent();
                        intent.putExtra("note", noteItem);
                        setResult(REMOVE_PASSWORD, intent);
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void addVoice() {
        Toast.makeText(UpdateActivity.this, "This function will come soon", Toast.LENGTH_SHORT).show();
    }

}