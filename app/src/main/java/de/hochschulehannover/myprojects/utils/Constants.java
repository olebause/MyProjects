package de.hochschulehannover.myprojects.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.text.SimpleDateFormat;

/**
 * Klasse für globale Konstanten und Methoden
 * Autor: Alle
 */
public class Constants {

    // Tabellennamen in Firebase
    public static final String USERS_TABLE = "users";
    public static final String PROJECTS_TABLE = "projects";

    //Keys für Firebase innerhalb eines Projektes
    public static final String TASK_LIST = "taskList";
    public static final String ASSIGNED_TO = "assignedUsers";
    public static final String DOCUMENT_ID = "documentId";

    public static final String IMAGE = "image";
    public static final String NAME = "name";

    public static final String PROJECT_DETAILS = "project_details";

    public static int READ_STORAGE_PERMISSION_CODE = 1;
    public static int PICK_IMAGE_REQUEST_CODE = 2;

    // Bezeichnungen und Index der Aufgabenlisten
    public static final String BACKLOG = "backlog";
    public static final int BACKLOG_INDEX = 0;
    public static final String PROGRESS = "in_progress";
    public static final int PROGRESS_INDEX = 1;
    public static final String DONE = "done";
    public static final int DONE_INDEX = 2;

    public static void showImageChooser(Activity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // TODO: Veraltete Funktion ersetzen
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE);
    }

    //Dateityp zurückgeben
    public static String getFileExtension(Activity activity, Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.getContentResolver().getType(uri));
    }

}
