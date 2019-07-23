package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class MainActivity extends AppCompatActivity {
    private boolean signUpMode = true;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Login");

        passwordField = (EditText) findViewById(R.id.password);
        passwordField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                //check if enter key is being pressed down
                if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    buttonClick(view);
                }

                return false;
            }
        });

        //hide keyboard on clicking main layout
        ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

        if (ParseUser.getCurrentUser() != null) {
            redirect();
        }
    }

    public void textSwitch(View view) {
        Button button = (Button) findViewById(R.id.button);
        TextView textView = (TextView) findViewById(R.id.textView);

        String temp = button.getText().toString();

        button.setText(textView.getText());
        textView.setText(temp);

        signUpMode ^= true;//flip the value for signUpMode
    }

    public void buttonClick(View view) {
        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = passwordField.getText().toString();
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        if (username.equals("") || password.equals("")) {
            toaster("Username and password required", false);
            return;
        }

        if (signUpMode) {
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i("Signup", "Successful");
                        redirect();
                    } else {
                        toaster(e.getLocalizedMessage(), false);
                    }
                }
            });
        } else {
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        Log.i("Log In", "Successful");
                        if (ParseUser.getCurrentUser() != null) {
                            Log.i("Log In", "Same user signed in");
                        }
                        redirect();
                    } else {
                        toaster(e.getLocalizedMessage(), false);
                    }
                }
            });
        }
    }

    private void toaster(String string, boolean longToast) {
        if (longToast) {
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
        }
        //getApplicationContext() gets context of app
    }

    public void redirect(){
        Intent i = new Intent(getApplicationContext(), UserList.class);
        startActivity(i);
    }
}
