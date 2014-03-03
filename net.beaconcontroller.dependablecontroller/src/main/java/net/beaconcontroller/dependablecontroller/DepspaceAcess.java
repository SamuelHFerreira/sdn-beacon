package net.beaconcontroller.dependablecontroller;

import static br.ufsc.das.util.TSUtil.DPS_NAME;
import static br.ufsc.das.util.TSUtil.CONFIG_HOME;

import java.util.Properties;

import br.ufsc.das.client.DepSpaceAccessor;
import br.ufsc.das.client.DepSpaceAdmin;
import br.ufsc.das.general.DepTuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepspaceAcess {
	private int executions;
    private boolean createSpace;
    private static Logger log = LoggerFactory.getLogger(DController.class);


    /** Creates a new instance of ClientTest */
    public DepspaceAcess(int exec, boolean createSpace) {
        this.executions = exec;
        this.createSpace = createSpace;
    }
    

    public void run(){
        try{
            String name = "Demo Space";
            Properties prop = new Properties();
            //setting config home.
            prop.put(CONFIG_HOME,"/media/Arquivos principais/Meus DOCS/Facul/9º período/TCC2/WorkspaceLinux/beacon-tutorial-1.0.2/src/beacon-1.0.2/net.beaconcontroller.dependablecontroller/config");
             
            //the Interceptor
            //String loader = "br.ufsc.das.demo.InterceptorLoaderDemo";
            //prop.put(DPS_CLIENT_INTERCEPTOR_LOADER,loader);
            //prop.put(DPS_SERVER_INTERCEPTOR_LOADER,loader);

            //the DepSpace name
            prop.put(DPS_NAME,name);
            //use confidentiality?
            //prop.put(DPS_CONFIDEALITY,"true");

            //the DepSpace Accessor, who will access the DepSpace.
            DepSpaceAccessor accessor = null;
            if(this.createSpace){
            	DepSpaceAdmin admin = new DepSpaceAdmin();
            	accessor = admin.createSpace(prop);
            }else{
                accessor = new DepSpaceAdmin().createAccessor(prop);
            }

            access(accessor);

            /*if(this.createSpace){
                //this will delete the DepSpace
                accessor.finalize();
            }*/
            log.info("THE END!!!");
        }catch(Exception e){
            e.printStackTrace();
        }
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

     public DepTuple get(int i){
           return DepTuple.createTuple(i,"confidentiality","I'm the client","BUMMMM!!!");
    }

    private void access(DepSpaceAccessor accessor) throws Exception{
    	log.info("Using the tuple: "+get(0));

       /* if(executions > 1){
           DepTuple dt = get(1);
           accessor.out(dt);
        
        }else{
           DepTuple template = DepTuple.createTuple(1,"*","*","*");
           System.out.println("Vai dar o in");
           DepTuple result = accessor.in(template);
           System.out.println("Passou pelo in: "+result);
           
        }*/
        DepTuple dt = get(0);
        DepTuple template = DepTuple.createTuple(0,"*","*","*");
        accessor.out(dt);
        for(int i = 0; i < executions; i++){
        	log.info("Sending "+i);
            //OUT
            //dt = get(0);
            
            //CAS READ
            log.info("CAS READ: "+accessor.cas(template,dt));
            //pause();
                     
            //CAS INSERT
            //System.out.println("CAS INSERT: "+accessor.cas(template,dt));
            //pause();
        }
    }
}
