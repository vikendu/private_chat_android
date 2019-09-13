package com.vikendu.chat;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainChatActivity extends AppCompatActivity {

    private String mDisplayName;
    private ListView mChatListView;
    private EditText mInputText;
    private ImageButton mSendButton;
    private DatabaseReference mDatabaseRef;
    private ChatAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        setupUsername();
        Log.d("Speed", "username set up");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        Log.d("Speed", "Database ref gotten");

        mInputText = (EditText) findViewById(R.id.messageInput);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        mChatListView = (ListView) findViewById(R.id.chat_list_view);

        /*Persistant Notification Channel:*/

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Ignored on API 26 and below
            NotificationChannel channel = new NotificationChannel(
                    "1",
                    "Persitant Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);

        }

        /*New Message Notification channel*/

        NotificationManager message =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Ignored on API 26 & below
            NotificationChannel channel = new NotificationChannel(
                    "2",
                    "New Message",
                    NotificationManager.IMPORTANCE_DEFAULT);
            message.createNotificationChannel(channel);
        }

        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                sendMessage();
                return true;
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        SharedPreferences preferences = getSharedPreferences("login",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.clear();
                        editor.commit();

                        goToLogin();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        mSendButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainChatActivity.this);
                builder.setMessage("Are you sure you want to Log Out?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return false;
            }
        });
    }

    private void setupUsername() {

        /*Switched from SharedPreferences to Firebase username storage*/

//        SharedPreferences prefs = getSharedPreferences(RegisterActivity.CHAT_PREFS, MODE_PRIVATE);
//
//        mDisplayName = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY, null);
//        if(mDisplayName == null) mDisplayName = "Anonymous";

        /*Delete for local storage usage && Comment the next 2 lines*/

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDisplayName = user.getDisplayName();

    }

    private void sendMessage() {

        Log.d("Chat", "sendMessage() Called");

        String message_text = mInputText.getText().toString();

        if (!message_text.equals("")) {
            Message chat = new Message(message_text, mDisplayName);
            mDatabaseRef.child("message").push().setValue(chat);
            mInputText.setText("");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new ChatAdapter(this, mDatabaseRef, mDisplayName);
        mChatListView.setAdapter(mAdapter);

        scrollMyListViewToBottom();

        Log.d("Speed", "Adapter gotten");

    }

    @Override
    public void onStop() {
        super.onStop();

        Intent service_intent = new Intent(this,NotificationService.class);
        startService(service_intent);

        mAdapter.cleanup();

    }

    private void scrollMyListViewToBottom() {

        mChatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mChatListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                Log.d("Speed", Integer.toString(mAdapter.getCount()));
                mChatListView.smoothScrollToPosition(mAdapter.getCount() - 1);
            }
        });
    }

    private void goToLogin()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        finish();
        startActivity(intent);

        Intent stopFGService = new Intent(this, NotificationService.class);
        stopService(stopFGService);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent myService = new Intent(this, NotificationService.class);
        stopService(myService);

    }
}
