package com.gosilama.journal.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.gosilama.journal.R;

public class SignInActivity extends AppCompatActivity {

    private int EXTRA_GOOGLE_SIGN_IN = 1;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    private EditText userEmail;
    private EditText userPassword;

    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerConfirmPassword;

    private String email;
    private String password;

    private Button signInButton;
    private TextView signUpTrigger;
    private Button googleSignInButton;

    private Button registerButton;

    private AlertDialog registrationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        userEmail = findViewById(R.id.edit_text_email);
        userPassword = findViewById(R.id.edit_text_password);

        signInButton = findViewById(R.id.button_sign_in);
        signUpTrigger = findViewById(R.id.text_view_sign_up);
        googleSignInButton = findViewById(R.id.button_google_sign_in);

        signInUser();
        signUpUser();
        signInWithGoogle();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            goToJournalEntryList();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EXTRA_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showAuthSnackBar();
                e.printStackTrace();
            }
        }
    }

    public void signInUser() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!TextUtils.isEmpty(userEmail.getText())
                        && !TextUtils.isEmpty(userPassword.getText())) {

                    email = userEmail.getText().toString();
                    password = userPassword.getText().toString();

                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignInActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),
                                                "Welcome",
                                                Toast.LENGTH_LONG).show();

                                        goToJournalEntryList();
                                    } else {
                                        showAuthSnackBar();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.fill_all_fields,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void signUpUser() {
        signUpTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRegistrationDialog();
                registerUser();
            }
        });
    }

    public void registerUser() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(registerEmail.getText())
                        && !TextUtils.isEmpty(registerPassword.getText())
                        && registerConfirmPassword.getText().toString().equals(registerPassword.getText().toString())) {

                    email = registerEmail.getText().toString();
                    password = registerPassword.getText().toString();

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignInActivity.this,
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(),
                                                        R.string.registration_successful,
                                                        Toast.LENGTH_LONG).show();

                                                registrationDialog.cancel();
                                                goToJournalEntryList();
                                            } else {
                                                showAuthSnackBar();
                                            }
                                        }
                                    });

                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.fill_all_fields,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void signInWithGoogle() {
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent googleSignInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(googleSignInIntent, EXTRA_GOOGLE_SIGN_IN);
            }
        });
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), R.string.google_sign_in_success, Toast.LENGTH_SHORT).show();
                            goToJournalEntryList();
                        } else {
                            showAuthSnackBar();
                        }
                    }
                });
    }

    public void createRegistrationDialog() {
        View view = getLayoutInflater().inflate(R.layout.registration_popup, null);

        registerEmail = view.findViewById(R.id.edit_text_register_email);
        registerPassword = view.findViewById(R.id.edit_text_register_password);
        registerConfirmPassword = view.findViewById(R.id.edit_text_confirm_password);
        registerButton = view.findViewById(R.id.button_register);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this).setView(view);
        registrationDialog = alertDialogBuilder.create();
        registrationDialog.show();
    }

    public void goToJournalEntryList() {
        Intent intent = new Intent(getApplicationContext(), JournalListActivity.class);
        startActivity(intent);
    }

    public void showAuthSnackBar() {
        Snackbar.make(findViewById(R.id.sign_in_layout),
                R.string.failed_authentication, Snackbar.LENGTH_SHORT).show();
    }
}
