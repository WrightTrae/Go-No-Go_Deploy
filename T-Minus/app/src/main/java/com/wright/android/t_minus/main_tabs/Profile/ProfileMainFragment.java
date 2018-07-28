package com.wright.android.t_minus.main_tabs.Profile;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wright.android.t_minus.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileMainFragment extends Fragment {

    public ProfileMainFragment() {
        // Required empty public constructor
    }

    public static ProfileMainFragment newInstance() {
        ProfileMainFragment fragment = new ProfileMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_main, container, false);
    }

}
