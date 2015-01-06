package net.beaconcontroller.dependablecontroller;

import static br.ufsc.das.util.TSUtil.CONFIG_HOME;
import static br.ufsc.das.util.TSUtil.DPS_NAME;
import static br.ufsc.das.util.TSUtil.DPS_CONFIDEALITY;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufsc.das.client.DepSpaceAccessor;
import br.ufsc.das.client.DepSpaceAdmin;
import br.ufsc.das.general.DepTuple;

public class DepspaceAcess {
    private static Logger log = LoggerFactory.getLogger(DController.class);
    private Properties prop;
    private DepSpaceAccessor accessor;
    private int groupId; //group of controllers TODO

    private DepTuple template; 


    public DepspaceAcess(boolean createSpace,String controllerId, int groupId) {
        prop = new Properties();
        //setting config home.
        prop.put(CONFIG_HOME,"C:\\Documentos\\git\\tcc-code\\net.beaconcontroller.dependablecontroller\\config");
        //the DepSpace name
        log.info("Creating Tuple: "+controllerId);
        prop.put(DPS_NAME,controllerId);
        // TODO primeiro campo vai ser o groupID
        template = DepTuple.createTuple(groupId,"*","*","*");
        //the DepSpace Accessor, who will access the DepSpace.
        log.info("Created Tuple groupId: "+groupId);
        try{
        	if(createSpace){
        		log.info("Creating Tuple space with problem =s");
        		DepSpaceAdmin admin = new DepSpaceAdmin();
        		accessor = admin.createSpace(prop);
        		log.info("Created Tuple space");
        	}else{
        		log.info("Creating Acessor");
        		accessor = new DepSpaceAdmin().createAccessor(prop);
        		log.info("Created Acessor");
        	}
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void preConfig() {
        try{
            //the Interceptor
            //String loader = "br.ufsc.das.demo.InterceptorLoaderDemo";
            //prop.put(DPS_CLIENT_INTERCEPTOR_LOADER,loader);
            //prop.put(DPS_SERVER_INTERCEPTOR_LOADER,loader);

            //use confidentiality?
            prop.put(DPS_CONFIDEALITY,"true");

        }catch(Exception e){
            e.printStackTrace();
        }

    }
    private DepTuple getBasicTuple(int i){
         return DepTuple.createTuple(i,"OpenFlowLeader","I'm the leader","BUMMMM!!!");
    }
    
    public DepTuple getPatternTuple(String firstField, String secondField, String thirdField) {
    	 return DepTuple.createTuple(this.groupId,firstField,secondField,thirdField);
    }
    public DepTuple getTemplate() {
    	 return template;
    }
    public void setTemplate(DepTuple template) {
    	this.template = template;
    }
    
    public void outOp(){
    	try { 
	    	 DepTuple dt = getBasicTuple(0);
	         log.info("OUT operation: "+dt);
	         accessor.out(dt);
    	}catch (Exception e) {
    		 e.printStackTrace();
    	}
    }
    public void casOp(){
    	try { 
	    	 DepTuple dt = getBasicTuple(0);
	         DepTuple template = getTemplate();
	         log.info("CAS operation: "+accessor.cas(template,dt));
    	}catch (Exception e) {
    		 e.printStackTrace();
    	}
    }
    public void rdpOp(){
    	try { 
	         DepTuple template = getTemplate();
	         log.info("RDP operation: "+accessor.rdp(template));
    	}catch (Exception e) {
    		 e.printStackTrace();
    	}
    }
    public void inpOp() {
    	try { 
	         DepTuple template = getTemplate();
	         log.info("INP operation: "+accessor.inp(template));
	   	}catch (Exception e) {
	   		 e.printStackTrace();
	   	}
    }
    public void rdOp() {
    	try { 
	         DepTuple template = getTemplate();
	         log.info("RD operation: "+accessor.rd(template));
	   	}catch (Exception e) {
	   		 e.printStackTrace();
	   	}
    }
    public void inOp() {
    	try { 
	         DepTuple template = getTemplate();
	         log.info("IN operation: "+accessor.in(template));
	   	}catch (Exception e) {
	   		 e.printStackTrace();
	   	}
    }
}
