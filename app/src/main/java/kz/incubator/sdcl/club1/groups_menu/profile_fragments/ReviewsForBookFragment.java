package kz.incubator.sdcl.club1.groups_menu.profile_fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.adapters.BookReviewsAdapter;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;
import kz.incubator.sdcl.club1.book_list_menu.one_book_fragments.RecyclerItemClickListener;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.groups_menu.UserReviewCheckActivity;
import kz.incubator.sdcl.club1.users_list_menu.module.User;

public class ReviewsForBookFragment extends Fragment {
    ArrayList<ReviewInUser> reviewList = new ArrayList<>();
    BookReviewsAdapter reviewAdapter;
    RecyclerView recyclerView;
    View view;
    DatabaseReference mDatabase;
    ProgressBar progressBar;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    TextView checkIsEmpty;
    String userId;
    FirebaseUser currentUser;
    String TAG = "ReviewsForBookFragment";
    User user;
    String classType;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_comment_books, container, false);
        initUserId();
        initialize();
        getReviews();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);
        reviewAdapter = new BookReviewsAdapter(getActivity(), reviewList);

        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(reviewAdapter);

        progressBar = view.findViewById(R.id.ProgressBar);
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        if (!classType.equals("userProfile"))
            recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, final int pos) {

                            Intent intent = new Intent(getActivity(), UserReviewCheckActivity.class);

                            Bundle bundle = new Bundle();
                            Log.i(TAG, "reviewList text: " + reviewList.get(pos).getBook_id());

                            bundle.putSerializable("userReview", reviewList.get(pos));
                            bundle.putSerializable("user", user);
                            intent.putExtras(bundle);

                            startActivityForResult(intent, 101);

                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    })
            );
    }

    public void initUserId() {
        Bundle bundle = this.getArguments();
        userId = "";

        if (bundle != null) {
            classType = bundle.getString("class");

            if (classType != null && classType.equals("myCabinet")) {

                currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
                    userId = currentUser.getPhoneNumber();
                } else {
                    userId = currentUser.getDisplayName();
                }
                user = (User) bundle.getSerializable("user");

            } else if (classType != null && classType.equals("userProfile")) {
                user = (User) bundle.getSerializable("user");
                userId = user.getPhoneNumber();
            }
        }

        Log.i(TAG, "userId: " + userId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                String point = data.getStringExtra("point");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void getReviews() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_list").child(userId).child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewList.clear();
                if (dataSnapshot.exists()) {

                    checkIsEmpty.setVisibility(View.GONE);
                    for (DataSnapshot reviews : dataSnapshot.getChildren()) {

                        ReviewInUser reviewInUser = reviews.getValue(ReviewInUser.class);
                        reviewList.add(reviewInUser);

                    }

                    progressBar.setVisibility(View.GONE);

                } else {
                    checkIsEmpty.setVisibility(View.VISIBLE);
                }

                reviewAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
