package com.example.socket.activities;

/**
 * Created by mykha on 8/5/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socket.R;
import com.example.socket.socketiochat.AppContext;
import com.example.socket.socketiochat.AppSocketListener;
import com.example.socket.socketioservice.SocketEventConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;


import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {

    String TAG = "LoginActivity";
    private AppSocketListener appSocketListeners;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appSocketListeners = new AppSocketListener();
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainTabbedActivity.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                            Toast.makeText(LoginActivity.this, "User doesn\'t exist.", Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.e(TAG, "onComplete: " + auth.getCurrentUser().getUid() + ":::" + email);
                                    Log.e(TAG, "onComplete: data    " + SocketEventConstants.loginResponse);
                                    AppSocketListener.getInstance().addOnHandler(SocketEventConstants.loginResponse, AppContext.getEmitterListeners().onLogin);
                                    AppSocketListener.getInstance().addOnHandler(SocketEventConstants.loginResponse, startActivity);
                                    AppSocketListener.getInstance().emit(SocketEventConstants.login, auth.getCurrentUser().getUid(), email);

                                }
                            }
                        });
            }
        });
    }

    public Emitter.Listener startActivity = args -> {
        AppSocketListener.getInstance().off(SocketEventConstants.loginResponse);
        Intent intent = new Intent(LoginActivity.this, MainTabbedActivity.class);
        startActivity(intent);
        finish();
    };


}