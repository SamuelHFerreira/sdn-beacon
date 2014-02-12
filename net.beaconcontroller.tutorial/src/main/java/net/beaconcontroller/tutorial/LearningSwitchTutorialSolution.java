/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.tutorial;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.packet.Ethernet;

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
 * Tutorial class used to teach how to build a simple layer 2 learning switch.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - 10/14/12
 */
public class LearningSwitchTutorialSolution implements IOFMessageListener, IOFSwitchListener {
    protected static Logger log = LoggerFactory.getLogger(LearningSwitchTutorialSolution.class);
    protected IBeaconProvider beaconProvider;
    protected Map<IOFSwitch, Map<Long,Short>> macTables =
        new HashMap<IOFSwitch, Map<Long,Short>>();

    public Command receive(IOFSwitch sw, OFMessage msg) throws IOException {
        initMACTable(sw);
        OFPacketIn pi = (OFPacketIn) msg;

        /**
         * This is the basic flood-based forwarding that is enabled.
         */
        //forwardAsHub(sw, pi);

        /**
         * This is the layer 2 based switching you will create. Once you have
         * created the appropriate code in the forwardAsLearningSwitch method
         * (see below), comment out the above call to forwardAsHub, and
         * uncomment the call here to forwardAsLearningSwitch.
         */
        forwardAsLearningSwitch(sw, pi);
        return Command.CONTINUE;
    }

    /**
     * EXAMPLE CODE: Floods the packet out all switch ports except the port it
     * came in on.
     *
     * @param sw the OpenFlow switch object
     * @param pi the OpenFlow Packet In object
     * @throws IOException
     */
    public void forwardAsHub(IOFSwitch sw, OFPacketIn pi) throws IOException {
        // Create the OFPacketOut OpenFlow object
        OFPacketOut po = new OFPacketOut();

        // Create an output action to flood the packet, put it in the OFPacketOut
        OFAction action = new OFActionOutput(OFPort.OFPP_FLOOD.getValue());
        po.setActions(Collections.singletonList(action));

        // Set the port the packet originally arrived on
        po.setInPort(pi.getInPort());

        // Reference the packet buffered at the switch by id
        po.setBufferId(pi.getBufferId());
        if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
            /**
             * The packet was NOT buffered at the switch, therefore we must
             * copy the packet's data from the OFPacketIn to our new
             * OFPacketOut message.
             */
            po.setPacketData(pi.getPacketData());
        }
        // Send the OFPacketOut to the switch
        sw.getOutputStream().write(po);
    }

    /**
     * Learn the source MAC:port pair for each arriving packet, and send packets
     * out the port previously learned for the destination MAC of the packet,
     * if it exists.  Otherwise flood the packet similarly to forwardAsHub.
     * @param sw the OpenFlow switch object
     * @param pi the OpenFlow Packet In object
     * @throws IOException
     */
    public void forwardAsLearningSwitch(IOFSwitch sw, OFPacketIn pi) throws IOException {
        Map<Long,Short> macTable = macTables.get(sw);

        // Build the Match
        OFMatch match = OFMatch.load(pi.getPacketData(), pi.getInPort());

        // Learn the port to reach the packet's source MAC
        macTable.put(Ethernet.toLong(match.getDataLayerSource()), pi.getInPort());

        // Retrieve the port previously learned for the packet's dest MAC
        Short outPort = macTable.get(Ethernet.toLong(match.getDataLayerDestination()));

        if (outPort != null) {
            // Destination port known, push down a flow
            OFFlowMod fm = new OFFlowMod();
            fm.setBufferId(pi.getBufferId());
            // Use the Flow ADD command
            fm.setCommand(OFFlowMod.OFPFC_ADD);
            // Time out the flow after 5 seconds if inactivity
            fm.setIdleTimeout((short) 5);
            // Match the packet using the match created above
            fm.setMatch(match);
            // Send matching packets to outPort
            OFAction action = new OFActionOutput(outPort);
            fm.setActions(Collections.singletonList((OFAction)action));
            // Send this OFFlowMod to the switch
            sw.getOutputStream().write(fm);

            if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
                /**
                 * EXTRA CREDIT: This is a corner case, the packet was not
                 * buffered at the switch so it must be sent as an OFPacketOut
                 * after sending the OFFlowMod
                 */
                OFPacketOut po = new OFPacketOut();
                action = new OFActionOutput(outPort);
                po.setActions(Collections.singletonList(action));
                po.setBufferId(OFPacketOut.BUFFER_ID_NONE);
                po.setInPort(pi.getInPort());
                po.setPacketData(pi.getPacketData());
                sw.getOutputStream().write(po);
            }
        } else {
            // Destination port unknown, flood packet to all ports
            forwardAsHub(sw, pi);
        }
    }

    // ---------- NO NEED TO EDIT ANYTHING BELOW THIS LINE ----------

    /**
     * Ensure there is a MAC to port table per switch
     * @param sw
     */
    private void initMACTable(IOFSwitch sw) {
        Map<Long,Short> macTable = macTables.get(sw);
        if (macTable == null) {
            macTable = new HashMap<Long,Short>();
            macTables.put(sw, macTable);
        }
    }

    @Override
    public void addedSwitch(IOFSwitch sw) {
    }

    @Override
    public void removedSwitch(IOFSwitch sw) {
        macTables.remove(sw);
    }

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }

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

    public String getName() {
        return "tutorial";
    }
}
