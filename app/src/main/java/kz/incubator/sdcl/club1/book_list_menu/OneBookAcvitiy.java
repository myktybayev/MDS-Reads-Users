package kz.incubator.sdcl.club1.book_list_menu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.module.Book;
import kz.incubator.sdcl.club1.book_list_menu.one_book_fragments.AlreadyReadFragment;
import kz.incubator.sdcl.club1.book_list_menu.one_book_fragments.BookDescFragment;
import kz.incubator.sdcl.club1.book_list_menu.one_book_fragments.UserReadingFragment;
import kz.incubator.sdcl.club1.book_list_menu.one_book_fragments.UserReviewsFragment;

public class OneBookAcvitiy extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    AppBarLayout appBarLayout;
    ImageView bookImage;
    TextView bookName, bookAuthor;
    TextView page_number;
    RatingBar bookRating;

    ViewPager viewPager;
    DatabaseReference mDatabase, userRef;
    StorageReference storageReference;
    Book book;
    TabLayout tabLayout;
    Button readBookBtn;
    String userId;
    BookDescFragment bookDescFragment;
    UserReadingFragment userReadingFragment;
    UserReviewsFragment userReviewsFragment;
    AlreadyReadFragment alreadyReadFragment;
    String currentUserEmail = "empty";

    private int[] tabIcons = {
            R.drawable.ic_class_black_24dp,
            R.drawable.ic_cloud_done_black_24dp,
            R.drawable.ic_receipt_black_24dp,
            R.drawable.ic_assistant_black_24dp,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_book2);
        initUserId();
        initWidgets();

        Bundle bundle = getIntent().getExtras();
        book = (Book) bundle.getSerializable("book");

        initializeBundle(bundle, book);
        initializeToolbar();
    }

    FirebaseUser currentUser;

    public void initUserId() {
        userId = "";

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
            userId = currentUser.getPhoneNumber();
        } else {
            userId = currentUser.getDisplayName();
//            currentUserEmail = currentUser.getEmail();
        }
    }

    public void initializeToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(bName);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void initWidgets() {
        toolbar = findViewById(R.id.toolbars);
        appBarLayout = findViewById(R.id.app_bar);

        bookName = findViewById(R.id.bookName);
        bookAuthor = findViewById(R.id.bookAuthor);

        page_number = findViewById(R.id.page_number);
        bookRating = findViewById(R.id.bookRating);

        bookImage = findViewById(R.id.bookImage);
        viewPager = findViewById(R.id.viewPager);
        readBookBtn = findViewById(R.id.readBookBtn);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        bookDescFragment = new BookDescFragment();
        readBookBtn.setOnClickListener(this);
    }

    public static String bName, bDesc = "", bId, bAuthor;

    public void initializeBundle(Bundle bundle, Book book) {
        if (bundle != null && book != null) {
            bId = book.getFirebaseKey();
            bName = book.getName();
            bDesc = book.getDesc();

            bAuthor = book.getAuthor();
            int bPage_number = book.getPage_number();
            String bRating = book.getRating();

            Glide.with(getApplicationContext())
                    .load(book.getPhoto())
                    .placeholder(R.drawable.item_book)
                    .centerCrop()
                    .into(bookImage);

            bookName.setText(bName);
            bookAuthor.setText(bAuthor);
            page_number.setText(getString(R.string.page) + bPage_number);

            int ratingInt = Integer.parseInt(bRating.split(",")[0]);
            bookRating.setRating(ratingInt);


            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putString("bookId", bId);

            userReadingFragment = new UserReadingFragment();
            userReviewsFragment = new UserReviewsFragment();
            alreadyReadFragment = new AlreadyReadFragment();

            userReadingFragment.setArguments(args);
            userReviewsFragment.setArguments(args);
            alreadyReadFragment.setArguments(args);

            setupViewPager(viewPager);
            setupTabIcons();
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.readBookBtn:

                mDatabase.child("user_list").child(userId).child("reading").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Toast.makeText(OneBookAcvitiy.this, getString(R.string.not_finished_book), Toast.LENGTH_LONG).show();

                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(OneBookAcvitiy.this);
                            builder.setTitle(getString(R.string.sure_read_book));

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                builder.setMessage(Html.fromHtml(bAuthor + "- <b>" + bName + "</b>", Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                builder.setMessage(Html.fromHtml(bAuthor + "- <b>" + bName + "</b>"));
                            }

                            builder.setCancelable(false)
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {


                                            String bID = book.getFirebaseKey();

                                            mDatabase.child("user_list").child(userId).child("reading").child(bID).setValue(1);
                                            mDatabase.child("book_list").child(bID).child("reading").child(userId).setValue(1);
                                            Toast.makeText(OneBookAcvitiy.this, getString(R.string.book_added_to_profile), Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                break;
        }
    }


    int BOOK_EDIT = 98;

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    private void setupViewPager(ViewPager viewPager) {
        SimplePageFragmentAdapter adapter = new SimplePageFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(bookDescFragment, getString(R.string.bookDescFragment));
        adapter.addFragment(userReadingFragment, getString(R.string.userReadingFragment));
        adapter.addFragment(alreadyReadFragment, getString(R.string.alreadyReadFragment));
        adapter.addFragment(userReviewsFragment, getString(R.string.userReviewsFragment));

        viewPager.setAdapter(adapter);
    }

    public static String getBookDesc() {
        return bDesc;
    }

    public static String getBookId() {
        return bId;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public class SimplePageFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        public SimplePageFragmentAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String one) {
            mFragmentList.add(fragment);
            titles.add(one);
        }
    }
}
