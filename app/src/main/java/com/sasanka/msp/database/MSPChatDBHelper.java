package com.sasanka.msp.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.models.MSPUserModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class takes care of operations related to SQLite database for persistent storage.
 */
public class MSPChatDBHelper extends SQLiteOpenHelper {

    private static MSPChatDBHelper sInstance = null;

    private static final String DATABASE_NAME               =   "chat_db";

    /**
     * Properties of ChatModel are added into this table.
     */
    private static final String DATABASE_MAIN_TABLE         =   "chat_table";

    /**
     * This table stores the last sync dates wrt to product and bidder(in case of vendor) so that
     * updates can be fetched for those messages which are after the last sync date.
     */
    private static final String DATABASE_LAST_SYNC_TABLE    =   "chat_last_sync";

    /**
     * This table holds the list of bidders available for a vendor.
     */
    private static final String DATABASE_BIDDERS_TABLE      =   "chat_bidders";

    private static final int DATABASE_VERSION               =   1;

    private static final String COL_ID                      =   "_id";

    private static final String COL_CREATED_TIME            =   "createdTime";
    private static final String COL_MESSAGE                 =   "message";
    private static final String COL_PRODUCT_ID              =   "productId";
    private static final String COL_SENDER_ID               =   "senderId";
    private static final String COL_SENDER_TYPE             =   "senderType";
    private static final String COL_SENDER_NAME             =   "senderName";
    private static final String COL_RECEIVERS               =   "receivers";
    private static final String COL_IS_UPDATED_TO_SERVER    =   "isUpdatedToServer";

    private static final String COL_RECEIVER_ID             =   "receiverId";
    private static final String COL_LAST_SYNC_TIME          =   "lastSyncTime";

    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();

    /**
     * Callback for adding messages as a batch operation.
     */
    public interface MSAddMessagesCallback {
        /**
         * Called when the operation is finished.
         * @param e not null in case of failure.
         */
        void onMessagesAdded(Exception e);
    }

    private MSPChatDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MSPChatDBHelper getSharedInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MSPChatDBHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createMainTableQuery = "CREATE TABLE IF NOT EXISTS "
                + DATABASE_MAIN_TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CREATED_TIME + " INTEGER NOT NULL, "
                + COL_MESSAGE + " TEXT, "
                + COL_PRODUCT_ID + " TEXT, "
                + COL_SENDER_ID + " TEXT, "
                + COL_SENDER_TYPE + " TEXT, "
                + COL_SENDER_NAME + " TEXT, "
                + COL_RECEIVERS + " TEXT, "
                + COL_IS_UPDATED_TO_SERVER + " INTEGER DEFAULT 0, "
                + "UNIQUE (" + COL_CREATED_TIME + ") ON CONFLICT REPLACE)";
        String createIndexQuery = "CREATE INDEX createdAtIndex ON " + DATABASE_MAIN_TABLE + " ("
                + COL_CREATED_TIME + ")";
        String createLastSyncTableQuery = "CREATE TABLE IF NOT EXISTS "
                + DATABASE_LAST_SYNC_TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECEIVER_ID + " TEXT, "
                + COL_PRODUCT_ID + " TEXT, "
                + COL_LAST_SYNC_TIME + " INTEGER, "
                + "UNIQUE (" + COL_RECEIVER_ID + ", " + COL_PRODUCT_ID + ") ON CONFLICT REPLACE)";
        String createBiddersTableQuery = "CREATE TABLE IF NOT EXISTS "
                + DATABASE_BIDDERS_TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PRODUCT_ID + " TEXT, "
                + COL_SENDER_ID + " TEXT, "
                + COL_SENDER_NAME + " TEXT, "
                + "UNIQUE (" + COL_PRODUCT_ID + ", " + COL_SENDER_ID + ") ON CONFLICT IGNORE)";

        sqLiteDatabase.execSQL(createMainTableQuery);
        sqLiteDatabase.execSQL(createIndexQuery);
        sqLiteDatabase.execSQL(createLastSyncTableQuery);
        sqLiteDatabase.execSQL(createBiddersTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Used to add a message to main table.
     * @param chatModel model to be added.
     * @return id of the row created.
     * @throws Exception not null in case of failure.
     */
    public long addMessage(MSPChatModel chatModel) throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(COL_CREATED_TIME, chatModel.getCreatedTime());
        cv.put(COL_MESSAGE, chatModel.getMessage());
        cv.put(COL_PRODUCT_ID, chatModel.getProductId());
        cv.put(COL_SENDER_ID, chatModel.getSenderId());
        cv.put(COL_SENDER_TYPE, chatModel.getSenderType());
        cv.put(COL_SENDER_NAME, chatModel.getSenderFullName());
        if (chatModel.getReceivers() != null)
            cv.put(COL_RECEIVERS, chatModel.getReceivers().toString());
        cv.put(COL_IS_UPDATED_TO_SERVER, chatModel.isUpdatedToServer() ? 1 : 0);

        long rowId = getWritableDatabase().insert(DATABASE_MAIN_TABLE, null, cv);
        if (rowId != -1)
            sendContentChangedNotification();
        return rowId;
    }

