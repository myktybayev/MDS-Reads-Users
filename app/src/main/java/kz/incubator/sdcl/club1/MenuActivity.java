package kz.incubator.sdcl.club1;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dk.view.folder.ResideMenu;
import com.dk.view.folder.ResideMenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kz.incubator.sdcl.club1.authentications.LoginByPhoneActivity;
import kz.incubator.sdcl.club1.about_us_menu.AboutUsFragment;
import kz.incubator.sdcl.club1.rules_menu.RuleFragment;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.book_list_menu.BookListFragment;
import kz.incubator.sdcl.club1.users_list_menu.UserFragment;
import kz.incubator.sdcl.club1.groups_menu.GroupsFragment;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.user.MyCabinetActivity;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    public ResideMenu resideMenu;
    private ResideMenuItem users, userProfile, book_list, groupListMenu;
    private ResideMenuItem rules, about_us, log_out;
    public static Toolbar actionToolbar;
    DatabaseReference mDatabaseRef, booksRef, usersRef;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    BookListFragment bookListFragment;
    RuleFragment ruleFragment;
    FirebaseUser currentUser;
    Book book;
    static String currentUserEmail = "empty";
    String userLoginType;

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


        if (savedInstanceState == null) {
            changeFragment(bookListFragment);
            setTitle("Book list");
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        usersRef = mDatabaseRef.child("user_list");
        booksRef = mDatabaseRef.child("book_list");
        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();
        checkInternetConnection();
    }

    public static void setTitle(String title) {
        actionToolbar.setTitle(title);
    }


    @Override
    public void onBackPressed() {

    }

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
//        resideMenu.setBackground(R.color.back);
        resideMenu.setBackground(R.drawable.back_menu);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);

        resideMenu.setScaleValue(0.6f);

        users = new ResideMenuItem(this, R.drawable.ic_supervisor_account_black_24dp, "Users");
        userProfile = new ResideMenuItem(this, R.drawable.ic_home_black, "My Cabinet");
        book_list = new ResideMenuItem(this, R.drawable.ic_list_black_24dp, "Book list");
        groupListMenu = new ResideMenuItem(this, R.drawable.ic_groups, "Groups");

        rules = new ResideMenuItem(this, R.drawable.ic_assignment_black_24dp, "Rules");
//        wishes = new ResideMenuItem(this, R.drawable.ic_local_library_black_24dp, "Wishes");
        about_us = new ResideMenuItem(this, R.drawable.ic_info_outline_black_24dp, "About us");
        log_out = new ResideMenuItem(this, R.drawable.ic_exit_to_app_black_24dp, "Sign Out");

        users.setOnClickListener(this);
        userProfile.setOnClickListener(this);
        book_list.setOnClickListener(this);
        groupListMenu.setOnClickListener(this);
//        reserve.setOnClickListener(this);
//        wishes.setOnClickListener(this);

        rules.setOnClickListener(this);
        about_us.setOnClickListener(this);
        log_out.setOnClickListener(this);

//        if (isAdmin()) resideMenu.addMenuItem(users, ResideMenu.DIRECTION_LEFT);
        if (isAdmin()) resideMenu.addMenuItem(groupListMenu, ResideMenu.DIRECTION_LEFT);
        else resideMenu.addMenuItem(userProfile, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(book_list, ResideMenu.DIRECTION_LEFT);

//        if (isAdmin()) resideMenu.addMenuItem(groupListMenu, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(rules, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(about_us, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(log_out, ResideMenu.DIRECTION_LEFT);

        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    public static boolean isAdmin() {

        if (currentUserEmail != null && currentUserEmail.contains("admin")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View view) {

        if (view == users) {
            changeFragment(new UserFragment());
            getSupportActionBar().setTitle("Users");
            actionToolbar.setNavigationIcon(R.drawable.ic_supervisor_account_black_24dp);

        } else if (view == userProfile) {
            goToUserProfile();

        } else if (view == book_list) {
            changeFragment(bookListFragment);
            getSupportActionBar().setTitle("Books");
            actionToolbar.setNavigationIcon(R.drawable.ic_list_black_24dp);

        }else if (view == groupListMenu) {
            changeFragment(new GroupsFragment());
            getSupportActionBar().setTitle("Groups");
            actionToolbar.setNavigationIcon(R.drawable.ic_groups);


        } else if (view == about_us) {
            changeFragment(new AboutUsFragment());
            getSupportActionBar().setTitle("About us");
            actionToolbar.setNavigationIcon(R.drawable.ic_info_outline_black_24dp);

        } else if (view == rules) {
            changeFragment(ruleFragment);
            getSupportActionBar().setTitle("Rules");
            actionToolbar.setNavigationIcon(R.drawable.ic_assignment_black_24dp);
        } else if (view == log_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
            currentUserEmail = "empty";

            startActivity(new Intent(MenuActivity.this, LoginByPhoneActivity.class));
        }

        resideMenu.closeMenu();
    }

    public void goToUserProfile() {

        Intent userIntent = new Intent(MenuActivity.this, MyCabinetActivity.class);
        startActivity(userIntent);

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
