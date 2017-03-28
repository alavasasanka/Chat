package com.sasanka.msp.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for chat list view.
 */
public class MSPChatListViewAdapter extends ArrayAdapter<MSPChatModel> {

    private Context mContext;
    private ChatHolder mHolder;
    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();

    public MSPChatListViewAdapter(Context context) {
        super(context, 0, new ArrayList<MSPChatModel>());
        mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item, parent, false);
            mHolder = new ChatHolder();
            mHolder.receivedMessage = (TextView) view.findViewById(R.id.messageReceived);
            mHolder.sentMessage = (TextView) view.findViewById(R.id.messageSent);
            mHolder.senderName = (TextView) view.findViewById(R.id.senderName);
            mHolder.tickMark = (ImageView) view.findViewById(R.id.tickMark);
            mHolder.sentTime = (TextView) view.findViewById(R.id.sentTime);
            mHolder.receivedTime = (TextView) view.findViewById(R.id.receivedTime);
            view.setTag(mHolder);
        } else {
            mHolder = (ChatHolder) view.getTag();
        }
        MSPChatModel currentChatModel = getItem(position);
        if (mUser.getObjectId().equals(currentChatModel.getSenderId())) {
            mHolder.sentMessage.setText(currentChatModel.getMessage());
            mHolder.sentTime.setText(DateUtils.getRelativeTimeSpanString(currentChatModel.getCreatedTime()));
            mHolder.senderName.setVisibility(View.GONE);
            mHolder.receivedTime.setVisibility(View.GONE);
            mHolder.sentTime.setVisibility(View.VISIBLE);
            mHolder.sentMessage.setVisibility(View.VISIBLE);
            mHolder.receivedMessage.setVisibility(View.GONE);
            if (currentChatModel.isUpdatedToServer())
                mHolder.tickMark.setVisibility(View.VISIBLE);
            else
                mHolder.tickMark.setVisibility(View.INVISIBLE);
        } else {
            mHolder.receivedMessage.setText(currentChatModel.getMessage());
            mHolder.receivedTime.setText(DateUtils.getRelativeTimeSpanString(currentChatModel.getCreatedTime()));
            if (!mUser.isVendor()) {
                mHolder.senderName.setText(currentChatModel.getSenderFullName());
                mHolder.senderName.setVisibility(View.VISIBLE);
            } else {
                mHolder.senderName.setVisibility(View.GONE);
            }
            mHolder.receivedMessage.setVisibility(View.VISIBLE);
            mHolder.receivedTime.setVisibility(View.VISIBLE);
            mHolder.sentMessage.setVisibility(View.GONE);
            mHolder.tickMark.setVisibility(View.GONE);
            mHolder.sentTime.setVisibility(View.GONE);
        }
        return view;
    }

    public void updateAdapterData(List<MSPChatModel> chatModels) {
        clear();
        addAll(chatModels);
    }

    private class ChatHolder {
        TextView receivedMessage, sentMessage, senderName, sentTime, receivedTime;
        ImageView tickMark;
    }
}
