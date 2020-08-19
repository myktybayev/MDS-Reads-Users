package kz.incubator.sdcl.club1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dk.view.folder.ResideMenu;
import com.dk.view.folder.ResideMenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import kz.incubator.sdcl.club1.about_us_menu.AboutUsFragment;
import kz.incubator.sdcl.club1.authentications.LoginByPhoneActivity;
import kz.incubator.sdcl.club1.book_list_menu.BookListFragment;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.groups_menu.module.User;
import kz.incubator.sdcl.club1.my_cabinet.MyCabinetActivity;
import kz.incubator.sdcl.club1.rating_by_users.UserRatingFragment;
import kz.incubator.sdcl.club1.rules_menu.RuleFragment;
import kz.incubator.sdcl.club1.settings_menu.SettingsFragment;
import kz.incubator.sdcl.club1.users_list_menu.GetUsersAsyncTask;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    public ResideMenu resideMenu;
    private ResideMenuItem myCabinetMenu, bookListMenu, usersMenu;
    private ResideMenuItem rules, about_us, settings, log_out;
    public static Toolbar actionToolbar;
    DatabaseReference mDatabaseRef, booksRef, usersRef, ratingRef;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    BookListFragment bookListFragment;
    RuleFragment ruleFragment;
    SettingsFragment settingsFragment;
    FirebaseUser currentUser;
    static String currentUserEmail = "empty";
    String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout2);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
        }

        setUpMenu();
        setupViews(savedInstanceState);
        initUserEnterDate();
    }

    public void setupViews(Bundle savedInstanceState) {

        actionToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(actionToolbar);
        actionToolbar.setNavigationIcon(R.drawable.ic_home_black);
        actionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });


        bookListFragment = new BookListFragment();
        ruleFragment = new RuleFragment();
        settingsFragment = new SettingsFragment();


        if (savedInstanceState == null) {
            changeFragment(bookListFragment);
            setTitle(getString(R.string.menu_books));
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        usersRef = mDatabaseRef.child("user_list");
        booksRef = mDatabaseRef.child("book_list");
        ratingRef = mDatabaseRef.child("rating_users").child("other");
        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();
        checkInternetConnection();
        checkVersion();
        addUserListListener();
    }

    public static void setTitle(String title) {
        actionToolbar.setTitle(title);
    }


    public void initUserEnterDate() {
        if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
            userId = currentUser.getPhoneNumber();
        } else {
            userId = currentUser.getDisplayName();
        }

        assert userId != null;
        usersRef.child(userId).child("enterDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String eDate = dataSnapshot.getValue().toString();
                if (eDate.contains("not")) {

                    DateFormat dateF = new SimpleDateFormat("dd.MM.yyyy");
                    String date = dateF.format(Calendar.getInstance().getTime());
                    usersRef.child(userId).child("enterDate").setValue(date);
                    ratingRef.child(userId).child("point").setValue(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {

    }

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.back_menu);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);

        resideMenu.setScaleValue(0.6f);
        myCabinetMenu = new ResideMenuItem(this, R.drawable.ic_home_black, getString(R.string.menu_my_cabinet));
        usersMenu = new ResideMenuItem(this, R.drawable.ic_users, getString(R.string.menu_users));
        bookListMenu = new ResideMenuItem(this, R.drawable.ic_list_black_24dp, getString(R.string.menu_books));

        rules = new ResideMenuItem(this, R.drawable.ic_assignment_black_24dp, getString(R.string.menu_rules));
        about_us = new ResideMenuItem(this, R.drawable.ic_info_outline_black_24dp, getString(R.string.menu_about_us));
        settings = new ResideMenuItem(this, R.drawable.ic_language_white, getString(R.string.menu_change_language));
        log_out = new ResideMenuItem(this, R.drawable.ic_exit_to_app_black_24dp, getString(R.string.menu_sing_out));

        myCabinetMenu.setOnClickListener(this);
        usersMenu.setOnClickListener(this);
        bookListMenu.setOnClickListener(this);
        rules.setOnClickListener(this);
        about_us.setOnClickListener(this);
        settings.setOnClickListener(this);
        log_out.setOnClickListener(this);

        resideMenu.addMenuItem(myCabinetMenu, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(usersMenu, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(bookListMenu, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(rules, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(about_us, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(settings, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(log_out, ResideMenu.DIRECTION_RIGHT);

//        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {

        if (view == myCabinetMenu) {
            Intent userIntent = new Intent(MenuActivity.this, MyCabinetActivity.class);
            startActivity(userIntent);

        } else if (view == usersMenu) {
            changeFragment(new UserRatingFragment());
            getSupportActionBar().setTitle(getString(R.string.menu_users));
            actionToolbar.setNavigationIcon(R.drawable.ic_users);

        } else if (view == bookListMenu) {
            changeFragment(bookListFragment);
            getSupportActionBar().setTitle(getString(R.string.menu_books));
            actionToolbar.setNavigationIcon(R.drawable.ic_list_black_24dp);

        }else if (view == about_us) {
            changeFragment(new AboutUsFragment());
            getSupportActionBar().setTitle(getString(R.string.menu_about_us));
            actionToolbar.setNavigationIcon(R.drawable.ic_info_outline_black_24dp);

        } else if (view == rules) {
            changeFragment(ruleFragment);
            getSupportActionBar().setTitle(getString(R.string.menu_rules));
            actionToolbar.setNavigationIcon(R.drawable.ic_assignment_black_24dp);

        } else if (view == settings) {
            changeFragment(settingsFragment);
            getSupportActionBar().setTitle(getString(R.string.menu_change_language));
            actionToolbar.setNavigationIcon(R.drawable.ic_assignment_black_24dp);

        } else if (view == log_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
            currentUserEmail = "empty";

            startActivity(new Intent(MenuActivity.this, LoginByPhoneActivity.class));
        }

        resideMenu.closeMenu();
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("user_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsersFromFirebase(newVersion);
                    }
                } else {
                    Toast.makeText(MenuActivity.this, "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public void refreshUsersFromFirebase(String version) {
        Log.d("M_MenuActivity", "refresh users");
        new GetUsersAsyncTask(this, version).execute();
    }

    public void addUserListListener() {
        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                storeDb.updateUser(sqdb, user);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private boolean checkInternetConnection() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(this, getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
//            Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            //Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    private void changeFragment(Fragment targetFragment) {
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public ResideMenu getResideMenu() {
        return resideMenu;
    }
}
