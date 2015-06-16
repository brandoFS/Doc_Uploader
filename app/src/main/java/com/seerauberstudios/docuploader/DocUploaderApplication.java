package com.seerauberstudios.docuploader;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.seerauberstudios.docuploader.util.ParseConstants;

/**
 * Created by bmadden on 6/15/15.
 */
public class DocUploaderApplication extends Application {

    public void onCreate() {

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "pYgSMojEEN8SCbNJtIJZvPih8dCcxnNmb3xzmZt2", "12N5UFk7egAyVePw1QUfiOe7dUc46AzgFqU6Lzbf");
        ParseInstallation.getCurrentInstallation().saveInBackground();

    }


    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();

    }

}
