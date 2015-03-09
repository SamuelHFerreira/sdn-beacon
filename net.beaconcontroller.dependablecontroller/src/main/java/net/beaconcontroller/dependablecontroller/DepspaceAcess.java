package net.beaconcontroller.dependablecontroller;

import static br.ufsc.das.util.TSUtil.CONFIG_HOME;
import static br.ufsc.das.util.TSUtil.DPS_NAME;
import static br.ufsc.das.util.TSUtil.DPS_CONFIDEALITY;
import static br.ufsc.das.util.TSUtil.DPS_SUPPORT_TRANSACTION;

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
	private int groupId; // group of controllers TODO

	private DepTuple groupTemplate;

	public DepspaceAcess(boolean createSpace, String controllerId, int groupId) {
		prop = new Properties();
		// setting config home.
		 prop.put(CONFIG_HOME,"/media/Arquivos principais/Meus DOCS/Facul/9º período/TCC2/WorkspaceLinux/beacon-tutorial-1.0.2/src/beacon-1.0.2/net.beaconcontroller.dependablecontroller/config");
		// prop.put(CONFIG_HOME,"C:\\Documentos\\Outrascoisas\\Facul\\TCC\\depspace-0.2\\config");
//		prop.put(CONFIG_HOME,
//				"C:/Documentos/Outrascoisas/Facul/TCC/depspace-0.2/config");
		// the DepSpace name
		log.info("Creating Tuple: " + controllerId);
		prop.put(DPS_NAME, controllerId);
//		prop.put(DPS_SUPPORT_TRANSACTION, "true");
		// TODO primeiro campo vai ser o groupID
		groupTemplate = createBasicTemplate(groupId);
		// the DepSpace Accessor, who will access the DepSpace.
		log.info("Created Tuple groupId: " + groupId);
		try {
			if (createSpace) {
				DepSpaceAdmin admin = new DepSpaceAdmin();
				log.info("Creating Tuple space");
				accessor = admin.createSpace(prop);
				log.info("Created Tuple space");
			} else {
				log.info("Creating Acessor");
				accessor = new DepSpaceAdmin().createAccessor(prop);
				log.info("Created Acessor");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void preConfig() {
		try {
			// the Interceptor
			// String loader = "br.ufsc.das.demo.InterceptorLoaderDemo";
			// prop.put(DPS_CLIENT_INTERCEPTOR_LOADER,loader);
			// prop.put(DPS_SERVER_INTERCEPTOR_LOADER,loader);

			// use confidentiality?
			prop.put(DPS_CONFIDEALITY, "true");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private DepTuple getBasicTuple(int i) {
		return DepTuple.createTuple(i, "OpenFlowLeader", "I'm the leader",
				"BUMMMM!!!");
	}

	private DepTuple createBasicTemplate(int i) {
		return DepTuple.createTuple(i, "*", "*", "*");
	}

	public DepTuple getPatternTuple(String firstField, String secondField,
			String thirdField) {
		return DepTuple.createTuple(this.groupId, firstField, secondField,
				thirdField);
	}

	public void outOp(int tupleId, String fieldOne, String fieldTwo,
			String fieldThree) {
		try {
			DepTuple dt = DepTuple.createTuple(tupleId, fieldOne, fieldTwo,
					fieldThree);
			log.info("OUT operation: " + dt);
			accessor.out(dt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Se n�o tiver o template com o tupleId, insere a tupla com tupleId e os
	 * campos fieldOne, fieldTwo e fieldThree
	 * 
	 * @param tupleId
	 * @param fieldOne
	 * @param fieldTwo
	 * @param fieldThree
	 */
	public void casOp(int tupleId, String fieldOne, String fieldTwo,
			String fieldThree) {
		try {
			DepTuple template = createBasicTemplate(tupleId);
			DepTuple dt = DepTuple.createTuple(tupleId, fieldOne, fieldTwo,
					fieldThree);
			log.info("CAS operation: " + accessor.cas(template, dt));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Le tupla que combina com o template (tupleId,"*","*","*") e 'retorna true
	 * se tiver encontrado pra leitura, e false caso n�o tenha encontrado'"
	 * 
	 * @param tupleId
	 */
	public DepTuple rdpOp(int tupleId) {
		DepTuple result = null;
		try {
			DepTuple template = createBasicTemplate(tupleId);
			result = accessor.rdp(template);
			log.info("RDP operation: " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Le tupla que combina com o template (tupleId,"*","*","*") e a remove,
	 * 'retorna true se tiver encontrado pra leitura, e false caso n�o tenha
	 * encontrado'"
	 * 
	 * @param tupleId
	 */
	public void inpOp(int tupleId) {
		try {
			DepTuple template = createBasicTemplate(tupleId);
			log.info("INP operation: " + accessor.inp(template));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Le tupla que combina com o template (tupleId,"*","*","*") e , processo
	 * que se mant�m bloqueado at� que se encontre a tupla"
	 * 
	 * @param tupleId
	 */
	public void rdOp(int tupleId) {
		try {
			DepTuple template = createBasicTemplate(tupleId);
			log.info("RD operation: " + accessor.rd(template));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Le tupla que combina com o template (tupleId,"*","*","*") e a remove,
	 * processo que se mant�m bloqueado at� que se encontre a tupla"
	 * 
	 * @param tupleId
	 */
	public void inOp(int tupleId) {
		try {
			DepTuple template = createBasicTemplate(tupleId);
			log.info("IN operation: " + accessor.in(template));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
}
