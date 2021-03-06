package de.hochschulehannover.myprojects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hochschulehannover.myprojects.adapter.ProjectAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import de.hochschulehannover.myprojects.firebase.FirestoreClass;
import de.hochschulehannover.myprojects.helper.DBHelper;
import de.hochschulehannover.myprojects.model.Project;
import de.hochschulehannover.myprojects.model.User;
import de.hochschulehannover.myprojects.utils.Constants;

/**
 * <h2>Activity ProjectListActivity</h2>
 * <p>Liste der Projekte. Diese erbt von {@link BaseActivity}.
 * Über einen RecyclerView werden alle Projekte die dem eingeloggten Nutzer zugeordnet sind, angezeigt.
 * Weiterleitung zur Aufgabenliste ({@link TaskListActivity}) bei Klick auf ein Projekt</p>
 *<p>
 * <b>Autor: Ole</b>
 * </p>
 */

public class ProjectListActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbarProjectList;
    private NavigationView navigationView;

    public static final int PROFILE_REQUEST_CODE = 1;
    public static final int CREATE_PROJECT_REQUEST_CODE = 2;

    private static final String TAG = "ProjectListActivity";

    private FloatingActionButton createProjectFab;
    private String userName;

    private RecyclerView projectRecylerView;

    private ListView projectListView;
    private TextView noProjects;

    public static ArrayList<String> projectItems = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;
    static Map<String, Integer> map = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        toolbarProjectList = findViewById(R.id.projectListToolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

        projectRecylerView = findViewById(R.id.projectRecyclerView);
        noProjects = findViewById(R.id.no_projects);

        setupActionBar();

        // Floating Action Button für neues Projekt erstellen initialisieren
        createProjectFab = findViewById(R.id.createProjectFab);
        createProjectFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProjectListActivity.this, AddProject.class);
                intent.putExtra(Constants.NAME, userName);
                // Activity starten und auf Ergebnis warten, dass das Projekt erfolgreich erstellt wurde.
                // Dann wird die Projektliste aktualisiert, um Änderungen direkt anzuzeigen
                startActivityForResult(intent, CREATE_PROJECT_REQUEST_CODE);
            }
        });

        // Listener für AppDrawer setzen, um eine Ereignisbehandlung für die Menüelemente in
        // onNavigationItemSelected() zu implementieren
        navigationView.setNavigationItemSelectedListener(this);

        Log.i(TAG, "Lade Nutzerdaten und Projekte...");
        // Nutzerdaten aus Firestore abrufen, danach an updateUserDetails() übergeben und diese im AppDrawer aktualisieren
        new FirestoreClass().loadUserData(this, true);

        /*
        projectListView = findViewById(R.id.projectListView);
        DBHelper dbHelper = new DBHelper(this);

        projectItems.clear();
        map.clear();

        readProjects(dbHelper);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, projectItems);
        projectListView.setAdapter(arrayAdapter);

        projectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Integer projectID = map.get(projectListView.getItemAtPosition(i).toString());
                Intent intent = new Intent(getApplicationContext(), TasksByStatus.class);
                intent.putExtra("projectID",projectID);
                startActivity(intent);
            }
        });
         */
    }

    /**
     * Projekte im UI über den RecyclerView anzeigen mit eigener Adapterklasse {@link ProjectAdapter}
     * @param projectList ArrayList bestehend aus Objekten vom Typ Project. Beinhaltet alle Projekte
     *                    des Nutzers
     */
    public void projectsToUi (ArrayList<Project> projectList) {
        hideDialog();
        if (projectList.size() > 0) {
            projectRecylerView.setVisibility(View.VISIBLE);
            noProjects.setVisibility(View.GONE);

            projectRecylerView.setLayoutManager(new LinearLayoutManager(this));
            projectRecylerView.setHasFixedSize(true);

            // Objekt der Adapterklasse erstellen und mit RecyclerView verbinden
            ProjectAdapter adapter = new ProjectAdapter(this, projectList);
            projectRecylerView.setAdapter(adapter);
            Log.i(TAG, "Projekte erfolgreich geladen und zum UI hinzugefügt.");

            /* Implementierung des Interface aus der Adapterklasse, Bei Klick auf Projekt TaskListActivity
             * starten und documentId des entsprechenden Projekts übergeben
             */
            adapter.setOnClickListener(new ProjectAdapter.ItemClickListener() {
                @Override
                public void onClick(int position, Project model) {
                    Intent intent = new Intent(ProjectListActivity.this, TaskListActivity.class);
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId);
                    intent.putExtra(Constants.NAME, userName);
                    startActivity(intent);
                }
            });
        } else {
            projectRecylerView.setVisibility(View.GONE);
            noProjects.setVisibility(View.VISIBLE);
            Log.i(TAG, "Keine Projekte vorhanden");
        }
    }

    /**
     * Nutzerdaten im UI aktualisieren (App-Drawer) und Projektliste
     * @param user User-Objekt wird von {@link FirestoreClass} übergeben
     * @param readProjectsList Ob Projektliste auch aktualisiert werden soll oder nicht
     */
    public void updateUserDetails(User user, Boolean readProjectsList) {

        userName = user.name;

        View headerView = navigationView.getHeaderView(0);
        CircleImageView userImage = findViewById(R.id.userImageView);

        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(userImage);

        TextView navUsername = findViewById(R.id.usernameTextView);
        navUsername.setText(user.name);

        // Wenn die Projektliste neu von Firestore geladen werden soll
        if (readProjectsList) {
            showDialog("Lade Projekte...");
            Log.i(TAG, "Lade Projekte von Firestore...");
            new FirestoreClass().getProjectList(this);
        }
    }

    /**
     * Diese Methode wird aufgerufen nachdem entweder eine Änderung der Nutzerdaten vorgenommen wurde
     * oder wenn ein neues Projekt angelegt wurde. Je nach Vorgang werden entweder die Nutzerdaten neu
     * vom Firestore geladen oder die Projektliste
     * @param requestCode Der request-Code der beim Aufrufen der Activity übergeben wurde
     * @param resultCode Der result-Code ob das Ziel der Activity erfolgreich war
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Abfragen, ob Profilupdate bzw. Projekterstellung erfolgreich war
        if (resultCode == RESULT_OK && requestCode == PROFILE_REQUEST_CODE) {
            // Nutzerdaten aus Firestore neu abrufen und danach im UI aktualisieren
            new FirestoreClass().loadUserData(this);
        } else if (resultCode == RESULT_OK && requestCode == CREATE_PROJECT_REQUEST_CODE) {
            // Projektliste aus Firestore neu laden und danach im UI anzeigen
            new FirestoreClass().getProjectList(this);
        } else {
            Log.e("ProjectListActivity","Profiländerung/Projekterstellung abgebrochen");
        }
    }

    // Custom ActionBar initialisieren
    private void setupActionBar() {
        setSupportActionBar(toolbarProjectList);
        toolbarProjectList.setNavigationIcon(R.drawable.ic_action_navigation_menu);

        toolbarProjectList.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDrawer();
            }
        });
    }

    // Methode, um den AppDrawer aus- bzw. einzuklappen
    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    // Bei Klick auf Zurück-Taste App-Drawer schließen, wenn er geöffnet ist, ansonsten Meldung anzeigen,
    // dass die App nach zweimaligen klicken der Zurück-Taste geschlossen wird
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            doubleBackExit();
        }
    }

    // Implementierung der Klicks auf die Elemente im App-Drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_my_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            // TODO: Veraltete Methode ersetzen
            startActivityForResult(intent, PROFILE_REQUEST_CODE);
        }
        if (item.getItemId()==R.id.nav_logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent);
            finish();
        }
        if (item.getItemId() == R.id.nav_overview) {
            showErrorSnackBar("Noch nicht verfügbar");
        }
        if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        if (item.getItemId()==R.id.info){
            goToInfo();
        }
        return true;
    }
    public void goToInfo() {
        Intent intent = new Intent(ProjectListActivity.this, InfosActivity.class);
        startActivity(intent);
    }

    /*public void createProject(View view) {
        Intent intent = new Intent(ProjectListActivity.this, AddProject.class);
        startActivity(intent);
    }*/

    public static void readProjects (DBHelper dbHelper) {
        projectItems.clear();
        map.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM projects", null);

        int nameIndex = cursor.getColumnIndex("name");
        int idIndex = cursor.getColumnIndex("id");
        while (cursor.moveToNext()) {
            projectItems.add(cursor.getString(nameIndex));
            map.put(cursor.getString(nameIndex), cursor.getInt(idIndex));
        }
    }
}