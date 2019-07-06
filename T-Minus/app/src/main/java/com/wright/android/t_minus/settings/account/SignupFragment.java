package com.wright.android.t_minus.settings.account;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.wright.android.t_minus.universal_utils.TextFieldUtils;
import com.wright.android.t_minus.R;

import java.util.Objects;

public class SignupFragment extends Fragment {
    //TODO: Add error handling for wrong information
    private LoginListener mListener;
    private FirebaseAuth mAuth;
    private EditText mNameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private EditText mReTypePassword;

    public SignupFragment() {
        // Required empty public constructor
    }

    public static SignupFragment newInstance() {
        SignupFragment fragment = new SignupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNameView = view.findViewById(R.id.signup_name);
        mEmailView = view.findViewById(R.id.signup_email);
        mPasswordView = view.findViewById(R.id.signup_password);
        mReTypePassword = view.findViewById(R.id.signup_password_retype);
        mPasswordView.setOnEditorActionListener(passwordEditorAction);
        mReTypePassword.setOnEditorActionListener(passwordEditorAction);

        view.findViewById(R.id.signup_register).setOnClickListener((v)-> attemptSignIn());
        view.findViewById(R.id.signup_email_sign_in_button).setOnClickListener((v)->mListener.OperationSwitch(this));
        mProgressView = view.getRootView().findViewById(R.id.blankProgressBar);
    }

    private final TextView.OnEditorActionListener passwordEditorAction = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int id, KeyEvent event) {
            if(mReTypePassword.getText() == mPasswordView.getText()) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSignIn();
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginListener) {
            mListener = (LoginListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentUploadListener");
        }
    }

    private void attemptSignIn() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mReTypePassword.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = mReTypePassword.getText().toString();
        String name = mNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(rePassword)) {
            mReTypePassword.setError(getString(R.string.error_invalid_password));
            focusView = mReTypePassword;
            cancel = true;
        }else if(!rePassword.equals(password)){
            mReTypePassword.setError("The passwords do not match");
            focusView = mReTypePassword;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) && TextFieldUtils.isPasswordInvalid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!TextFieldUtils.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid Name
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            setmProgressView(true);
            loginUser(name, email, password);
        }
    }

    private void setmProgressView(Boolean state){
        mProgressView.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    private void loginUser(String name, String email, String password){
        if(getActivity()==null){
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), (@NonNull Task<AuthResult> task)-> {
            setmProgressView(false);
            if (task.isSuccessful()) {
                mListener.SignUpButton(Objects.requireNonNull(mAuth.getCurrentUser()), name);
            } else {
                try
                {
                    throw Objects.requireNonNull(task.getException());
                }
                catch (FirebaseAuthUserCollisionException existEmail)
                {
                    if(getView() == null){
                        return;
                    }
                    Snackbar.make(getView(), "Email is already in use please sign in", Snackbar.LENGTH_SHORT).
                            setAction("Sign In", (v) -> ToSignIn()).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void ToSignIn(){
        mListener.OperationSwitch(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
