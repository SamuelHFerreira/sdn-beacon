package net.beaconcontroller.dependablecontroller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DController implements IOFMessageListener, IOFSwitchListener {
    protected static Logger log = LoggerFactory.getLogger(DController.class);
    protected IBeaconProvider beaconProvider;
    protected Map<IOFSwitch, Map<Long,Short>> macTables =
        new HashMap<IOFSwitch, Map<Long,Short>>();

    public Command receive(IOFSwitch sw, OFMessage msg) throws IOException {
        initMACTable(sw);
        OFPacketIn pi = (OFPacketIn) msg;
        log.info("Before pass on forward as hub");
        forwardAsHub(sw, pi);
        log.info("After pass on forward as hub");
        DepspaceAcess depspace = new DepspaceAcess(1, true, getName());
        log.info("after created depspace tuple");
        depspace.outOperation(1);
        log.info("has on depspace this "+depspace.casOperation(1)+"");
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
     * TODO: Learn the source MAC:port pair for each arriving packet. Next send
     * the packet out the port previously learned for the destination MAC, if it
     * exists. Otherwise flood the packet similarly to forwardAsHub.
     *
     * @param sw the OpenFlow switch object
     * @param pi the OpenFlow Packet In object
     * @throws IOException
     */
    public void forwardAsLearningSwitch(IOFSwitch sw, OFPacketIn pi) throws IOException {
        Map<Long,Short> macTable = macTables.get(sw);
       
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
        return "ControllerSafety";
    }
}