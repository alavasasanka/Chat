package com.sasanka.msp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sasanka.msp.adapters.MSPUserListViewAdapter;
import com.sasanka.msp.common.MSPAlertHelper;
import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.common.MSPConverter;
import com.sasanka.msp.common.MSPUtils;
import com.sasanka.msp.database.MSPChatDBHelper;
import com.sasanka.msp.fragments.MSPMapFragment;
import com.sasanka.msp.fragments.MSPProductListFragment;
import com.sasanka.msp.fragments.MSPUsersListFragment;
import com.sasanka.msp.managers.Interfaces.MSPServerInterface;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.managers.MSPServerManagerProvider;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.models.MSPChatModel;
import com.sasanka.msp.models.MSPUserModel;
import com.sasanka.msp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity handles the post-login flow except the chat feature.
 */
public class MSPPostLoginHomeActivity extends AppCompatActivity implements
        MSPProductListFragment.OnFragmentInteractionListener,
        MSPMapFragment.OnFragmentInteractionListener,
        MSPUsersListFragment.OnFragmentInteractionListener {

    private static final String TAG_MAP_FRAGMENT  = "com.sasanka.msp.mapfragment";
    private static final String TAG_PRODUCTS_LIST_FRAGMENT = "com.sasanka.msp.productslistfragment";
    private static final String TAG_USERS_LIST_FRAGMENT = "com.sasanka.msp.userslistfragment";

    private Location mCurrentLocation;
    private MSPServerInterface mServer = MSPServerManagerProvider.getProvider();
    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();
    private String mProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_login_home);
        if (!mServer.isRegisteredForPushNotifications())
            mServer.registerForPushNotifications();
        if (!mServer.isUserObjectIdUpdatedToInstallation())
            mServer.updateInstallation();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container,
                    MSPMapFragment.newInstance(), TAG_MAP_FRAGMENT)
                    .commit();
            setTitle("Current Location");
        } else {
            mProductId = savedInstanceState.getString(MSPConstants.BundleKeys.productId);
        }
        if (getIntent() != null && getIntent().hasExtra(MSPConstants.PushNotification.payload)) {
            try {
                Intent newIntent = new Intent(this, MSPPostLoginChatActivity.class);
                JSONObject jsonObject = new JSONObject(getIntent().getExtras().getString(
                        MSPConstants.PushNotification.payload));
                MSPChatModel chatModel = MSPConverter.toChatModel(jsonObject);
                newIntent.putExtra(MSPConstants.BundleKeys.productId, chatModel.getProductId());
                if (!chatModel.isSentByVendor()) {
                    newIntent.putExtra(MSPConstants.BundleKeys.receiverId, chatModel.getSenderId());
                    newIntent.putExtra(MSPConstants.BundleKeys.receiverName, chatModel.getSenderFullName());
                }
                startActivity(newIntent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        getMenuInflater().inflate(R.menu.menu_post_login_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_next) {
            if (getSupportFragmentManager().findFragmentByTag(TAG_MAP_FRAGMENT) != null) {
                if (getCurrentLocation() != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,
                            MSPProductListFragment.newInstance(), TAG_PRODUCTS_LIST_FRAGMENT).commit();
                    setTitle("Products");
                    item.setVisible(false);
                } else {
                    Toast.makeText(this, "Please wait... Current location not yet identified...",
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        } else if (id == R.id.action_logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setMessage("Are you sure?")
                    .setCancelable(true)
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mUser.logout(MSPPostLoginHomeActivity.this);
                            Intent intent = new Intent(MSPPostLoginHomeActivity.this,
                                    MSPSplashScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Later", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            String tag = fm.getBackStackEntryAt(count - 1).getName();
            if (tag.equals(TAG_USERS_LIST_FRAGMENT)) {
                setTitle("Products");
            }
        }
        super.onBackPressed();
    }

    private void setCurrentLocation(Location location) {
        mCurrentLocation = location;
    }

    private Location getCurrentLocation() {
        return mCurrentLocation;
    }

    @Override
    public void onCurrentLocationChanged(Location newCurrentLocation) {
        if (getCurrentLocation() == null && newCurrentLocation != null) {
            mUser.updateUserLocation(newCurrentLocation);
        }
        setCurrentLocation(newCurrentLocation);
    }

    @Override
    public void onCallToActionButtonClicked(String productId) {
        mProductId = productId;
        if (!mUser.isVendor()) {
            Intent intent = new Intent(this, MSPPostLoginChatActivity.class);
            intent.putExtra(MSPConstants.BundleKeys.productId, productId);
            startActivity(intent);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MSPUsersListFragment.newInstance(), TAG_USERS_LIST_FRAGMENT)
                    .addToBackStack(TAG_USERS_LIST_FRAGMENT)
                    .commit();
            setTitle("Users");
        }
    }

    @Override
    public void onUsersListViewCreated(MSPUsersListFragment fragment) {
        final MSPUserListViewAdapter adapter = fragment.getAdapter();
        final MSPChatDBHelper helper = MSPChatDBHelper.getSharedInstance(this);
        final List<MSPUserModel> userModels = helper.getBidders(mProductId);
        adapter.updateAdapterData(userModels);
        List<String> userIds = new ArrayList<>();
        for (MSPUserModel userModel : userModels) {
            userIds.add(userModel.getObjectId());
        }
        if (!MSPUtils.isConnectedToNetwork(this)) {
            Toast.makeText(this, "Please check your network connection and try again",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (userModels.size() == 0 && !isFinishing())
            MSPAlertHelper.showProgressDialog(MSPPostLoginHomeActivity.this, "Fetching users list...");
        mServer.fetchUsersList(mProductId, userIds, new MSPServerInterface.OnFetchUsersListener() {
            @Override
            public void onUsersFetched(List<MSPUserModel> newUserModels, Exception e) {
                if (userModels.size() == 0 && !isFinishing())
                    MSPAlertHelper.dismissProgressDialog();
                if (e == null) {
                    for (MSPUserModel userModel : newUserModels) {
                        try {
                            helper.addBidder(mProductId, userModel);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    adapter.updateAdapterData(helper.getBidders(mProductId));
                }
            }
        });
    }

    @Override
    public void onItemClicked(MSPUserModel userModel) {
        Intent intent = new Intent(this, MSPPostLoginChatActivity.class);
        intent.putExtra(MSPConstants.BundleKeys.productId, mProductId);
        intent.putExtra(MSPConstants.BundleKeys.receiverId, userModel.getObjectId());
        intent.putExtra(MSPConstants.BundleKeys.receiverName, userModel.getFullName());
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MSPConstants.BundleKeys.productId, mProductId);
    }
}
