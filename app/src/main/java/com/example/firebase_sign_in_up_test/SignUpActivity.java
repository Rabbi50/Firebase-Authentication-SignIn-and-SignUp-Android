package com.example.firebase_sign_in_up_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @BindView(R.id.input_name)
    EditText _nameText;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_mobile)
    EditText _mobileText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup)
    Button _signupButton;
    @BindView(R.id.link_login)
    TextView _loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //get firebase authentication instance
        mAuth=FirebaseAuth.getInstance();
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move_login_activity();
            }
        });
    }
    /**
     * move one activity to another with intent
     */
    private void move_login_activity(){
        // Finish the registration screen and return to the Login activity
        Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
    /**
     * create new user and store user information in Firebase database
     *
     */
    private void sendUserInfoToSignInActivity(String email,String password){
        String name = _nameText.getText().toString().trim();
        String phone = _mobileText.getText().toString().trim();
//        String password = _passwordText.getText().toString();
        String address="Unknown";
        String gender="Unknown";
        Intent intent=new Intent(this,SignInActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("address",address);
        intent.putExtra("phone",phone);
        intent.putExtra("email",email);
        intent.putExtra("password",password);
        intent.putExtra("gender",gender);
        startActivity(intent);
    }

    /**
     * Email verification code using FirebaseUser object and using isSucccessful()function.
     */
    private void sendEmailVerification(String email,String password) {
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this,"Check your Email for verification",Toast.LENGTH_SHORT).show();
                            sendUserInfoToSignInActivity(email,password);
                            set_input_field_empty();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(SignUpActivity.this,"Email not send",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            progressDialog.dismiss();
            Toast.makeText(SignUpActivity.this,"Email not send",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * user sign up
     */
    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();



        // Implement firebase authentication signup .
        authentication();
    }


    /**
     * user authentication signup with email and password
     */

    private void authentication(){
        String email = _emailText.getText().toString().trim();
        String reEnterPassword = _reEnterPasswordText.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email,reEnterPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    sendEmailVerification(email,reEnterPassword);
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(SignUpActivity.this,"error on creating user",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //if sign up success set signup button enable
    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    //if signup failed show toast message
    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }
    //input field validation function
    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=11) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 6 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    /**
     * set all input field empty
     */
    private void set_input_field_empty(){
        _nameText.setText("");
        _emailText.setText("");
        _mobileText.setText("");
        _passwordText.setText("");
        _reEnterPasswordText.setText("");
    }

}
