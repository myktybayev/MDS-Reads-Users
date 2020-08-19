package kz.incubator.sdcl.club1.users_list_menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.groups_menu.adapters.UserListAdapter;
import kz.incubator.sdcl.club1.groups_menu.module.Groups;
import kz.incubator.sdcl.club1.groups_menu.module.User;

import static kz.incubator.sdcl.club1.MenuActivity.setTitle;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BCOUNT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_EMAIL;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_ENTER_DATE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_GROUP;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_GROUP_ID;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_INFO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_POINT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_RAINTING_IN_GROUPS;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_REVIEW_SUM;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_USER_TYPE;

public class UserFragment extends Fragment implements View.OnClickListener {
    DatabaseReference mDatabaseRef, userRef;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager linearLayoutManager;
    ArrayList<User> userListCopy;
    ArrayList<User> userList;
    ArrayList<String> groupIdsList;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    SearchView searchView;
    View view;
    FloatingActionButton fabBtn;
    ProgressBar progressBar;
    UserListAdapter listAdapter;
    String TABLE_USER = "user_store";
    User user;
    Spinner spinnerGroups;

    View sortDialogView;
    AlertDialog sortDialog;
    Switch filterSwitch;
    ArrayAdapter<String> spinnerAdapter;
    String TAG = "UserFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_user_list, container, false);


        initView();
        fillUsers();


        addListener();
        sortDialog();

