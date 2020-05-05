package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Date;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class PingMapBehaviour extends OneShotBehaviour{
	
	private int transition = 0;
	
	public PingMapBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	private static final long serialVersionUID = 8094154711157901076L;

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		
		if(agent.getTeamates() == null)
			agent.setTeamates(agent.getListAgents());
		List<String>agents = agent.getTeamates();
		
		if(agent.getMap() == null)
			agent.setMap(new MapRepresentation());
		
		
		for(String s : agents) {
			ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
			ping.setSender(myAgent.getAID());
			ping.addReceiver(new AID(s, AID.ISLOCALNAME));
			System.out.println(agent.getLocalName()+" - addReceiver ::: "+s);
			ping.setContent(agent.compressInfo(s));
			agent.setLastSend(ping);
			myAgent.send(ping);
		}
		System.out.println(myAgent.getLocalName()+" - PING");
		System.out.println(agent.getLocalName()+" - transition to SWITCH");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
