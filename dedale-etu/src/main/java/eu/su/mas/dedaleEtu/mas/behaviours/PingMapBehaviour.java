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
		
		if(agent.getMap() == null)
			agent.setMap(new MapRepresentation());
		
		ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
		ping.setSender(myAgent.getAID());
		for(String s : agents) {
			ping.addReceiver(new AID(s, AID.ISLOCALNAME));
			System.out.println("addReceiver ::: "+s);
		}
		Date date= new Date();
		long time = date.getTime();
		ping.setContent(String.valueOf(time));
//		ping.setContent("WANT MY MAP ?");
		agent.setLastSend(ping);
		myAgent.send(ping);
		System.out.println(myAgent.getLocalName()+" - ACK");
		System.out.println(agent.getLocalName()+" - transition to SWITCH");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
