package com.sasanka.msp.models;

/**
 * Model for a bidder. Used by Vendor.
 */
public class MSPUserModel {

    private String mObjectId, mFullName;

    public String getFullName() {
        return mFullName;
    }

    public String getObjectId() {
        return mObjectId;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public void setUserObjectId(String objectId) {
        mObjectId = objectId;
    }

}
