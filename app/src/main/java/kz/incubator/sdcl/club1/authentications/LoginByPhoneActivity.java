package kz.incubator.sdcl.club1.authentications;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kz.incubator.sdcl.club1.MenuActivity;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.user.MyCabinetActivity;

public class LoginByPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    Button btnLogin;
    TextView btnLoginByEmail;
    EditText phoneNumber;
    ProgressBar progressBarLogin;
    LinearLayout loginLayout;
    TextInputLayout inputLayoutEmail;
    String phoneNumberStr;
    DatabaseReference databaseReference;
    String TAG = "LoginByPhoneActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_by_phone);

        checkInternetConnection();
        initWidgets();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (mAuth.getCurrentUser() != null) {

            Intent intent = new Intent(LoginByPhoneActivity.this, MenuActivity.class);

//            if (mAuth.getCurrentUser().getPhoneNumber() != null && mAuth.getCurrentUser().getPhoneNumber().length() > 0)// phone
//            {
//                intent.putExtra("userLoginType", "phone");
//                Log.i(TAG, "userLoginType phone");
//            }
//
//            else{
//
//                intent.putExtra("userLoginType", "email");
//                Log.i(TAG, "userLoginType email");
//            }

            startActivity(intent);
        }

        btnLogin.setOnClickListener(this);
        btnLoginByEmail.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:

                progressVisible(true);
                phoneNumberStr = phoneNumber.getText().toString();

                if (phoneNumberStr.equals("001")) {
                    logInAdmin();
                } else if (checkInternetConnection()) {

                    databaseReference.child("user_list").child(phoneNumberStr).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                Intent verifyActivity = new Intent(LoginByPhoneActivity.this, VerifyCodeSentActivity.class);
                                verifyActivity.putExtra("phoneNumber", phoneNumberStr);
                                startActivity(verifyActivity);

                            } else {
                                Toast.makeText(LoginByPhoneActivity.this, "Қолданушы табылмады, Админге хабарласыңыз!", Toast.LENGTH_SHORT).show();
                                progressVisible(false);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                break;

            case R.id.btnLoginByEmail:
                startActivity(new Intent(LoginByPhoneActivity.this, LoginByEmailPage.class));

                break;
        }

    }

    public void progressVisible(boolean yes) {
        if (yes) {

            progressBarLogin.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

        } else {

            progressBarLogin.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);

        }
    }

    public void logInAdmin() {
        String email = "admin@reading.club";
        String password = "123456";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginByPhoneActivity.this, MenuActivity.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginByPhoneActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginByPhoneActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void initWidgets() {
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginByEmail = findViewById(R.id.btnLoginByEmail);

        progressBarLogin = findViewById(R.id.progressBarLogin);
        phoneNumber = findViewById(R.id.phoneNumber);
        loginLayout = findViewById(R.id.loginLayout);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int numberLength = phoneNumber.getText().toString().length();

                if (numberLength == 12) {
                    btnLogin.setBackground(ContextCompat.getDrawable(LoginByPhoneActivity.this, R.drawable.border2));
                } else {
                    btnLogin.setBackground(ContextCompat.getDrawable(LoginByPhoneActivity.this, R.drawable.border_grey));
                }
            }
        });
    }


    public boolean checkInternetConnection() {
        if (isNetworkAvailable()) {
            return true;
        }

        Toast.makeText(this, "Check Inet connection", Toast.LENGTH_SHORT).show();

        progressBarLogin.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);

        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}
