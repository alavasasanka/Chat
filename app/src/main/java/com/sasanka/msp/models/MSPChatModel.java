package com.sasanka.msp.models;

import com.sasanka.msp.common.MSPConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * All attributes corresponding to a message are set and retrieved using this model.
 */
public class MSPChatModel {

    private JSONObject mChatJson;
    private int mServerUpdateStatus = 0;

    public MSPChatModel() {
        mChatJson = new JSONObject();
    }

    private Object get(String key) {
        try {
            return mChatJson.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void put(String key, Object value) {
        try {
            mChatJson.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return (String) get(MSPConstants.ServerKeys.message);
    }

    public String getProductId() {
        return (String) get(MSPConstants.ServerKeys.productId);
    }

    public String getSenderId() {
        return (String) get(MSPConstants.ServerKeys.senderId);
    }

    public String getSenderType() {
        return (String) get(MSPConstants.ServerKeys.senderType);
    }

    public String getSenderFullName() {
        return (String) get(MSPConstants.ServerKeys.senderName);
    }

    public boolean isSentByVendor() {
        return getSenderType().equals(MSPConstants.ServerKeys.vendor);
    }

    public JSONArray getReceivers() {
        return (JSONArray) get(MSPConstants.ServerKeys.receivers);
    }

    public long getCreatedAt() {
        return (long) get(MSPConstants.ServerKeys.createdAt);
    }

    public long getCreatedTime() {
        return (long) get(MSPConstants.ServerKeys.createdTime);
    }

    public boolean isUpdatedToServer() {
        return mServerUpdateStatus == 1;
    }

    public String toString() {
        return mChatJson.toString();
    }

    public void setCreatedAt(long createdAt) {
        put(MSPConstants.ServerKeys.createdAt, createdAt);
    }

    public void setCreatedTime(long createdTime) {
        put(MSPConstants.ServerKeys.createdTime, createdTime);
    }

    public void setMessage(String message) {
        put(MSPConstants.ServerKeys.message, message);
    }

    public void setProductId(String productId) {
        put(MSPConstants.ServerKeys.productId, productId);
    }

    public void setSenderId(String senderId) {
        put(MSPConstants.ServerKeys.senderId, senderId);
    }

    public void setSenderType(String senderType) {
        put(MSPConstants.ServerKeys.senderType, senderType);
    }

    public void setSenderFullName(String fullName) {
        put(MSPConstants.ServerKeys.senderName, fullName);
    }

    public void setReceivers(JSONArray receivers) {
        put(MSPConstants.ServerKeys.receivers, receivers);
    }

    public void setServerUpdateStatus(int serverUpdateStatus) {
        mServerUpdateStatus = serverUpdateStatus;
    }

}
