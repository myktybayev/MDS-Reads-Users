package kz.incubator.sdcl.club1.rules_menu;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.sdcl.club1.MenuActivity;
import kz.incubator.sdcl.club1.R;

import static kz.incubator.sdcl.club1.MenuActivity.setTitle;

public class RuleFragment extends Fragment{

    View view;
    MenuActivity menuActivity;
    ArrayList<Rules> ruleList;
    RulesListAdapter ruleListAdapter;
    RecyclerView.LayoutManager linearLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DatabaseReference databaseReference;
    Rules rules;

    @BindView(R.id.groupRecyclerView)
    RecyclerView ruleRecyclerView;
    @BindView(R.id.llProgressBar)
    View progressLoading;

    public RuleFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rule, container, false);
        ButterKnife.bind(this, view);
        setTitle(getString(R.string.menu_rules));

        initViews();
        return view;
    }

    public void initViews() {
        ruleList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getActivity());
        ruleRecyclerView.setLayoutManager(linearLayoutManager);
        ruleRecyclerView.setHasFixedSize(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        addRulesChangeListener();
        ruleListAdapter = new RulesListAdapter(getActivity(), ruleList);
        ruleRecyclerView.setAdapter(ruleListAdapter);

        progressLoading.setVisibility(View.GONE);
        setupSwipeRefresh();
    }

    public void addRulesChangeListener() {
        databaseReference.child("rules_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Rules rules = dataSnapshot.getValue(Rules.class);
                ruleList.add(rules);
                ruleListAdapter.notifyDataSetChanged();
                progressLoading.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                rules = dataSnapshot.getValue(Rules.class);
                for (int i = 0; i < ruleList.size(); i++) {
                    if (ruleList.get(i).getRuleId().equals(rules.getRuleId())) {
                        ruleList.set(i, rules);
                        ruleListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                rules = dataSnapshot.getValue(Rules.class);
                for (int i = 0; i < ruleList.size(); i++) {
                    if (ruleList.get(i).getRuleId().equals(rules.getRuleId())) {
                        ruleList.remove(i);
                        ruleListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
//            addRulesChangeListener();
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

    public String getFId() {
        Date date = new Date();
        String idN = "r" + date.getTime();
        return idN;
    }

}
