package com.nostalgia.controller.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.nostalgia.Nostalgia;
import com.nostalgia.controller.capturemoment.MainCaptureActivity;
import com.nostalgia.persistence.repo.UserRepository;
import com.vuescape.nostalgia.R;


/**
 * Created by alex on 11/3/15.
 */
public class BlockingLoginActivity extends AppCompatActivity implements LoginFragment.OnLoginConfirmListener {

    public static final String TAG = "BlockingLoginActivity";

    protected FragmentManager mainFragmentManager;

    private LoginFragment loginFragment = new LoginFragment();
    
    private UserRepository userRepo;

    /*
     * Side drawer setup
     */
    private Nostalgia app;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blocking_login_activity);

        this.app = ((Nostalgia) getApplication());
        this.userRepo = app.getUserRepo();
        mainFragmentManager = getSupportFragmentManager();

        loginFragment = new LoginFragment();
        loginFragment.setOnConfirmListener(this);

        //initially, mStart with the choice fragment
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.add(R.id.blocking_login_root, loginFragment);
        fragTransaction.commit();
    }

    public void backClicked(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void cancel() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private void goToCentralActivity(){
        Intent mainCaptureActivity = new Intent(this, MainCaptureActivity.class);
        startActivity(mainCaptureActivity);

        //Finish this activity so user can't return to it!
        finish();
    }

    @Override
    public void onLogout() {

    }

    @Override
    public void onLoginSuccess(String sessionToken, String region) {

        if(region == null || region.length() < 2){
            region = "us_east";
        }

        Intent updated = new Intent("com.nostalgia.update");
        updated.putExtra("sessionToken", sessionToken);
        updated.putExtra("region", region);
        sendBroadcast(updated);
        goToCentralActivity();
    }


    @Override
    public void onStop(){
        super.onStop();

    }
}
