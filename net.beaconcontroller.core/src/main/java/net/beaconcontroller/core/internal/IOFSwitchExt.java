/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core.internal;

import org.openflow.protocol.statistics.OFDescriptionStatistics;

import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.OFSwitchState;

/**
 *  This interface is an extension of IOFSwitch, but is only used internally
 *  for access to members that should not be exposed externally to other
 *  packages.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface IOFSwitchExt extends IOFSwitch {
    /**
     * Change state of the switch
     * @param state
     */
    public void transitionToState(OFSwitchState state);

    /**
     * Sets the description statistics received from the switch
     * @param descriptionStatistics
     */
    public void setDescriptionStatistics(OFDescriptionStatistics descriptionStatistics);
}
