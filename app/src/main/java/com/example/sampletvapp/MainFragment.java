package com.example.sampletvapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.leanback.app.BrowseSupportFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends BrowseSupportFragment {

    private static final int SPLASH_TIME_OUT = 3000; // 3 seconds timeout for splash screen

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Simulate splash screen timeout and navigate to the next fragment after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Replace the current splash fragment with ScanDeviceFragment
                Fragment nextFragment = new ScanDeviceFragment();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction(); // Updated
                transaction.replace(R.id.main_browse_fragment, nextFragment); // Assuming you have a container with this ID
                transaction.commit();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment (you can set a splash image here)
        return inflater.inflate(R.layout.fragment_main, container, false); // Make sure you have a layout for this
    }
}
