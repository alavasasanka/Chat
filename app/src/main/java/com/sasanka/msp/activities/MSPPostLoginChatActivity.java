package com.sasanka.msp.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sasanka.msp.common.MSPAlertHelper;
import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.models.MSPProducts;
import com.sasanka.msp.common.MSPUtils;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.managers.MSPChatManager;
import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.R;

import org.json.JSONArray;

/**
 * This activity handles the chat feature.
 */
public class MSPPostLoginChatActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mChatText;
    private MSPChatManager mChatManager = MSPChatManager.getSharedInstance();
    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();
    private String mReceiverId, mReceiverName, mProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_login_chat);
        mReceiverId = getIntent().getStringExtra(MSPConstants.BundleKeys.receiverId);
        mReceiverName = getIntent().getStringExtra(MSPConstants.BundleKeys.receiverName);
        mProductId = getIntent().getStringExtra(MSPConstants.BundleKeys.productId);
        MSPProducts products = MSPProducts.getInstance();
        if (!mUser.isVendor())
            setTitle(products.getList().get(Integer.valueOf(mProductId)).get("name"));
        else
            setTitle(products.getList().get(Integer.valueOf(mProductId)).get("name")
                    + " - '" + mReceiverName + "'");
        mChatManager.setProductId(mProductId);
        mChatManager.setReceiverId(mReceiverId);
        mChatText = (EditText) findViewById(R.id.sendMessageText);
        Button sendButton = (Button) findViewById(R.id.sendMessageButton);
        final ListView chatListView = (ListView) findViewById(R.id.chatListView);
        chatListView.setDivider(null);
        chatListView.setDividerHeight(0);
        chatListView.setBackgroundResource(R.drawable.bricks_background);
        chatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        chatListView.setStackFromBottom(true);
        sendButton.setOnClickListener(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        chatListView.setAdapter(mChatManager.getAdapter(this));
        mChatManager.reloadAdapter(this, mProductId, mReceiverId);
        if (mChatManager.getAdapter(this).getCount() == 0) {
            refresh();
        } else {
            mChatManager.fetchAndApplyMessagesFromServer(this, mProductId, mReceiverId, null);
        }
        mChatManager.resendFailedMessages(this);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MSPApplication.onResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MSPApplication.onPaused();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_login_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        if (!MSPUtils.isConnectedToNetwork(this)) {
            Toast.makeText(this, "Please check your network connection and try again",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isFinishing())
            MSPAlertHelper.showProgressDialog(MSPPostLoginChatActivity.this, "Refreshing...");
        mChatManager.fetchAndApplyMessagesFromServer(this, mProductId, mReceiverId,
                new MSPChatManager.OnFetchAndApplyMessagesCallback() {
                    @Override
                    public void onFetchAndApplyCompleted(Exception e) {
                        if (!isFinishing())
                            MSPAlertHelper.dismissProgressDialog();
                    }
                });
        mChatManager.resendFailedMessages(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendMessageButton:
                if (TextUtils.isEmpty(mChatText.getText()))
                    return;
                MSPChatModel chatModel = new MSPChatModel();
                chatModel.setMessage(mChatText.getText().toString());
                chatModel.setProductId(mProductId);
                chatModel.setSenderId(mUser.getObjectId());
                chatModel.setSenderType(mUser.getUserType());
                chatModel.setSenderFullName(mUser.getFullName());
                chatModel.setCreatedTime(System.currentTimeMillis());
                if (mReceiverId != null) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(mReceiverId);
                    chatModel.setReceivers(jsonArray);
                }
                mChatManager.sendMessage(this, chatModel, new MSPChatManager.OnSendMessageCallback() {
                    @Override
                    public void onMessageAddedToLocalDB(Exception e) {
                        mChatText.setText("");
                    }

                    @Override
                    public void onMessageSentToServer(Exception e) {
                        if (e == null) {
                            mChatManager.reloadAdapter(MSPPostLoginChatActivity.this, mProductId, mReceiverId);
                        }
                    }
                });
                break;
        }
    }
}
