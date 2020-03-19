package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Date;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SwitchBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = 7899429217690526513L;
	
	private int transition=0;
	
	public SwitchBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		transition = 0;
		
		MessageTemplate tap = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage testAckPing = agent.receive(tap);
		if(testAckPing != null) {
			transition = 2;
			agent.setLastReceive(testAckPing);
			System.out.println(agent.getLocalName()+" - transition to SYNCHRONIZATION");
			return;
		}
		
		MessageTemplate tp = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage testPing = agent.receive(tp);
		if(testPing != null) {
			transition = 1;
			agent.setLastReceive(testPing);
			System.out.println(agent.getLocalName()+" - transition to ACKPING");
			return;
		}
		
		if(agent.toExplo())
			System.out.println(agent.getLocalName()+" - transition to EXPLO");
		else {
			transition = 4;
			System.out.println(agent.getLocalName()+" - transition to PING");
		}
	}

	@Override
	public int onEnd() {
		return transition;
	}
}
