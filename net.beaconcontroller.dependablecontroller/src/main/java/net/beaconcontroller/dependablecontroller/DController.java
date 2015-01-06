package net.beaconcontroller.dependablecontroller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.devicemanager.Device;
import net.beaconcontroller.devicemanager.IDeviceManagerAware;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DController implements IOFMessageListener, IOFSwitchListener, IDeviceManagerAware {
    protected static Logger log = LoggerFactory.getLogger(DController.class);
    protected IBeaconProvider beaconProvider;
    protected Map<IOFSwitch, Map<Long,Short>> macTables =
    		  new HashMap<IOFSwitch, Map<Long,Short>>();
    DepspaceAcess depsAccess;
    protected List<VirtualController> controllers;
    protected Integer switchOrderer;
    
    public void startUp() {
        log.info("Starting");
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFSwitchListener(this);
        String controllerID = beaconProvider.toString();
        MininetAccess mnAccesss = new MininetAccess("192.168.56.101", "openflow", "openflow");
        mnAccesss.executeCommand("sh /home/openflow/scripts/makeTopologies.sh 1 10.10.1.91");
        mnAccesss.executeCommand("sh /home/openflow/scripts/makeTopologies.sh 2 10.10.1.91");
        mnAccesss.executeCommand("sh /home/openflow/scripts/makeTopologies.sh 3 10.10.1.91");
        
        // Criando controllers virtuais para teste
        controllers = ControllersInstancer.createVirtualControllers();
        switchOrderer = 0;
        log.info("ControllerID: "+controllerID);
        depsAccess = new DepspaceAcess(true,controllerID,0);
        
//        controllers = ControllersInstancer.createVirtualControllers();
    }

    public Command receive(IOFSwitch sw, OFMessage msg) throws IOException {
        initMACTable(sw);
        OFPacketIn pi = (OFPacketIn) msg;
        log.info("Recebendo mensagem, vai repassar como hub");
//        forwardAsHub(sw, pi);
        log.info("Enviou mensagem como hub");
//        depsAccess.outOp();
//        depsAccess.casOp();
//        log.info("after created depspace tuple");
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
    	try {
//    		log.info("size of controllers:"+controllers.size() +" switch orderer:"+switchOrderer);
    		controllers.get(switchOrderer);
//    		controllers.get(switchOrderer).getSwitches().add(sw);
    		
			log.info("Added switch: "+sw.getId()+" state:"+sw.getState() +
					 " remote addresss:"+sw.getSocketChannel().getRemoteAddress().toString()+"to the controller:"+controllers.get(switchOrderer).getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	switchOrderer++;
    	//TODO treats of tuplespace
//    	log.info("adding swich:"+sw.getId()+" info into tuplespace");
//		depsAccess.outOp();
//	    depsAccess.casOp();
//    	executeCommand("ssh openflow");
    	
    }
    
    @Override
    public void removedSwitch(IOFSwitch sw) {
        macTables.remove(sw);
        log.info("removed switch: "+sw.getId());
        //TODO treats of tuplespace
    }

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
    	log.info("setting beaconProvider");
        this.beaconProvider = beaconProvider;
    }

    public void shutDown() {
        log.info("Stopping");
        beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.removeOFSwitchListener(this);
    }

    public String getName() {
        return "ControllerSafety";
    }



	@Override
	public void deviceAdded(Device device) {
//		TODO notify the dependable tuples
//		log.info("deviceAdded:"+device.get);
		log.info("Added device: "+device.getSw().getId()+"|"+device.getDataLayerAddress().toString());
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFSwitchListener(this);
		
	}



	@Override
	public void deviceRemoved(Device device) {
		log.info("removed device: "+device.getSw().getId()+"|"+device.getDataLayerAddress().toString());
//		TODO notify the dependable tuples
		
	}



	@Override
	public void deviceMoved(Device device, IOFSwitch oldSw, Short oldPort,
			IOFSwitch sw, Short port) {
		// TODO notify the dependable tuples
		
	}



	@Override
	public void deviceNetworkAddressAdded(Device device,
			Set<Integer> networkAddresses, Integer networkAddress) {
//		TODO notify the dependable tuples
		
	}



	@Override
	public void deviceNetworkAddressRemoved(Device device,
			Set<Integer> networkAddresses, Integer networkAddress) {
		// TODO notify the dependable tuples
		
	}
}