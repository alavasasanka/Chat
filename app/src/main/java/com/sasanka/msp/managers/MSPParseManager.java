package com.sasanka.msp.managers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.common.MSPConverter;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.common.MSPPreferences;
import com.sasanka.msp.managers.Interfaces.MSPServerInterface;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.models.MSPUserModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Since Parse is our server, this class handles all the interactions with parse server.
 */
public class MSPParseManager implements MSPServerInterface {

    private static MSPParseManager sInstance;
    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();

    private static final String APP_ID = "9neIFdGf9eioWcHfZF6lQNRECaiTSPDUbsbQM9yA";
    private static final String CLIENT_KEY = "AJ0NpEGap2ZslyhpxjEipXetfcvtQedCZrCXdpMy";

    private MSPParseManager() {}

    protected static synchronized MSPParseManager getSharedInstance() {
        if (sInstance == null) {
            sInstance = new MSPParseManager();
            sInstance.initializeServer(MSPApplication.getContext());
        }
        return sInstance;
    }

    private void setDefaultAccessPermissions() {
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(false);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    protected void initializeServer(Context context) {
        Parse.initialize(context, APP_ID, CLIENT_KEY);
        setDefaultAccessPermissions();
    }

    @Override
    public boolean isRegisteredForPushNotifications() {
        return MSPPreferences.contains(MSPConstants.PrefKeys.isRegisteredForPushNotifications)
                && MSPPreferences.getBoolean(MSPConstants.PrefKeys.isRegisteredForPushNotifications);
    }

    @Override
    public void registerForPushNotifications() {
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    MSPPreferences.putBoolean(MSPConstants.PrefKeys.isRegisteredForPushNotifications, true);
                    updateInstallation();
                } else {
                    MSPPreferences.putBoolean(MSPConstants.PrefKeys.isRegisteredForPushNotifications, false);
                }
            }
        });
    }

    @Override
    public boolean isUserObjectIdUpdatedToInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        return (installation != null) && (installation.get(MSPConstants.ServerKeys.userObjectId) != null);
    }

    @Override
    public void updateInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (installation != null && mUser.getObjectId() != null) {
            installation.put(MSPConstants.ServerKeys.userObjectId, mUser.getObjectId());
            installation.saveInBackground(null);
        }
    }

    @Override
    public void sendMessage(final MSPChatModel chatModel, final OnSendMessageListener listener) {
        HashMap<String, Object> body = new HashMap<>();
        body.put(MSPConstants.ServerKeys.message, chatModel.getMessage());
        body.put(MSPConstants.ServerKeys.productId, chatModel.getProductId());
        body.put(MSPConstants.ServerKeys.senderId, chatModel.getSenderId());
        body.put(MSPConstants.ServerKeys.senderType, chatModel.getSenderType());
        body.put(MSPConstants.ServerKeys.senderName, chatModel.getSenderFullName());
        body.put(MSPConstants.ServerKeys.createdTime, chatModel.getCreatedTime());
        if (chatModel.isSentByVendor()) {
            body.put(MSPConstants.ServerKeys.receivers, chatModel.getReceivers());
        }
        ParseCloud.callFunctionInBackground(MSPConstants.ServerKeys.sendMessage, body,
                new FunctionCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            MSPChatModel chatModel = MSPConverter.toChatModel(parseObject);
                            listener.onMessageSent(chatModel, null);
                        } else {
                            listener.onMessageSent(null, e);
                        }
                    }
                });
    }

    @Override
    public void fetchMessagesAfterLastSyncTime(@NonNull String productId,
                                               @Nullable String receiverId,
                                               @NonNull long lastSyncTime,
                                               @NonNull final OnFetchMessagesListener listener) {
        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        ParseQuery<ParseObject> query1 = ParseQuery.getQuery(MSPConstants.ServerKeys.chatMessages);
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(MSPConstants.ServerKeys.chatMessages);
        query1.whereEqualTo(MSPConstants.ServerKeys.senderId, mUser.getObjectId());
        query1.whereEqualTo(MSPConstants.ServerKeys.productId, productId);
        query1.whereGreaterThan(MSPConstants.ServerKeys.createdAt, new Date(lastSyncTime));
        if (receiverId != null)
            query1.whereEqualTo(MSPConstants.ServerKeys.receivers, receiverId);
        query2.whereEqualTo(MSPConstants.ServerKeys.receivers, mUser.getObjectId());
        query2.whereEqualTo(MSPConstants.ServerKeys.productId, productId);
        query2.whereGreaterThan(MSPConstants.ServerKeys.createdAt, new Date(lastSyncTime));
        if (receiverId != null)
            query2.whereEqualTo(MSPConstants.ServerKeys.senderId, receiverId);
        queries.add(query1);
        queries.add(query2);
        ParseQuery<ParseObject> orQuery = ParseQuery.or(queries);
        orQuery.addAscendingOrder(MSPConstants.ServerKeys.createdAt);
        orQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) {
                    listener.onMessagesFetched(null, e);
                } else {
                    List<MSPChatModel> chatModels = new ArrayList<>();
                    for (ParseObject parseObject : list) {
                        chatModels.add(MSPConverter.toChatModel(parseObject));
                    }
                    listener.onMessagesFetched(chatModels, null);
                }
            }
        });
    }

    @Override
    public void fetchUsersList(@NonNull String productId, @NonNull List<String> excludeList,
                               @NonNull final OnFetchUsersListener listener) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(MSPConstants.ServerKeys.chatMessages);
        parseQuery.whereEqualTo(MSPConstants.ServerKeys.receivers, mUser.getObjectId());
        parseQuery.whereNotContainedIn(MSPConstants.ServerKeys.senderId, excludeList);
        parseQuery.whereEqualTo(MSPConstants.ServerKeys.productId, productId);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    String senderId;
                    ArrayList<MSPUserModel> userModels = new ArrayList<>();
                    ArrayList<String> uniqueIds = new ArrayList<>();
                    for (ParseObject object : list) {
                        senderId = object.getString(MSPConstants.ServerKeys.senderId);
                        if (!uniqueIds.contains(senderId)) {
                            MSPUserModel userModel = new MSPUserModel();
                            userModel.setFullName(object.getString(MSPConstants.ServerKeys.senderName));
                            userModel.setUserObjectId(senderId);
                            userModels.add(userModel);
                            uniqueIds.add(senderId);
                        }
                    }
                    listener.onUsersFetched(userModels, null);
                } else {
                    listener.onUsersFetched(null, e);
                }
            }
        });
    }
}
