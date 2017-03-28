package com.sasanka.msp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sasanka.msp.R;

/**
 * This fragment displays the options to either login or signUp.
 */
public class MSPPreLoginHomeFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onLoginButtonClicked();
        void onSignUpButtonClicked();
    }

    public static MSPPreLoginHomeFragment newInstance() {
        MSPPreLoginHomeFragment fragment = new MSPPreLoginHomeFragment();
        return fragment;
    }

    public MSPPreLoginHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pre_login_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button login = (Button) view.findViewById(R.id.login);
        Button signUp = (Button) view.findViewById(R.id.signUp);
        login.setOnClickListener(this);
        signUp.setOnClickListener(this);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                mListener.onLoginButtonClicked();
                break;

            case R.id.signUp:
                mListener.onSignUpButtonClicked();
                break;
        }
    }

}
