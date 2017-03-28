package com.sasanka.msp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sasanka.msp.models.MSPProducts;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.R;

import java.util.HashMap;
import java.util.List;


public class MSPProductListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();

    public interface OnFragmentInteractionListener {
        void onCallToActionButtonClicked(String productId);
    }

    public static MSPProductListFragment newInstance() {
        MSPProductListFragment fragment = new MSPProductListFragment();
        return fragment;
    }

    public MSPProductListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_products_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(R.id.list);
        ListViewAdapter adapter = new ListViewAdapter(getActivity(), MSPProducts.getInstance().getList());
        listView.setAdapter(adapter);
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

    private class ListViewAdapter extends BaseAdapter {

        private Context mContext;
        private List<HashMap<String, String>> mValues;
        private ViewHolder mHolder;

        public ListViewAdapter(Context context, List<HashMap<String, String>> values) {
            mContext = context;
            mValues = values;
        }

        @Override
        public int getCount() {
            return mValues.size();
        }

        @Override
        public HashMap<String, String> getItem(int i) {
            return mValues.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.product_item, viewGroup, false);
                mHolder = new ViewHolder();
                mHolder.productName = (TextView) view.findViewById(R.id.productName);
                mHolder.productPrice = (TextView) view.findViewById(R.id.productPrice);
                mHolder.bidButton = (Button) view.findViewById(R.id.callToActionButton);
                view.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) view.getTag();
            }
            mHolder.productName.setText(getItem(i).get("name"));
            mHolder.productPrice.setText(getItem(i).get("price"));
            mHolder.bidButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onCallToActionButtonClicked(String.valueOf(i));
                }
            });
            if (!mUser.isVendor()) {
                mHolder.bidButton.setText("Bid");
            } else {
                mHolder.bidButton.setText("Join Chat");
            }
            return view;
        }
    }

    private class ViewHolder {
        public TextView productName, productPrice;
        public Button bidButton;
    }
}
