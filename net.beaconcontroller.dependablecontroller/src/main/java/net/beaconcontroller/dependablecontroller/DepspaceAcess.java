package net.beaconcontroller.dependablecontroller;

import static br.ufsc.das.util.TSUtil.DPS_NAME;
import static br.ufsc.das.util.TSUtil.DPS_SUPPORT_TRANSACTION;

import java.util.Properties;

import br.ufsc.das.client.DepSpaceAccessor;
import br.ufsc.das.client.DepSpaceAdmin;
import br.ufsc.das.general.DepSpaceException;
import br.ufsc.das.general.DepTuple;

public class DepspaceAcess {
	private boolean createSpace;
	private int executions;
	private String myName = "Controller"; 
	private DepSpaceAccessor accessor;
	
	
    public DepspaceAcess(int exec, boolean createSpace) {
        this.executions = exec;
        this.createSpace = createSpace;
        
        myName = "Controller";
        Properties prop = new Properties();
         
        prop.put(DPS_NAME,myName);
        //use confidentiality?
        //prop.put(DPS_CONFIDEALITY,"true");
        prop.put(DPS_SUPPORT_TRANSACTION,"true");

        //the DepSpace Accessor, who will access the DepSpace.
        try {
        	if(this.createSpace){
        		accessor = new DepSpaceAdmin().createSpace(prop);
            }else{
				accessor = new DepSpaceAdmin().createAccessor(prop);
            }
        } catch (DepSpaceException e) {
			e.printStackTrace();
        }
    }
    
    public boolean casOperation(int chave) {
    	boolean inserted = false;
    	try {
			int transId = accessor.openTransaction(0,9000);
			DepTuple dt = createTuple(chave, "valorCmp1","valorCmp2","valorCmp3");
			DepTuple template = DepTuple.createTuple(chave,"*","*","*");
	    	accessor.cas(template, dt, transId);
	        System.out.println("OUT ready.");
			pause();
		} catch (DepSpaceException e) {
			e.printStackTrace();
		}
    	return inserted;
    }
    public void outOperation(int chave) {
	    try{
	    	int transId = accessor.openTransaction(0,9000);
	    	DepTuple dt = createTuple(chave, "valorCmp1","valorCmp2","valorCmp3");
	    	
	    	accessor.out(dt, transId);
	        System.out.println("OUT ready.");
	        pause();
	    } catch (DepSpaceException e) {
			e.printStackTrace();
		}
    }
    
    public DepTuple createTuple(int i, String... fields) {
    	return DepTuple.createTuple(i,fields);
    }
	
	public DepTuple get(int i){
        return DepTuple.createTuple(i,"confidentiality","eu sou o cliente","BUMMMM!!!");
	}
	
	public void mainSpace() {
		  try{
//	            access(accessor);

	            /*if(this.createSpace){
	                // this will delete the DepSpace
	                accessor.finalize();
	            }*/
	            System.out.println("THE END!!!");
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	}
	
	private void access(DepSpaceAccessor accessor) throws Exception{
        System.out.println("Using the tuple: " + get(0));
        int transId = accessor.openTransaction(0,9000);
        System.out.println("Criou transacao: " + transId);
        
        for(int i = 0; i < executions; i++){
            System.out.println("Sending "+i);
            //OUT
            DepTuple dt = get(i);
            accessor.out(dt,transId);
            System.out.println("OUT ready.");
            pause();
            //RDP
            DepTuple template = DepTuple.createTuple(i,"*","*","*");
            System.out.println("RDP: "+accessor.rdp(template,transId));
            pause();
            //CAS READ
            System.out.println("CAS READ: "+accessor.cas(template,dt,transId));
            pause();
            //INP
            System.out.println("INP: "+accessor.inp(template,transId));
            pause();
            //CAS INSERT
            System.out.println("CAS INSERT: "+accessor.cas(template,dt,transId));
            pause();
        }
        accessor.renewTransactionTimeout(transId,9999);
        synchronized(this){
            try{
                this.wait(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        accessor.commitTransaction(transId);
    }
	
    private void pause(){
        synchronized(this){
            try{
                this.wait(100);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
