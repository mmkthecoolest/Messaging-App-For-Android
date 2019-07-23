package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String recipient;
    private Handler handler;
    private ArrayList<String> messages;
    private ListView chat;
    private ArrayAdapter<String> adapter;
    private int delay = 1000;
    private int oldSize;

    public void getMessages(){
        if(ParseUser.getCurrentUser() != null) {
            Log.i("Info", "Handler ping");
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
            List<String> criteriaList = new ArrayList<String>();
            criteriaList.add(recipient);
            criteriaList.add(ParseUser.getCurrentUser().getUsername());

            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername())
                    .whereContainedIn("username", criteriaList)
                    .whereContainedIn("recipient", criteriaList)
                    .orderByAscending("createdAt");

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects.size() > 0) {
                        messages.clear();
                        for (ParseObject object : objects) {
                            String sender;

                            if (object.get("username").toString().equals(ParseUser.getCurrentUser().getUsername())) {
                                sender = "You:\n\n";
                            } else {
                                sender = object.get("username").toString() + ":\n\n";
                            }

                            sender += object.get("message");

                            messages.add(sender);

                        }

                    } else if (objects == null || objects.size() == 0) {
                        Log.i("Info", "No messages found");
                    }

                    adapter.notifyDataSetChanged();
                    if (oldSize != messages.size()) {
                        chat.setSelection(adapter.getCount() - 1);
                        oldSize = messages.size();
                    }

                    if (ParseUser.getCurrentUser() != null) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getMessages();
                            }
                        }, delay);
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        recipient = intent.getStringExtra("recipient");

        setTitle(recipient +"'s chat");

        chat = (ListView) findViewById(R.id.chat);
        messages = new ArrayList<String>();
        oldSize = messages.size();

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                messages);
        chat.setAdapter(adapter);

        handler = new Handler();


        if(ParseUser.getCurrentUser() != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getMessages();
                }
            }, delay);
        }
    }

    public void send(View view){
        EditText chatEntry = (EditText) findViewById(R.id.chatEntry);
        ParseObject object = new ParseObject("Message");
        if(chatEntry.getText().toString().equals("")){
            return;
        }

        object.put("message", chatEntry.getText().toString());
        object.put("username", ParseUser.getCurrentUser().getUsername());
        object.put("recipient", recipient);

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    toaster("Message sent", false);
                } else {
                    toaster("Error: " + e.getMessage(), true);
                }
            }
        });
    }

    private void toaster(String string, boolean longToast) {
        if (longToast) {
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
        }
        //getApplicationContext() gets context of app
    }
}
