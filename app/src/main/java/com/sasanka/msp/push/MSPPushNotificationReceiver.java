package com.sasanka.msp.push;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;
import com.sasanka.msp.activities.MSPPostLoginChatActivity;
import com.sasanka.msp.activities.MSPSplashScreen;
import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.common.MSPConverter;
import com.sasanka.msp.database.MSPChatDBHelper;
import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.models.MSPProducts;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * This class handles actions related to push notifications.
 */
public class MSPPushNotificationReceiver extends ParsePushBroadcastReceiver {
    /*
        Invoked when a push notification is received.
     */
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        try {
            JSONObject jsonObject = new JSONObject(intent.getExtras().getString(
                    MSPConstants.PushNotification.payload));
            MSPChatModel chatModel = MSPConverter.toChatModel(jsonObject);
            MSPChatDBHelper.getSharedInstance(context).addMessage(chatModel);

            jsonObject.put(MSPConstants.PushNotification.title,
                    MSPProducts.getInstance().getList().get(Integer.valueOf(chatModel.getProductId())).get("name")
                            + " : " + chatModel.getSenderFullName() + " says");
            intent.putExtra(MSPConstants.PushNotification.payload, jsonObject.toString());
            if (shouldShowPushNotification(context))
                super.onPushReceive(context, intent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        Invoked upon tapping a push notification.
     */
    @Override
    protected void onPushOpen(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Intent newIntent = new Intent(context, MSPSplashScreen.class);
        newIntent.putExtras(intent);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(newIntent);
    }

    /**
     * This helper method identifies whether the app is in background or when the app is in foreground
     * if it is not on chat activity.
     * @param context Context.
     * @return boolean.
     */
    private boolean shouldShowPushNotification(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return MSPApplication.isInBackground() ||
                !componentInfo.getClassName().equals(MSPPostLoginChatActivity.class.getName());
    }
}
