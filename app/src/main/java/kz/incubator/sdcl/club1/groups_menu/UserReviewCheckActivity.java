package kz.incubator.sdcl.club1.groups_menu;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInBook;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.users_list_menu.module.User;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BDESC;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BRATING;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;

public class UserReviewCheckActivity extends AppCompatActivity implements View.OnClickListener {


    Toolbar toolbar;
    AppBarLayout appBarLayout;
    Bundle bundle;
    ReviewInUser reviewInUser;
    DatabaseReference mDatabase;
    String userId;
    StoreDatabase storeDb;
    @BindView(R.id.bookImage)
    ImageView bookImage;
    @BindView(R.id.bookName)
    TextView bookNameTxt;
    @BindView(R.id.bookAuthor)
    TextView bookAuthorTxt;
    @BindView(R.id.bookRating)
    RatingBar bookRatingTxt;
    @BindView(R.id.userRated)
    RatingBar userRatedTxt;
    @BindView(R.id.adminRate)
    RatingBar adminRateBar;
    @BindView(R.id.userReview)
    TextView userReviewTxt;
    @BindView(R.id.adminSaveBtn)
    Button adminSaveBtn;
    @BindView(R.id.saveProgress)
    ProgressBar saveProgress;

    ArrayList<User> userList;
    /*
    @BindString(R.string.title) String title;
    @BindDrawable(R.drawable.graphic)
    Drawable graphic;
    @BindColor(R.color.red) int red; // int or ColorStateList field
    @BindDimen(R.dimen.spacer) float spacer;
    */
    String TAG = "UserReviewCheckActivity";
    String currentUserEmail = "empty";
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_review_check);
        ButterKnife.bind(this);

        initWidgets();
        initializeBook();

    }

    public void initWidgets() {
        toolbar = findViewById(R.id.toolbars);
        appBarLayout = findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.write_a_review));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(this);
        adminSaveBtn.setOnClickListener(this);
        userList = new ArrayList<>();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
        }

        if(!isAdmin()){
            adminSaveBtn.setVisibility(View.GONE);
            saveProgress.setVisibility(View.GONE);

        }
    }

    Book book;
    User user;
    String reviewKey;

    public void initializeBook() {

        bundle = getIntent().getExtras();
        if (bundle != null) {

            reviewInUser = (ReviewInUser) bundle.getSerializable("userReview");

            user = (User) bundle.getSerializable("user");

            if(isAdmin()) userId = user.getPhoneNumber();

            reviewKey = reviewInUser.getfKey();
            String bookKey = reviewInUser.getBook_id();
            int user_rate = reviewInUser.getUser_rate();
            String review_text = reviewInUser.getReview_text();

            String bookImg, bookName, bookAuthor;
            String bookRating;

            Cursor userCursor = storeDb.getBookByFKey(bookKey);

            if (userCursor.getCount() > 0) {
                userCursor.moveToNext();

                bookImg = userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO));
                bookName = userCursor.getString(userCursor.getColumnIndex(COLUMN_BNAME));
                bookAuthor = userCursor.getString(userCursor.getColumnIndex(COLUMN_BDESC));
                bookRating = userCursor.getString(userCursor.getColumnIndex(COLUMN_BRATING));
                bookRating = bookRating.substring(0, bookRating.indexOf(","));
                Glide.with(this)
                        .load(bookImg)
                        .placeholder(R.drawable.user_def)
                        .into(bookImage);

                bookNameTxt.setText(bookName);
                bookAuthorTxt.setText(bookAuthor);
                bookRatingTxt.setRating(Float.parseFloat(bookRating));
                userRatedTxt.setRating(user_rate);

                userReviewTxt.setMovementMethod(new ScrollingMovementMethod());
                userReviewTxt.setText(review_text);

                adminRateBar.setRating(reviewInUser.getAdmin_rate());
            }
        }
    }

    int reviewSum;
    int pointSum;
    long groupPointSum = 0;

    @Override
    public void onClick(View view) {
        //adminSaveBtn
        adminSaveBtn.setVisibility(View.GONE);


        final int adminRate = (int) adminRateBar.getRating();
        mDatabase.child("user_list").child(userId).child("reviews").child(reviewKey).child("admin_rate").setValue(adminRate);


        Log.i(TAG, "user.getReview_sum(): " + user.getReview_sum());
        Log.i(TAG, "reviewInUser.getAdmin_rate(): " + reviewInUser.getAdmin_rate());
        Log.i(TAG, "adminRate: " + adminRate);

//            reviewSum = user.getReview_sum() - reviewInUser.getAdmin_rate() + adminRate;
        // 3 - 3 + 4

        mDatabase.child("user_list").child(userId).child("review_sum").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long reviewSumData = (long) dataSnapshot.getValue();

                    reviewSum = (int) reviewSumData - reviewInUser.getAdmin_rate() + adminRate;

                    mDatabase.child("user_list").child(userId).child("review_sum").setValue(reviewSum);

                    pointSum = user.getBookCount() * 10 + reviewSum;
                    mDatabase.child("user_list").child(userId).child("point").setValue(pointSum);

                    new ReCalcGroupPoints(UserReviewCheckActivity.this).execute();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private class ReCalcGroupPoints extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public ReCalcGroupPoints(UserReviewCheckActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.re_calc_points));
            dialog.show();
        }
        @Override
        protected Void doInBackground(Void... args) {
            mDatabase.child("user_list").orderByChild("group_id").equalTo(user.getGroup_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot usersData: dataSnapshot.getChildren()){
                        User user = usersData.getValue(User.class);

                        assert user != null;
                        int point = user.getPoint();
                        groupPointSum += point;
                        userList.add(user);
                    }
                    mDatabase.child("group_list").child(user.getGroup_id()).child("sum_point").setValue(groupPointSum);
                    updateUsersRating();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }

        public void updateUsersRating(){
            Collections.sort(userList, User.userPoint);
            for(int i = 0; i < userList.size(); i++){
                String uId = userList.get(i).getPhoneNumber();
                mDatabase.child("user_list").child(uId).child("ratingInGroups").setValue(i+1);

                Log.i(TAG, "userId: "+uId);
                Log.i(TAG, "ratingInGroups: "+(i+1));
            }

        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.user_review_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.done:
//
//                float newRating = bookNewRating.getRating();
//                String reviewText = userReviewEditText.getText().toString();
//
//                if (newRating > 0 && reviewText.length() > 0) {
//
//                    addRateAndReview(newRating, reviewText);
//
//                } else {
//                    Toast.makeText(this, getString(R.string.rating_error), Toast.LENGTH_SHORT).show();
//                }

                break;

        }


        return super.onOptionsItemSelected(item);
    }

    public boolean isAdmin() {

        if (currentUserEmail != null && currentUserEmail.contains("admin")) {
            return true;
        } else {
            return false;
        }
    }

    public void addRateAndReview(float rate, String comment) {

        String ratingSplit[] = book.getRating().split(",");

        int ratingFirstPart = Integer.parseInt(ratingSplit[0]);
        int ratingSecondPart = Integer.parseInt(ratingSplit[1]);
        int increasedSecdPart = ratingSecondPart + 1;
        int lastRate = (ratingFirstPart + (int) rate) / increasedSecdPart;

        String bId = book.getFirebaseKey();

        mDatabase.child("book_list").child(bId).child("rating").setValue("" + lastRate + "," + increasedSecdPart);

        mDatabase.child("book_list").child(bId).child("readed").child(userId).setValue(1);
        mDatabase.child("book_list").child(bId).child("reading").child(userId).removeValue();

        mDatabase.child("user_list").child(userId).child("reading").child(bId).removeValue();
        mDatabase.child("user_list").child(userId).child("readed").child(bId).setValue(1);


        String fKey = mDatabase.child("book_list").child(bId).child("reviews").push().getKey();

        ReviewInBook review = new ReviewInBook("" + fKey, "" + userId, (int) rate, "" + comment);
        ReviewInUser review2 = new ReviewInUser("" + fKey, "" + bId, (int) rate, "" + comment, 0);

        mDatabase.child("book_list").child(bId).child("reviews").child(fKey).setValue(review);
        mDatabase.child("user_list").child(userId).child("reviews").child(fKey).setValue(review2);


        Toast.makeText(this, book.getName() + " rated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
