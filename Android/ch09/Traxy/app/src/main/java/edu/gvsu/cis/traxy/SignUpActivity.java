package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    @BindView(R.id.email) EditText email;
    @BindView(R.id.password) EditText passwd;
    @BindView(R.id.password2) EditText verifyPasswd;
    @BindView(R.id.toolbar) Toolbar toolbar;
    Animation shake;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.fab)
    public void verify() {
        String emailStr = email.getText().toString();
        if (emailStr.length() == 0) {
            Snackbar.make(email, R.string.email_required,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!EMAIL_REGEX.matcher(emailStr).find()) {
            Snackbar.make(email, R.string.incorrect_email,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        String passStr = passwd.getText().toString();
        String verifyPassStr = verifyPasswd.getText().toString();
        if (!verifyPassStr.equals(passStr)) {
            verifyPasswd.startAnimation(shake);
            passwd.startAnimation(shake);
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.putExtra("email",emailStr);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity (intent);
            } else {
                String msg = task.getException().getMessage();
                Snackbar.make(email, msg, Snackbar.LENGTH_SHORT).show();
            }
        });

    }

}
