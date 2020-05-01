package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Date;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class AckPingMapBehaviour extends OneShotBehaviour{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -23290922115850084L;
	
	private int transition = 0;

	public AckPingMapBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		ACLMessage ping = agent.getLastReceive();
		ACLMessage ack = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		ack.setSender(myAgent.getAID());
		ack.addReceiver(ping.getSender());
		ack.setContent(agent.compressInfo());
		agent.setLastSend(ack);
		((AbstractDedaleAgent)myAgent).sendMessage(ack);
		System.out.println(myAgent.getLocalName()+" - ACK PING");
		System.out.println(myAgent.getLocalName()+" - transition to SWITCH");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
