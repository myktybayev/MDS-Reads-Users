package kz.incubator.sdcl.club1.groups_menu.profile_fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Collections;
import java.util.Date;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.adapters.RecommendationBookListAdapter;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.users_list_menu.module.User;


public class RecommendationBookListFragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    RecommendationBookListAdapter adapter;
    ArrayList<Book> bookList = new ArrayList<>();
    Button fab;
    ProgressBar progressBar;
    DatabaseReference mDatabase;
    ArrayList<String> keys = new ArrayList<>();
    String userId;
    FirebaseUser currentUser;
    String classType;

    public RecommendationBookListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommendation_book_list, container, false);

        initUserId();
        getRecommendedBooks();
        initializeFloatingActionButton();
        return view;
    }

    User user;

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

            } else if (classType != null && classType.equals("userProfile")) {
                user = (User) bundle.getSerializable("user");
                userId = user.getPhoneNumber();
            }
        }
    }

    public void initializeRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerView);
        Collections.reverse(bookList);
        adapter = new RecommendationBookListAdapter(getActivity(), bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public void initializeFloatingActionButton() {
        fab = view.findViewById(R.id.fab);

        if (classType.equals("userProfile")) fab.setVisibility(View.INVISIBLE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View alerDialogView = getLayoutInflater().inflate(R.layout.create_recommendation_book, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                final TextView author = alerDialogView.findViewById(R.id.AuthorOfBook);
                final TextView name = alerDialogView.findViewById(R.id.BookName);
                Button btn = alerDialogView.findViewById(R.id.addBook);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!author.getText().toString().equals("") && !name.getText().toString().equals("")) {
                            String bookId = getIdNumber();
                            Book book = new Book(bookId, author.getText().toString(), name.getText().toString());
                            mDatabase.child("user_list").child(userId).child("recommendations").child(bookId).setValue(book);
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog.setView(alerDialogView);
                alertDialog.show();
            }
        });
    }

    public void getRecommendedBooks() {
        progressBar = view.findViewById(R.id.ProgressBar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_list").child(userId).child("recommendations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                keys.clear();
                bookList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Book book = data.getValue(Book.class);
                    bookList.add(book);
                }
                initializeRecyclerView();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getIdNumber() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }

}
