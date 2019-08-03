package com.vikendu.chat;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainChatActivity extends AppCompatActivity {

    // TODO: Add member variables here:
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

        // Link the Views in the layout to the Java code
        mInputText = (EditText) findViewById(R.id.messageInput);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        mChatListView = (ListView) findViewById(R.id.chat_list_view);

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


        // TODO: Add an OnClickListener to the sendButton to send a message

    }

    private void setupUsername() {
//        SharedPreferences prefs = getSharedPreferences(RegisterActivity.CHAT_PREFS, MODE_PRIVATE);
//
//        mDisplayName = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY, null);
//        if(mDisplayName == null) mDisplayName = "Anonymous";

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
//        mAdapter = new ChatAdapter(this, mDatabaseRef, mDisplayName);
//        mChatListView.setAdapter(mAdapter);

        new AsyncCaller().execute();
        Log.d("Speed", "Adapter gotten");

    }


    @Override
    public void onStop() {
        super.onStop();

        mAdapter.cleanup();

    }


    private class AsyncCaller extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdLoading = new ProgressDialog(MainChatActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading Messages...");
            pdLoading.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAdapter = new ChatAdapter(MainChatActivity.this, mDatabaseRef, mDisplayName);
            mChatListView.setAdapter(mAdapter);

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //this method will be running on UI thread

            pdLoading.dismiss();
        }

    }
}
