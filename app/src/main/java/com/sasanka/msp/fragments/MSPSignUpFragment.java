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
 * This fragment handles the ui for signing up a user.
 */
public class MSPSignUpFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private EditText mEmail, mPassword, mFullName;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton;

    public interface OnFragmentInteractionListener {
        void onSignUpButtonClicked(String fullName, String email, String password, String userType);
    }

    public static MSPSignUpFragment newInstance() {
        MSPSignUpFragment fragment = new MSPSignUpFragment();
        return fragment;
    }

    public MSPSignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFullName = (EditText) view.findViewById(R.id.name);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPassword = (EditText) view.findViewById(R.id.password);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.userType);
        Button signUp = (Button) view.findViewById(R.id.signUp);
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
            case R.id.signUp:
                int selectedId = mRadioGroup.getCheckedRadioButtonId();
                mRadioButton = (RadioButton) mRadioGroup.findViewById(selectedId);
                mListener.onSignUpButtonClicked(mFullName.getText().toString(), mEmail.getText().toString(),
                        mPassword.getText().toString(), mRadioButton.getText().toString());
                break;
        }
    }
}
