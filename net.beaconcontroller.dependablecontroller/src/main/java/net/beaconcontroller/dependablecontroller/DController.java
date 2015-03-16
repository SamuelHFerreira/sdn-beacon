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
import net.beaconcontroller.dependablecontroller.controllers.DepspaceAcess;
import net.beaconcontroller.dependablecontroller.controllers.MininetAccess;
import net.beaconcontroller.dependablecontroller.data.RouteMap;
import net.beaconcontroller.dependablecontroller.simulation.ControllersInstancer;
import net.beaconcontroller.devicemanager.Device;
import net.beaconcontroller.devicemanager.IDeviceManagerAware;
import net.beaconcontroller.packet.Ethernet;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
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

import br.ufsc.das.general.DepTuple;

public class DController implements IOFMessageListener, IOFSwitchListener, IDeviceManagerAware {
    protected static Logger log = LoggerFactory.getLogger(DController.class);
    protected IBeaconProvider beaconProvider;
    protected Map<IOFSwitch, Map<Long,Short>> macTables =
    		  new HashMap<IOFSwitch, Map<Long,Short>>();
    DepspaceAcess depsAccess;
    protected MininetAccess mnAccesss;
    
    protected String thisControllerId;
    protected String mode;
    
    public void startUp() {
        log.info("Starting");
        mode = "learningSwitch";
        
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFSwitchListener(this);
//        thisControllerId = beaconProvider.toString();
        thisControllerId = "0";
//        mnAccesss = new MininetAccess("192.168.56.101", "openflow", "openflow");
       
        log.info("ControllerID: "+thisControllerId);
        depsAccess = new DepspaceAcess(true,thisControllerId,0);
        // TODO rotina de recuperacao do anterior
        DepTuple resultRead =  depsAccess.rdpOp(Integer.valueOf(thisControllerId));
        if(resultRead != null) {
        	log.info("li um objeto da tuplespace: "+resultRead);
        	
        	Object[] fields = resultRead.getFields();
        	String RouteMapObject = (String) fields[4];
        	RouteMap routeMap = readJson(RouteMapObject);
//        	mnAccesss.executeCommand("sh /home/openflow/scripts/breakdownSwitch.sh "+thisControllerId);
//			mnAccesss.executeCommand("sh /home/openflow/scripts/createTopology.sh "+thisControllerId+" 200.131.206.168");
        } else {
        	log.info("nao encontrei nada na tuplespace"+resultRead);
        	
        	Map<String, Map<Long,Short>> macTablesReplica = new HashMap<String, Map<Long,Short>>();
        	Map<Long,Short> mapTest = new HashMap<Long,Short>();
        	mapTest.put(9l, (short)5);
        	macTablesReplica.put("chave1", mapTest);
        	RouteMap routeMap = new RouteMap();
        	routeMap.setMacTablesReplica(macTablesReplica);
        	String routeMapJson = writeJson(routeMap);
        	
        	log.info("inserindo routeMapJson"+routeMapJson);
        	
        	depsAccess.outOp(0,"endereço", "tempo", "masterId", routeMapJson);
        	
//        	mnAccesss.executeCommand("sh /home/openflow/scripts/createTopology.sh 1 200.131.206.168");
//	        mnAccesss.executeCommand("sh /home/openflow/scripts/createTopology.sh 2 200.131.206.168");
//	        mnAccesss.executeCommand("sh /home/openflow/scripts/createTopology.sh 3 200.131.206.168");
        }
        // Criando controllers virtuais para teste
    }
    
