package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
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

    Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
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
        if (!passStr.contains("traxy")) {
            signin.startAnimation (shake);
            return;
        }
        Snackbar.make(email, "Login verified",
                Snackbar.LENGTH_LONG).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("email",emailStr);
        startActivity (intent);
        finish();
    }
}
