package com.sasanka.msp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sasanka.msp.models.MSPUserModel;
import com.sasanka.msp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for users list view. Used only when user type is vendor.
 */
public class MSPUserListViewAdapter extends ArrayAdapter<MSPUserModel> {

    private Context mContext;
    private UserHolder mHolder;

    public MSPUserListViewAdapter(Context context) {
        super(context, 0, new ArrayList<MSPUserModel>());
        mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
            mHolder = new UserHolder();
            mHolder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(mHolder);
        } else {
            mHolder = (UserHolder) view.getTag();
        }
        MSPUserModel currentUserModel = getItem(position);
        mHolder.name.setText(currentUserModel.getFullName());
        return view;
    }

    public void updateAdapterData(List<MSPUserModel> userModels) {
        clear();
        addAll(userModels);
    }

    private class UserHolder {
        TextView name;
    }
}
