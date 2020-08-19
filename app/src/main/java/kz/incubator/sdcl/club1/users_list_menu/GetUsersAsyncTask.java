package kz.incubator.sdcl.club1.users_list_menu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.groups_menu.adapters.UserListAdapter;
import kz.incubator.sdcl.club1.groups_menu.module.User;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BCOUNT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_EMAIL;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_ENTER_DATE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_GROUP;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_GROUP_ID;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_INFO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_POINT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_RAINTING_IN_GROUPS;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_REVIEW_SUM;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_USER_TYPE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.TABLE_USER;

public class GetUsersAsyncTask extends AsyncTask<Void, User, Void> {

    ArrayList<User> userList;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    DatabaseReference mDatabaseRef, userRef;
    UserListAdapter listAdapter;
    Context context;
    String version;

    public GetUsersAsyncTask(Context context, String version) {
        this.context = context;
        this.version = version;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(context);
        sqdb = storeDb.getWritableDatabase();
        userList = new ArrayList<>();
        userRef = mDatabaseRef.child("user_list");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        String getDay = res.getString(0);

        ContentValues versionValues = new ContentValues();
        versionValues.put("user_ver", version);
        sqdb.update("versions", versionValues, "user_ver=" + getDay, null);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                storeDb.cleanUsers(sqdb);

                for (DataSnapshot usersSnapshot : dataSnapshot.getChildren()) {
                    if (!usersSnapshot.getKey().equals("reading")) {
                        User user = usersSnapshot.getValue(User.class);
                        Log.i("user_info", user.getInfo());

                        String info = user.getInfo();
                        String email = user.getEmail();
                        String phoneNumber = user.getPhoneNumber();
                        String group = user.getGroupName();
                        String group_id = user.getGroup_id();

                        String photo = user.getPhoto();
                        String enter_date = user.getEnterDate();
                        String imgStorageName = user.getImgStorageName();
                        String user_type = user.getUserType();

                        int bookCount = user.getBookCount();
                        int point = user.getPoint();
                        int review_sum = user.getReview_sum();
                        int ratingInGroups = user.getRatingInGroups();

                        ContentValues teacherValue = new ContentValues();
                        teacherValue.put(COLUMN_INFO, info);
                        teacherValue.put(COLUMN_EMAIL, email);
                        teacherValue.put(COLUMN_PHONE, phoneNumber);
                        teacherValue.put(COLUMN_GROUP, group);
                        teacherValue.put(COLUMN_GROUP_ID, group_id);
                        teacherValue.put(COLUMN_PHOTO, photo);
                        teacherValue.put(COLUMN_ENTER_DATE, enter_date);
                        teacherValue.put(COLUMN_USER_TYPE, user_type);
                        teacherValue.put(COLUMN_POINT, point);
                        teacherValue.put(COLUMN_REVIEW_SUM, review_sum);
                        teacherValue.put(COLUMN_RAINTING_IN_GROUPS, ratingInGroups);
                        teacherValue.put(COLUMN_IMG_STORAGE_NAME, imgStorageName);
                        teacherValue.put(COLUMN_BCOUNT, bookCount);

                        sqdb.insert(TABLE_USER, null, teacherValue);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return null;
    }

    @Override
    protected void onProgressUpdate(User... values) {
        super.onProgressUpdate(values);
        userList.add(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
