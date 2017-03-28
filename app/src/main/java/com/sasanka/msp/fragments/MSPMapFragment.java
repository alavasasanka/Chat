package com.sasanka.msp.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.sasanka.msp.R;

/**
 * This fragment shows user's current location.
 */
public class MSPMapFragment extends Fragment implements GoogleMap.OnMyLocationChangeListener {

    private OnFragmentInteractionListener mListener;
    private GoogleMap mMapFragment;
    private Location mCurrentLocation;

    public interface OnFragmentInteractionListener {
        void onCurrentLocationChanged(Location newCurrentLocation);
    }

    public static MSPMapFragment newInstance() {
        MSPMapFragment fragment = new MSPMapFragment();
        return fragment;
    }

    public MSPMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mMapFragment == null) {
            mMapFragment = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapFragment)).getMap();
            if (mMapFragment != null) {
                mMapFragment.setMyLocationEnabled(true);
                mMapFragment.setOnMyLocationChangeListener(this);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null && mCurrentLocation == null) {
            mMapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    location.getLatitude(), location.getLongitude()), 18.0f));
        }
        mCurrentLocation = location;
        if (mListener != null)
            mListener.onCurrentLocationChanged(mCurrentLocation);
    }

}
