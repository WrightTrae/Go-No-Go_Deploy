package com.wright.android.t_minus.main_tabs.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wright.android.t_minus.universal_utils.ImageUtils;
import com.wright.android.t_minus.R;

import java.io.File;
import java.util.Objects;

public class PhotoCaptureDetailFragment extends Fragment {

    private static final String IMAGE_FILE = "param1";
    private File imageFile;
    private EditText locationEditText;

    private OnFragmentUploadListener mListener;

    public PhotoCaptureDetailFragment() {
        // Required empty public constructor
    }

    public static PhotoCaptureDetailFragment newInstance(File file) {
        PhotoCaptureDetailFragment fragment = new PhotoCaptureDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(IMAGE_FILE, file);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageFile = (File) getArguments().getSerializable(IMAGE_FILE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_capture_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap bitmap;
        ExifInterface exif;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getContext()).getContentResolver(), ImageUtils.getOutputUri(getContext(), imageFile));
            exif = new ExifInterface(
                    imageFile.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        bitmap = ImageUtils.getRotatedBitmap(bitmap, exif);

        ((ImageView)view.findViewById(R.id.photo_detail_preview)).setImageBitmap(bitmap);

        locationEditText = view.findViewById(R.id.photo_detail_location_name);
        view.findViewById(R.id.photo_detail_upload_btn).setOnClickListener((View v)-> mListener.onUpload(imageFile, locationEditText.getText().toString()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentUploadListener) {
            mListener = (OnFragmentUploadListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentUploadListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentUploadListener {
        void onUpload(File imageFile, String locationName);
    }
}
