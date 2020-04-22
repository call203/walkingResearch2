package org.walkpackage.notification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.walkingresearch.R;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

  Button  mLoginBtn;
  TextView mResigettxt;
  EditText mEmailText, mPasswordText;
  private FirebaseAuth firebaseAuth = null;

  private GoogleSignInClient mGoogleSignInClient;
  private static final int RC_SIGN_IN = 9001;
  private SignInButton signInithGoogle_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        firebaseAuth =  FirebaseAuth.getInstance();
        //버튼 등록하기
        mResigettxt = findViewById(R.id.register_t2);
        mLoginBtn = findViewById(R.id.login_btn);
        mEmailText = findViewById(R.id.emailEt);
        mPasswordText = findViewById(R.id.passwordEdt);

        //구글 Sign in
        signInithGoogle_btn = findViewById(R.id.GoogleSignin_btn);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        if (firebaseAuth.getCurrentUser() != null) {
//            Intent intent = new Intent(getApplication(), BottomNaviActivity.class);
//            startActivity(intent);
//            finish();
//        }
        signInithGoogle_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });



        //가입 버튼이 눌리면
        mResigettxt.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //intent함수를 통해 register액티비티 함수를 호출한다.
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));

            }
        });

        //로그인 버튼이 눌리면
        mLoginBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                String email = mEmailText.getText().toString().trim();
                String pwd = mPasswordText.getText().toString().trim();
                firebaseAuth.signInWithEmailAndPassword(email,pwd)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent intent = new Intent(MainActivity.this, BottomNaviActivity.class);
                                    startActivity(intent);

                                }else{
                                    Toast.makeText(MainActivity.this,"로그인 오류",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }
    //구글로 로그인
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final int[] exist = {0};
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String email = user.getEmail();
                            final String uid = user.getUid();
                            String name = user.getDisplayName();

                            //해쉬맵 테이블을 파이어베이스 데이터베이스에 저장

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                        if (snapshot.getKey().equals(uid)){
                                            exist[0] = 1;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            if(exist[0] == 0){
                                //해쉬맵 테이블을 파이어베이스 데이터베이스에 저장
                                Log.d("기록 완료!",uid);

                                HashMap<Object,String> hashMap = new HashMap<>();

                                hashMap.put("uid",uid);
                                hashMap.put("email",email);
                                hashMap.put("name",name);

                                reference.child(uid).setValue(hashMap);

                            }

                            Intent intent = new Intent(MainActivity.this, BottomNaviActivity.class);
                            startActivity(intent);

                        } else {

                        }
                    }
                });
    }
}
