package com.cilic.zlatan.travelhop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import utils.ProgressDialogHandler;

public class AuthActivity extends AppCompatActivity {

    RelativeLayout signInLayout;
    RelativeLayout signUpLayout;
    EditText signInEmailEditText;
    EditText signInPasswordEditText;
    TextView signInTextView;
    TextView signUpTextView;
    Button signInButton;
    View lineSeparatorSignIn;
    View lineSeparatorSignUp;
    RelativeLayout bottomLayoutSignIn;
    RelativeLayout bottomLayoutSignUp;
    Button signUpButton;
    EditText signUpEmailEditText;
    EditText signUpFullNameEditText;
    EditText signUpUsernameEditText;
    EditText signUpPasswordEditText;
    ProgressDialog progressDialog;
    ProgressDialogHandler progressDialogHandler;

    boolean emailValid = false;
    boolean passwordValid = false;
    boolean keyboardOpen = false;
    boolean signUpEmailValid = false;
    boolean signUpFullNameValid = false;
    boolean signUpPasswordValid = false;
    boolean signUpUsernameValid = false;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private final String TAG = AuthActivity.class.getName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);


        signInLayout = (RelativeLayout) findViewById(R.id.sign_in_layout);
        signUpLayout = (RelativeLayout) findViewById(R.id.sign_up_layout);
        signInTextView = (TextView) findViewById(R.id.sign_in_textview);
        signUpTextView = (TextView) findViewById(R.id.sign_up_textview);
        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInEmailEditText = (EditText) findViewById(R.id.email_edittext);
        signInPasswordEditText= (EditText) findViewById(R.id.password_edittext);
        lineSeparatorSignIn = findViewById(R.id.line_separator_sign_in);
        lineSeparatorSignUp = findViewById(R.id.line_separator_sign_up);
        bottomLayoutSignIn = (RelativeLayout) findViewById(R.id.bottom_layout_auth);
        bottomLayoutSignUp = (RelativeLayout) findViewById(R.id.bottom_layout_sign_up_auth);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpEmailEditText = (EditText) findViewById(R.id.email_signup_edittext);
        signUpFullNameEditText = (EditText) findViewById(R.id.full_name_signup_edittext);
        signUpUsernameEditText = (EditText) findViewById(R.id.username_signup_edittext);
        signUpPasswordEditText = (EditText) findViewById(R.id.password_signup_edittext);

        progressDialogHandler = new ProgressDialogHandler();

        signInButton.setEnabled(false);
        signUpButton.setEnabled(false);

        signInLayout.setVisibility(View.GONE);

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInLayout.setVisibility(View.GONE);
                signUpLayout.setVisibility(View.VISIBLE);
            }
        });

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpLayout.setVisibility(View.GONE);
                signInLayout.setVisibility(View.VISIBLE);
            }
        });

        signInEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentEmail = s.toString();
                emailValid = Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches();
                validateSignIn();
            }
        });

        signInPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentPassword = s.toString();
                passwordValid = (currentPassword.length() >= 8);
                validateSignIn();
            }
        });

        signUpEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentEmail = s.toString();
                signUpEmailValid = Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches();
                validateSignUp();
            }
        });

        signUpFullNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentName = s.toString();
                signUpFullNameValid = (currentName.length() >= 1);
                validateSignUp();
            }
        });

        signUpPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentPassword = s.toString();
                signUpPasswordValid = (currentPassword.length() >= 8);
                validateSignUp();
            }
        });

        signUpUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentUsername = s.toString();
                signUpUsernameValid = (currentUsername.length() >= 1);
                validateSignUp();
            }
        });

        final View activityRootView = findViewById(R.id.rootAuthActivity);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);

                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > activityRootView.getRootView().getHeight() / 4 && !keyboardOpen) {
                    keyboardOpen = true;
                    onKeyboardOpen();
                }
                else if (heightDiff < activityRootView.getRootView().getHeight() / 4 && keyboardOpen) {
                    keyboardOpen = false;
                    onKeyboardClosed();
                }
            }
        });


        activityRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });



        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialogHandler.showProgressDialog(progressDialog, AuthActivity.this);
                signIn(signInEmailEditText.getText().toString(), signInPasswordEditText.getText().toString());
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(signUpEmailEditText.getText().toString(), signUpPasswordEditText.getText().toString(), signUpFullNameEditText.getText().toString(), signUpUsernameEditText.getText().toString());
            }
        });

        signInPasswordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    if(signInButton.isEnabled()) {
                        signIn(signInEmailEditText.getText().toString(), signInPasswordEditText.getText().toString());
                    }
                }
                return false;
            }
        });

        signUpUsernameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    if(signUpButton.isEnabled()) {
                        signUp(signUpEmailEditText.getText().toString(), signUpPasswordEditText.getText().toString(), signUpFullNameEditText.getText().toString(), signUpUsernameEditText.getText().toString());
                    }
                }
                return false;
            }
        });


    }

    private void onKeyboardOpen() {
        keyboardOpen = true;
        bottomLayoutSignIn.setVisibility(View.GONE);
        bottomLayoutSignUp.setVisibility(View.GONE);
        lineSeparatorSignIn.setVisibility(View.GONE);
        lineSeparatorSignUp.setVisibility(View.GONE);
    }

    private void onKeyboardClosed() {
        keyboardOpen = false;
        bottomLayoutSignIn.setVisibility(View.VISIBLE);
        bottomLayoutSignUp.setVisibility(View.VISIBLE);
        lineSeparatorSignIn.setVisibility(View.VISIBLE);
        lineSeparatorSignUp.setVisibility(View.VISIBLE);
    }

    private void validateSignIn() {
        if(emailValid && passwordValid) {
            signInButton.setEnabled(true);
            signInButton.setBackgroundResource(R.drawable.sign_in_up_enabled);
            signInButton.setTextColor(Color.parseColor("#ffffff"));
        }
        else {
            signInButton.setEnabled(false);
            signInButton.setBackgroundResource(R.drawable.login_button);
            signInButton.setTextColor(Color.parseColor("#97bdd9"));
        }
    }

    private void validateSignUp() {
        if(signUpEmailValid && signUpFullNameValid && signUpPasswordValid && signUpUsernameValid) {
            signUpButton.setEnabled(true);
            signUpButton.setBackgroundResource(R.drawable.sign_in_up_enabled);
            signUpButton.setTextColor(Color.parseColor("#ffffff"));
        }
        else {
            signUpButton.setEnabled(false);
            signUpButton.setBackgroundResource(R.drawable.login_button);
            signUpButton.setTextColor(Color.parseColor("#97bdd9"));
        }
    }

    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            progressDialogHandler.hideProgressDialog(progressDialog);
                            updateUI(currentUser);
                        }
                        else {
                            Toast.makeText(AuthActivity.this, "Email and/or password are incorrect",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signOut() {
        firebaseAuth.signOut();
        updateUI(null);
    }

    private void signUp(String email, String password, final String fullName, final String username) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()) {
                            Log.i(TAG, "User creation success!");
                            final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName).build();
                            currentUser.updateProfile(profileUpdates);
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("fullName", fullName);
                            map.put("username", username);
                            databaseReference.child("userDetails").child(currentUser.getUid()).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Bitmap imageForUpload = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_avatar);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    imageForUpload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] data = baos.toByteArray();
                                    final String img_path = "userProfileImages/" + currentUser.getUid();

                                    StorageReference imageRef = storageReference.child(img_path);
                                    UploadTask uploadTask = imageRef.putBytes(data);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            updateUI(currentUser);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            });


                        }
                        if (!task.isSuccessful()) {
                            Toast.makeText(AuthActivity.this, "Sign Up failed - try again later",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            MyFirebaseService.sendRegistrationToServer(user.getUid());
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            AuthActivity.this.finish();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
