package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendMapBehaviour extends SimpleBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2606822683718779669L;
	private MapRepresentation myMap;
	private List<String> receivers;
	private boolean finished=false;

	public SendMapBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap, List<String> receivers) {
		super(myAgent);
		this.myMap = myMap;
		this.receivers = receivers;
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage ackPing = agent.receive(template);
		if(ackPing != null) {
			agent.setRole(2);
			System.out.println(agent.getLocalName()+" - ACK PING receive, SEND MAP TO "+ackPing.getSender().getLocalName());
			if(agent.getLocalName().equals("Explo1"))return;
			if(myMap == null) {
				myMap = agent.getMap();
				return;
			}
			ACLMessage sendMap = new ACLMessage(ACLMessage.PROPAGATE);
			sendMap.setSender(myAgent.getAID());
			for(String s : receivers) {
				if(!s.equals(myAgent.getLocalName())) {
					sendMap.addReceiver(new AID(s,AID.ISLOCALNAME));
				}
			}
			try {
				System.out.println(myAgent.getLocalName()+" - SEND MAP !!!!!");
				sendMap.setContentObject(myMap.serialize());
				((AbstractDedaleAgent) myAgent).sendMessage(sendMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
}
