package com.nostalgia.controller.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.LoginRegisterThread;
import com.vuescape.nostalgia.R;

import org.geojson.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 11/6/15.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private UserRepository userRepo;
    private Button registerButton;
    private Button loginButton;
    private Button showRegisterButton;
    private Button showLoginButton;
    private ImageButton backToMethodsButton;
    private EditText emailInput;
    private EditText passwordInput;
    private Nostalgia app;

    //LOGIN_METHODS | LOGIN | REGISTER
    private String currentFrame = "METHODS";

    private CallbackManager fbCallbackManager;
    private LoginButton fbLoginButton;

    private OnLoginConfirmListener nostalgiaCallbacks;

    public interface OnLoginConfirmListener{
        void onLoginSuccess(String sessionToken, String region);
        void onLogout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
//        if(b != null){
//            this.focusedPlayerId = b.getString("playerId");
//
//        }
        app = (Nostalgia) getActivity().getApplication();
        userRepo = app.getUserRepo();
        FacebookSdk.sdkInitialize(app);
    }
    private View mView;

    public void setOnConfirmListener(OnLoginConfirmListener listener){
        nostalgiaCallbacks = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        mView = inflater.inflate(R.layout.fragment_login, container, false);

        loginButton = (Button) mView.findViewById(R.id.nostalgia_login_button);
        registerButton = (Button) mView.findViewById(R.id.nostalgia_register_button);
        final User loggedIn = userRepo.getLoggedInUser();

        //usernameInput = (EditText) mView.findViewById(R.id.login_uname);
        passwordInput = (EditText) mView.findViewById(R.id.password_input);
        emailInput = (EditText) mView.findViewById(R.id.email_input);

        //openId = (ImageButton) mView.findViewById(R.id.openidbutton);
        showRegisterButton = (Button) mView.findViewById(R.id.nostalgia_show_register);
        showLoginButton = (Button) mView.findViewById(R.id.nostalgia_show_login);
        registerButton = (Button) mView.findViewById(R.id.nostalgia_register_button);
        loginButton = (Button) mView.findViewById(R.id.nostalgia_login_button);
        backToMethodsButton = (ImageButton) mView.findViewById(R.id.login_back_button);
        showLoginButton.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                    showLoginOptions();
                                               }
                                           });

        showRegisterButton.setOnClickListener(new View.OnClickListener(){
                                                    @Override
                                                    public void onClick(View v) {
                                                        showRegisterOptions();
                                                    }
                                                });

        loginButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                doAppLogin();

            }
        });

       registerButton.setOnClickListener(new Button.OnClickListener() {
           public void onClick(View v) {

               doAppRegister();

           }
       });

        backToMethodsButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                backToMethods();

            }
        });



        setupFacebook();

        return mView;
    }

    private void showRegisterOptions(){
        Button registerOptions = (Button) mView.findViewById(R.id.nostalgia_register_button);
        registerOptions.setVisibility(View.VISIBLE);
        hideMethodChooser();
        showEditOptions();
        currentFrame="REGISTER";
    }

    private void showLoginOptions(){
        Button loginOptions = (Button) mView.findViewById(R.id.nostalgia_login_button);
        loginOptions.setVisibility(View.VISIBLE);
        hideMethodChooser();
        showEditOptions();
        currentFrame="LOGIN";
    }
    private void showEditOptions(){
        LinearLayout editOptions = (LinearLayout) mView.findViewById(R.id.enter_signup_info);
        editOptions.setVisibility(View.VISIBLE);
    }
    private void hideEditOptions(){
        LinearLayout editOptions = (LinearLayout) mView.findViewById(R.id.enter_signup_info);
        editOptions.setVisibility(View.GONE);
    }

    private void hideMethodChooser(){
        LinearLayout methodChooser = (LinearLayout) mView.findViewById(R.id.login_methods_container);
        methodChooser.setVisibility(View.GONE);
    }

    private void showMethodChooser(){
        LinearLayout methodChooser = (LinearLayout) mView.findViewById(R.id.login_methods_container);
        methodChooser.setVisibility(View.VISIBLE);
    }

    private void backToMethods(){
        if(currentFrame.equals("LOGIN")) {
            Button loginOptions = (Button) mView.findViewById(R.id.nostalgia_login_button);
            loginOptions.setVisibility(View.GONE);
        } else if(currentFrame.equals("REGISTER")){
            Button registerOptions = (Button) mView.findViewById(R.id.nostalgia_register_button);
            registerOptions.setVisibility(View.GONE);
        }
        hideEditOptions();
        showMethodChooser();
        currentFrame="METHODS";
    }

    private void doAppLogin() {
        String email = emailInput.getText().toString();
        String userPassword = passwordInput.getText().toString();

        if(!email.contains("@")){
            emailInput.setBackgroundColor(getResources().getColor(R.color.md_red_800));
            return;
        }
        String name = email.substring(0, email.indexOf('@'));

        Point here = null;

        if(app.getLocation() != null ) {
            here = new Point(app.getLocation().getLongitude(), app.getLocation().getLatitude());
        }
        LoginRegisterThread loginThread = new LoginRegisterThread(app, email, name, userPassword, "app", false,here);
        loginThread.start();

        try {
            loginThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String sessToken = loginThread.getLoginResponse().getSessionTok();
            String region = loginThread.getLoginResponse().getRegion();
            userRepo.save(loginThread.getLoginResponse().getUser());
            if (nostalgiaCallbacks != null) {
                nostalgiaCallbacks.onLoginSuccess(sessToken, region);
            }
        } catch (Exception e){
            Log.e(TAG, "error with returned credentials", e);
            Toast.makeText(getActivity(), "No session token.", Toast.LENGTH_LONG).show();
        }
    }

    private void doAppRegister() {
        String userPassword = passwordInput.getText().toString();
        String emailText = emailInput.getText().toString();

        if(!emailText.contains("@")){
            emailInput.setBackgroundColor(getResources().getColor(R.color.md_red_800));
            return;
        }

        Point here = null;

        if(app.getLocation() != null ) {
            here = new Point(app.getLocation().getLongitude(), app.getLocation().getLatitude());
        }
        String name = emailText.substring(0, emailText.indexOf('@'));
        LoginRegisterThread register = new LoginRegisterThread(app, emailText, name, userPassword, "app", true, here);
        register.start();
        try {
            register.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String sessToken;
        String region;

        try {
            sessToken = register.getLoginResponse().getSessionTok();
            region = register.getLoginResponse().getRegion();
            if(nostalgiaCallbacks != null && sessToken != null){
                nostalgiaCallbacks.onLoginSuccess(sessToken, region);
            }
        } catch (NullPointerException e) {
            //getLoginResponse might be null;
            e.printStackTrace();
            Toast.makeText(this.getActivity(), "Can't connect to user database.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean doFBLogin(AccessToken token){
        String fb = LoginRegisterThread.LoginTypes.facebook.toString();

        LoginRegisterThread login  = new LoginRegisterThread(app, token.getToken(), fb, false, new Point(app.getLocation().getLongitude(), app.getLocation().getLatitude()));
        login.start();

        try {
            login.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String sessToken = login.getLoginResponse().getSessionTok();
            String region = login.getLoginResponse().getRegion();

            if (nostalgiaCallbacks != null) {
                nostalgiaCallbacks.onLoginSuccess(sessToken, region);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("LoginFragment", "Login didn't work, trying register.");

            doFBRegister(token);
        }

        return true;
    }

    private boolean doFBRegister(AccessToken token){
        String fb = LoginRegisterThread.LoginTypes.facebook.toString();
        LoginRegisterThread register  = new LoginRegisterThread(app, token.getToken(), fb, true, new Point(app.getLocation().getLongitude(), app.getLocation().getLatitude()));
        register.start();

        try {
            register.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String sessToken = register.getLoginResponse().getSessionTok();
        String region = register.getLoginResponse().getRegion();
        if(nostalgiaCallbacks != null){
            nostalgiaCallbacks.onLoginSuccess(sessToken, region);
        }
        return true;
    }

    private void setupFacebook() {
        fbLoginButton = (LoginButton) mView.findViewById(R.id.fb_login_button);
        //TODO: FacebookSDK Initialized in OnCreate method, delete below call when OnCreate call is confirmed to be working.
        //FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        fbCallbackManager = CallbackManager.Factory.create();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(null == accessToken){
            Log.d("LoginFragment", "FB user is not already Logged in");
        }

        List<String> permissions = new ArrayList<String>();
        permissions.add("public_profile");
        permissions.add("email");
        permissions.add("user_friends");

        fbLoginButton.setReadPermissions(permissions);
        // If using in a fragment
        fbLoginButton.setFragment(this);
        // Other app specific specialization

        fbLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken token = loginResult.getAccessToken();
                Log.d("LoginFragment","User connected to facebook.");
                doFBLogin(token);
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
