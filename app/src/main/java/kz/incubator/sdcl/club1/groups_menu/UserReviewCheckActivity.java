package kz.incubator.sdcl.club1.groups_menu;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.groups_menu.module.User;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BDESC;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BRATING;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;

public class UserReviewCheckActivity extends AppCompatActivity{


    Toolbar toolbar;
    AppBarLayout appBarLayout;
    Bundle bundle;
    ReviewInUser reviewInUser;
    DatabaseReference mDatabase;
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
    TextView adminRate;
    @BindView(R.id.userReview)
    TextView userReviewTxt;

    ArrayList<User> userList;
    String TAG = "UserReviewCheckActivity";
    String currentUserEmail = "empty";
    FirebaseUser currentUser;
    User user;
    String reviewKey;

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
        getSupportActionBar().setTitle(getString(R.string.admin_rate));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(this);
        userList = new ArrayList<>();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
        }
    }


    public void initializeBook() {

        bundle = getIntent().getExtras();
        if (bundle != null) {

            reviewInUser = (ReviewInUser) bundle.getSerializable("userReview");
            user = (User) bundle.getSerializable("user");

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

                int adminRating = reviewInUser.getAdmin_rate();


                if(adminRating==0){
                    adminRate.setTextColor(getResources().getColor(R.color.orange));

                }else if(adminRating < 0){

                    adminRate.setTextColor(getResources().getColor(R.color.red));
                    adminRate.setText("" + adminRating);

                }else{
                    adminRate.setTextColor(getResources().getColor(R.color.green_dark));
                    adminRate.setText("" + adminRating);
                }
            }
        }
    }
}
