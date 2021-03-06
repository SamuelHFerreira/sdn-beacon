/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.learningswitch;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.packet.Ethernet;
import net.beaconcontroller.util.LongShortHopscotchHashMap;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - 04/04/10
 */
public class LearningSwitch implements IOFMessageListener, IOFSwitchListener {
    protected static Logger log = LoggerFactory.getLogger(LearningSwitch.class);
    protected IBeaconProvider beaconProvider;
    protected Map<IOFSwitch, LongShortHopscotchHashMap> macTables =
        new HashMap<IOFSwitch, LongShortHopscotchHashMap>();

    public void startUp() {
        log.trace("Starting");
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFSwitchListener(this);
    }

    public void shutDown() {
        log.trace("Stopping");
        beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.removeOFSwitchListener(this);
    }

    public Command receive(IOFSwitch sw, OFMessage msg) throws IOException {
        OFPacketIn pi = (OFPacketIn) msg;
        LongShortHopscotchHashMap macTable = macTables.get(sw);
        if (macTable == null) {
            macTable = new LongShortHopscotchHashMap();
            macTables.put(sw, macTable);
        }

        // Build the Match
        OFMatch match = OFMatch.load(pi.getPacketData(), pi.getInPort());

        byte[] dlDst = match.getDataLayerDestination();
        byte[] dlSrc = match.getDataLayerSource();
        long dlDstLong = Ethernet.toLong(dlDst);
        long dlSrcLong = Ethernet.toLong(dlSrc);
        int bufferId = pi.getBufferId();
        short outPort = -1;

        // if the src is not multicast, learn it
        if ((dlSrc[0] & 0x1) == 0 && dlSrcLong != 0) {
            if (!macTable.contains(dlSrcLong) ||
                    macTable.get(dlSrcLong) != pi.getInPort()) {
                macTable.put(dlSrcLong, pi.getInPort());
            }
        }

        // if the destination is not multicast, look it up
        if ((dlDst[0] & 0x1) == 0 && dlDstLong != 0) {
            outPort = macTable.get(dlDstLong);
        }

        // push a flow mod if we know where the destination lives
        if (outPort != -1) {
            // don't send out the port it came in
            if (outPort == pi.getInPort()) {
                return Command.CONTINUE;
            }

            OFActionOutput action = new OFActionOutput(outPort);
            OFFlowMod fm = new OFFlowMod()
                .setBufferId(bufferId)
                .setCommand(OFFlowMod.OFPFC_ADD)
                .setIdleTimeout((short) 5)
                .setMatch(match)
                .setActions(Collections.singletonList((OFAction)action));
            sw.getOutputStream().write(fm);
        }

        // If the destination is unknown or the OFPacketIn was not buffered
        // then send an OFPacketOut.
        if (outPort == -1 || bufferId == OFPacketOut.BUFFER_ID_NONE) {
            OFActionOutput action = new OFActionOutput(
                    (short) ((outPort == -1) ? OFPort.OFPP_FLOOD.getValue()
                            : outPort));

            OFPacketOut po = new OFPacketOut()
                .setBufferId(bufferId)
                .setInPort(pi.getInPort())
                .setActions(Collections.singletonList((OFAction)action));

            // Set the packet data if it is included in the Packet In
            if (bufferId == OFPacketOut.BUFFER_ID_NONE) {
                po.setPacketData(pi.getPacketData());
            }

            sw.getOutputStream().write(po);
        }
        return Command.CONTINUE;
    }

    public String getName() {
        return "switch";
    }

    @Override
    public void addedSwitch(IOFSwitch sw) {
    }

    @Override
    public void removedSwitch(IOFSwitch sw) {
        if (macTables.remove(sw) != null)
            log.debug("Removed l2 table for {}", sw);
    }

    /**
     * @return the macTables
     */
    public Map<IOFSwitch, LongShortHopscotchHashMap> getMacTables() {
        return macTables;
    }

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }
}
