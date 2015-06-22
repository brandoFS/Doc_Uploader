package com.seerauberstudios.docuploader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.seerauberstudios.docuploader.adapters.ListAdapter;
import com.seerauberstudios.docuploader.util.FileHelper;
import com.seerauberstudios.docuploader.util.ParseConstants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.mainactivity_recycler_view) RecyclerView recyclerView;
    @InjectView(R.id.main_emptylayout)   LinearLayout emptyLayoutContainer;

    public final static String TAG = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 1;

    public static final int MEDIA_TYPE_IMAGE = 4;

    protected Uri MediaURI;

    ParseFile file;
    ParseUser currentUser;


    private ListAdapter listAdapter;
    private GridLayoutManager gridLayoutManager;
    protected SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this); //inject our views

        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        currentUser = ParseUser.getCurrentUser();
        if(currentUser == null) {
            navigateToLogin();
        }
        else{
            Log.i(TAG, currentUser.getUsername());
            retrieveDocuments();
        }


        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        gridLayoutManager = new GridLayoutManager(this,2);

        //attach layoutmanager to recyclerview
        recyclerView.setLayoutManager(gridLayoutManager);



        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        swipeRefreshLayout.setColorSchemeColors(R.color.swipeRefresh1, R.color.swipeRefresh2,R.color.swipeRefresh3,R.color.swipeRefresh4);



    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_camera:
            case R.id.action_upload:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, DialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

        }


        return super.onOptionsItemSelected(item);
    }


    protected DialogInterface.OnClickListener DialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case  0: //take picture
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    MediaURI = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(MediaURI == null){
                        Toast.makeText(MainActivity.this, getString(R.string.storage_error), Toast.LENGTH_LONG).show();
                    }
                    else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaURI);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1: //choose picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent,PICK_PHOTO_REQUEST);
                    break;
            }
        }

        private Uri getOutputMediaFileUri(int mediaType) {

            if(isExternalStorageAvailable()) {
                String appName = MainActivity.this.getString(R.string.app_name);
                //Get external storage directory
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);

                //create subdirectory
                if(!mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs());{
                        Log.e(TAG, "Failed to create Directory");
                        return null;
                    }
                }

                //create a file name
                File mediaFile;
                Date now = new Date();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                String path = mediaStorageDir.getPath() + File.separator;
                //Create a file
                if(mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG" + timestamp + ".jpg");
                }
                else{
                    return null;
                }
                Log.d(TAG, "File: " + Uri.fromFile(mediaFile));


                //create file URI
                return Uri.fromFile(mediaFile);
            }
            else{
                return null;
            }

        }


        private boolean isExternalStorageAvailable(){
            String state = Environment.getExternalStorageState();

            if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
            }
            else {
                return false;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == PICK_PHOTO_REQUEST){
                if(data == null){
                    Toast.makeText(MainActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                }
                else{
                    MediaURI = data.getData();
                }
                Log.i(TAG, "Media URI: " + MediaURI);
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(MediaURI);
                sendBroadcast(mediaScanIntent);
            }

            byte[] fileBytes = FileHelper.getByteArrayFromFile(this, MediaURI);
            if(fileBytes == null){
                Log.e(TAG,"Filebytes is NULL");
            }

            String fileName = "document.png";
            file = new ParseFile(fileName, fileBytes);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        //Success
                        uploadDoc(file);
                    }
                }
            });
        }
        else if (resultCode != RESULT_CANCELED){
            Toast.makeText(MainActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
        }




    }

    private void uploadDoc(ParseFile fileForUpload) {
        ParseObject doc = new ParseObject("doc");
        doc.put(ParseConstants.KEY_USER_ID, currentUser.getObjectId());
        doc.put(ParseConstants.KEY_USERNAME,currentUser.getUsername());
        doc.put(ParseConstants.KEY_REVIEW, false);
        doc.put(ParseConstants.KEY_DOCUMENT, fileForUpload);
        Toast.makeText(MainActivity.this, R.string.uploading_message, Toast.LENGTH_LONG).show();


        doc.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Success
                    Toast.makeText(MainActivity.this, R.string.upload_success_message, Toast.LENGTH_LONG).show();
                    retrieveDocuments();
                } else {
                    Log.e(TAG, e.toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.error))
                            .setTitle(getString(R.string.sorry_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }


    private void retrieveDocuments() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("doc");
        query.whereEqualTo(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> documents, ParseException e) {
                if(swipeRefreshLayout.isRefreshing())
                {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (e == null) {
                    if(!documents.isEmpty() ) {
                        emptyLayoutContainer.setVisibility(View.GONE);
                    }
                    ArrayList<ParseObject> docs = new ArrayList<ParseObject>(documents.size());
                    int i = 0;
                    for (ParseObject document : documents) {
                        docs.add(document);
                        i++;
                    }
                    if(recyclerView.getAdapter() == null) {
                        listAdapter = new ListAdapter(docs, getBaseContext());
                        recyclerView.setAdapter(listAdapter);
                    }
                    else{
                        // refill the adapter
                        listAdapter.refill(docs);
                    }

                }

            }
        });
    }

    protected SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveDocuments();
        }
    };

}
