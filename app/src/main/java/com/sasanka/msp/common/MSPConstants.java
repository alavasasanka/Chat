package com.sasanka.msp.common;

/**
 * Class for handling constant values.
 */
public class MSPConstants {

    public static class PrefKeys {
        public static final String isRegisteredForPushNotifications = "isRegisteredForPushNotifications";
    }

    public static class ServerKeys {
        // User
        public static final String fullName         = "fullName";
        public static final String userType         = "type";
        public static final String location         = "location";
        public static final String vendor           = "Vendor";

        // Installation
        public static final String userObjectId     = "userObjectId";

        // ChatMessages
        public static final String message          = "message";
        public static final String productId        = "productId";
        public static final String senderId         = "senderId";
        public static final String senderType       = "senderType";
        public static final String senderName       = "senderName";
        public static final String receivers        = "receivers";
        public static final String createdAt        = "createdAt";
        public static final String createdTime      = "createdTime";

        // Class Names
        public static final String chatMessages     = "ChatMessages";

        // Cloud Code Function Names
        public static final String sendMessage      = "sendMessage";
    }

    public static class BundleKeys {
        public static final String productId        = "productId";
        public static final String receiverId       = "receiverId";
        public static final String receiverName     = "receiverName";
    }

    public static class LocalBroadcast {
        public static final String messageLocalBroadcast = "com.sasanka.msp.messagelocalbroadcast";
    }

    public static final class PushNotification {
        public static final String payload = "com.parse.Data";
        public static final String title = "title";
    }

}
