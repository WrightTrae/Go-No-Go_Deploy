package com.wright.android.t_minus.main_tabs.photos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wright.android.t_minus.R;

import java.io.File;

public class PhotoCaptureDetailActivity extends AppCompatActivity implements PhotoCaptureDetailFragment.OnFragmentUploadListener {

    public static final String IMAGE_FILE = "IMAGE_FILE";
    public static final String IMAGE_LOCATION = "IMAGE_LOCATION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("New Photo");
        }
        File file = (File) getIntent().getSerializableExtra(IMAGE_FILE);
        getSupportFragmentManager().beginTransaction().add( R.id.blankFrame, PhotoCaptureDetailFragment.newInstance(file)).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onUpload(File imageFile, String locationName) {
        Intent uploadIntent = new Intent();
        uploadIntent.putExtra(IMAGE_FILE, imageFile);
        uploadIntent.putExtra(IMAGE_LOCATION, locationName);
        setResult(RESULT_OK, uploadIntent);
        finish();
    }
}
