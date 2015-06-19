package com.seerauberstudios.docuploader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

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

    @InjectView(R.id.mainactivity_recycler_view) RecyclerView recyclerView;;


    public final static String TAG = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 1;

    public static final int MEDIA_TYPE_IMAGE = 4;

    protected Uri MediaURI;

    ParseFile file;
    ParseUser currentUser;


    private ListAdapter listAdapter;
    private LinearLayoutManager linearLayoutManager;

    protected ArrayList<ParseObject> Documents;


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
        }


        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        //attach layoutmanager to recyclerview
       recyclerView.setLayoutManager(linearLayoutManager);

       // retrieveDocuments();
        // specify an adapter
       // listAdapter = new ListAdapter(textPosts);
        //recyclerView.setAdapter(listAdapter);


    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
                System.out.println("Filebytes is NULL");
                //return null;
            }

            String fileName = "document.png";
            file = new ParseFile(fileName, fileBytes);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        //Success
                        System.out.println("HERE!!!!!!!! 11111");
                        Toast.makeText(MainActivity.this, "Sucess FILE SAVED", Toast.LENGTH_LONG);
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
        doc.put("Document", fileForUpload);

        doc.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Success
                    System.out.println("HERE!!!!!!!! ");

                    Toast.makeText(MainActivity.this, "Sucess", Toast.LENGTH_LONG);
                } else {
                    System.out.println("EXCEPTION!!!!!!!! " + e.toString());
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

    @Override
    public void onResume() {
        super.onResume();

        //retrieveDocuments();

    }

    private void retrieveDocuments() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("doc");
        query.whereEqualTo(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> documents, ParseException e) {


               /* if(swipeRefreshLayout.isRefreshing())
                {
                    swipeRefreshLayout.setRefreshing(false);
                }*/


                if (e == null) {
                    //success
                   // Documents = documents;
                    System.out.println("HERE!!!!!!!! ");
                    ArrayList<ParseFile> docs = new ArrayList<ParseFile>(documents.size());
                    int i = 0;
                    for (ParseObject document : documents) {
                        docs.add(document.getParseFile(ParseConstants.KEY_FILE));
                        i++;
                        System.out.println("HERE!!!!!!!! 2");

                    }
                    if(recyclerView.getAdapter() == null) {
                        listAdapter = new ListAdapter(docs, getBaseContext());
                        recyclerView.setAdapter(listAdapter);
                        System.out.println("HERE!!!!!!!! 3");

                    }
                    else{
                       // refill the adapter
                        recyclerView.getAdapter().notifyDataSetChanged();
                        //((MessageAdapter)getListView().getAdapter()).refill(Messages);
                    }

                }

            }
        });
    }

    protected SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //retrieveDocuments();

        }
    };

}
