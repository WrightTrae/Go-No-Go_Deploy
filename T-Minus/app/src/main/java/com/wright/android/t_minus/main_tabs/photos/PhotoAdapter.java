package com.wright.android.t_minus.main_tabs.photos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.wright.android.t_minus.R;
import com.wright.android.t_minus.objects.ImageObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder>{
    // BASE ID
    private static final long BASE_ID = 0x100000;
    private final Context mContext;
    private ArrayList<ImageObj> imageObjArrayList;
    private Boolean clickable = true;

    // C-tor
    public PhotoAdapter(Context _context){
        mContext = _context;
        imageObjArrayList = new ArrayList<>();
    }

    public void addData(ArrayList<ImageObj> imageObjs){
        imageObjArrayList.addAll(imageObjs);
        notifyDataSetChanged();
    }

    public void resetData(){
        imageObjArrayList.clear();
        notifyDataSetChanged();
    }

    public void setClickable(Boolean enabled){
        clickable = enabled;
    }

    @Override
    public int getItemCount() {
        if(imageObjArrayList !=null) {
            return imageObjArrayList.size();
        }
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder vh;
            View _recycleView = LayoutInflater.from(mContext).inflate(R.layout.photo_grid_cell, parent, false);
            vh = new ViewHolder(_recycleView);
            _recycleView.setTag(vh);
            vh.likesView.setOnClickListener((View v)->likeImage(vh));
            _recycleView.setOnClickListener(new DoubleClickListener(500) {
                @Override
                public void onDoubleClick(View view) {
                    if(mContext == null||!clickable){
                        return;
                    }
                    likeImage(vh);
                }
                @Override
                public void onSingleClick(View view) {
                    if(mContext == null||!clickable){
                        return;
                    }
                    Dialog settingsDialog = new Dialog(mContext);
                    int selectedIndex = (int) view.findViewById(R.id.grid_image).getTag();
                    if(settingsDialog.getWindow()!=null) {
                        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    }
                    View popup = LayoutInflater.from(mContext).inflate(R.layout.image_popup_layout, null);
                    Picasso.get().load(Objects.requireNonNull(getItem(selectedIndex)).getDownloadUrl()).
                            placeholder(R.drawable.rocket_default_image).purgeable()
                            .noFade().into((ImageView)popup.findViewById(R.id.popup_image));
                    popup.findViewById(R.id.popup_image).setOnClickListener((View v) -> settingsDialog.dismiss());
                    popup.findViewById(R.id.popup_report_btn).setOnClickListener((View v) -> {
                        showReportDialog(parent, vh);
                        settingsDialog.dismiss();
                    });
                    settingsDialog.setContentView(popup);
                    settingsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    settingsDialog.setCancelable(true);
                    settingsDialog.show();
                }
            });
            return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int _position) {
        ImageObj imageObj = getItem(_position);
        if(imageObj != null) {
            vh.locationName.setText(imageObj.getLocationName());
            if(imageObj.isLiked()) {
                vh.ivLikesIcon.setColorFilter(mContext.getColor(R.color.colorAccent));
                vh.likesView.setTag(false);
            }else {
                vh.ivLikesIcon.setColorFilter(mContext.getColor(android.R.color.white));
                vh.likesView.setTag(true);
            }
            vh.tvLikes.setText(String.valueOf(imageObj.getLikes()));
            Picasso picasso = Picasso.get();
            //get screen size for picasso image loader
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) mContext).getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displayMetrics);
            picasso.load(Objects.requireNonNull(getItem(_position)).getDownloadUrl()).resize(displayMetrics.widthPixels/2, 0).into(vh);
            vh.ivImage.setTag(_position);
        }
    }

    // Item
    private ImageObj getItem(int _position){
        if(imageObjArrayList !=null) {
            return imageObjArrayList.get(_position);
        }
        return null;
    }

    // Item ID
    public long getItemId(int _position){
        return BASE_ID + _position;
    }

    private void likeImage(ViewHolder vh){
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null){
            return;
        }
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        ImageObj imageObj = getItem((int) vh.ivImage.getTag());
        if((Boolean)vh.likesView.getTag()){
            Objects.requireNonNull(imageObj).addLike();
            vh.ivLikesIcon.setColorFilter(mContext.getColor(R.color.colorAccent));
            vh.likesView.setTag(false);
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put(imageObj.getId(), imageObj.getPath());
            firebaseDatabase.child("users").child(userId).child("likes").updateChildren(userMap);
        }else{
            Objects.requireNonNull(imageObj).removeLike();
            vh.ivLikesIcon.setColorFilter(mContext.getColor(android.R.color.white));
            vh.likesView.setTag(true);
            firebaseDatabase.child("users").child(userId).child("likes").child(imageObj.getId()).removeValue();
        }

        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("likes", imageObj.getLikes());
        firebaseDatabase.child("images").child(imageObj.getId()).updateChildren(imageMap);
        vh.tvLikes.setText(String.valueOf(imageObj.getLikes()));
    }

    private void showReportDialog(ViewGroup _parentView, ViewHolder vh){
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(mContext);
        builder.setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT);
        builder.setCancelable(true);
        builder.setTitle("Please Select the reason you reported this image.");
        builder.setItems(new String[]{"Inappropriate image", "Content not about rocket launches", "Other"},
                (DialogInterface dialogInterface, int index)->{
                    String reason;
                    switch (index){
                        case 0:
                            reason = "Inappropriate image";
                            break;
                        case 1:
                            reason = "Content not about rocket launches";
                            break;

                        case 2:
                            reason = "Other";
                            break;
                        default:
                            reason = "Error";
                            break;
                    }
                    reportImage(vh, reason);
                    Snackbar.make(_parentView, "Thank you for helping improve our community", Snackbar.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                });
        builder.show();
    }

    private void reportImage(ViewHolder vh, String reason){
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null){
            return;
        }
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        ImageObj imageObj = getItem((int) vh.ivImage.getTag());
        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("reported", true);
        imageMap.put("reported reason", reason);
        firebaseDatabase.child("images").child(Objects.requireNonNull(imageObj).getId()).updateChildren(imageMap);
        imageObjArrayList.remove(imageObj);
        notifyDataSetChanged();
    }

    // Optimize with view holder!
    static class ViewHolder extends RecyclerView.ViewHolder implements Target{
        final DynamicHeightImageView ivImage;
        final TextView tvLikes;
        final ImageView ivLikesIcon;
        final View likesView;
        final TextView locationName;

        private ViewHolder(View _layout){
            super(_layout);
            ivImage = _layout.findViewById(R.id.grid_image);
            ivImage.setScaleType(ImageView.ScaleType.CENTER);
            tvLikes = _layout.findViewById(R.id.grid_cell_likes);
            ivLikesIcon = _layout.findViewById(R.id.photo_cell_like_image);
            likesView = _layout.findViewById(R.id.photo_cell_like_layout);
            locationName = _layout.findViewById(R.id.grid_cell_location);
            _layout.getWidth();
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            ivImage.setHeightRatio(ratio);
            ivImage.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }
}
