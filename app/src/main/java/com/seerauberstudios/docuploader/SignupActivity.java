package com.seerauberstudios.docuploader;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.seerauberstudios.docuploader.util.ParseConstants;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {

    @InjectView(R.id.signup_username)EditText userName;
    @InjectView(R.id.signup_password)EditText userPassword;
    @InjectView(R.id.signup_customerid)EditText customerID;
    @InjectView(R.id.signup_progressbar)ProgressBar progressBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.inject(this);


    }

    @OnClick(R.id.signup_regbutton)
    void Register(){
        String username = userName.getText().toString();
        String password = userPassword.getText().toString();
        String customerId = customerID.getText().toString();

        username = username.trim();
        password = password.trim();
        customerId = customerId.trim();

        if(username.isEmpty() || password.isEmpty() || customerId.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
            builder.setMessage(R.string.signup_error_message).setTitle(R.string.signup_error_title).setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            //create the user
            ParseUser newUser = new ParseUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.put(ParseConstants.KEY_CUSTOMER_ID,customerId);
            newUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    progressBar.setVisibility(View.GONE);

                    if( e == null){                        //Success!

                        DocUploaderApplication.updateParseInstallation(ParseUser.getCurrentUser());
                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                        builder.setMessage(e.getMessage()).setTitle(R.string.signup_error_title).setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });

        }


    }

}
