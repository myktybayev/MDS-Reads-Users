package kz.incubator.sdcl.club1.my_cabinet;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.groups_menu.profile_fragments.ReadedBookListFragment;
import kz.incubator.sdcl.club1.groups_menu.profile_fragments.ReadingBookListFragment;
import kz.incubator.sdcl.club1.groups_menu.profile_fragments.RecommendationBookListFragment;
import kz.incubator.sdcl.club1.groups_menu.profile_fragments.ReviewsForBookFragment;
import kz.incubator.sdcl.club1.users_list_menu.EditUser;
import kz.incubator.sdcl.club1.groups_menu.module.User;

public class MyCabinetActivity extends AppCompatActivity {
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    TextView username, ticketType, phoneNumber, userEmail, userRating, userPoint;
    TextView readBookCount;
    CircleImageView userImage;
    ImageView userTypeIcon;
    ViewPager viewPager;
    TabLayout tabLayout;
    int USER_EDIT = 97;

    public static User user;
    StorageReference storageReference;
    DatabaseReference mDatabase;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    View progressLoading;

    ReadingBookListFragment readingBookListFragment;
    ReadedBookListFragment readedBookListFragment;
    ReviewsForBookFragment reviewsForBookFragment;
    RecommendationBookListFragment recommendationBookListFragment;
    Bundle bundleFragment;

    private int[] tabIcons = {
            R.drawable.ic_class_black_24dp,
            R.drawable.ic_cloud_done_black_24dp,
            R.drawable.ic_receipt_black_24dp,
            R.drawable.ic_assistant_black_24dp,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_cabinet_activity);

        initWidgets();
    }

    public void initWidgets() {
        toolbar = findViewById(R.id.toolbars);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(getString(R.string.menu_my_cabinet));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        appBarLayout = findViewById(R.id.app_bar);
        progressLoading = findViewById(R.id.llProgressBar);

        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        username = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        phoneNumber = findViewById(R.id.phoneNumber);
        userRating = findViewById(R.id.userRating);
        ticketType = findViewById(R.id.ticketType);
        readBookCount = findViewById(R.id.readBookCount);
        userImage = findViewById(R.id.userImage);
        userPoint = findViewById(R.id.userPoint);
        userTypeIcon = findViewById(R.id.userTypeIcon);
        viewPager = findViewById(R.id.viewPager);

        readingBookListFragment = new ReadingBookListFragment();
        readedBookListFragment = new ReadedBookListFragment();
        reviewsForBookFragment = new ReviewsForBookFragment();
        recommendationBookListFragment = new RecommendationBookListFragment();

        initUserId();
        initializeUser();
//        bookReadedCountListener();

        addListener();

    }

    FirebaseUser currentUser;
    String userId = "";

    public void initUserId() {
        userId = "";

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
            userId = currentUser.getPhoneNumber();
        } else {
            userId = currentUser.getDisplayName();
        }
    }

    public void initializeUser() {
        Query myTopPostsQuery = mDatabase.child("user_list").child(userId);
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    user = dataSnapshot.getValue(User.class);
                    updateUserUI(user);

                    progressLoading.setVisibility(View.GONE);

                } else {
                    Toast.makeText(MyCabinetActivity.this, getString(R.string.can_not_find_user), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateUserUI(User user) {
        Glide.with(getApplicationContext())
                .load(user.getPhoto())
                .crossFade()
                .dontAnimate()
                .placeholder(R.drawable.user_def)
                .into(userImage);

        username.setText(user.getInfo());
        userEmail.setText(user.getEmail());
        phoneNumber.setText(user.getPhoneNumber());
        ticketType.setText(user.getGroupName());
        readBookCount.setText("" + user.getBookCount());
        userRating.setText("" + user.getRatingInGroups());
        userPoint.setText("" + user.getPoint());

        int uTypeIcon = R.color.transparent;

        switch (user.getUserType()) {
            case "gold":
                uTypeIcon = R.drawable.ic_gold;
                break;
            case "silver":
                uTypeIcon = R.drawable.ic_silver;
                break;
            case "bronze":
                uTypeIcon = R.drawable.ic_bronze;
                break;
        }

        userTypeIcon.setImageResource(uTypeIcon);

        if (readingBookListFragment.getArguments() == null) {

            bundleFragment = new Bundle();
            bundleFragment.putSerializable("user", user);

            readingBookListFragment.setArguments(bundleFragment);
            readedBookListFragment.setArguments(bundleFragment);
            reviewsForBookFragment.setArguments(bundleFragment);
            recommendationBookListFragment.setArguments(bundleFragment);

            setupViewPager(viewPager);
        }
    }

    public void addListener() {
        mDatabase.child("user_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                updateUserUI(user);
                storeDb.updateUser(sqdb, user);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    int userReadBooksCount = 0;

    //    public void bookReadedCountListener() {
//        mDatabase.child("user_list").child(userId).child("readed").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    userReadBooksCount = (int)dataSnapshot.getChildrenCount();
//                    readBookCount.setText("" + userReadBooksCount);
//                    mDatabase.child("user_list").child(userId).child("bookCount").setValue(dataSnapshot.getChildrenCount());
////                    fragmentAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
    SimplePageFragmentAdapter fragmentAdapter;

    private void setupViewPager(ViewPager viewPager) {
        fragmentAdapter = new SimplePageFragmentAdapter(getSupportFragmentManager());

        fragmentAdapter.addFragment(readingBookListFragment, getString(R.string.reading));
        fragmentAdapter.addFragment(readedBookListFragment, getString(R.string.readed));
        fragmentAdapter.addFragment(reviewsForBookFragment, getString(R.string.reviews));
        fragmentAdapter.addFragment(recommendationBookListFragment, getString(R.string.recommendations));

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(fragmentAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_cabinet_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.editUser:

                Intent intent = new Intent(this, EditUser.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                intent.putExtras(bundle);
                startActivityForResult(intent, USER_EDIT);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USER_EDIT) {
            if (resultCode == Activity.RESULT_OK) {

                Bundle bundle = data.getExtras();
                User savedUser = (User) bundle.getSerializable("user");

                Glide.with(getApplicationContext())
                        .load(savedUser.getPhoto())
                        .crossFade()
                        .dontAnimate()
                        .placeholder(R.drawable.user_def)
                        .into(userImage);


                username.setText(savedUser.getInfo());

                user.setInfo(savedUser.getInfo());
                user.setPhoto(savedUser.getPhoto());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
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