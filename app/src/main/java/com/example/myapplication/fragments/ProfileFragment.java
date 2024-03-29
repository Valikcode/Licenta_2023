package com.example.myapplication.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.ModelChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {

    // Firebase instance
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;

    // Storage
    StorageReference storageReference;
    // Path where images of puser profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    // Views from xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    CheckBox gamingCheckbox, educationCheckbox, gymCheckbox;
    RecyclerView meetupRv;

    // Progress dialog
    ProgressDialog pd;

    // Permission constants
    private  static final int CAMERA_REQUEST_CODE = 100;
    private  static final int STORAGE_REQUEST_CODE = 200;
    private  static final int IMAGE_PICK_GALLERY_CODE = 300;
    private  static final int IMAGE_PICK_CAMERA_CODE = 400;
    // Arrays of permission to be requested
    String cameraPermissions[];
    String storagePermissions[];

    // Uri of picked image
    Uri image_uri;

    // For Checking profile or cover photo
    String profileOrCover;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        // Init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference(); // firebase storage reference

        // Init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Init Views
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        fab = view.findViewById(R.id.fab);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        gamingCheckbox = view.findViewById(R.id.gaming_checkbox);
        educationCheckbox = view.findViewById(R.id.education_checkbox);
        gymCheckbox = view.findViewById(R.id.gym_checkbox);
        meetupRv = view.findViewById(R.id.meetupsRv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        meetupRv.setLayoutManager(layoutManager);

        // Init progress dialog
        pd = new ProgressDialog(getActivity());

        // We have to get info of currently signed in user. We can get it using user`s email or uid
        // I`m gonna retrive user detail using his email
        // By using orderByChild query we will show the details from a node wose ket named email has
        // value equal to the currently signed in email. It will search all node and where the key
        // matches it will get it`s details
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Ckeck until required data get
                for(DataSnapshot ds: snapshot.getChildren()){

                    // get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    // set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {
                        // if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    }catch (Exception e){
                        // if there is any execption while getting image then set def.
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        // if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    }catch (Exception e){

                        // if there is any execption while getting image then set def.
                        Picasso.get().load(R.drawable.ic_add_image).into(coverIv);
                    }

                    // CheckBoxes
                    // Get the boolean values for the checkboxes from the database
                    Boolean gamingBool = ds.child("gaming").getValue(Boolean.class);
                    Boolean educationBool = ds.child("education").getValue(Boolean.class);
                    Boolean gymBool = ds.child("gym").getValue(Boolean.class);

                    // Update the checkboxes to match the values in the database
                    gamingCheckbox.setChecked(gamingBool);
                    educationCheckbox.setChecked(educationBool);
                    gymCheckbox.setChecked(gymBool);

                    // Add an OnCheckedChangeListener to each checkbox to update the database when the checkbox is checked or unchecked
                    gamingCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                            userRef.child("gaming").setValue(gamingCheckbox.isChecked());
                        }
                    });

                    educationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                            userRef.child("education").setValue(educationCheckbox.isChecked());
                        }
                    });

                    gymCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                            userRef.child("gym").setValue(gymCheckbox.isChecked());
                        }
                    });

                    // Load accepted meetups
                    List<ModelChat> acceptedMeetups = new ArrayList<>();
                    DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats");
                    chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                ModelChat chat = chatSnapshot.getValue(ModelChat.class);
                                if(chat != null && chat.getMeetupDate() != null){
                                    if(chat.getReceiver().equals(user.getUid()) && chat.getMeetupStatus().equals("accepted")){
                                        acceptedMeetups.add(chat);
                                    }
                                }
                            }
                            MeetupAdapter adapter = new MeetupAdapter(acceptedMeetups);
                            meetupRv.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                    // adapter.notifyDataSetChanged();
                    // listView.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // Fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        return view;
    }


    public class MeetupAdapter extends RecyclerView.Adapter<MeetupAdapter.MeetupViewHolder> {

        private List<ModelChat> meetupList;

        public MeetupAdapter(List<ModelChat> meetupList) {
            this.meetupList = meetupList;
        }

        @NonNull
        @Override
        public MeetupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meetup, parent, false);
            return new MeetupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MeetupViewHolder holder, int position) {
            ModelChat meetup = meetupList.get(position);
            // Bind the meetup data to the ViewHolder
            holder.bindMeetup(meetup);
        }

        @Override
        public int getItemCount() {
            return meetupList.size();
        }

        public class MeetupViewHolder extends RecyclerView.ViewHolder {
            private TextView dateTv;
            private TextView messageTv;
            private TextView timeTv;

            public MeetupViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTv = itemView.findViewById(R.id.dateTv);
                messageTv = itemView.findViewById(R.id.messageTv);
                timeTv = itemView.findViewById(R.id.timeTv);
            }

            public void bindMeetup(ModelChat meetup) {
                String date = meetup.getMeetupDate();
                String location = meetup.getMeetupLocation();
                String interest = meetup.getMeetupInterest();

                dateTv.setText(date);
                messageTv.setText(location);
                timeTv.setText(interest);
            }
        }
    }
    private boolean checkStoragePermission() {
        // Check if storage permission is enabled or not
        // Return true if enabled
        // Return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        // Check if permission to read and write external storage is already granted
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permissions are already granted, launch gallery
            pickFromGallery();
        } else {
            // Permissions are not granted, request them
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_REQUEST_CODE);
        }
    }

    private boolean checkCameraPermission(){
        // Check if storage permission is enabled or not
        // return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(getActivity(), "Camera permission is needed to take photos",
                    Toast.LENGTH_LONG).show();
        }
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        // Show dialog containing options
        // 1) Edit Profile Picture
        // 2) Edit Cover Photo
        // 3) Edit Name
        // 4) Edit Phone

        // Options to show in dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};
        // Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose an action:");
        // Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle dialog item clicks
                if(i == 0) {
                    // Edit profile picture clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCover = "image";
                    showImagePicDialog();
                } else if(i == 1) {
                    // Edit cover clicked
                    pd.setMessage("Updating Cover Picture");
                    profileOrCover = "cover";
                    showImagePicDialog();
                } else if(i == 2) {
                    // Edit name clicked
                    pd.setMessage("Updating Name");
                    // Calling method and pass key "name" as parameter to update it`s value in database
                    showNamePhoneUpdateDialog("name");
                } else if(i == 3) {
                    // Edit phone clicked
                    pd.setMessage("Updating Phone Number");
                    showNamePhoneUpdateDialog("phone");
                }

            }
        });
        // Create and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        // Custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);
        // set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        // add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        // add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // input text from editText
                String value = editText.getText().toString().trim();
                // validate if user has enteded something or not
                if(!TextUtils.isEmpty((value))){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // updated, dismiss progress
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // failde, dismiss progress, get and show error message
                            pd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else{
                    Toast.makeText(getActivity(), "Please enter" + key, Toast.LENGTH_SHORT).show();                }
            }
        });

        // add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        // Create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        // show dialog containing options Camera or Gallery to pick the image
        // Options to show in dialog
        String options[] = {"Camera", "Gallery"};
        // Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From:");
        // Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle dialog item clicks
                switch (i){
                    case 0: {
                        // Camera clicked
                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        } else {
                            pickFromCamera();
                        }
                        break;
                    }
                    case 1:{
                        // Gallery clicked
                        if(!checkStoragePermission()){
                            requestStoragePermission();
                        } else {
                            pickFromGallery();
                        }
                        break;
                    }
                }
            }
        });
        // Create and show dialog
        builder.create().show();
    }


