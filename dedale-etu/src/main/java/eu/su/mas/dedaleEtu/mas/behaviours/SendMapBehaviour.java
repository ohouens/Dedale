package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class SendMapBehaviour extends SimpleBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2606822683718779669L;
	private MapRepresentation myMap;
	private String receiver;
	private boolean finished=false;

	public SendMapBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap, String receiver) {
		super(myAgent);
		this.myMap = myMap;
		this.receiver = receiver;
	}
	
	@Override
	public void action() {
		ACLMessage sendMap = new ACLMessage(ACLMessage.PROPAGATE);
		sendMap.setSender(myAgent.getAID());
		sendMap.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		try {
			sendMap.setContentObject(myMap.serialize());
			((AbstractDedaleAgent) myAgent).sendMessage(sendMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
}
