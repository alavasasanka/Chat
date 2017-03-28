package com.sasanka.msp.managers.Interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.models.MSPUserModel;

import java.util.List;

/**
 * This interface contains all the operations related to server.
 */
public interface MSPServerInterface {

    /**
     * listener for sending message to server.
     */
    interface OnSendMessageListener {
        /**
         * Called when message is updated to server.
         * @param chatModel model which was updated to server.
         * @param e null in case of success else not null.
         */
        void onMessageSent(MSPChatModel chatModel, Exception e);
    }

    /**
     * listener for fetching all messages from server.
     */
    interface OnFetchMessagesListener {
        /**
         * Called when fetching messages from server is completed.
         * @param chatModels List of chatModels fetched.
         * @param e null in case of success else not null.
         */
        void onMessagesFetched(List<MSPChatModel> chatModels, Exception e);
    }

    /**
     * listener for fetching all bidders wrt to a vendor.
     */
    interface OnFetchUsersListener {
        /**
         * Called when fetch operation is completed.
         * @param userModels List of bidders wrt to a vendor.
         * @param e null in case of success else not null.
         */
        void onUsersFetched(List<MSPUserModel> userModels, Exception e);
    }

    /**
     * This method notifies if the device is registered for push notifications.
     * @return boolean.
     */
    boolean isRegisteredForPushNotifications();

    /**
     * This method subscribes the device for push notifications.
     */
    void registerForPushNotifications();

    /**
     * This method notifies if the user's unique id is updated to installations table in server.
     * @return boolean
     */
    boolean isUserObjectIdUpdatedToInstallation();

    /**
     * This method updates user's unique id to installations table in server.
     */
    void updateInstallation();

    /**
     * This method adds this message to server and send this message to its respective receivers.
     * @param chatModel model to be stored in server.
     * @param listener OnSendMessageListener.
     */
    void sendMessage(MSPChatModel chatModel, OnSendMessageListener listener);

    /**
     * This method fetches messages from server that are after lastSyncTime wrt to productId and receiverId.
     * @param productId id of product.
     * @param receiverId not null in case of vendor.
     * @param lastSyncTime previous sync time.
     * @param listener OnFetchMessagesListener.
     */
    void fetchMessagesAfterLastSyncTime(@NonNull String productId,
                                        @Nullable String receiverId,
                                        @NonNull long lastSyncTime,
                                        @NonNull OnFetchMessagesListener listener);

    /**
     * This method fetches the list of bidders available for a vendor.
     * @param productId id of product.
     * @param excludeList list of unique ids of users already in local db.
     * @param listener OnFetchUsersListener.
     */
    void fetchUsersList(@NonNull String productId,
                        @NonNull List<String> excludeList,
                        @NonNull OnFetchUsersListener listener);
}
