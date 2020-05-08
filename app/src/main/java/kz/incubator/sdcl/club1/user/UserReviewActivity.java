package kz.incubator.sdcl.club1.user;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInBook;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;
import kz.incubator.sdcl.club1.users_list_menu.module.User;

public class UserReviewActivity extends AppCompatActivity {

    Toolbar toolbar;
    AppBarLayout appBarLayout;
    ImageView bookImage;
    TextView bookName, bookAuthor;
    RatingBar bookRating, bookNewRating;
    Bundle bundle;
    Book book;
    EditText userReviewEditText;
    DatabaseReference mDatabase;
    String userId;
    ArrayList<User> userList;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_review);

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
        userList = new ArrayList<>();

        bookName = findViewById(R.id.bookName);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookRating = findViewById(R.id.bookRating);
        bookImage = findViewById(R.id.bookImage);

        bookNewRating = findViewById(R.id.bookNewRating);
        userReviewEditText = findViewById(R.id.userReviewEditText);
    }

    public void initializeBook() {

        bundle = getIntent().getExtras();
        if (bundle != null) {
            book = (Book) bundle.getSerializable("book");
            user = (User) bundle.getSerializable("user");
            userId = bundle.getString("userId");

            Log.i("review_sum", "UserReviewActivity: " + user.getReview_sum());
            Glide.with(getApplicationContext())
                    .load(book.getPhoto())
                    .dontAnimate()
                    .placeholder(R.drawable.item_book)
                    .centerCrop()
                    .into(bookImage);

            bookName.setText(book.getName());
            bookAuthor.setText(book.getAuthor());

            String bRating = book.getRating();
            int ratingInt = Integer.parseInt(bRating.split(",")[0]);
            bookRating.setRating(ratingInt);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_review_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.done:

                float newRating = bookNewRating.getRating();
                String reviewText = userReviewEditText.getText().toString();

                if (newRating > 0 && reviewText.length() > 0) {

                    addRateAndReview(newRating, reviewText);

                } else {
                    Toast.makeText(this, getString(R.string.rating_error), Toast.LENGTH_SHORT).show();
                }

                break;

        }


        return super.onOptionsItemSelected(item);
    }
    String bId;
    public void addRateAndReview(float rate, String comment) {

        String ratingSplit[] = book.getRating().split(",");

        int ratingFirstPart = Integer.parseInt(ratingSplit[0]);
        int ratingSecondPart = Integer.parseInt(ratingSplit[1]);
        int increasedSecdPart = ratingSecondPart + 1;
        int lastRate = (ratingFirstPart + (int) rate) / increasedSecdPart;

        bId = book.getFirebaseKey();

        mDatabase.child("book_list").child(bId).child("rating").setValue("" + lastRate + "," + increasedSecdPart);

        mDatabase.child("book_list").child(bId).child("readed").child(userId).setValue(1);
        mDatabase.child("book_list").child(bId).child("reading").child(userId).removeValue();

        mDatabase.child("user_list").child(userId).child("reading").child(bId).removeValue();
        mDatabase.child("user_list").child(userId).child("readed").child(bId).setValue(1);


        String fKey = mDatabase.child("book_list").child(bId).child("reviews").push().getKey();

        ReviewInBook review = new ReviewInBook("" + fKey, "" + userId, (int) rate, "" + comment);
        ReviewInUser review2 = new ReviewInUser("" + fKey, "" + bId, (int) rate, comment, 0);

        mDatabase.child("book_list").child(bId).child("reviews").child(fKey).setValue(review);
        mDatabase.child("user_list").child(userId).child("reviews").child(fKey).setValue(review2);


        Toast.makeText(this, book.getName() + getString(R.string.rated_successfully), Toast.LENGTH_SHORT).show();
        mDatabase.child("user_list").child(userId).child("readed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long userBookCount = dataSnapshot.getChildrenCount();
                    mDatabase.child("user_list").child(userId).child("bookCount").setValue(userBookCount);

                    int reviewSum = user.getReview_sum();
                    long pointSum = userBookCount * 10 + reviewSum;

                    mDatabase.child("user_list").child(userId).child("point").setValue(pointSum);

                    new ReCalcGroupPoints(UserReviewActivity.this).execute();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        finish();
    }

    int groupPointSum =  0;
    String TAG = "updateUsersRating";

    private class ReCalcGroupPoints extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public ReCalcGroupPoints(UserReviewActivity activity) {
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
}

