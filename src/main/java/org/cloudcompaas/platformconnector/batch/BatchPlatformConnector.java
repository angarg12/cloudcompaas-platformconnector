package org.cloudcompaas.platformconnector.batch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.http.HttpStatus;
import org.apache.xmlbeans.XmlObject;
import org.cloudcompaas.common.communication.RESTComm;
import org.cloudcompaas.common.util.XMLWrapper;
import org.cloudcompaas.platformconnector.APlatformConnector;
import org.ogf.schemas.graap.wsAgreement.AgreementPropertiesDocument;
import org.ogf.schemas.graap.wsAgreement.ServiceDescriptionTermType;

/**
 * @author angarg12
 *
 */
@Scope(Scope.ScopeType.SINGLETON)
@Path("/agreement")
public class BatchPlatformConnector extends APlatformConnector {
	//private static final int DEPLOY_RETRIES = 120;
	//private static final int DEPLOY_WAIT = 10000;
	private Collection<PaaSAgent> paasagents = new Vector<PaaSAgent>();

	public BatchPlatformConnector() throws Exception {
		super();
		
		BufferedReader bis = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("vagents.xml")));
		String vagents = "";
		while(bis.ready()){
			vagents += bis.readLine();
		}
		XMLWrapper wrap = new XMLWrapper(vagents);
		XmlObject[] agents = wrap.getNodes("//paasagent");
		for(int i = 0; i < agents.length; i++){
			Map<String,Collection<String>> extensions = new HashMap<String,Collection<String>>();

			XMLWrapper agentWrap = new XMLWrapper(agents[i]);
			String[] extNames = agentWrap.get("//@name");
			String[] extValues = agentWrap.get("//extension");
			for(int j = 0; j < extNames.length; j++){
				Collection<String> values = extensions.get(extNames[j]);
				if(values == null){
					values = new Vector<String>();
					values.add(extValues[j]);
					extensions.put(extNames[j], values);
				}else{
					values.add(extValues[j]);
				}
			}
			PaaSAgent current = new PaaSAgent();
			current.setId(i);

			current.setEpr(agentWrap.getFirst("//epr"));
			current.setExtensions(extensions);
			paasagents.add(current);
		}
	}
	
	@POST
	@Path("{id}")
	@Consumes("text/plain")
	public Response deploySLA(@HeaderParam("Authorization") String auth, @PathParam("id") String idSla, String eprsPlain) {
		try{
			if(auth == null || securityHandler.authenticate(auth) == false){
				return Response
				.status(HttpStatus.UNAUTHORIZED.getCode())
				.build();
			}
		}catch(Exception e){
			e.printStackTrace();
			return Response
					.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode())
					.build();
		}
		int status = HttpStatus.INTERNAL_SERVER_ERROR.getCode();
		try{
			/**
			 * FIXME: Actually add the eprs for the virtual containers deployed.
			 */
			String eprsXml = "<eprs/>";
			String[] eprs = eprsPlain.split("\n");
			
			/**
			 * FIXME: Do NOT hardcode the port. It should be extracted from the vc description.
			 */
			for(int i = 0; i < eprs.length; i++){
				eprs[i] = eprs[i]+"4444";
			}

        	RESTComm comm = new RESTComm("Catalog");
        	comm.setUrl("sla/"+idSla);
    		XMLWrapper wrap = comm.get();

			AgreementPropertiesDocument xmlsla = AgreementPropertiesDocument.Factory.parse(wrap.getFirst("//xmlsla"));


			ServiceDescriptionTermType[] terms = xmlsla.getAgreementProperties().getTerms().getAll().getServiceDescriptionTermArray();
			String sdt_id = null;
			
			for(int i = 0; i < terms.length; i++){
				if(terms[i].getDomNode().getFirstChild().getNodeName().equals("ccpaas:VirtualContainer")){
					sdt_id = terms[i].getName();
				}
			}
			if(paasagents.isEmpty() == false){
				BatchVCContextualizer vccont = new BatchVCContextualizer(eprs, idSla, sdt_id, paasagents.iterator().next(),"METRIC_INIT");
				vccont.execute();
			}
	        return Response
	        .status(HttpStatus.OK.getCode())
	        .entity(eprsXml)
	        .build();
		}catch(Exception e){
			e.printStackTrace();
			return Response
			.status(status)
			.entity(e.getMessage())
			.build();
		}
	}

	@POST
	@Path("{id}/{servicename}/ServiceTermState/Metadata/Replicas/")
	@Consumes("text/plain")
	public Response deployReplicas(@HeaderParam("Authorization") String auth, @PathParam("id") String idSla, @PathParam("servicename") String serviceName, String eprsPlain) {
		if(auth == null || securityHandler.authenticate(auth) == false){
			return Response
			.status(HttpStatus.UNAUTHORIZED.getCode())
			.build();
		}
		int status = HttpStatus.INTERNAL_SERVER_ERROR.getCode();
		
		try {
			/**
			 * FIXME: Actually add the eprs for the virtual containers deployed.
			 */
			String eprsXml = "<eprs/>";
			String[] eprs = eprsPlain.split("\n");
			/**
			 * FIXME: Do NOT hardcode the port. It should be extracted from the vc description.
			 */
			for(int i = 0; i < eprs.length; i++){
				eprs[i] = eprs[i]+"4444";
			}
			
        	RESTComm comm = new RESTComm("Catalog");
        	comm.setUrl("sla/"+idSla);
    		XMLWrapper wrap = comm.get();

			AgreementPropertiesDocument xmlsla = AgreementPropertiesDocument.Factory.parse(wrap.getFirst("//xmlsla"));

			ServiceDescriptionTermType[] terms = xmlsla.getAgreementProperties().getTerms().getAll().getServiceDescriptionTermArray();
			String sdt_id = null;
			
			for(int i = 0; i < terms.length; i++){
				if(terms[i].getDomNode().getFirstChild().getNodeName().equals("ccpaas:VirtualContainer")){
					sdt_id = terms[i].getName();
				}
			}
			if(paasagents.isEmpty() == false){
				BatchVCContextualizer vccont = new BatchVCContextualizer(eprs, idSla, sdt_id, paasagents.iterator().next(),"METRIC_INIT");
				vccont.execute();
			}
	        return Response
	        .status(HttpStatus.OK.getCode())
	        .entity(eprsXml)
	        .build();
		}catch(Exception e){
			e.printStackTrace();
			return Response
			.status(status)
			.entity(e.getMessage())
			.build();
		}
	}

	@DELETE
	@Path("{id}")
	public Response undeploySLA(@HeaderParam("Authorization") String auth, @PathParam("id") String idSla){
		if(auth == null || securityHandler.authenticate(auth) == false){
			return Response
			.status(HttpStatus.UNAUTHORIZED.getCode())
			.build();
		}
		int status = HttpStatus.INTERNAL_SERVER_ERROR.getCode();
		try{

		}catch(Exception e){
			return Response
			.status(status)
			.entity(e.getMessage())
			.build();
		}
        return Response
        .status(HttpStatus.OK.getCode())
        .build();
	}
	
	@DELETE
	@Path("{id}/{servicename}/ServiceTermState/Metadata/Replicas/")
	@Consumes("text/plain")
	public Response undeployReplicas(@HeaderParam("Authorization") String auth, 
			@PathParam("id") String idSla, @PathParam("servicename") String serviceName, 
			String eprsPlain) {

		if(auth == null || securityHandler.authenticate(auth) == false){
			return Response
			.status(HttpStatus.UNAUTHORIZED.getCode())
			.build();
		}
		int status = HttpStatus.INTERNAL_SERVER_ERROR.getCode();
		try{
			String[] eprs = eprsPlain.split("\n");
		}catch(Exception e){
			e.printStackTrace();
			return Response
			.status(status)
			.entity(e.getMessage())
			.build();
		}
		
        return Response
        .status(HttpStatus.OK.getCode())
        .build();
	}
}
