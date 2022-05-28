package com.example.todolistapp.ReLoginActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolistapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener{

    private ProgressBar progressBar;
    private EditText newPass, confirmPass;
    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        newPass = findViewById(R.id.newPass);
        confirmPass = findViewById(R.id.confirmPass);
        Button changePassBtn = findViewById(R.id.changePassBtn);

        progressBar = findViewById(R.id.progressBar);
        changePassBtn.setOnClickListener(this);

        SetToggleButtonInPasswordInput();
        SetToggleButtonInConfirmPasswordInput();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetToggleButtonInPasswordInput() {
        newPass.setOnTouchListener((View view, @SuppressLint("ClickableViewAccessibility") MotionEvent motionEvent) -> {
            final int Right = 2;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (motionEvent.getRawX() >= newPass.getRight() - newPass.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = confirmPass.getSelectionEnd();
                    if (passwordVisible) {
                        newPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                        newPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        newPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                        newPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }
                    newPass.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetToggleButtonInConfirmPasswordInput() {
        confirmPass.setOnTouchListener((View view, @SuppressLint("ClickableViewAccessibility") MotionEvent motionEvent) -> {
            final int Right = 2;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (motionEvent.getRawX() >= confirmPass.getRight() - confirmPass.getCompoundDrawables()[Right].getBounds().width()) {
                    int selection = confirmPass.getSelectionEnd();
                    if (passwordVisible) {
                        confirmPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                        confirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        confirmPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                        confirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }
                    confirmPass.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    @SuppressLint("ShowToast")
    @Override
    public void onClick(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = Objects.requireNonNull(user).getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        final String newPassEnter = newPass.getText().toString();
        final String confirmPassEnter = confirmPass.getText().toString();

        if (TextUtils.isEmpty(newPassEnter)) {
            newPass.setError("Please enter your new password");
            newPass.requestFocus();
            return;
        }

        if (newPassEnter.length() < 6) {
            newPass.setError("The password must be at least 6 characters");
            newPass.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassEnter)) {
            confirmPass.setError("Please confirm your new password");
            confirmPass.requestFocus();
            return;
        }

        if (!newPassEnter.equals(confirmPassEnter)) {
            confirmPass.setError("The password must be the same");
            confirmPass.requestFocus();
            return;
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().child("password").setValue(confirmPassEnter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
            }
        });

        progressBar.setVisibility(View.VISIBLE);


        user.updatePassword(confirmPassEnter)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ChangePasswordActivity.this, "Change password successfully", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ChangePasswordActivity.this, "Failed to change password, some errors has occurred", Toast.LENGTH_SHORT).show()
                );
        progressBar.setVisibility(View.GONE);

    }
}

