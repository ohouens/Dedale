package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
		MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage ping = myAgent.receive(template);
		if(ping != null) {
			transition = 1;
			System.out.println(myAgent.getLocalName()+" - transition to RECEIVEMAP");
			ACLMessage ack = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			ack.setSender(myAgent.getAID());
			ack.addReceiver(ping.getSender());
			ack.setContent("WANT YOUR MAP");
			((AbstractDedaleAgent)myAgent).sendMessage(ack);
			System.out.println(myAgent.getLocalName()+" - ACK PING");
		}else {System.out.println(myAgent.getLocalName()+"Ping LOOOOOse");}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
