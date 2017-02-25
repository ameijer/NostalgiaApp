package com.nostalgia.menu.settings;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.UserAttributeUpdaterThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by alex on 12/25/15.
 */
public class UserInfoFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "UserInfoFragment";
    private RelativeLayout usernameHolder;
    private RelativeLayout userEmailHolder;
    private RelativeLayout userPasswordHolder;
    private RelativeLayout userLocaleHolder;
    private LinearLayout iconHolder;

    private CircleImageView userIcon;
//    private Button changeImageButton;

    private UserRepository userRepo;

    //username
    private TextView username_title;
    private TextView username;

    //email
    private TextView email_title;
    private TextView email;

    //password
    private TextView password_title;
    private TextView password;

    //locale
    private TextView locale_title;
    private TextView locale;

    private Context context;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        userRepo = ((Nostalgia)activity.getApplication()).getUserRepo();
        context = getContext();

    }

    private void updateEmailSection(User hasNewEmail){
        String emailString = hasNewEmail.getEmail();

        email_title.setTextSize(14);

        email.setTextSize(12);
        email.setText(emailString);

        return;
    }

    private void updateUsernameSection(User hasNewUsername){
        String usernameString = hasNewUsername.getUsername();

        username_title.setTextSize(14);

        username.setTextSize(12);
        username.setText(usernameString);

        return;
    }

    private void updatePasswordSection(User hasPassword){
        long paswwordLastReset = 0L;



        password_title.setTextSize(14);

        password.setTextSize(12);

        if(paswwordLastReset > 100) {
            Date friendly = new Date(paswwordLastReset);
            password.setText("Password last set on: " + friendly);
        } else {
            password.setText("Change Password");
        }
        return;
    }

    private void updateLocaleSection(User hasLocale){
        String localeString = hasLocale.getHomeRegion();

        locale_title.setTextSize(14);

        locale.setTextSize(12);
        locale.setText(localeString);

        return;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.user_settings_frag, container, false);

        //bind views
        usernameHolder = (RelativeLayout) myView.findViewById(R.id.username_settings_holder);
        usernameHolder.setClickable(true);
        usernameHolder.setOnClickListener(this);

        username_title = (TextView) myView.findViewById(R.id.name_header);
        username = (TextView) myView.findViewById(R.id.settings_username);

        userEmailHolder = (RelativeLayout) myView.findViewById(R.id.username_email_holder);
        userEmailHolder.setClickable(true);
        userEmailHolder.setOnClickListener(this);

        email_title = (TextView) myView.findViewById(R.id.email_header);
        email = (TextView) myView.findViewById(R.id.settings_email);

        userPasswordHolder = (RelativeLayout) myView.findViewById(R.id.username_password_holder);
        userPasswordHolder.setClickable(true);
        userPasswordHolder.setOnClickListener(this);

        password_title = (TextView) myView.findViewById(R.id.password_header);
        password = (TextView) myView.findViewById(R.id.settings_password);


        userLocaleHolder = (RelativeLayout) myView.findViewById(R.id.username_locale_holder);
        userLocaleHolder.setClickable(true);
        userLocaleHolder.setOnClickListener(this);

        locale_title = (TextView) myView.findViewById(R.id.locale_header);
        locale = (TextView) myView.findViewById(R.id.settings_locale);


        userIcon = (CircleImageView) myView.findViewById(R.id.profile_image_settings);
        iconHolder = (LinearLayout) myView.findViewById(R.id.user_icon_holder);
        iconHolder.setClickable(true);
        iconHolder.setOnClickListener(this);

        refreshSettings();


        return myView;
    }

    private void refreshSettings() {
        User current = userRepo.getLoggedInUser();

        updateEmailSection(current);
        updateLocaleSection(current);
        updatePasswordSection(current);
        updateUsernameSection(current);
        updateProfilePic(current);

        return;
    }

    private void updateProfilePic(User current) {
        String encodedIcon = current.getIcon();

        if(encodedIcon == null){
            return;
        }


        byte[] pngRaw = Base64.decode(encodedIcon, Base64.DEFAULT);


        Bitmap bmp = BitmapFactory.decodeByteArray(pngRaw, 0, pngRaw.length);
        userIcon.setImageBitmap(bmp);
    }

    @Override
    public void onClick(View v) {
        User current = userRepo.getLoggedInUser();
        switch(v.getId()) {
            case R.id.username_settings_holder:
                startUsernameChangeDialog(current.getUsername());
                break;
            case R.id.username_email_holder:
                startEmailChangeDialog(current.getEmail());
                break;
            case R.id.username_password_holder:
                startPasswordChangeDialog(0L);
                break;
            case R.id.username_locale_holder:
                startLocaleChangeDialog(current.getHomeRegion());
                break;
            case R.id.user_icon_holder:
                startProfilePicChangeDialog(current.getIcon());
                break;
            default:
                Log.e(TAG, "Unhandled view onclick: " + v.getTag());
                break;
        }
    }

    ImageHolder holder;

    private void startProfilePicChangeDialog(String currentPic) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_iconchange, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final CircleImageView iconHolder = (CircleImageView) promptsView.findViewById(R.id.prompt_image_display);
        holder = new ImageHolder(iconHolder);
        holder.onImageGenerated(currentPic);


        final Spinner userInput = (Spinner) promptsView
                .findViewById(R.id.icon_src_spinner);

        CustomOnItemSelectedListener selector = new CustomOnItemSelectedListener();


        selector.setImageGeneratedListener(holder);
        userInput.setOnItemSelectedListener(selector);



        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {

                                changeIcon(holder.getLatestEncodedImage());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public void onFilePicked(String filePath) {
        if(holder == null){
            Log.e(TAG, "null image holder! doing nothing..");
            return;
        }

        File picked = new File(filePath);
        //process file into image
        String encodedPng = encodeFileAsB64Png(picked);

        holder.onImageGenerated(encodedPng);
    }

    private String encodeFileAsB64Png(File picked) {
        String encoded = null;
        //file to bitmap
        Bitmap myBitmap = BitmapFactory.decodeFile(picked.getAbsolutePath());

        //resize bitmap
        Bitmap myScaledBitmap = Bitmap.createScaledBitmap(myBitmap, 512, 512, false);

        //bitmap to base64 png

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        myScaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private class ImageHolder implements UserInfoFragment.ImageGeneratedListener {
        private final CircleImageView view;
        private String latestEncodedImage= null;

        public ImageHolder(CircleImageView imgHolder){
            this.view = imgHolder;
        }
        @Override
        public void onImageGenerated(String newEncodedImage) {
            latestEncodedImage = newEncodedImage;
            byte[] pngRaw = Base64.decode(newEncodedImage, Base64.DEFAULT);


            Bitmap bmp = BitmapFactory.decodeByteArray(pngRaw, 0, pngRaw.length);
            view.setImageBitmap(bmp);
        }

        public String getLatestEncodedImage(){
            return latestEncodedImage;
        }
    }

    private void changeIcon(String latestEncodedImage) {
        //save to user
        User current = userRepo.getLoggedInUser();
        current.setIcon(latestEncodedImage);
        try {
            Map<String, String> changed = new HashMap<>();
            changed.put("icon", latestEncodedImage);
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.ICON, changed);
            updatr.start();


            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user with updated name");
        }        //update icon
        updateProfilePic(current);
    }





    private interface ImageGeneratedListener{
        void onImageGenerated(String newEncodedImage);
    }

    public static final int FILE_PICKER_CODE = 1999;
    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void setImageGeneratedListener(ImageGeneratedListener callback){
            this.callback = callback;
        }

        private ImageGeneratedListener callback;

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            Log.i(TAG, "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString());

            String selected = parent.getItemAtPosition(pos).toString();

            Log.i(TAG, "Icon source chosen: " + selected);
            switch(selected){
                case("files"):
                    Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                    startActivityForResult(intent, FILE_PICKER_CODE);

                    break;
                default:
                    Toast.makeText(getActivity(), "not yet implemented",
                            Toast.LENGTH_LONG).show();
                    break;

            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UserInfoFragment.FILE_PICKER_CODE && resultCode == getActivity().RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            onFilePicked(filePath);
        }
    }

    private void startLocaleChangeDialog(String currentLocale) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_localechange, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final Spinner userInput = (Spinner) promptsView
                .findViewById(R.id.locale_prompt_spinner);

        int current = getIndex(userInput, currentLocale);

        userInput.setSelection(current);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {

                                changeLocale(userInput.getSelectedItem().toString());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void changeLocale(String s) {

        //save to user in db
        User current = userRepo.getLoggedInUser();
        current.setHomeRegion(s);
        try {
            Map<String, String> changed = new HashMap<>();
            changed.put("home", s);
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.HOME, changed);
            updatr.start();
            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user with updated name");
        }

        //update display

        updateLocaleSection(current);
    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }

    private void startPasswordChangeDialog(long passwordChangeDate) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_passwordchange, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.password_prompt_input);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                changePassword(userInput.getText());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void changePassword(Editable text) {
        Toast.makeText(getActivity(), "not yet implemented, password not changed",
                Toast.LENGTH_LONG).show();
    }

    private void startEmailChangeDialog(String emailText) {
// get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_emailchange, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.email_prompt_input);
        userInput.setText(emailText);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                updateEmail(userInput.getText());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void startUsernameChangeDialog(String nameText) {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_usernamechange, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.username_prompt_input);

        userInput.setText(nameText);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                updateUserName(userInput.getText());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    private void updateEmail(Editable text) {
        final String newEmail = text.toString();
        //save to user in db
        User current = userRepo.getLoggedInUser();
        current.setEmail(newEmail);
        try {
            Map<String, String> changed = new HashMap<>();
            changed.put("email", newEmail);
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.EMAIL, changed);
            updatr.start();

            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user with updated name");
        }

        //update display

        updateEmailSection(current);

    }
    private void updateUserName(Editable text) {
        final String newName = text.toString();
        //save to user in db
        User current = userRepo.getLoggedInUser();
        current.setUsername(newName);
        try {

            Map<String, String> changed = new HashMap<>();
            changed.put("name", newName);
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(current.get_id(), UserAttributeUpdaterThread.Attribute.NAME, changed);
            updatr.start();

            userRepo.save(current);
        } catch (Exception e) {
            Log.e(TAG, "error saving user with updated name");
        }

        //update display

        updateUsernameSection(current);

    }
}
