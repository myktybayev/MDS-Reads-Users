package kz.incubator.sdcl.club1.groups_menu;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.users_list_menu.GetUsersAsyncTask;

public class GroupsFragment extends Fragment implements View.OnClickListener {
    View view;
    ArrayList<Groups> groupList;
    RecyclerView groupRecyclerView;
    GroupListAdapter groupListAdapter;
    RecyclerView.LayoutManager linearLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Dialog addGroupDialog;
    Button addGroupBtn;
    EditText groupName;
    ProgressBar progressBar;
    FloatingActionButton fabBtn;
    DatabaseReference databaseReference;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    View progressLoading;


    public GroupsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_groups, container, false);
        initViews();

        checkVersion();

        return view;
    }

    public void initViews() {
        groupList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getActivity());
        groupRecyclerView = view.findViewById(R.id.groupRecyclerView);
        groupRecyclerView.setLayoutManager(linearLayoutManager);
        groupRecyclerView.setHasFixedSize(true);

        fabBtn = view.findViewById(R.id.fabBtn);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        progressLoading = view.findViewById(R.id.llProgressBar);

        fabBtn.setOnClickListener(this);

        groupListAdapter = new GroupListAdapter(getActivity(), groupList);
        groupRecyclerView.setAdapter(groupListAdapter);

        addGroupsChangeListener();
        setupSwipeRefresh();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fabBtn:
                addGroupDialog = new Dialog(getActivity());
                addGroupDialog.setContentView(R.layout.activity_add_group);

                groupName = addGroupDialog.findViewById(R.id.groupName);
                progressBar = addGroupDialog.findViewById(R.id.progressBar);

                addGroupBtn = addGroupDialog.findViewById(R.id.addBtn);
                addGroupBtn.setOnClickListener(this);

                addGroupDialog.show();

                break;

            case R.id.addBtn:
                String gName = groupName.getText().toString();
                String gId = getFId();

                if(!TextUtils.isEmpty(gName)){

                    addGroupBtn.setVisibility(View.GONE);
                    Groups groups = new Groups(gId, gName, 0, 0);
                    databaseReference.child("group_list").child(gId).setValue(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), getString(R.string.group_added), Toast.LENGTH_SHORT).show();
                            addGroupDialog.dismiss();
                        }
                    });

                }else{

                    groupName.setError(getString(R.string.enter_group_error));
                }

                break;
        }
    }

    String TAG = "GroupsFragment";
    public void addGroupsChangeListener(){
        databaseReference.child("group_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Groups groups = dataSnapshot.getValue(Groups.class);
                groupList.add(groups);

                Collections.sort(groupList, Groups.groupPlace);
                groupListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Groups groups = dataSnapshot.getValue(Groups.class);
                Log.i(TAG, "previousChildName: "+groups.getGroup_id());

                for(int i = 0; i < groupList.size(); i++){
                    if(groupList.get(i).getGroup_id().equals(groups.getGroup_id())){
                        groupList.set(i, groups);
                        Collections.sort(groupList, Groups.groupPlace);
                        groupListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void checkVersion() {
        Query myTopPostsQuery = databaseReference.child("user_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsersFromFirebase(newVersion);
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

    public void refreshUsersFromFirebase(String version) {
        new GetUsersAsyncTask(getActivity(), version, mSwipeRefreshLayout, progressLoading).execute();
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
        progressLoading.setVisibility(View.GONE);
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

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public String getFId() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }
}