    private String writeJson(RouteMap routes) {
    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	String json = "";
		try {
			json = ow.writeValueAsString(routes);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	log.info("json obtido do objeto " + json);
    	return json;
    }
    
    private RouteMap readJson(String json) {
    	RouteMap ob =  null;
    	try {
    		ob = new ObjectMapper().readValue(json, RouteMap.class);
    	} catch (JsonParseException e) {
    		e.printStackTrace();
    	} catch (JsonMappingException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return ob;
    	
    }

    public Command receive(IOFSwitch sw, OFMessage msg) throws IOException {
    	initMACTable(sw);
    	OFPacketIn pi = (OFPacketIn) msg;
        if(!"learningSwitch".equalsIgnoreCase(mode)) {
        	log.info("Recebendo mensagem, vai repassar como hub:"+msg.toString());
        	forwardAsHub(sw, pi);
        	log.info("Enviou mensagem como hub");
//        log.info("after created depspace tuple");
        } else {
        	forwardAsLearningSwitch(sw,pi);
        	
//        	log.info("vai recuperar o controller da tuplespace");
//        	DepTuple tupla = depsAccess.rdpOp(Integer.valueOf(thisControllerId));
//        	log.info("Campos obtidos");
//        	for(Object object : tupla.getFields()){
//        		log.info(object+"");
//        	}
        }
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
//        if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
            /**
             * The packet was NOT buffered at the switch, therefore we must
             * copy the packet's data from the OFPacketIn to our new
             * OFPacketOut message.
             */
            po.setPacketData(pi.getPacketData());
//        }
        // Send the OFPacketOut to the switch
//        for(IOFSwitch switchEach : macTables.keySet()) {
//        	if(sw != switchEach) {
//        		Map<Long,Short> macTable = macTables.get(switchEach);
//        		OFMatch match = OFMatch.load(pi.getPacketData(), pi.getInPort());

        		// Learn the port to reach the packet's source MAC
//        		macTable.put(Ethernet.toLong(match.getDataLayerSource()), pi.getInPort());
//        		if(po.getPacketData() != null)
//        			log.info("-------- forwarding as hub to another switch:"+Arrays.toString(po.getPacketData()));
//        		if(match != null) {
//        			log.info("-------- Ethernet:"+Ethernet.toLong(match.getDataLayerSource()));
//        			log.info("-------- NetworkDestination:"+match.getNetworkDestination());
//        			log.info("-------- getNetworkSource:"+match.getNetworkSource());
//        		}
//        		log.info("-------- po.getInPort:"+po.getInPort()+"pi.getInPort:"+pi.getInPort());
        		
//        		if(Ethernet.toLong(match.getDataLayerSource()) == 2L && pi.getInPort() == 1) {
//        			po.setInPort(2);
//        		}
        		
//        		switchEach.getOutputStream().write(po);
//        	}
//        }
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
    	initMACTable(sw);
    	addingSWNormalMode(sw);
    	
//    	addingSWSimulationMode(sw);
    	
    }

	private void addingSWNormalMode(IOFSwitch sw) {
		try {
			log.info("adding swich:"+sw.getSocketChannel().getRemoteAddress().toString());
			DepTuple resultRead =  depsAccess.rdpOp(Integer.valueOf(thisControllerId));
	        if(resultRead == null) {
	        	depsAccess.outOp(Integer.valueOf(depsAccess.getGroupId()), sw.getSocketChannel().getRemoteAddress().toString(), System.currentTimeMillis()+"", depsAccess.getGroupId()+"","");
	        } else {
	        	Object[] fields = resultRead.getFields();
	        	log.info("field[1]" +((String) fields[1]));
//	        	String switchesBare = (String) fields[1];
//	        	String switchesList[] = switchesBare.split("|");
//	        	switchesList[switchesList.length] = "|"+ sw.getSocketChannel().getRemoteAddress().toString();
	        	String sws = (String) fields[1];
	        	sws = sws + ","+ sw.getSocketChannel().getRemoteAddress().toString();
//	        	depsAccess.outOp(Integer.valueOf(depsAccess.getGroupId()), sws, System.currentTimeMillis()+"", depsAccess.getGroupId()+"","");
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void addingSWSimulationMode(IOFSwitch sw) {
		try {
//    		log.info("number of controllers:"+ControllersInstancer.getVirtualControllers().size() +" switch orderer:"+switchOrderer);
			log.info("starting test");
    		if(ControllersInstancer.getVirtualControllers().get(0).getSwitches() != null) {
    			log.info("adding swich:"+sw.getSocketChannel().getRemoteAddress().toString() +" to the controller:"+ControllersInstancer.getVirtualControllers().get(0).getId());
    			ControllersInstancer.getVirtualControllers().get(0).getSwitches().add(sw);
    			log.info("saving this reference into the Depspace Tuple");
    			depsAccess.outOp(ControllersInstancer.getVirtualControllers().get(0).getId().intValue(), sw.getSocketChannel().getRemoteAddress().toString(), System.currentTimeMillis()+"", 
    					ControllersInstancer.getMasterController() != null ? ControllersInstancer.getMasterController().getId()+""  : "", "");
    		}
    		
			log.info("Added switch: "+sw.getId()+" state:"+sw.getState() +
					 " remote addresss:"+sw.getSocketChannel().getRemoteAddress().toString()+"to the controller:"+ControllersInstancer.getVirtualControllers().get(0).getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
//    	switchOrderer++;
    	// TODO tretas do sleep e exit nas topologias
//    	if((switchOrderer+1) == ControllersInstancer.getVirtualControllers().size()) {
//    		log.info("tempo..");
//    		try {
//    		    Thread.sleep(100);
//    		} catch(InterruptedException ex) {
//    		    Thread.currentThread().interrupt();
//    		}
    		// starts to simulate the breakdown of a virtual controller
    		// breakdown the masterController (0)
    		// TODO pegar switches do depspace
    		log.info("turning off the controller");
    		int oldMasterControllerId = 0;
    		List<IOFSwitch> switches = ControllersInstancer.getVirtualControllers().get(oldMasterControllerId).getSwitches();
    		ControllersInstancer.getVirtualControllers().get(0).turnOff();
//    		VirtualController newMasterController = null;
//    		if(ControllersInstancer.getMasterController() == null) {
//    			newMasterController = ControllersInstancer.chooseNewMaster();
//    			newMasterController.getSwitches().addAll(ControllersInstancer.getVirtualControllers().get(oldMasterControllerId).getSwitches());
//    		}
    		// atribui o switch a um novo controller
//    		log.info("reakdown the masterController script");
//    		if (newMasterController != null) {
//    			mnAccesss.executeCommand("sh /home/openflow/scripts/breakdownSwitch.sh "+ControllersInstancer.getVirtualControllers().get(oldMasterControllerId).getId());
//    			mnAccesss.executeCommand("sh /home/openflow/scripts/makeTopologies.sh "+newMasterController.getId()+" 200.131.206.168");
//    		}
//    	}
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