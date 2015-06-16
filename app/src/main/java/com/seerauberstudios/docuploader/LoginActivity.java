package com.seerauberstudios.docuploader;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    @InjectView(R.id.login_username)EditText userName;
    @InjectView(R.id.login_password)EditText userPassword;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "GZ5z6s0fvwyHmuG9sVe0XbgRz";
    private static final String TWITTER_SECRET = "zdhyrNqKrviE7abySqzCYUqMWJcw8XlwGJOGSrfqEkwvqXGpBH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.login_digits_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // Do something with the session and phone number
                Toast.makeText(LoginActivity.this, "SUCESS!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void failure(DigitsException exception) {
                // Do something on failure
                Toast.makeText(LoginActivity.this, "Error, something went wrong... Sorry!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @OnClick(R.id.login_register)
    void launchSignup(){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.login_loginButton)
    void Register(){
        String username = userName.getText().toString();
        String password = userPassword.getText().toString();

        username = username.trim();
        password = password.trim();


        if(username.isEmpty() || password.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage(R.string.login_error_message).setTitle(R.string.login_error_title).setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            //Login
            setProgressBarIndeterminateVisibility(true);
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    setProgressBarIndeterminateVisibility(false);

                    if(e == null){
                        //Success!

                        DocUploaderApplication.updateParseInstallation(parseUser);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage(e.getMessage()).setTitle(R.string.login_error_title).setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });

        }

        }




}
