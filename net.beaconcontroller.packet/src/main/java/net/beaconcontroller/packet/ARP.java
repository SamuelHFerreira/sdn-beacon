/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class ARP extends BasePacket {
    public static short HW_TYPE_ETHERNET = 0x1;

    public static short PROTO_TYPE_IP = 0x800;

    public static short OP_REQUEST = 0x1;
    public static short OP_REPLY = 0x2;

    protected short hardwareType;
    protected short protocolType;
    protected byte hardwareAddressLength;
    protected byte protocolAddressLength;
    protected short opCode;
    protected byte[] senderHardwareAddress;
    protected byte[] senderProtocolAddress;
    protected byte[] targetHardwareAddress;
    protected byte[] targetProtocolAddress;

    /**
     * @return the hardwareType
     */
    public short getHardwareType() {
        return hardwareType;
    }

    /**
     * @param hardwareType the hardwareType to set
     */
    public ARP setHardwareType(short hardwareType) {
        this.hardwareType = hardwareType;
        return this;
    }

    /**
     * @return the protocolType
     */
    public short getProtocolType() {
        return protocolType;
    }

    /**
     * @param protocolType the protocolType to set
     */
    public ARP setProtocolType(short protocolType) {
        this.protocolType = protocolType;
        return this;
    }

    /**
     * @return the hardwareAddressLength
     */
    public byte getHardwareAddressLength() {
        return hardwareAddressLength;
    }

    /**
     * @param hardwareAddressLength the hardwareAddressLength to set
     */
    public ARP setHardwareAddressLength(byte hardwareAddressLength) {
        this.hardwareAddressLength = hardwareAddressLength;
        return this;
    }

    /**
     * @return the protocolAddressLength
     */
    public byte getProtocolAddressLength() {
        return protocolAddressLength;
    }

    /**
     * @param protocolAddressLength the protocolAddressLength to set
     */
    public ARP setProtocolAddressLength(byte protocolAddressLength) {
        this.protocolAddressLength = protocolAddressLength;
        return this;
    }

    /**
     * @return the opCode
     */
    public short getOpCode() {
        return opCode;
    }

    /**
     * @param opCode the opCode to set
     */
    public ARP setOpCode(short opCode) {
        this.opCode = opCode;
        return this;
    }

    /**
     * @return the senderHardwareAddress
     */
    public byte[] getSenderHardwareAddress() {
        return senderHardwareAddress;
    }

    /**
     * @param senderHardwareAddress the senderHardwareAddress to set
     */
    public ARP setSenderHardwareAddress(byte[] senderHardwareAddress) {
        this.senderHardwareAddress = senderHardwareAddress;
        return this;
    }

    /**
     * @return the senderProtocolAddress
     */
    public byte[] getSenderProtocolAddress() {
        return senderProtocolAddress;
    }

    /**
     * @param senderProtocolAddress the senderProtocolAddress to set
     */
    public ARP setSenderProtocolAddress(byte[] senderProtocolAddress) {
        this.senderProtocolAddress = senderProtocolAddress;
        return this;
    }

    /**
     * @return the targetHardwareAddress
     */
    public byte[] getTargetHardwareAddress() {
        return targetHardwareAddress;
    }

    /**
     * @param targetHardwareAddress the targetHardwareAddress to set
     */
    public ARP setTargetHardwareAddress(byte[] targetHardwareAddress) {
        this.targetHardwareAddress = targetHardwareAddress;
        return this;
    }

    /**
     * @return the targetProtocolAddress
     */
    public byte[] getTargetProtocolAddress() {
        return targetProtocolAddress;
    }

    /**
     * @param targetProtocolAddress the targetProtocolAddress to set
     */
    public ARP setTargetProtocolAddress(byte[] targetProtocolAddress) {
        this.targetProtocolAddress = targetProtocolAddress;
        return this;
    }

    @Override
    public byte[] serialize() {
        int length = 8 + (2 * (0xff & this.hardwareAddressLength))
                + (2 * (0xff & this.protocolAddressLength));
        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.putShort(this.hardwareType);
        bb.putShort(this.protocolType);
        bb.put(this.hardwareAddressLength);
        bb.put(this.protocolAddressLength);
        bb.putShort(this.opCode);
        bb.put(this.senderHardwareAddress, 0, 0xff & this.hardwareAddressLength);
        bb.put(this.senderProtocolAddress, 0, 0xff & this.protocolAddressLength);
        bb.put(this.targetHardwareAddress, 0, 0xff & this.hardwareAddressLength);
        bb.put(this.targetProtocolAddress, 0, 0xff & this.protocolAddressLength);
        return data;
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length) {
        ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        this.hardwareType = bb.getShort();
        this.protocolType = bb.getShort();
        this.hardwareAddressLength = bb.get();
        this.protocolAddressLength = bb.get();
        this.opCode = bb.getShort();
        this.senderHardwareAddress = new byte[0xff & this.hardwareAddressLength];
        bb.get(this.senderHardwareAddress, 0, this.senderHardwareAddress.length);
        this.senderProtocolAddress = new byte[0xff & this.protocolAddressLength];
        bb.get(this.senderProtocolAddress, 0, this.senderProtocolAddress.length);
        this.targetHardwareAddress = new byte[0xff & this.hardwareAddressLength];
        bb.get(this.targetHardwareAddress, 0, this.targetHardwareAddress.length);
        this.targetProtocolAddress = new byte[0xff & this.protocolAddressLength];
        bb.get(this.targetProtocolAddress, 0, this.targetProtocolAddress.length);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 13121;
        int result = super.hashCode();
        result = prime * result + hardwareAddressLength;
        result = prime * result + hardwareType;
        result = prime * result + opCode;
        result = prime * result + protocolAddressLength;
        result = prime * result + protocolType;
        result = prime * result + Arrays.hashCode(senderHardwareAddress);
        result = prime * result + Arrays.hashCode(senderProtocolAddress);
        result = prime * result + Arrays.hashCode(targetHardwareAddress);
        result = prime * result + Arrays.hashCode(targetProtocolAddress);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ARP))
            return false;
        ARP other = (ARP) obj;
        if (hardwareAddressLength != other.hardwareAddressLength)
            return false;
        if (hardwareType != other.hardwareType)
            return false;
        if (opCode != other.opCode)
            return false;
        if (protocolAddressLength != other.protocolAddressLength)
            return false;
        if (protocolType != other.protocolType)
            return false;
        if (!Arrays.equals(senderHardwareAddress, other.senderHardwareAddress))
            return false;
        if (!Arrays.equals(senderProtocolAddress, other.senderProtocolAddress))
            return false;
        if (!Arrays.equals(targetHardwareAddress, other.targetHardwareAddress))
            return false;
        if (!Arrays.equals(targetProtocolAddress, other.targetProtocolAddress))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ARP [hardwareType=" + hardwareType + ", protocolType="
                + protocolType + ", hardwareAddressLength="
                + hardwareAddressLength + ", protocolAddressLength="
                + protocolAddressLength + ", opCode=" + opCode
                + ", senderHardwareAddress="
                + Arrays.toString(senderHardwareAddress)
                + ", senderProtocolAddress="
                + Arrays.toString(senderProtocolAddress)
                + ", targetHardwareAddress="
                + Arrays.toString(targetHardwareAddress)
                + ", targetProtocolAddress="
                + Arrays.toString(targetProtocolAddress) + "]";
    }
}
