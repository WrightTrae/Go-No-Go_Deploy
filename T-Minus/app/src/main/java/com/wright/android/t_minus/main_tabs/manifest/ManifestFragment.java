package com.wright.android.t_minus.main_tabs.manifest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paginate.Paginate;
import com.wright.android.t_minus.universal_utils.DialogUtils;
import com.wright.android.t_minus.network_connection.GetAgencyUrlAPI;
import com.wright.android.t_minus.network_connection.GetManifestsFromAPI;
import com.wright.android.t_minus.network_connection.NetworkUtils;
import com.wright.android.t_minus.objects.LaunchPad;
import com.wright.android.t_minus.objects.Manifest;
import com.wright.android.t_minus.R;
import com.wright.android.t_minus.objects.PadLocation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ManifestFragment extends Fragment implements ListView.OnItemClickListener, GetAgencyUrlAPI.OnFinished, GetManifestsFromAPI.OnFinished{

    private DatabaseReference mDatabaseRef;
    private int manifestOffset = -10;
    private boolean isLoading;
    private ManifestListAdapter manifestListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ManifestFragment() {
        // Required empty public constructor
    }

    public static ManifestFragment newInstance() {
        return  new ManifestFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        return inflater.inflate(R.layout.fragment_manifest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutManifest);
        swipeRefreshLayout.setOnRefreshListener(()->{
            manifestOffset = -10;
            manifestListAdapter.resetData();
        });
        ListView manifestListView = getView().findViewById(R.id.manifestList);
        manifestListAdapter = new ManifestListAdapter(getContext(), new ArrayList<>());
        manifestListView.setAdapter(manifestListAdapter);
        Paginate.Callbacks callbacks = new Paginate.Callbacks() {
            @Override
            public void onLoadMore() {
                // Load next page of data (e.g. network or database)
                manifestOffset += 10;
                downloadManifests();
            }

            @Override
            public boolean isLoading() {
                // Indicate whether new page loading is in progress or not
                return isLoading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                // Indicate whether all data (pages) are loaded or not
                return manifestOffset>100;
            }
        };
        Paginate.with(manifestListView, callbacks)
                .build();
        manifestListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), ManifestDetailsActivity.class);
        intent.putExtra(ManifestDetailsActivity.ARG_MANIFEST, (Manifest)manifestListAdapter.getItem(position));
        startActivity(intent);
    }

    private void downloadManifests(){
        if(getContext() == null){
            return;
        }
        if(getView() == null){
            DialogUtils.showUnexpectedError(getContext());
            return;
        }
        if(NetworkUtils.isConnected(getContext())){
            isLoading = true;
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();
            calendar.add(Calendar.YEAR, 1);
            Date yearAdvanceDate = calendar.getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            new GetManifestsFromAPI(this, manifestOffset, 10).execute(
                    simpleDateFormat.format(currentDate),simpleDateFormat.format(yearAdvanceDate));
        }else{
            Snackbar.make(getView(), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", (View v) -> downloadManifests()).show();
        }
    }

    @Override
    public void onManifestFinished(ArrayList<Manifest> _manifests) {
        ArrayList<PadLocation> padLocations = new ArrayList<>();
        for(Manifest manifest:_manifests){
            if(manifest.getPadLocation() == null){
                continue;
            }
            if (containsName(padLocations, manifest.getPadLocation().getId())) {
                padLocations.add(manifest.getPadLocation());
            }
        }
        checkIfPadLocationExist(padLocations);
        addData(_manifests);
    }

    private boolean containsName(final ArrayList<PadLocation> list, final int name){
        return list.stream().noneMatch((o -> o.getId() == (name)));
    }

    private void checkIfPadLocationExist(ArrayList<PadLocation> _padLocations){
        mDatabaseRef.child("launch_locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (PadLocation pad: _padLocations) {
                    if(pad.getLaunchPads()==null || pad.getName().toLowerCase().equals("air launch to orbit")){
                        continue;
                    }
                    if (!dataSnapshot.hasChild(String.valueOf(pad.getId()))) {
                        updateFirebasePadLocations(pad);
                    }else{
                        pad.setName((String) dataSnapshot.child(String.valueOf(pad.getId())).child("name").getValue());
                    }
                }

                for(DataSnapshot locationSnap: dataSnapshot.getChildren()){
                    int locationId = Integer.parseInt(locationSnap.getKey());
                    if (containsName(_padLocations, locationId)){
                        String name = (String) locationSnap.child("name").getValue();
                        PadLocation padLocation = new PadLocation(locationId, name, new ArrayList<>());
                        _padLocations.add(padLocation);
                    }
                }
                checkIfPadExist(_padLocations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateFirebasePadLocations(PadLocation _padLocation){
        HashMap<String, Object> locationMap = new HashMap<>();
        locationMap.put("name", _padLocation.getName());
        DatabaseReference locationRef = mDatabaseRef.child("launch_locations").child(String.valueOf(_padLocation.getId()));
        locationRef.setValue(locationMap);
    }

    private void checkIfPadExist(ArrayList<PadLocation> _padLocations){
        mDatabaseRef.child("launch_pads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (PadLocation pad: _padLocations) {
                    if(pad.getLaunchPads()==null || pad.getName().toLowerCase().equals("air launch to orbit")){
                        continue;
                    }
                    for(LaunchPad launchPad : pad.getLaunchPads()) {
                        if (!dataSnapshot.hasChild(String.valueOf(launchPad.getId()))) {
                            updateFirebasePad(launchPad);
                        }else{
                            launchPad.setName((String) dataSnapshot.child(String.valueOf(launchPad.getId())).child("name").getValue());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateFirebasePad(LaunchPad launchPad){
        if(launchPad.getLatitude() == 0||launchPad.getLongitude()==0){
            return;
        }
        HashMap<String, Object> padMap = new HashMap<>();
        padMap.put("latitude", launchPad.getLatitude());
        padMap.put("longitude", launchPad.getLongitude());
        padMap.put("locationId", launchPad.getLocationId());
        padMap.put("name", launchPad.getName());
        DatabaseReference padRef = mDatabaseRef.child("launch_pads").child(String.valueOf(launchPad.getId()));
        padRef.setValue(padMap);
    }


    private void addData(ArrayList<Manifest> _manifests){
        if(getContext() == null){
            return;
        }
        if(getView() == null){
            DialogUtils.showUnexpectedError(getContext());
            return;
        }
        downloadAgencyLogo(_manifests);
    }

    private void downloadAgencyLogo(ArrayList<Manifest> manifests){
        if(getContext() == null){
            return;
        }
        if(getView() == null){
            DialogUtils.showUnexpectedError(getContext());
            return;
        }
        if(NetworkUtils.isConnected(getContext())){
            new GetAgencyUrlAPI(this, manifests).execute();
        }else{
            Snackbar.make(getView(), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", (View v) -> downloadAgencyLogo(manifests)).show();
        }
    }

    @Override
    public void onAgencyFinished(ArrayList<Manifest> _manifests) {//downloadAgencyLogo Finish
        manifestListAdapter.updateData(_manifests);
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
        isLoading = false;
    }
}
