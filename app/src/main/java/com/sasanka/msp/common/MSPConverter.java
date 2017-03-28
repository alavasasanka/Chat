package com.sasanka.msp.common;

import com.parse.ParseObject;
import com.sasanka.msp.models.MSPChatModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class to convert any object to chatModel.
 */
public class MSPConverter {

    public static MSPChatModel toChatModel(ParseObject parseObject) {
        MSPChatModel chatModel = new MSPChatModel();
        chatModel.setCreatedAt(parseObject.getCreatedAt().getTime());
        chatModel.setCreatedTime(parseObject.getNumber(MSPConstants.ServerKeys.createdTime).longValue());
        chatModel.setMessage(parseObject.getString(MSPConstants.ServerKeys.message));
        chatModel.setProductId(parseObject.getString(MSPConstants.ServerKeys.productId));
        chatModel.setSenderId(parseObject.getString(MSPConstants.ServerKeys.senderId));
        chatModel.setSenderFullName(parseObject.getString(MSPConstants.ServerKeys.senderName));
        chatModel.setSenderType(parseObject.getString(MSPConstants.ServerKeys.senderType));
        if (parseObject.containsKey(MSPConstants.ServerKeys.receivers))
            chatModel.setReceivers(parseObject.getJSONArray(MSPConstants.ServerKeys.receivers));
        chatModel.setServerUpdateStatus(1);
        return chatModel;
    }

    public static MSPChatModel toChatModel(JSONObject jsonObject) {
        MSPChatModel chatModel = new MSPChatModel();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = null;
            try {
                date = dateFormat.parse(jsonObject.getString(MSPConstants.ServerKeys.createdAt));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null)
                chatModel.setCreatedAt(date.getTime() + 19800000);
            chatModel.setCreatedTime(jsonObject.getLong(MSPConstants.ServerKeys.createdTime));
            chatModel.setMessage(jsonObject.getString(MSPConstants.ServerKeys.message));
            chatModel.setProductId(jsonObject.getString(MSPConstants.ServerKeys.productId));
            chatModel.setSenderId(jsonObject.getString(MSPConstants.ServerKeys.senderId));
            chatModel.setSenderType(jsonObject.getString(MSPConstants.ServerKeys.senderType));
            chatModel.setSenderFullName(jsonObject.getString(MSPConstants.ServerKeys.senderName));
            if (jsonObject.has(MSPConstants.ServerKeys.receivers))
                chatModel.setReceivers(jsonObject.getJSONArray(MSPConstants.ServerKeys.receivers));
            chatModel.setServerUpdateStatus(1);
            return chatModel;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
