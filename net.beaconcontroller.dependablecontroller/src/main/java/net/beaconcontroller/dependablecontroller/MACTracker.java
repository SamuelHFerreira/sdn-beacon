package net.beaconcontroller.dependablecontroller;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFType;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MACTracker implements IOFMessageListener {
	protected static Logger logger = LoggerFactory.getLogger(MACTracker.class);
	protected IBeaconProvider beaconProvider;
	protected Set<Integer> macAddresses = new ConcurrentSkipListSet<Integer>();
	
	public IBeaconProvider getBeaconProvider() {
		return beaconProvider;
	}

	public void setBeaconProvider(IBeaconProvider beaconProvider) {
		this.beaconProvider = beaconProvider;
	}

	public Command receive(IOFSwitch sw, OFMessage msg) {
        OFPacketIn pi = (OFPacketIn) msg;
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), (short) 0);
        Integer sourceMACHash = Arrays.hashCode(match.getDataLayerSource());
        if (!macAddresses.contains(sourceMACHash)) {
            macAddresses.add(sourceMACHash);
            logger.info("MAC Address: {} seen on switch: {}",
                    HexString.toHexString(match.getDataLayerSource()),
                    sw.getId());
        }
        return Command.CONTINUE;
    }

	@Override
	public String getName() {
		
		return "MacTracker";
	}
	
	public void startUp() {
		beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}
	
	public void shutDown() {
		beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
	}
}
