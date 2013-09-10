package org.cloudcompaas.platformconnector;

import javax.ws.rs.core.Response;

/**
 * @author angarg12
 *
 */
public interface IPlatformConnector {
	public Response deploySLA(String auth, String idSla, String eprs);
	public Response deployReplicas(String auth, String idSla, String serviceName, String eprs);
	public Response undeploySLA(String auth, String idSla);
	public Response undeployReplicas(String auth, String idSla, String serviceName, String eprs);
}
