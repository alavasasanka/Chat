package com.sasanka.msp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sasanka.msp.adapters.MSPUserListViewAdapter;
import com.sasanka.msp.models.MSPUserModel;
import com.sasanka.msp.R;

/**
 * This fragment displays the list of bidders available for a particular product wrt a vendor.
 */
public class MSPUsersListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private MSPUserListViewAdapter mAdapter;

    public interface OnFragmentInteractionListener {
        void onUsersListViewCreated(MSPUsersListFragment fragment);
        void onItemClicked(MSPUserModel userModel);
    }

    public static MSPUsersListFragment newInstance() {
        MSPUsersListFragment fragment = new MSPUsersListFragment();
        return fragment;
    }

    public MSPUsersListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_list, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(R.id.list);
        mAdapter = new MSPUserListViewAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setEmptyView(view.findViewById(android.R.id.empty));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onItemClicked(mAdapter.getItem(i));
            }
        });
        mListener.onUsersListViewCreated(this);
    }

    public MSPUserListViewAdapter getAdapter() {
        return mAdapter;
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

}
