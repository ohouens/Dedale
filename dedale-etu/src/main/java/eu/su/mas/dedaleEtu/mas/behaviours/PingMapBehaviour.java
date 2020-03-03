package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PingMapBehaviour extends OneShotBehaviour{
	
	private List<String> agents;
	private int transition = 0;
	
	public PingMapBehaviour(final AbstractDedaleAgent myAgent, List<String> agents) {
		super(myAgent);
		this.agents = agents;
		this.agents.remove(myAgent.getLocalName());
	}

	private static final long serialVersionUID = 8094154711157901076L;

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		
		MessageTemplate tp = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage testPing = agent.receive(tp);
		if(testPing != null) {
			transition = 1;
			System.out.println(agent.getLocalName()+" - transition to ACKPING");
			return;
		}
		
		ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
		ping.setSender(myAgent.getAID());
		for(String s : agents) {
			ping.addReceiver(new AID(s, AID.ISLOCALNAME));
			System.out.println("addReceiver ::: "+s);
		}
		ping.setContent("WANT MY MAP ?");
		myAgent.send(ping);
		System.out.println(myAgent.getLocalName()+" - ACK");
		System.out.println(agent.getLocalName()+" - transition to EXPLO");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
