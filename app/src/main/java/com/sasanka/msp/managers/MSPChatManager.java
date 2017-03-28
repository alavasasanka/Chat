package com.sasanka.msp.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.sasanka.msp.database.MSPChatDBHelper;
import com.sasanka.msp.adapters.MSPChatListViewAdapter;
import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.managers.Interfaces.MSPServerInterface;
import com.sasanka.msp.models.MSPChatModel;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the operations wrt to chat between users and vendors.
 */
public class MSPChatManager {

    private static MSPChatManager sInstance;
    private MSPChatListViewAdapter mAdapter;
    private MSPServerInterface mServer = MSPServerManagerProvider.getProvider();
    private String mProductId, mReceiverId;

    /**
     * callback for fetching new messages from server and applying them to our local db.
     */
    public interface OnFetchAndApplyMessagesCallback {
        /**
         * On fetch from server and apply to local db completed.
         * @param e null in case of success else not null.
         */
        void onFetchAndApplyCompleted(Exception e);
    }

    /**
     * callback for adding message to server.
     */
    public interface OnSendMessageCallback {
        /**
         * On message added to local db.
         * @param e null in case of success else not null.
         */
        void onMessageAddedToLocalDB(Exception e);

        /**
         * On message added to server.
         * @param e null in case of success else not null.
         */
        void onMessageSentToServer(Exception e);
    }

    private MSPChatManager() {
        // registering for a local broadcast so that adapter could be refreshed.
        LocalBroadcastManager.getInstance(MSPApplication.getContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final String action = intent.getAction();
                        if ((action != null) &&
                                (action.equals(MSPConstants.LocalBroadcast.messageLocalBroadcast))) {
                            if (mProductId != null)
                                reloadAdapter(context, mProductId, mReceiverId);
                        }
                    }
                }, new IntentFilter(MSPConstants.LocalBroadcast.messageLocalBroadcast));
    }

    public static MSPChatManager getSharedInstance() {
        if (sInstance == null) {
            sInstance = new MSPChatManager();
        }
        return sInstance;
    }

    /**
     * Used to set current productId.
     * @param productId id of product.
     */
    public void setProductId(String productId) {
        mProductId = productId;
    }

    /**
     * Used to set current receiverId if any.
     * @param receiverId not null in case of vendor.
     */
    public void setReceiverId(@Nullable String receiverId) {
        mReceiverId = receiverId;
    }

    /**
     * Gives an instance of chat adapter.
     * @param context Context.
     * @return MSChatListViewAdapter.
     */
    public MSPChatListViewAdapter getAdapter(Context context) {
        if (mAdapter == null) {
            mAdapter = new MSPChatListViewAdapter(context);
        }
        return mAdapter;
    }

    /**
     * Reloads the adapter by updating the data with new date retrieved from local db
     * wrt to productId and receiverId.
     * @param context Context
     * @param productId id of product.
     * @param receiverId not null in case of vendor.
     */
    public void reloadAdapter(Context context, @NonNull String productId, @Nullable String receiverId) {
        getAdapter(context).updateAdapterData(getAllMessagesFromLocalDB(context, productId, receiverId));
    }

    /**
     * Gets all messages from local db as a list of chatModels.
     * @param context Context.
     * @param productId id of product.
     * @param receiverId not null in case of vendor.
     * @return list of chatModels.
     */
    private List<MSPChatModel> getAllMessagesFromLocalDB(Context context,
                                                        @NonNull String productId,
                                                        @Nullable String receiverId) {
        try {
            return MSPChatDBHelper.getSharedInstance(context).getAllMessages(productId, receiverId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Fetches new messages from server and applies them to local db.
     * @param context Context.
     * @param productId id of product.
     * @param receiverId not null in case of vendor.
     * @param callback OnFetchAndApplyMessagesCallback.
     */
    public void fetchAndApplyMessagesFromServer(final Context context,
                                                @NonNull final String productId,
                                                @Nullable final String receiverId,
                                                @Nullable final OnFetchAndApplyMessagesCallback callback) {
        long lastSyncTime = 0;
        try {
            lastSyncTime = MSPChatDBHelper.getSharedInstance(context)
                    .getLastSyncTime(productId, receiverId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mServer.fetchMessagesAfterLastSyncTime(productId, receiverId, lastSyncTime,
                new MSPServerInterface.OnFetchMessagesListener() {
                    @Override
                    public void onMessagesFetched(List<MSPChatModel> chatModels, Exception e) {
                        if (e == null) {
                            MSPChatDBHelper.getSharedInstance(context).addMessages(chatModels,
                                    new MSPChatDBHelper.MSAddMessagesCallback() {
                                        @Override
                                        public void onMessagesAdded(Exception e) {
                                            if (callback != null)
                                                callback.onFetchAndApplyCompleted(e);
                                        }
                                    });
                            int count = chatModels.size();
                            if (count > 0) {
                                try {
                                    MSPChatDBHelper.getSharedInstance(context).updateLastSyncTime(
                                            productId,
                                            receiverId != null ? receiverId : "",
                                            chatModels.get(count - 1).getCreatedAt());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else {
                            if (callback != null)
                                callback.onFetchAndApplyCompleted(e);
                        }
                    }
                });
    }

    /**
     * Adds new message to local db and server.
     * @param context Context.
     * @param chatModel model to be sent.
     * @param callback OnSendMessageCallback.
     */
    public void sendMessage(final Context context,
                            final MSPChatModel chatModel,
                            @Nullable final OnSendMessageCallback callback) {
        try {
            MSPChatDBHelper.getSharedInstance(context).addMessage(chatModel);
            if (callback != null)
                callback.onMessageAddedToLocalDB(null);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null)
                callback.onMessageAddedToLocalDB(e);
        }
        mServer.sendMessage(chatModel, new MSPServerInterface.OnSendMessageListener() {
            @Override
            public void onMessageSent(MSPChatModel chatModel, Exception e) {
                if (e == null) {
                    try {
                        MSPChatDBHelper.getSharedInstance(context).onUpdateToServerSuccess(chatModel);
                        if (chatModel.isSentByVendor())
                            MSPChatDBHelper.getSharedInstance(context).updateLastSyncTime(
                                    chatModel.getProductId(),
                                    chatModel.getReceivers().getString(0),
                                    chatModel.getCreatedAt());
                        else
                            MSPChatDBHelper.getSharedInstance(context).updateLastSyncTime(
                                    chatModel.getProductId(),
                                    "",
                                    chatModel.getCreatedAt());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (callback != null)
                    callback.onMessageSentToServer(e);
            }
        });
    }

    /**
     * This method gets all the messages which are not yet updated to server and sends them to server.
     * @param context Context.
     */
    public void resendFailedMessages(Context context) {
        try {
            List<MSPChatModel> chatModels = MSPChatDBHelper.getSharedInstance(context)
                    .getAllMessagesWhichAreNotUpdatedToServer();
            for (MSPChatModel chatModel : chatModels) {
                sendMessage(context, chatModel, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
