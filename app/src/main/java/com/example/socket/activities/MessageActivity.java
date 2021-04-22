package com.example.socket.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socket.R;
import com.example.socket.data.DatabaseContract;
import com.example.socket.recyclerview.MessagesAdapter;
import com.example.socket.socketiochat.AppContext;
import com.example.socket.socketiochat.AppSocketListener;
import com.example.socket.socketioservice.SocketEventConstants;
import com.google.firebase.auth.FirebaseAuth;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

public class MessageActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    String TAG = "MessageActivity";

    private static final int ID_LOADER = 33;


    private String conversationId;
    private String userId;

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;

    private MessagesAdapter mAdapter;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Bundle extras = getIntent().getExtras();

        conversationId = extras.getString("conversationId");
        userId = extras.getString("userId");

        auth = FirebaseAuth.getInstance();

        mMessagesView = (RecyclerView) findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.message_toolbar);
        toolbar.setTitle(getUserName());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        mInputMessageView = (EditText) findViewById(R.id.message_input);

        ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        if (!conversationId.equals("") && conversationId != null) {
            AppSocketListener.getInstance().addOnHandler(SocketEventConstants.queryMessagesResponse, AppContext.getEmitterListeners().onMessagesListReceive);
            AppSocketListener.getInstance().emit(SocketEventConstants.queryMessages, conversationId);
        } else {
            queryConversation();
        }
        mAdapter = new MessagesAdapter(auth.getCurrentUser().getUid(), new ArrayList<String>() {{
            add("uid");
            add("sender");
            add("message");
            add("timestamp");
        }});
        mMessagesView.setAdapter(mAdapter);


        getLoaderManager().initLoader(ID_LOADER, null, this);
    }

    public String getUserName() {
        Cursor cursor = getApplicationContext().getContentResolver().query(DatabaseContract.Friends.CONTENT_URI,
                new String[]{DatabaseContract.Friends.COLUMN_NAME,},
                DatabaseContract.Friends.COLUMN_UID + " = '" + userId + "'",
                null,
                null);
        cursor.moveToPosition(0);
        return cursor.getString(0);
    }

    public void queryConversation() {
        Cursor cursor = getApplicationContext().getContentResolver().query(DatabaseContract.Conversations.buildConversationSearch(userId),
                new String[]{DatabaseContract.Conversations.COLUMN_UID,},
                DatabaseContract.Conversations.COLUMN_USER_ID,
                null,
                null);
        cursor.moveToPosition(0);
        if (cursor.getCount() > 0) {
            conversationId = cursor.getString(0);
            getLoaderManager().restartLoader(ID_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void attemptSend() {

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }
        mInputMessageView.setText("");

        if (!conversationId.equals("") && conversationId != null) {
            AppSocketListener.getInstance().emit(SocketEventConstants.sendMessage, conversationId, auth.getCurrentUser().getUid(), message);
        } else {
            AppSocketListener.getInstance().addOnHandler(SocketEventConstants.startConversationResponse, onConversationResponse);
            AppSocketListener.getInstance().emit(SocketEventConstants.startConversation, auth.getCurrentUser().getUid(), userId, message);
        }
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri insertContentUri = DatabaseContract.Messages.CONTENT_URI;
        String whereClause = DatabaseContract.Messages.COLUMN_CONVERSATION_ID;
        return new CursorLoader(this,
                insertContentUri,
                new String[]{
                        DatabaseContract.Messages._ID,
                        DatabaseContract.Messages.COLUMN_SENDER_ID,
                        DatabaseContract.Messages.COLUMN_MESSAGE,
                        DatabaseContract.Messages.COLUMN_CREATED_AT,
                },
                whereClause + " = '" + conversationId + "'",
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private Emitter.Listener onConversationResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        conversationId = data.getString("conversationID");
                        getLoaderManager().restartLoader(ID_LOADER, null, MessageActivity.this);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppSocketListener.getInstance().off(SocketEventConstants.startConversationResponse);
                }
            });
        }
    };
}





