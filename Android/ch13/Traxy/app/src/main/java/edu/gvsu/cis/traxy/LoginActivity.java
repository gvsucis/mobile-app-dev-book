package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends AppCompatActivity {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    @BindView(R.id.email) EditText email;
    @BindView(R.id.signin) Button signin;
    @BindView(R.id.password2) EditText passwd;

    private FirebaseAuth mAuth;
    Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
    }

    @Override
    public void onResume(){
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent toMain = new Intent(this, MainActivity.class);
            startActivity(toMain);
            finish();
        }

    }

    @OnClick(R.id.signup)
    public void signUp()
    {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity (intent);
    }

    @OnClick(R.id.signin)
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
        String passStr = passwd.getText().toString().toLowerCase();

        mAuth.signInWithEmailAndPassword(emailStr, passStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent toMain = new Intent(this, MainActivity.class);
                        toMain.putExtra("email", emailStr);
                        startActivity(toMain);
                        finish();
                    } else {
                        signin.startAnimation (shake);
                        Snackbar.make(email, R.string.incorrect_password,
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                });

    }
}
