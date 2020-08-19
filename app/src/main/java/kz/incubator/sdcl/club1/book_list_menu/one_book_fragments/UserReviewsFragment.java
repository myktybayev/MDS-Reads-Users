package kz.incubator.sdcl.club1.book_list_menu.one_book_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.adapters.UserReviewsAdapter;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInBook;
import kz.incubator.sdcl.club1.groups_menu.module.User;

public class UserReviewsFragment extends Fragment {
    ArrayList<ReviewInBook> reviewList = new ArrayList<>();
    UserReviewsAdapter reviewAdapter;
    RecyclerView recyclerView;
    View view;
    DatabaseReference mDatabase, userRef;
    ProgressBar progressBar;
    TextView checkIsEmpty;
    String userId, bookId, userGroupId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_comment_books, container, false);
        initialize();
        getReviews();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);
        reviewAdapter = new UserReviewsAdapter(getActivity(), reviewList);

        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        progressBar = view.findViewById(R.id.ProgressBar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(reviewAdapter);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            bookId = getArguments().getString("bookId");
        }


    }

    public void getReviews() {
        userRef = FirebaseDatabase.getInstance().getReference().child("user_list");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("book_list").child(bookId).child("reviews");

        userRef.child(userId).child("group_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userGroupId = dataSnapshot.getValue().toString();

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reviewList.clear();
                        if (dataSnapshot.exists()) {

                            checkIsEmpty.setVisibility(View.GONE);
                            for (DataSnapshot reviews : dataSnapshot.getChildren()) {

                                String userId = reviews.child("user_id").getValue().toString();
                                final String fKey = reviews.child("fKey").getValue().toString();
                                final String review_text = reviews.child("review_text").getValue().toString();
                                final int user_rate = Integer.parseInt(reviews.child("user_rate").getValue().toString());


                                userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            User userData = dataSnapshot.getValue(User.class);

                                            if (userData.getGroup_id().equals(userGroupId)) {
                                                ReviewInBook reviewInUser = new ReviewInBook(fKey, userData, user_rate, review_text);
                                                reviewList.add(reviewInUser);
                                                reviewAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
