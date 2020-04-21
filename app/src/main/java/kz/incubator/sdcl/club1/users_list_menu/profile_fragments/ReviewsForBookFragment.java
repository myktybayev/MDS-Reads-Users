package kz.incubator.sdcl.club1.users_list_menu.profile_fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_comment_books, container, false);

        initialize();
        initUserId();
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

    }

    FirebaseUser currentUser;
    public void initUserId(){
        userId = "";

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
            userId = currentUser.getPhoneNumber();
        }else{
            userId = currentUser.getDisplayName();
        }
    }

    public void getReviews() {
/*
        progressBar.setVisibility(View.GONE);
        ReviewInUser reviewInUser = new ReviewInUser("Yahoo","i1544448582108","Жақсы мощьный кітап екен!");
        ReviewInUser reviewInUser1 = new ReviewInUser("Yahoo","i1544460113539","Өте әдемі жақсы жазылған кітап, " +
                "барлығыңызға оқуға ақыл беремін");
        ReviewInUser reviewInUser2 = new ReviewInUser("Yahoo","i1544460301640","Жақсы мощьный кітап екен!" +
                "Жақсы мощьный кітап екен!, Жақсы мощьный кітап екен!, Жақсы мощьный кітап екен!");

        reviewList.add(reviewInUser);
        reviewList.add(reviewInUser1);
        reviewList.add(reviewInUser2);

        reviewAdapter.notifyDataSetChanged();
*/

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
