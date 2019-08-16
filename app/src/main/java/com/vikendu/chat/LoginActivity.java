package com.vikendu.chat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


public class LoginActivity extends AppCompatActivity {

    private String TAG = "Firebase";

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private FirebaseAuth mAuth;
    private SharedPreferences loginPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.login_email);
        mPasswordView = (EditText) findViewById(R.id.login_password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();

        /*FOLLOWING CODE IS TO IMPLEMENT NOTIFICATIONS!!!!!!*/

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, token);
                        //Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        /*DELETE IF ISSUES OCCUR!!*/

//        if(mAuth.getCurrentUser() != null)
//        {
//            Intent mainIntent = new Intent(LoginActivity.this, MainChatActivity.class);
//            startActivity(mainIntent);
//            finish();
//        }
//        else{
//            attemptLogin();
//        }

//        androidColors[] = getResources().getIntArray(R.array.androidcolors);

        loginPref = getSharedPreferences("login",MODE_PRIVATE);

        if(loginPref.getBoolean("logged",false)){
            goToChatActivity();
        }

    }

    // Executed when Sign in button pressed
    public void signInExistingUser(View v)   {
        attemptLogin();

    }

    // Executed when Register button pressed
    public void registerNewUser(View v) {
        Intent intent = new Intent(this, com.vikendu.chat.RegisterActivity.class);
        finish();
        startActivity(intent);
    }

    private void attemptLogin() {

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if(email.equals("") || password.equals(""))
        {
            return;
        }
        else
        {
            Toast.makeText(this, "Login in progress", Toast.LENGTH_SHORT).show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("Login", "Login" + task.isSuccessful());

                    if(!task.isSuccessful())
                    {
                        Log.d("Login","Login failed" + task.getException());
                        showLoginFailed("Failed to Login");
                    }
                    else
                    {
                        loginPref.edit().putBoolean("logged",true).apply();
                        goToChatActivity();
                    }
                }
            });
        }
    }

    private void goToChatActivity()
    {
        Intent intent = new Intent(LoginActivity.this, MainChatActivity.class);
        finish();
        startActivity(intent);
    }

    private void showLoginFailed(String message)
    {
        new AlertDialog.Builder(this)
                .setTitle("Error Logging in...")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}