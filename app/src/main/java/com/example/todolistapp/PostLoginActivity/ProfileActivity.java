package com.example.todolistapp.PostLoginActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.todolistapp.Models.User;
import com.example.todolistapp.R;
import com.example.todolistapp.ReLoginActivity.ChangePasswordActivity;
import com.example.todolistapp.ReLoginActivity.MainActivity;
import com.example.todolistapp.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseReference reference;
    StorageReference storageImageAvatarReference;
    private String imageAvatarUriTask, fullNameDB;

    private String userID;
    User userProfile;
    private Uri imageAvatarUri;

    private static final int REQUEST_CODE_STORAGE_AVATAR_PERMISSION = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE_AVATAR = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View viewRoot = this.binding.getRoot();
        setContentView(viewRoot);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.reference = FirebaseDatabase.getInstance().getReference("Users");
        this.userID = user.getUid();

        this.reference.child(this.userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfileValue = snapshot.getValue(User.class);

                if (userProfileValue != null) {
                    fullNameDB = userProfileValue.getFullName();
                    String email = userProfileValue.getEmail();
                    String imageAvatar = userProfileValue.getAvatarPath();

                    binding.fullName.setText(fullNameDB);
                    binding.emailAddress.setText(email);

                    if (imageAvatar != null && !imageAvatar.trim().isEmpty()) {
                        Picasso.get().load(imageAvatar).into(binding.imageAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        this.binding.avatarFAB.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_AVATAR_PERMISSION);
            }
            else {
                selectImageAvatar();
            }
        });

        this.binding.imageEditName.setOnClickListener(v -> {
            binding.fullName.setEnabled(true);
            binding.fullName.requestFocus();
        });

        this.binding.changePassBtn.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class)));

        this.binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.saveProfileBtn) {
            saveProfileData();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }



    private void saveProfileData() {

        String fullName = binding.fullName.getText().toString().trim();

        if (binding.imageAvatar.getDrawable() == null) {
            userProfile = new User(fullName, "");
            reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().child("fullName").setValue(fullName);
                    snapshot.getRef().child("avatarPath").setValue("");

                    Toast.makeText(ProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            storageImageAvatarReference = FirebaseStorage.getInstance().getReference("avatar");
            StorageReference imageCoverPhotoReference = storageImageAvatarReference.child(System.currentTimeMillis() +
                    "." + getFileExtension(imageAvatarUri));
            imageCoverPhotoReference.putFile(imageAvatarUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return imageCoverPhotoReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageAvatarUriTask = task.getResult().toString();
                    userProfile = new User(fullName, imageAvatarUriTask);
                    reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            snapshot.getRef().child("fullName").setValue(fullName);
                            snapshot.getRef().child("avatarPath").setValue(imageAvatarUriTask);

                            Toast.makeText(ProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }

        this.binding.fullName.setEnabled(false);

    }
// }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_AVATAR_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageAvatar();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImageAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE_AVATAR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGE_AVATAR && resultCode == RESULT_OK) {
            if (data != null) {
                imageAvatarUri = data.getData();
                if (imageAvatarUri != null) {
                    try {
                        Picasso.get().load(imageAvatarUri).into(binding.imageAvatar);

                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}