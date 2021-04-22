package com.example.socket.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.socket.R;
import com.example.socket.activities.MessageActivity;
import com.example.socket.socketiochat.AppSocketListener;
import com.example.socket.socketioservice.SocketEventConstants;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>{
    String TAG = "FriendsAdapter";

    private Cursor mCursor;

    List<String> cursorColumns = new ArrayList<>();

    public FriendsAdapter(List<String> columns) {
        cursorColumns = columns;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.userlist_name, viewGroup, false);

        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendViewHolder personViewHolder, final int position) {
        mCursor.moveToPosition(position);
        personViewHolder.personName.setText(mCursor.getString(cursorColumns.indexOf("name")));
        if(mCursor.getString(cursorColumns.indexOf("type")).equals("friend")) {
            personViewHolder.acceptButton.setVisibility(View.GONE);
            personViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(position);
                    Intent conversationIntent = new Intent(view.getContext(), MessageActivity.class);
                    conversationIntent.putExtra("conversationId", "");
                    conversationIntent.putExtra("userId", mCursor.getString(cursorColumns.indexOf("uid")));
                    view.getContext().startActivity(conversationIntent);
                }
            });

        }
        else if(mCursor.getString(cursorColumns.indexOf("type")).equals("nothing")) {

            personViewHolder.acceptButton.setText("Add");
            personViewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(position);
                    AppSocketListener.getInstance().emit(SocketEventConstants.sendFriendRequest, FirebaseAuth.getInstance().getCurrentUser().getUid(), mCursor.getString(cursorColumns.indexOf("uid")));
                }
            });
        }
        else{
            personViewHolder.acceptButton.setText("Accept");
            personViewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(position);
                    AppSocketListener.getInstance().emit(SocketEventConstants.acceptFriend, FirebaseAuth.getInstance().getCurrentUser().getUid(), mCursor.getString(cursorColumns.indexOf("uid")));
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor != null) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder{


        Context context;
        TextView personName;
        RelativeLayout relativeLayout;
        Button acceptButton;

        FriendViewHolder(View view) {
            super(view);
            personName = (TextView)itemView.findViewById(R.id.person_name);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.user_list_element);
            acceptButton = (Button) itemView.findViewById(R.id.acceptButton);
            context = itemView.getContext();
        }
    }
}