@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // Check if the request code matches either CAMERA_REQUEST_CODE or STORAGE_REQUEST_CODE
    if (requestCode == CAMERA_REQUEST_CODE || requestCode == STORAGE_REQUEST_CODE) {
        // Check if all permissions have been granted
        boolean allPermissionsGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (allPermissionsGranted) {
            // Permissions granted, handle accordingly
            if (requestCode == CAMERA_REQUEST_CODE) {
                pickFromCamera();
            } else if (requestCode == STORAGE_REQUEST_CODE) {
                pickFromGallery();
            }
        } else {
            // Permissions denied
            Toast.makeText(getActivity(), "Please enable permissions", Toast.LENGTH_SHORT).show();
        }
    }
}

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method will be called after picking a image from Camera or Gallery
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                // image is picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        // Show progress
        pd.show();

        // Instead of creating separate functions for Porfile Picture and Cover Photo
        // I`m doing both in the same function

        // Path and name of image to be stored in firebase storage
        String filePathAndName = storagePath + "" + profileOrCover + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                //check if image is uploaded or not and url is received
                if(uriTask.isSuccessful()){
                    // image uploaded
                    // Add/Update url is user`s database
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profileOrCover, downloadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // url in database of user is added successfully
                            // dimsiss progress bar
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // error adding url in database of user
                            // dismiss progress bar
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else{
                    // error
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Some error eccured", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickFromCamera() {
        // Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        // Put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private  void checkUserStatus(){
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null){
            // User is signed in => stay here
            // Set email of logged in user
            //textViewProfil.setText(user.getEmail());
        }else {
            // User is not signed in => go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    // To show the menu
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    // Inflate options menu option in fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);

        // Get the search item and show it
        MenuItem searchMenuItem = menu.findItem(R.id.action_profile);
        searchMenuItem.setVisible(false);
    }

    // Handle menu item click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // get item it
        int id = item.getItemId();
        if(id == R.id.action_logut){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}