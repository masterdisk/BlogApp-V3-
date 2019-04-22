package com.example.blogapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blogapp.Fragments.HomeFragment;
import com.example.blogapp.Fragments.ProfileFragment;
import com.example.blogapp.Fragments.SettingsFragment;
import com.example.blogapp.Models.Post;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseUser currentUser;
FirebaseAuth mAuth;
Dialog popAddPost;
ImageView popupUserImage;
ImageView popupPostImage;
ImageView popupAddBtn;
TextView popupTitle;
TextView popupDescription;
ProgressBar popupClickProgress;
    static int PReqCode = 2;
    static int REQUESTCODE = 2;
    private Uri pickedImgUrl=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initializer

        mAuth = FirebaseAuth.getInstance();
currentUser=mAuth.getCurrentUser();

//initial popup
        iniPopup();
setupPopupImageClick();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               popAddPost.show();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        updateNavHeader();

        //Set the home fragment as home
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();
    }

    private void setupPopupImageClick() {

        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //image clicked then gallery is opened
checkAndRequestForPermission();
            }
        });

    }


    private void checkAndRequestForPermission() {
        if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Toast.makeText(Home.this,"Accept required permission",Toast.LENGTH_SHORT).show();
            }
            else {}ActivityCompat.requestPermissions(Home.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqCode);

        }
        //everything ius good and gallery can be opened (permission is granted)
        else openGallery();
    }

    private void openGallery() {
        //TODO: open gallery external intent and wait for user to pick an image

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType(("image/*"));
        startActivityForResult(galleryIntent,REQUESTCODE);
    }




    private void iniPopup() {
popAddPost = new Dialog(this)
 ;      popAddPost.setContentView(R.layout.popup_add_post);
       popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
       popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
       popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

       // initial popup widgets
popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
popupPostImage = popAddPost.findViewById(R.id.popup_img);
popupTitle = popAddPost.findViewById(R.id.popup_title);
popupDescription = popAddPost.findViewById(R.id.popup_description);
popupAddBtn = popAddPost.findViewById(R.id.popup_add);
popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

//Load Current User Profile picture

        Glide.with(Home.this).load(currentUser.getPhotoUrl()).into(popupUserImage);



// Posts with click Listener


        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);
//description and title fields config

                if (!popupTitle.getText().toString().isEmpty()
                && !popupDescription.getText().toString().isEmpty()
                && pickedImgUrl !=null ) {
                    //everything is fine
                    // TODO Create OBject and connect to Firebase Data


                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(("blog_images"));
                    final StorageReference imageFilePath = storageReference.child(pickedImgUrl.getLastPathSegment());

                    imageFilePath.putFile(pickedImgUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownlaodLink = uri.toString();
                                    // create post Object
                                    Post post = new Post(popupTitle.getText().toString(),
                                            popupDescription.getText().toString(),
                                            imageDownlaodLink,
                                            currentUser.getUid(),
                                            currentUser.getPhotoUrl().toString());

                                    // Add post to firebase database

                                    addPost(post);



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //something wrong happened
                                    showMessage(e.getMessage());
                                popupClickProgress.setVisibility(View.INVISIBLE);
                                popupAddBtn.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });


                }
                else {
                    showMessage("Verify all fields!");
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void addPost(Post post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();


        //get posts unique ID and update post keys


        String key = myRef.getKey();
        post.setPostKey(key);

        //add post data to firebase Database

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Post has been added!");
            popupClickProgress.setVisibility(View.INVISIBLE);
            popupAddBtn.setVisibility(View.VISIBLE);
           popAddPost.dismiss();

            }
        });


    }

    private void showMessage(String message) {
    Toast.makeText(Home.this,message,Toast.LENGTH_LONG).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null)
        {
            //the user has successfullypicked an image from his gallery
            //image needs to be saved by its reference to an URL variable
            pickedImgUrl=data.getData();
            popupPostImage.setImageURI(pickedImgUrl);



        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {


            getSupportActionBar().setTitle("Home");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();


        } else if (id == R.id.nav_profile) {


            getSupportActionBar().setTitle("Profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment()).commit();

        } else if (id == R.id.nav_settings) {


            getSupportActionBar().setTitle("Settings");
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new SettingsFragment()).commit();

        }
        else if(id == R.id.nav_signout){

            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(this,LoginActivity.class);
            startActivity(loginActivity);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavHeader()
    {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);


        navUserMail.setText(currentUser.getEmail());
        navUserName.setText(currentUser.getDisplayName());


        //Glider for loading user profile image

        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);
    }

}
