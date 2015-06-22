package com.seerauberstudios.docuploader;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by brando on 6/20/15.
 */


public class ViewDocumentActivity extends AppCompatActivity {

    @InjectView(R.id.docviewer_imageview)ImageView docImage;

    @InjectView(R.id.docviewer_progressbar)ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdocument);

        ButterKnife.inject(this);

        Uri imageUri = getIntent().getData();

        Picasso.with(this).load(imageUri.toString()).fit().centerCrop().into(docImage,new Callback.EmptyCallback() {
            @Override public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError() {
                progressBar.setVisibility(View.GONE);
            }
        });


    }
}
