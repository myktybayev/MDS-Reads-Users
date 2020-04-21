package kz.incubator.sdcl.club1.users_list_menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.groups_menu.Groups;
import kz.incubator.sdcl.club1.users_list_menu.module.User;

public class AddUser extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    Button userRegisterBtn;
    TextView changeIt;
    EditText nameOfUser, numberOfUser, userEmail;

    DatabaseReference databaseReference;
    String version;
    ProgressBar progressBar;
    String uNameSurname, uPhone, uEmail, groupName, groupId;
    Spinner spinnerGroups;
    FirebaseAuth mAuth;
    ArrayAdapter<String> spinnerAdapter;
    ArrayList<String> groupList;
    ArrayList<Groups> groupsStore;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user);
        initView();
        initIncreaseVersion();
    }

    public void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add User");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        changeIt = findViewById(R.id.changeIt);
        userRegisterBtn = findViewById(R.id.userRegisterBtn);
        nameOfUser = findViewById(R.id.nameOfUser);
        numberOfUser = findViewById(R.id.numberOfUser);
        numberOfUser.setSelection(numberOfUser.getText().length());

        userEmail = findViewById(R.id.userEmail);
        progressBar = findViewById(R.id.ProgressBar);
        spinnerGroups = findViewById(R.id.spinnerGroups);

        groupList = new ArrayList<>();
        groupsStore = new ArrayList<>();
        getGroupsFromFirebase();

        spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_group_spinner, groupList);
        spinnerGroups.setAdapter(spinnerAdapter);

        progressBar.setVisibility(View.GONE);

        userRegisterBtn.setOnClickListener(this);

        spinnerGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                groupName = adapterView.getItemAtPosition(pos).toString();
                groupId = groupsStore.get(pos).getGroup_id();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userRegisterBtn:


                uNameSurname = nameOfUser.getText().toString();
                uPhone = numberOfUser.getText().toString();
                uEmail = userEmail.getText().toString();

                if (TextUtils.isEmpty(uNameSurname)) {
                    nameOfUser.setError("Please fill info");
                    return;
                }

                if (TextUtils.isEmpty(uEmail)) {
                    userEmail.setError("Please fill email ");
                    return;
                }

                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!uEmail.matches(emailPattern)) {
                    userEmail.setError("Please fill email correctly");
                    return;
                }

                if (TextUtils.isEmpty(uPhone)) {
                    numberOfUser.setError("Please fill Number ");
                    return;
                }

                registerUser();


                break;

        }
    }

    public void getGroupsFromFirebase(){
        databaseReference.child("group_list").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot group: dataSnapshot.getChildren()){
                        Groups itemGroup = group.getValue(Groups.class);
                        groupsStore.add(itemGroup);
                        groupList.add(itemGroup.getGroup_name());
                    }
                    spinnerAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    String TAG = "AddUser";

    public void progressVisible(boolean yes){
        if(yes){

            userRegisterBtn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

        }else{

            userRegisterBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        }
    }

    private void registerUser() {
        progressVisible(true);

        final User userInfo = new User(uNameSurname, uEmail, uPhone, groupId, groupName,"url", "url", 0, 0, 0);

        databaseReference.child("user_list").child(userInfo.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    Toast.makeText(AddUser.this, "User with this Phone number already exist", Toast.LENGTH_SHORT).show();
                    progressVisible(false);

                } else {

                    mAuth.createUserWithEmailAndPassword(uEmail, "123456")
                            .addOnCompleteListener(AddUser.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        FirebaseUser user = mAuth.getCurrentUser();

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(uPhone)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User profile updated.");
                                                        }
                                                    }
                                                });

                                        mAuth.sendPasswordResetEmail(uEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(AddUser.this, "User почтасына ссылка жіберілді", Toast.LENGTH_SHORT).show();


                                                    databaseReference.child("user_list").child(userInfo.getPhoneNumber()).setValue(userInfo);
                                                    databaseReference.child("user_ver").setValue(getIncreasedVersion());

                                                    progressBar.setVisibility(View.GONE);
                                                    logInAdmin();
                                                    finish();
                                                }
                                            }
                                        });

                                    } else {

                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e) {

                                            Toast.makeText(AddUser.this, "Құпия сөз әлсіз енгіздіңіз, қайтадан жасап көріңіз!", Toast.LENGTH_SHORT).show();

                                        } catch (FirebaseAuthInvalidCredentialsException e) {

                                            Toast.makeText(AddUser.this, "Email қате жаздыңыз,қайтадан жасап көріңіз!", Toast.LENGTH_SHORT).show();


                                        } catch (FirebaseAuthUserCollisionException e) {

                                            Toast.makeText(AddUser.this, "Мұндай почтамен қолданушы енгізілген!", Toast.LENGTH_SHORT).show();                    progressVisible(false);


                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }

                                        progressVisible(false);

                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void logInAdmin(){
        String email = "admin@reading.club";
        String password = "123456";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginByPhoneActivity", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    public void initIncreaseVersion() {
        databaseReference.child("user_ver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                version = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getIncreasedVersion() {
        int ver = Integer.parseInt(version);
        ver += 1;
        return ver;
    }


    public String getIdNumber() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }

}
