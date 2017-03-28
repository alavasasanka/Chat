package com.sasanka.msp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sasanka.msp.R;

/**
 * This fragment handles the ui for logging in a user.
 */
public class MSPLoginFragment extends Fragment implements View.OnClickListener {

    public interface OnFragmentInteractionListener {
        void onLoginButtonClicked(String email, String password, String userType);
    }

    private OnFragmentInteractionListener mListener;
    private EditText mEmail, mPassword;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton;

    public static MSPLoginFragment newInstance() {
        MSPLoginFragment fragment = new MSPLoginFragment();
        return fragment;
    }

    public MSPLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPassword = (EditText) view.findViewById(R.id.password);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.userType);
        Button login = (Button) view.findViewById(R.id.login);
        login.setOnClickListener(this);
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
                int selectedId = mRadioGroup.getCheckedRadioButtonId();
                mRadioButton = (RadioButton) mRadioGroup.findViewById(selectedId);
                mListener.onLoginButtonClicked(mEmail.getText().toString(), mPassword.getText().toString(),
                        mRadioButton.getText().toString());
                break;
        }
    }
}
