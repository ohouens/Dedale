package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class PingMapBehaviour extends TickerBehaviour{
	
	private List<String> agents;
	
	public PingMapBehaviour(final AbstractDedaleAgent myAgent, List<String> agents) {
		super(myAgent, 3000);
		this.agents = agents;
		this.agents.remove(myAgent.getLocalName());
	}

	private static final long serialVersionUID = 8094154711157901076L;

	@Override
	protected void onTick() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		if(agent.getRole() >= 0) {
			ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
			ping.setSender(myAgent.getAID());
			for(String s : agents) {
				ping.addReceiver(new AID(s, AID.ISLOCALNAME));
				System.out.println("addReceiver ::: "+s);
			}
			ping.setContent("WANT MY MAP ?");
			myAgent.send(ping);
			System.out.println(myAgent.getLocalName()+" - ACK");
		}else {
			System.out.println(agent.getLocalName()+ " - CODE: "+agent.getRole());
		}
	}

}
