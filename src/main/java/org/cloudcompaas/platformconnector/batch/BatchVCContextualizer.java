/*******************************************************************************
 * Copyright (c) 2013, Andrés García García All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * (2) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * (3) Neither the name of the Universitat Politècnica de València nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.cloudcompaas.platformconnector.batch;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;

import m4cloudcommon.messages.msgInfo;

import org.cloudcompaas.common.communication.RESTComm;
import org.cloudcompaas.common.util.XMLWrapper;

/**
 * @author angarg12
 *
 */
public class BatchVCContextualizer {
	private int timeout = 60000;
	private int timeoutConnection = 10000;
	private int connectionRetries = 5;
	private int contextualizerTalkingPort = 5555;
	private long monitorInterval = 120000;
	private String idSla;
	private String localSdtId;
	private String[] epr;
	private PaaSAgent agent;
	private String operation;

	public BatchVCContextualizer(String[] epr_, String idSla_, String localSdtId_, PaaSAgent agent_, String operation_){
		epr = epr_;
		idSla = idSla_;
		localSdtId = localSdtId_;
		agent = agent_;
		operation = operation_;
	}
	
	public void execute() throws Exception {	
		RESTComm comm = new RESTComm("Catalog");
		comm.setUrl("/service/search?name=Catalog");
		String id_service = comm.get().getFirst("//id_service");
		comm.setUrl("/service_instance/search?service="+id_service);
		String[] catalogEpr = comm.get().get("//epr");
		Random rand = new Random();
		String randomCatalogEpr = catalogEpr[rand.nextInt(catalogEpr.length)];

    	comm.setUrl("sla/"+idSla);
		XMLWrapper wrap = comm.get();
		String[] metricNames = wrap.get("declare namespace ccpaas='http://www.grycap.upv.es/cloudcompaas';//ccpaas:Metric/@ccpaas:Name");
		String[] metricLevels = wrap.get("declare namespace ccpaas='http://www.grycap.upv.es/cloudcompaas';//ccpaas:Metric/ccpaas:Level");
/*
		for(int i = 0; i < epr.length; i++){
			for(int j = 0; j < connectionRetries; j++){
				try{
					System.out.println(agent.getEpr()+" "+contextualizerTalkingPort);
					Socket sock = new Socket();
					SocketAddress saddr = new InetSocketAddress(agent.getEpr(), contextualizerTalkingPort);
					sock.setSoTimeout(timeout);
					sock.connect(saddr);
					System.out.println("connected!!");
					ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
					for(int k = 0; k < metricLevels.length; k++){
						if(metricLevels[k].equals("paas")){
							msgInfo message = new msgInfo();
							message.Params.put("REQUEST", operation); 
							message.Params.put("BASEURL", randomCatalogEpr);
							/** FIXME
							 * Hay que comprobar si al pasar la epr es suficiente con la ip o hay que añadir el puerto en el que debe estar escuchando el agente.
							 *//*
							message.Params.put("EPR", epr[i]);
							message.Params.put("ID_SLA", idSla);
							message.Params.put("LOCAL_SDT_ID", localSdtId);
							message.Params.put("INTERVAL", String.valueOf(monitorInterval));
							message.Params.put("METRIC_NAME", metricNames[k]);

							System.out.println("writing object");
							oos.writeObject(message);
							oos.flush();
							System.out.println("object wrote");
							message = (msgInfo) ois.readObject();
							if(message.isError()){
								throw new Exception(message.getError());
							}
						}
					}
					oos.close();
					ois.close();
					sock.close();
					break;
				}catch(Exception e){
					e.printStackTrace();
					Thread.sleep(timeoutConnection);
				}
			}
		}
			*/
	}

	public int getTimeoutConnection() {
		return timeoutConnection;
	}

	public void setTimeoutConnection(int timeoutConnection) {
		this.timeoutConnection = timeoutConnection;
	}

	public int getConnectionRetries() {
		return connectionRetries;
	}

	public void setConnectionRetries(int connectionRetries) {
		this.connectionRetries = connectionRetries;
	}

	public int getContextualizerTalkingPort() {
		return contextualizerTalkingPort;
	}

	public void setContextualizerTalkingPort(int contextualizerTalkingPort) {
		this.contextualizerTalkingPort = contextualizerTalkingPort;
	}

	public long getMonitorInterval() {
		return monitorInterval;
	}

	public void setMonitormonitorInterval(long monitorInterval) {
		this.monitorInterval = monitorInterval;
	}
}
