/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core.internal;

public interface IOrderName<T> {
    /**
     * Returns the name for this object used in the ordering String
     * @param obj
     * @return
     */
    public String get(T obj);
}
