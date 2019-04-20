package com.example.blogapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    Uri pickedImgUrl;

    private EditText userEmail;
    private EditText userPassword;
    private EditText userPassword2;
    private EditText userName;
    private ProgressBar loadingProgress;
    private Button regBtn;

private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    //identify views
userEmail = findViewById(R.id.regMail);
userPassword=findViewById(R.id.regPassword);
userPassword2=findViewById((R.id.regPassword2));
userName = findViewById(R.id.regName);
loadingProgress=findViewById(R.id.progressBar);
regBtn=findViewById(R.id.regBtn);

mAuth=FirebaseAuth.getInstance();



regBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
       regBtn.setVisibility(View.INVISIBLE);
       loadingProgress.setVisibility(View.VISIBLE);
       final String email = userEmail.getText().toString();
       final String password = userPassword.getText().toString();
       final String password2= userPassword2.getText().toString();
       final String name=userName.getText().toString();

    if(email.isEmpty()|| name.isEmpty() || password.isEmpty() || password2.isEmpty() || !password.equals(password2))
    {

      //A;ll fields mult be filled, we need to display error mesage
        showMessage("Please verify all fields !");
        regBtn.setVisibility(View.VISIBLE);
        loadingProgress.setVisibility(View.INVISIBLE);
    }
else{
    //everything is fine and data can be sent to Firebase

        CreateUserAccount(email,name,password);
    }


    }
});

loadingProgress.setVisibility(View.INVISIBLE);


        ImgUserPhoto = findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=22){
                    checkAndRequestForPermission();
                }
            else {openGallery();}
            }
        });

    }

    private void CreateUserAccount(String email, final String name, String password) {

        //creates UserAccount with specific email and pass
mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if(task.isSuccessful()){
            //UserAccount created succesfully
            showMessage("Account Created!");
            //Update profile picture and name of the account created
            updateUserInfo(name,pickedImgUrl,mAuth.getCurrentUser());
        }
        else{
           //UserAccount creation failed
            showMessage("Account failed to be created!" + task.getException().getMessage());
            regBtn.setVisibility(View.VISIBLE);
        loadingProgress.setVisibility(View.INVISIBLE);
        }
    }
});
    }

//User image and name update
    private void updateUserInfo(final String name, Uri pickedImgUrl, final FirebaseUser currentUser) {

       //UserAccount photo upload and URL get
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUrl.getLastPathSegment());
imageFilePath.putFile(pickedImgUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

      //The iamge was suuccessfully uploaded

       imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
           @Override
           public void onSuccess(Uri uri) {
               //URL containing useer's image can be generated


               UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(uri).build();

               currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           //UserInformation was updated succesfully
                           showMessage("Register Complete");
                           updateUserInterface();
                       }
                   }
               });
           }
       });



    }
});

    }

    private void updateUserInterface() {


Intent homeActivity = new Intent(getApplicationContext(),Home.class);
startActivity(homeActivity);
finish();

    }


    //simple error showing in case of failed register
    private void showMessage(String message) {

    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
       //TODO: open gallery external intent and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType(("image/*"));
        startActivityForResult(galleryIntent,REQUESTCODE);
    }

    private void checkAndRequestForPermission() {
      if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
         if(ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
         {
             Toast.makeText(RegisterActivity.this,"Accept required permission",Toast.LENGTH_SHORT).show();
         }
         else {}ActivityCompat.requestPermissions(RegisterActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);

      }
      else openGallery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null)
        {
         //the user has successfullypicked an image from his gallery
        //image needs to be saved by its reference to an URL variable
        pickedImgUrl=data.getData();
        ImgUserPhoto.setImageURI(pickedImgUrl);


        }
    }
}
