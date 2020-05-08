package kz.incubator.sdcl.club1.groups_menu.profile_fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.ArrayList;
import java.util.Arrays;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.interfaces.ItemClickListener;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInBook;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.user.UserReviewActivity;
import kz.incubator.sdcl.club1.users_list_menu.adapters.UserBookListAdapter;
import kz.incubator.sdcl.club1.users_list_menu.module.User;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BAUTHOR;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BDESC;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BPAGE_NUMBER;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BRATING;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BRESERVED;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_QR_CODE;

public class ReadingBookListFragment extends Fragment implements RatingDialogListener {
    ArrayList<Book> bookList;
    UserBookListAdapter bookAdapter;
    RecyclerView recyclerView;
    View view;
    DatabaseReference mDatabase;
    ProgressBar progressBar;
    TextView checkIsEmpty;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String TABLE_BOOKS = "book_store";
    static Activity activity;
    String userId;
    FirebaseUser currentUser;
    User user;
    String classType;

    public ReadingBookListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reading_book_list, container, false);
        initUserId();
        initialize();

        downloadReadBooks();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        activity = getActivity();

        bookList = new ArrayList<>();
        bookAdapter = new UserBookListAdapter(getActivity(), bookList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookAdapter);

        addListener();
        progressBar = view.findViewById(R.id.ProgressBar);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

    }

    public void initUserId() {
        Bundle bundle = this.getArguments();
        userId = "";

        if (bundle != null) {
            classType = bundle.getString("class");
            user = (User) bundle.getSerializable("user");

            if (classType != null && classType.equals("myCabinet")) {
                currentUser = FirebaseAuth.getInstance().getCurrentUser();

                assert currentUser != null;
                if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
                    userId = currentUser.getPhoneNumber();
                } else {
                    userId = currentUser.getDisplayName();
                }

            } else if (classType != null && classType.equals("userProfile")) {
                assert user != null;
                userId = user.getPhoneNumber();
            }
        }
    }

    private void addListener() {
        if (classType.equals("userProfile")) {
            bookAdapter.setOnItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    //nothing to do
                }
            });
        } else {
            bookAdapter.setOnItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, final int pos) {
                    LayoutInflater factory = LayoutInflater.from(getActivity());

                    final View deleteDialogView = factory.inflate(R.layout.dialog_for_reading_book, null);
                    final AlertDialog deleteDialog = new AlertDialog.Builder(getActivity()).create();

                    LinearLayout delete = deleteDialogView.findViewById(R.id.Delete);
                    LinearLayout finished = deleteDialogView.findViewById(R.id.Finished);
                    final String bookId = bookList.get(pos).getFirebaseKey();
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                                    .setTitle("Not finished book: " + bookList.get(pos).getName())
                                    .setMessage("Are you sure to delete this book?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @SuppressLint("MissingPermission")
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mDatabase.child("user_list").child(userId).child("reading").child(bookId).removeValue();
                                            downloadReadBooks();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();

                            deleteDialog.dismiss();
                        }
                    });

                    finished.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                                    .setTitle("Finished book: " + bookList.get(pos).getName())
                                    .setMessage("Are you sure?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @SuppressLint("MissingPermission")
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent userReviewIntent = new Intent(getActivity(), UserReviewActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("book", bookList.get(pos));
                                            bundle.putSerializable("user", user);
                                            bundle.putString("userId", userId);
                                            userReviewIntent.putExtras(bundle);
                                            getActivity().startActivity(userReviewIntent);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();

                            deleteDialog.dismiss();
                        }
                    });
                    deleteDialog.setView(deleteDialogView);
                    deleteDialog.show();
                }
            });
        }

    }

    String bId;
    Book book2;
    public void openRateDialog(Book book, String bookId) {
        book2 = book;
        bId = bookId;

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(3)
                .setTitle("Rate book: " + book.getName())
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
//                .setDefaultComment("This app is pretty cool !")
                .setStarColor(R.color.starColor)
                .setNoteDescriptionTextColor(R.color.noteDescriptionTextColor)
                .setTitleTextColor(R.color.titleTextColor)
                .setDescriptionTextColor(R.color.hintTextColor)
                .setHint("Please write your comment here ... (250 chars)")
                .setHintTextColor(R.color.hintTextColor)
                .setCommentTextColor(R.color.black)
                .setCommentBackgroundColor(R.color.back_color)
//                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(getActivity())
                .setTargetFragment(this, 15) // only if listener is implemented by fragment
                .show();
    }

    @Override
    public void onPositiveButtonClicked(int rate, String comment) {

        String ratingSplit[] = book2.getRating().split(",");

        int ratingFirstPart = Integer.parseInt(ratingSplit[0]);
        int ratingSecondPart = Integer.parseInt(ratingSplit[1]);
        int increasedSecdPart = ratingSecondPart + 1;

        int lastRate = (ratingFirstPart + rate) / (increasedSecdPart);

        mDatabase.child("book_list").child(bId).child("rating").setValue("" + lastRate + "," + increasedSecdPart);

        if (comment.length() > 0) {
            String fKey = mDatabase.child("book_list").child(bId).child("reviews").push().getKey();

            ReviewInBook review = new ReviewInBook("" + fKey, "" + userId, rate, "" + comment);
            ReviewInUser review2 = new ReviewInUser("" + fKey, "" + bId, rate, "" + comment, 0);

            mDatabase.child("book_list").child(bId).child("reviews").child(fKey).setValue(review);
            mDatabase.child("user_list").child(userId).child("reviews").child(fKey).setValue(review2);
        }

        Toast.makeText(getActivity(), book2.getName() + " rated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    public void downloadReadBooks() {
        mDatabase.child("user_list").child(userId).child("reading").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookList.clear();
                if (dataSnapshot.exists()) {

                    checkIsEmpty.setVisibility(View.GONE);
                    for (DataSnapshot last : dataSnapshot.getChildren()) {
                        String bookKey = last.getKey();

                        Cursor userCursor = getBookByFKey(bookKey);
                        if (userCursor != null && userCursor.getCount() > 0) {
                            userCursor.moveToNext();
                            bookList.add(new Book(
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_FKEY)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BNAME)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BAUTHOR)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BDESC)),
                                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_BPAGE_NUMBER)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BRATING)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BRESERVED)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_QR_CODE)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME))
                            ));

                        }
                    }

                } else {
                    checkIsEmpty.setVisibility(View.VISIBLE);
                }

                bookAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Cursor getBookByFKey(String fkey) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE " +
                COLUMN_FKEY + "=?", new String[]{fkey});
        return res;

    }

}