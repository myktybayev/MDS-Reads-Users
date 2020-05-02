//package kz.incubator.sdcl.club1.users_list_menu.Copy_classes;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.AsyncTask;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.View;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
//import kz.incubator.sdcl.club1.database.StoreDatabase;
//import kz.incubator.sdcl.club1.groups_menu.adapters.UserListAdapter;
//import kz.incubator.sdcl.club1.users_list_menu.module.User;
//
//import static kz.incubator.sdcl.club1.MenuActivity.setTitle;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BCOUNT;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_EMAIL;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_GROUP;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_GROUP_ID;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_INFO;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHONE;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_POINT;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_REVIEW_SUM;
//import static kz.incubator.sdcl.club1.database.StoreDatabase.TABLE_USER;
//
//public class GetUsersAsyncTaskCopy extends AsyncTask<Void, User, Void> {
//
//    ArrayList<User> userList;
//    RecyclerView recyclerView;
//    SwipeRefreshLayout swipeRefreshLayout;
//    StoreDatabase storeDb;
//    SQLiteDatabase sqdb;
//    DatabaseReference mDatabaseRef, userRef;
//    UserListAdapter listAdapter;
//    Context context;
//    String version;
//    View progressLoading;
//    String groupId;
//
//    public GetUsersAsyncTaskCopy(Context context, String groupId, RecyclerView recyclerView, SwipeRefreshLayout refreshLayout, String version, View progressLoading) {
//        this.recyclerView = recyclerView;
//        this.groupId = groupId;
//        this.swipeRefreshLayout = refreshLayout;
//        this.context = context;
//        this.version = version;
//        this.progressLoading = progressLoading;
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        swipeRefreshLayout.setRefreshing(true);
//        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
//        storeDb = new StoreDatabase(context);
//        sqdb = storeDb.getWritableDatabase();
//        userList = new ArrayList<>();
//        userRef = mDatabaseRef.child("user_list");
//    }
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
//        res.moveToNext();
//        String getDay = res.getString(0);
//
//        ContentValues versionValues = new ContentValues();
//        versionValues.put("user_ver", version);
//        sqdb.update("versions", versionValues, "user_ver=" + getDay, null);
//
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                userList.clear();
//                storeDb.cleanUsers(sqdb);
//
//                for (DataSnapshot usersSnapshot : dataSnapshot.getChildren()) {
//                    if (!usersSnapshot.getKey().equals("reading")) {
//                        User user = usersSnapshot.getValue(User.class);
//                        Log.i("user_info", user.getInfo());
//
//                        String info = user.getInfo();
//                        String email = user.getEmail();
//                        String phoneNumber = user.getPhoneNumber();
//                        String group = user.getGroupName();
//                        String group_id = user.getGroup_id();
//
//                        String photo = user.getPhoto();
//                        String imgStorageName = user.getImgStorageName();
//
//                        int bookCount = user.getBookCount();
//                        int point = user.getPoint();
//                        int review_sum = user.getReview_sum();
//
//                        ContentValues teacherValue = new ContentValues();
//                        teacherValue.put(COLUMN_INFO, info);
//                        teacherValue.put(COLUMN_EMAIL, email);
//                        teacherValue.put(COLUMN_PHONE, phoneNumber);
//                        teacherValue.put(COLUMN_GROUP, group);
//                        teacherValue.put(COLUMN_GROUP_ID, group_id);
//                        teacherValue.put(COLUMN_PHOTO, photo);
//                        teacherValue.put(COLUMN_POINT, point);
//                        teacherValue.put(COLUMN_REVIEW_SUM, review_sum);
//                        teacherValue.put(COLUMN_IMG_STORAGE_NAME, imgStorageName);
//                        teacherValue.put(COLUMN_BCOUNT, bookCount);
//
//                        sqdb.insert(TABLE_USER, null, teacherValue);
//
//                        if(group_id.equals(groupId)) userList.add(user);
//                    }
//                }
//                Collections.reverse(userList);
//                listAdapter = new UserListAdapter(context, userList);
//                recyclerView.setAdapter(listAdapter);
//                progressLoading.setVisibility(View.GONE);
//
//                setTitle("Users "+userList.size());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        return null;
//    }
//
//    @Override
//    protected void onProgressUpdate(User... values) {
//        super.onProgressUpdate(values);
//        userList.add(values[0]);
//        listAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
//        swipeRefreshLayout.setRefreshing(false);
//    }
//}