        return view;
    }

    public void initView() {

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        userRef = mDatabaseRef.child("user_list");
        userList = new ArrayList<>();
        groupIdsList = new ArrayList<>();
        progressBar = view.findViewById(R.id.ProgressBar);
        spinnerGroups = view.findViewById(R.id.spinnerGroups);

        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        int resId = R.anim.layout_anim;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutAnimation(animation);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));


        fabBtn = view.findViewById(R.id.fabBtn);
        fabBtn.setVisibility(View.GONE);

        searchView = view.findViewById(R.id.searchView);
        userListCopy = new ArrayList<>();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filter(s);
                return false;
            }
        });

        groupIdsList.add(getString(R.string.all));
        spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_group_spinner, groupIdsList);
        spinnerGroups.setAdapter(spinnerAdapter);
        getGroupsFromFirebase();

        spinnerGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                userList.clear();
                if (pos == 0) {
                    userList.addAll(userListCopy);
                } else {
                    String gId = groupIdsList.get(pos);

                    for (User item : userListCopy) {
                        Log.i(TAG, "group name: " + item.getGroupName());
                        if (item.getGroupName() != null && item.getGroupName().equals(gId)) {
                            userList.add(item);
                        }
//
                    }
                }

                recyclerView.setAdapter(listAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setupSwipeRefresh();
    }

    public void fillUsers() {
        new BackgroundTaskForUserFill(getActivity(), recyclerView, progressBar).execute();
        mSwipeRefreshLayout.setRefreshing(false);

        listAdapter = new UserListAdapter(getActivity(), userList);
    }

    public void addListener() {
        userRef.addChildEventListener(new ChildEventListener() {
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

    public void getGroupsFromFirebase() {
        mDatabaseRef.child("group_list").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot group : dataSnapshot.getChildren()) {
                        Groups itemGroup = group.getValue(Groups.class);
                        groupIdsList.add(itemGroup.getGroup_name());
                    }
                    spinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void sortDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());

        sortDialogView = factory.inflate(R.layout.dialog_user_filter, null);
        sortDialog = new AlertDialog.Builder(getActivity()).create();

        LinearLayout sort_name = sortDialogView.findViewById(R.id.sort_name);
        LinearLayout sort_readed = sortDialogView.findViewById(R.id.sort_readed_books);
        LinearLayout filter_period = sortDialogView.findViewById(R.id.sort_point);

        filterSwitch = sortDialogView.findViewById(R.id.filterSwitch);

        sort_name.setOnClickListener(this);
        sort_readed.setOnClickListener(this);
        filter_period.setOnClickListener(this);

        sortDialog.setView(sortDialogView);

    }

    @Override
    public void onClick(View v) {
        userList.clear();
        userList.addAll(userListCopy);

        switch (v.getId()) {

            case R.id.sort_name:

                Collections.sort(userList, User.userNameComprator);
                sortDialog.dismiss();

                break;
            case R.id.sort_readed_books:

                Collections.sort(userList, User.userReadedBooks);
                sortDialog.dismiss();

                break;

            case R.id.sort_point:

//                ArrayList<User> copyUserList = (ArrayList<User>) userList.clone();
//                userList.clear();
//
//                for (User user : copyUserList) {
//                    String abone = checkAbone(user.getGroup());
//
//                    if (abone.contains("subscription")) {
//                        userList.add(user);
//                    }
//                }

                sortDialog.dismiss();
                break;


        }

        listAdapter.notifyDataSetChanged();
    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListCopy);
        } else {
            text = text.toLowerCase();
            for (User item : userListCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toLowerCase().contains(text) ||
                        item.getPhoneNumber().toUpperCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.menu_card_read, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    Dialog dialog;
    EditText card_id;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.login_page:

                break;

            case R.id.filter_user:

                sortDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String checkAbone(String ticketDay) {
        String abone = "norm";

        DateFormat day = new SimpleDateFormat("dd");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat year = new SimpleDateFormat("yyyy");
        int first_slash = ticketDay.indexOf('/');
        int last_slash = ticketDay.lastIndexOf('/');

        String period_day = ticketDay.substring(0, first_slash);
        String period_month = ticketDay.substring(first_slash + 1, last_slash);
        String period_year = ticketDay.substring(last_slash + 1, ticketDay.length());

        Calendar cal = Calendar.getInstance();

        if (period_year.equals(year.format(cal.getTime()))) {
            if (period_month.equals(month.format(cal.getTime()))) {
                if (period_day.equals(day.format(cal.getTime()))) {
                    abone = "last day";
                } else if (Integer.parseInt(period_day) > Integer.parseInt(day.format(cal.getTime()))) {
                    int counter = Integer.parseInt(period_day) - Integer.parseInt(day.format(cal.getTime()));
                    if (counter <= 7) {
                        abone = counter + " days left";
                    }
                } else {
                    abone = "Your subscription is up";

                }
            } else if (Integer.parseInt(period_month) < Integer.parseInt(month.format(cal.getTime()))) {
                abone = "Your subscription is up";

            }
        } else if (Integer.parseInt(period_year) < Integer.parseInt(year.format(cal.getTime()))) {
            abone = "Your subscription is up";

        }

        return abone;
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("user_ver");

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsers(newVersion);
                    } else {
                        onItemsLoadComplete();
                    }
                } else {
                    Toast.makeText(getActivity(), "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshUsers(String version) {
//        new GetUsersAsyncTask(getActivity(), "01", mSwipeRefreshLayout, version).execute();
    }

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void onItemsLoadComplete() {
        fillUsers();
        mSwipeRefreshLayout.setRefreshing(false);

    }

    public void refreshItems() {
        if (isOnline()) {
            checkVersion();
        }

    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class BackgroundTaskForUserFill extends AsyncTask<Void, Void, Void> {
        RecyclerView recyclerView;
        ProgressBar progressBar;
        StoreDatabase storeDb;
        SQLiteDatabase sqdb;
        DatabaseReference mDatabaseRef, userRef;
        Context context;

        public BackgroundTaskForUserFill(Context context, RecyclerView recyclerView, ProgressBar progressBar) {
            this.recyclerView = recyclerView;
            this.progressBar = progressBar;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            progressBar.setVisibility(View.VISIBLE);
            storeDb = new StoreDatabase(context);
            sqdb = storeDb.getWritableDatabase();
            userRef = mDatabaseRef.child("user_list");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor userCursor = sqdb.rawQuery("SELECT * FROM " + TABLE_USER, null);
            if (((userCursor != null) && (userCursor.getCount() > 0))) {
                userList.clear();
                while (userCursor.moveToNext()) {
                    userList.add(new User(
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_EMAIL)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP_ID)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_ENTER_DATE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_USER_TYPE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_BCOUNT)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_POINT)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_REVIEW_SUM)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_RAINTING_IN_GROUPS))
                    ));

                }

                userListCopy = (ArrayList<User>) userList.clone();
                Collections.reverse(userListCopy);

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Collections.reverse(userList);
            recyclerView.setAdapter(listAdapter);
            progressBar.setVisibility(View.GONE);

            setTitle("Users " + userList.size());
        }

    }
}