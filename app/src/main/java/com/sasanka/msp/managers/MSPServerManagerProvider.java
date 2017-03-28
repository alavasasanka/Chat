package com.sasanka.msp.managers;

import com.sasanka.msp.managers.Interfaces.MSPServerInterface;

/**
 * This class provides an instance of server manager.
 */
public class MSPServerManagerProvider {

    public static MSPServerInterface getProvider() {
        return MSPParseManager.getSharedInstance();
    }

}
