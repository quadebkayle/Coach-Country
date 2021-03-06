package com.example.quade_laptop.coachcountry;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class CoachSessionsHistory extends AppCompatActivity {

    RecyclerView sessionHistoryRV;
    LinearLayoutManager llm;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DrawerLayout mDrawerLayout;
    private String runnerID;

    private List<CCSession> sessionsSummaryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_country_sessions_history);

        Intent i = getIntent();

        runnerID = i.getStringExtra("runnerID");

        //Init toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.c_h_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        NavigationView navigationView = findViewById(R.id.c_h_navbar);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.coachDashboard:
                                startActivity(new Intent(CoachSessionsHistory.this, CoachHomePage.class));
                                finish();
                                return true;
                        }
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        mDrawerLayout = findViewById(R.id.c_h_drawer_layout);





        sessionHistoryRV = (RecyclerView)findViewById(R.id.runnerRV);
        llm = new LinearLayoutManager(getApplicationContext());
        sessionHistoryRV.setLayoutManager(llm);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        sessionsSummaryList = new ArrayList<CCSession>();
        generateRunnerData();
        getSessionsData();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, CoachCountrySignIn.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void generateRunnerData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("runners").document(runnerID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();
                    Runner runner = result.toObject(Runner.class);
                    TextView name = findViewById(R.id.runner_name);
                    TextView year = findViewById(R.id.runner_year);
                    TextView event = findViewById(R.id.runner_eventgroup);
                    TextView wM = findViewById(R.id.runner_weeklymileage);

                    name.setText(runner.getFullName());
                    year.setText(runner.getYear());
                    event.setText(runner.getEvent());
                    wM.setText("35mi/week");
                }
            }
        });
    }

    private void getSessionsData(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("runners").document(runnerID).collection("sessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    QuerySnapshot result = task.getResult();
                    List<DocumentSnapshot> results = result.getDocuments();
                    if(results.size() == 0)
                        Toast.makeText(getApplicationContext(), "There are no sessions to show.", Toast.LENGTH_LONG);
                    else
                        for(DocumentSnapshot sessionDoc : results){
                            List<GeoPoint> locations = new ArrayList<GeoPoint>();
                            locations = (List<GeoPoint>) sessionDoc.get("locations");
                            Pace sessionPace = new Pace(Integer.parseInt(sessionDoc.get("sessionPace.minute").toString()),Integer.parseInt(sessionDoc.get("sessionPace.seconds").toString()));
                            sessionsSummaryList.add(new CCSession(
                                    sessionDoc.getId(),
                                    (Date)sessionDoc.get("sessionDate"),
                                    locations,
                                    sessionDoc.get("sessionDuration").toString(),
                                    sessionPace,
                                    Double.parseDouble(sessionDoc.get("sessionDistance").toString()),
                                    Integer.parseInt(sessionDoc.get("sessionNum").toString())
                            ));
                        }
                    SessionRVAdapter adapter = new SessionRVAdapter(sessionsSummaryList,CoachSessionsHistory.this, runnerID, CoachSessionSummary.class);
                    sessionHistoryRV.setAdapter(adapter);
                }
            }
        });
    }


}
