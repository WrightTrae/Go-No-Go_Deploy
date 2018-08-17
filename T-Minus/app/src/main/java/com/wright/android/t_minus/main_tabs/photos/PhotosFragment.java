package com.wright.android.t_minus.main_tabs.photos;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.paginate.Paginate;
import com.wooplr.spotlight.SpotlightView;
import com.wright.android.t_minus.universal_utils.DialogUtils;
import com.wright.android.t_minus.universal_utils.ImageUtils;
import com.wright.android.t_minus.R;
import com.wright.android.t_minus.objects.ImageObj;
import com.wright.android.t_minus.settings.PreferencesActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PhotosFragment extends Fragment {
//TODO:RE ADD LIKES
    public static final int MY_CAMERA_PERMISSION_CODE = 1231;
    public static final int CAPTURE_DETAIL_REQUEST = 0x012;
    private static final String PHOTO_SPOTLIGHT = "PHOTO_SPOTLIGHT";

    private static final String IMAGE_FOLDER = "images/";
    public static final int CAMERA_REQUEST = 0x1010;
    public static final String PATH_KEY = "path";
    public static final String LIKES_KEY = "likes";
    public static final String REPORTED_KEY = "reported";
    public static final String TIME_STAMP_KEY = "time_stamp";
    public static final String USER_ID_KEY = "user_id";
    public static final String LOCATION_NAME_KEY = "location_name";
    private boolean imagesDownloaded = true;
    FirebaseStorage storage;
    StorageReference storageReference;
    private PhotoAdapter photoAdapter;
    private int urlDownloadDoneCount = 0;
    private String image_path;
    private View fab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View signInLayout;
    private DatabaseReference activeImageDatabaseReference;
    private ArrayList<ImageObj>imageObjArrayList = new ArrayList<>();
    private int photoDownloadOffset;
    private final int offsetRate = 5;
    private boolean initalDownload = true;

    public PhotosFragment() {
        // Required empty public constructor
    }

    public static PhotosFragment newInstance() {
        return new PhotosFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkForSignIn(false)){
            if(!imagesDownloaded) {
                if (!initalDownload){
                    getLikedImages();
                    imagesDownloaded = true;
                }
            }
        }else{
            imagesDownloaded = false;
        }
    }

    public void onTabClick(){
        if(getActivity() == null || getContext() == null){
            return;
        }
        new SpotlightView.Builder(getActivity())
                .introAnimationDuration(400)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(getContext().getColor(R.color.colorAccent))
                .headingTvSize(20)
                .headingTvText("Take A Photo")
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(15)
                .subHeadingTvText("This is used to take a photo of a rocket launch to share with your fellow rocket enthusiasts.")
                .maskColor(Color.parseColor("#dc000000"))
                .target(fab)
                .lineAnimDuration(400)
                .lineAndArcColor(getContext().getColor(R.color.colorAccent))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId(PHOTO_SPOTLIGHT)
                .show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        photoDownloadOffset = -offsetRate;
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    private boolean checkForSignIn(Boolean showToast){
        assert getContext() != null;
        if(getView() == null){
            return false;
        }
        Boolean userSigned = FirebaseAuth.getInstance().getCurrentUser() != null;
        ((CustomStaggeredViewLayout)recyclerView.getLayoutManager()).setScrollEnabled(userSigned);
        swipeRefreshLayout.setEnabled(userSigned);
        photoAdapter.setClickable(userSigned);
        signInLayout.setVisibility(userSigned? View.GONE: View.VISIBLE);
        fab.setVisibility(userSigned? View.VISIBLE: View.GONE);
        if(!userSigned){
            if(showToast){
                Toast.makeText(getContext(), "Sign In Required",Toast.LENGTH_SHORT).show();
            }
            getView().findViewById(R.id.photo_sign_in_button).setOnClickListener((View v)->{
                Intent intent = new Intent(getContext(), PreferencesActivity.class);
                startActivity(intent);
            });
        }
        return userSigned;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutPhoto);
        swipeRefreshLayout.setOnRefreshListener(()->{
            photoAdapter.resetData();
            imageObjArrayList.clear();
            photoDownloadOffset = -offsetRate;
            initalDownload = true;
        });
        recyclerView = view.findViewById(R.id.photoGrid);
        recyclerView.setLayoutManager(new CustomStaggeredViewLayout(2, StaggeredGridLayoutManager.VERTICAL));
        photoAdapter = new PhotoAdapter(getContext());
        recyclerView.setAdapter(photoAdapter);
        signInLayout = view.findViewById(R.id.photos_sign_in_layout);
        fab = view.findViewById(R.id.photo_capture_fab);
        if(checkForSignIn(false)){
            resetPushPath();
            fab.setOnClickListener((View v) -> showDataFromFAB());
        }

        Paginate.Callbacks callbacks = new Paginate.Callbacks() {
            @Override
            public void onLoadMore() {
                // Load next page of data (e.g. network or database)
                photoDownloadOffset += offsetRate;
                getLikedImages();
            }

            @Override
            public boolean isLoading() {
                // Indicate whether new page loading is in progress or not
                return swipeRefreshLayout.isRefreshing();
            }

            @Override
            public boolean hasLoadedAllItems() {
                // Indicate whether all data (pages) are loaded or not
                if(imageObjArrayList.size()==0){
                    return false;
                }
                return urlDownloadDoneCount>=imageObjArrayList.size();
            }
        };
        Paginate.with(recyclerView, callbacks)
                .build();
    }

    private void resetPushPath(){
        if(getActivity()==null){
            return;
        }
        activeImageDatabaseReference = FirebaseDatabase.getInstance().getReference().child("images").push();
        image_path = getActivity().getExternalFilesDir(IMAGE_FOLDER)+"/"+activeImageDatabaseReference.getKey()+".jpg";
    }

    private void showDataFromFAB(){
        if(checkForSignIn(true) && getActivity() != null) {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getOutputUri(getContext(), getImageFile()));
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, CAMERA_REQUEST);
//                Intent intent = new Intent(getContext(), PhotoCaptureDetailActivity.class);
//                startActivityForResult(intent, CAPTURE_DETAIL_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                showDataFromFAB();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void onActivityResult ( int requestCode, int resultCode, Intent data){
        if(getContext()==null||getImageFile()==null){
            return;
        }
        if (requestCode == CAPTURE_DETAIL_REQUEST && resultCode == Activity.RESULT_OK) {
            File imageFile = (File) data.getSerializableExtra(PhotoCaptureDetailActivity.IMAGE_FILE);
            Bitmap bitmap;
            ExifInterface exif;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),
                        ImageUtils.getOutputUri(getContext(), imageFile));
                exif = new ExifInterface(
                        imageFile.getAbsolutePath());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            bitmap = ImageUtils.getRotatedBitmap(bitmap, exif);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bitmap.recycle();
            uploadImage(byteArray,
                    data.getStringExtra(PhotoCaptureDetailActivity.IMAGE_LOCATION));
        }else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
