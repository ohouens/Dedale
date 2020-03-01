package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AckPingMapBehaviour extends SimpleBehaviour{
	
	private boolean finished=false;

	public AckPingMapBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage ping = myAgent.receive(template);
		if(ping != null) {
			ACLMessage ack = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			ack.setSender(myAgent.getAID());
			ack.addReceiver(ping.getSender());
			ack.setContent("WANT YOUR MAP");
			((AbstractDedaleAgent)myAgent).sendMessage(ack);
			System.out.println(myAgent.getLocalName()+" - ACK PING");
			agent.setRole(2);
		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
