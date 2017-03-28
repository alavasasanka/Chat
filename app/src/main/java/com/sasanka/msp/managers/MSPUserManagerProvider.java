package com.sasanka.msp.managers;

import com.sasanka.msp.managers.Interfaces.MSPUserInterface;

/**
 * This class provides an instance of user manager.
 */
public class MSPUserManagerProvider {

    public static MSPUserInterface getProvider() {
        return MSPUserManager.getSharedInstance();
    }

}