    /**
     * Used to add messages to main table as a batch operation.
     * @param chatModels models to be added.
     * @param callback MSAddMessagesCallback.
     */
    public void addMessages(List<MSPChatModel> chatModels,
                                 MSAddMessagesCallback callback) {
        MSPChatModel[] models = new MSPChatModel[chatModels.size()];
        chatModels.toArray(models);
        new AddMessagesInBackground(callback).execute(models);
    }

    private class AddMessagesInBackground extends AsyncTask<MSPChatModel, Void, Exception> {

        MSAddMessagesCallback callback;

        public AddMessagesInBackground(MSAddMessagesCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Exception doInBackground(MSPChatModel... models) {
            String sql = "INSERT OR REPLACE INTO " + DATABASE_MAIN_TABLE + " (" +
                    COL_CREATED_TIME + ", " + COL_MESSAGE + ", " + COL_PRODUCT_ID + ", " +
                    COL_SENDER_ID + ", " + COL_SENDER_TYPE + ", " + COL_SENDER_NAME + ", " +
                    COL_RECEIVERS + ", " + COL_IS_UPDATED_TO_SERVER + ") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransactionNonExclusive();
            try {
                SQLiteStatement sqliteStatement = db.compileStatement(sql);
                for (MSPChatModel chatModel : models) {
                    sqliteStatement.bindLong(1, chatModel.getCreatedTime());
                    sqliteStatement.bindString(2, chatModel.getMessage());
                    sqliteStatement.bindString(3, chatModel.getProductId());
                    sqliteStatement.bindString(4, chatModel.getSenderId());
                    sqliteStatement.bindString(5, chatModel.getSenderType());
                    sqliteStatement.bindString(6, chatModel.getSenderFullName());
                    if (chatModel.getReceivers() != null)
                        sqliteStatement.bindString(7, chatModel.getReceivers().toString());
                    if (chatModel.isUpdatedToServer())
                        sqliteStatement.bindLong(8, 1);
                    else
                        sqliteStatement.bindLong(8, 0);
                    sqliteStatement.execute();
                    sqliteStatement.clearBindings();
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                return e;
            } finally {
                db.endTransaction();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e == null) {
                sendContentChangedNotification();
                callback.onMessagesAdded(null);
            } else {
                callback.onMessagesAdded(e);
            }
        }
    }

    /**
     * Sending local broadcast so that the chat list adapter is refreshed.
     */
    protected void sendContentChangedNotification() {
        Intent i = new Intent(MSPConstants.LocalBroadcast.messageLocalBroadcast);
        LocalBroadcastManager.getInstance(MSPApplication.getContext()).sendBroadcast(i);
    }

    /**
     * Gets all the messages corresponding to a productId and senderId.
     * @param productId id of product.
     * @param senderId not null in case of vendor.
     * @return List of chatModels.
     * @throws Exception not null in case of failure.
     */
    public List<MSPChatModel> getAllMessages(@NonNull String productId, @Nullable String senderId)
            throws Exception {
        String selection;
        if (senderId == null)
            selection = COL_PRODUCT_ID + " =? " +
                    "AND (" + COL_SENDER_ID + " LIKE '%" + mUser.getObjectId() + "%' " +
                    "OR " + COL_RECEIVERS + " LIKE '%" + mUser.getObjectId() + "%')";
        else
            selection = COL_PRODUCT_ID + " =? " +
                    "AND ((" + COL_SENDER_ID + " LIKE '%" + mUser.getObjectId() + "%' " +
                    "AND " + COL_RECEIVERS + " LIKE '%" + senderId + "%') " +
                    "OR (" + COL_RECEIVERS + " LIKE '%" + mUser.getObjectId() + "%' " +
                    "AND " + COL_SENDER_ID + " LIKE '%" + senderId + "%'))";
        Cursor cursor = getReadableDatabase().query(
                DATABASE_MAIN_TABLE,
                null,
                selection,
                new String[]{productId},
                null,
                null,
                COL_CREATED_TIME);

        List<MSPChatModel> chatModels = createChatModels(cursor);
        cursor.close();
        return chatModels;
    }

    /**
     * Helper method to create list of chat models from cursor.
     * @param cursor Cursor.
     * @return list of chat models.
     */
    private List<MSPChatModel> createChatModels(Cursor cursor) {
        final int colIndexCreatedAt = 1;
        final int colIndexMessage = 2;
        final int colIndexProductId = 3;
        final int colIndexSenderId = 4;
        final int colIndexSenderType = 5;
        final int colIndexSenderName = 6;
        final int colIndexReceivers = 7;
        final int colIndexServerUpdateStatus = 8;
        List<MSPChatModel> chatModels = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            MSPChatModel chatModel = new MSPChatModel();
            chatModel.setCreatedTime(cursor.getLong(colIndexCreatedAt));
            chatModel.setMessage(cursor.getString(colIndexMessage));
            chatModel.setProductId(cursor.getString(colIndexProductId));
            chatModel.setSenderId(cursor.getString(colIndexSenderId));
            chatModel.setSenderType(cursor.getString(colIndexSenderType));
            chatModel.setSenderFullName(cursor.getString(colIndexSenderName));
            try {
                if (cursor.getString(colIndexReceivers) != null)
                    chatModel.setReceivers(new JSONArray(cursor.getString(colIndexReceivers)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chatModel.setServerUpdateStatus(cursor.getInt(colIndexServerUpdateStatus));
            chatModels.add(chatModel);
        }
        return chatModels;
    }

    /**
     * Gets the list of messages which are not yet updated to server.
     * @return list of chat models.
     * @throws Exception not null in case of failure.
     */
    public List<MSPChatModel> getAllMessagesWhichAreNotUpdatedToServer() throws Exception {
        Cursor cursor = getReadableDatabase().query(
                DATABASE_MAIN_TABLE,
                null,
                COL_IS_UPDATED_TO_SERVER + " = " + 0,
                null,
                null,
                null,
                COL_CREATED_TIME);

        List<MSPChatModel> chatModels = createChatModels(cursor);
        cursor.close();
        return chatModels;
    }

    /**
     * Used to update the server update status as 1 wrt to a chat model.
     * @param chatModel model whose status is to be updated.
     * @return boolean.
     * @throws Exception not null in case of failure.
     */
    public boolean onUpdateToServerSuccess(MSPChatModel chatModel) throws Exception {
        chatModel.setServerUpdateStatus(1);
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_UPDATED_TO_SERVER, 1);
        int numberOfRows = getWritableDatabase().update(
                DATABASE_MAIN_TABLE,
                cv,
                COL_SENDER_ID + " =? AND " + COL_CREATED_TIME + " = " + chatModel.getCreatedTime(),
                new String[]{mUser.getObjectId()});
        if (numberOfRows == 1) {
            sendContentChangedNotification();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Used to update the last sync time wrt to productId and receiverId.
     * @param productId id of product.
     * @param receiverId in case of vendor pass the receiverId else pass "".
     * @param createdAt new lastSyncTime.
     * @return id of the row updated.
     * @throws Exception
     */
    public long updateLastSyncTime(@NonNull String productId, @NonNull String receiverId,
                                   long createdAt) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PRODUCT_ID, productId);
        contentValues.put(COL_RECEIVER_ID, receiverId);
        contentValues.put(COL_LAST_SYNC_TIME, createdAt);
        return getWritableDatabase().insert(DATABASE_LAST_SYNC_TABLE, null, contentValues);
    }

    /**
     * Gives the lastSyncTime wrt to productId and receiverId.
     * @param productId id of product.
     * @param receiverId in case of vendor pass the receiverId else pass null.
     * @return lastSyncTime
     * @throws Exception
     */
    public long getLastSyncTime(@NonNull String productId, @Nullable String receiverId) throws Exception {
        Cursor cursor = getReadableDatabase().query(
                DATABASE_LAST_SYNC_TABLE,
                null,
                COL_PRODUCT_ID + " =? AND " + COL_RECEIVER_ID + " =? ",
                new String[]{productId, receiverId != null ? receiverId : ""},
                null,
                null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            long lastSyncTime = cursor.getLong(3);
            cursor.close();
            return lastSyncTime;
        }
        return new Date(0).getTime();
    }

    /**
     * Used by vendors to add a new bidder to bidders table
     * @param productId id of product.
     * @param userModel userModel which contains details of bidder.
     * @return id of the row created.
     * @throws Exception
     */
    public long addBidder(String productId, MSPUserModel userModel) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PRODUCT_ID, productId);
        contentValues.put(COL_SENDER_ID, userModel.getObjectId());
        contentValues.put(COL_SENDER_NAME, userModel.getFullName());
        long rowId = getWritableDatabase().insert(DATABASE_BIDDERS_TABLE, null, contentValues);
        return rowId;
    }

    /**
     * Gets the list of bidders from bidders table wrt to a productId.
     * @param productId id of product.
     * @return list of userModels.
     */
    public List<MSPUserModel> getBidders(String productId) {
        Cursor cursor = getReadableDatabase().query(
                DATABASE_BIDDERS_TABLE,
                null,
                COL_PRODUCT_ID + " =? ",
                new String[]{productId},
                null,
                null,
                null);

        List<MSPUserModel> userModels = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                MSPUserModel userModel = new MSPUserModel();
                userModel.setUserObjectId(cursor.getString(2));
                userModel.setFullName(cursor.getString(3));
                userModels.add(userModel);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return userModels;
    }

    /**
     * Method used to delete all rows from all tables.
     * Called during logout.
     */
    public void reset() {
        getWritableDatabase().delete(DATABASE_MAIN_TABLE, null, null);
        getWritableDatabase().delete(DATABASE_LAST_SYNC_TABLE, null, null);
        getWritableDatabase().delete(DATABASE_BIDDERS_TABLE, null, null);
    }
}