//            Bitmap bitmap;
//            File imageFile = getImageFile();
//            ExifInterface exif;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), getOutputUri());
//                exif = new ExifInterface(
//                        imageFile.getAbsolutePath());
//            } catch (Exception e) {
//                Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT)
//                        .show();
//                return;
//            }

//            bitmap = ImageUtils.getRotatedBitmap(bitmap, exif);
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//            bitmap.recycle();
            Intent intent = new Intent(getContext(), PhotoCaptureDetailActivity.class);
            intent.putExtra(PhotoCaptureDetailActivity.IMAGE_FILE, getImageFile());
            startActivityForResult(intent, CAPTURE_DETAIL_REQUEST);
//            uploadImage(byteArray);
        }

    }

    private File getImageFile(){
        assert getContext() != null;
        File imageFile = new File(image_path);
        boolean created;
        try {
            created = imageFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return imageFile;
    }

    private void downloadImages(ArrayList<String> likedImagesIds){
        if(checkForSignIn(true)) {
            DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference().child("images");
            imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {//TODO: Only run this on itital downlaod/refreshing then rely on item array for url downloads\
                    int downloadedImages = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        downloadedImages++;
                        String id = snapshot.getKey();
                        String path = (String) snapshot.child(PATH_KEY).getValue();
                        long likes = (long) snapshot.child(LIKES_KEY).getValue();
                        boolean reported = (boolean) snapshot.child(REPORTED_KEY).getValue();
                        String time = (String) snapshot.child(TIME_STAMP_KEY).getValue();
                        String userId = (String) snapshot.child(USER_ID_KEY).getValue();
                        String locationName = (String) snapshot.child(LOCATION_NAME_KEY).getValue();
                        boolean liked = likedImagesIds.stream().anyMatch((listId -> listId.equals(id)));
                        if (path == null || path.trim().equals("") || reported) {
                            continue;
                        }
                        ImageObj imageObj = new ImageObj(
                                id,
                                path,
                                likes,
                                false,
                                time == null ? "" : time,
                                userId == null ? "" : userId,
                                locationName == null ? "Unknown Location" : locationName,
                                liked);
                        imageObjArrayList.add(imageObj);
                        if(downloadedImages>=50){
                            break;
                        }
                    }
                    getDownloadUrls();
                    imageRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    imageRef.removeEventListener(this);
                    if (getContext() == null) {
                        return;
                    }
                    DialogUtils.showUnexpectedError(getContext());
                }
            });
        }
    }

    private void getDownloadUrls(){
        if(initalDownload){
            initalDownload = false;
        }
        final int limit = photoDownloadOffset + offsetRate;
        ArrayList<ImageObj> freshImages = new ArrayList<>(imageObjArrayList.subList(photoDownloadOffset, limit));
        for(int i = 0; i<freshImages.size(); i++) {
            final int index = i;
            StorageReference storageRef = storageReference.child(freshImages.get(i).getPath());
            storageRef.getDownloadUrl().addOnCompleteListener((@NonNull Task<Uri> task) -> {
                if (task.getException() == null) {
                    freshImages.get(index).setDownloadUrl(task.getResult().toString());
                }else {
                    freshImages.remove(freshImages.get(index));
                }
                if(checkIfFinished(limit)){
                    downloadFinished(freshImages);
                }
            });
        }
    }

    private boolean checkIfFinished(int limit){
        urlDownloadDoneCount++;
        return urlDownloadDoneCount>=imageObjArrayList.size()||urlDownloadDoneCount>=limit;
    }

    private void downloadFinished(ArrayList<ImageObj> newImages){
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
        newImages.sort((ImageObj o1, ImageObj o2)->
                o2.getTime_stamp().compareTo(o1.getTime_stamp()));
        photoAdapter.addData(newImages);
    }

    private void getLikedImages(){
        if(checkForSignIn(false)) {
            swipeRefreshLayout.setRefreshing(true);
            if (initalDownload) {
                imageObjArrayList.clear();
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(FirebaseAuth.getInstance().getUid());
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<String> likedImagesId = new ArrayList<>();
                        for (DataSnapshot snap : dataSnapshot.child("likes").getChildren()) {
                            likedImagesId.add(snap.getKey());
                        }
                        downloadImages(likedImagesId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        downloadImages(new ArrayList<>());
                    }
                });
            }else{
                getDownloadUrls();
            }
        }
    }

    private void uploadImage(byte[] imageBytes, String locationName) {
        if(imageBytes != null&&checkForSignIn(true))
        {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String uuid = activeImageDatabaseReference.getKey();
            String stringUri = "images/"+ uuid;
            StorageReference ref = storageReference.child(stringUri);
            ref.putBytes(imageBytes)
                    .addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot)-> {
                        progressDialog.dismiss();
                        addImageUrlToDatabase(stringUri, locationName);
                        photoAdapter.resetData();
                        getLikedImages();
                        Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener((@NonNull Exception e)-> {
                        progressDialog.dismiss();
                        resetPushPath();
                        Toast.makeText(getActivity(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener((UploadTask.TaskSnapshot taskSnapshot)-> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
    }
//
    private void addImageUrlToDatabase(String stringUri, String locationName){
        if(checkForSignIn(true)) {
            HashMap<String, Object> imageMap = new HashMap<>();
            imageMap.put(PATH_KEY, stringUri);
            imageMap.put(LOCATION_NAME_KEY, locationName);
            imageMap.put(LIKES_KEY, 0);
            imageMap.put(REPORTED_KEY, false);
            imageMap.put(USER_ID_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());
            Calendar calendar = Calendar.getInstance();
            imageMap.put(TIME_STAMP_KEY, calendar.getTime().toString());
            activeImageDatabaseReference.setValue(imageMap);
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getUid()).child("images");
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put(activeImageDatabaseReference.getKey(), stringUri);
            userRef.updateChildren(userMap);
            resetPushPath();
        }
    }
}
