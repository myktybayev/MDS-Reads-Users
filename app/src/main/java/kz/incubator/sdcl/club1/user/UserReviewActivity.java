package kz.incubator.sdcl.club1.user;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInBook;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;

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
            userId = bundle.getString("userId");

            Glide.with(this)
                    .load(book.getPhoto())
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
        ReviewInUser review2 = new ReviewInUser("" + fKey, "" + bId, (int) rate, comment, 0);

        mDatabase.child("book_list").child(bId).child("reviews").child(fKey).setValue(review);
        mDatabase.child("user_list").child(userId).child("reviews").child(fKey).setValue(review2);


        Toast.makeText(this, book.getName() + " rated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
