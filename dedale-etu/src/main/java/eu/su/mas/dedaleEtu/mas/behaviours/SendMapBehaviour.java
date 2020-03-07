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

public class SendMapBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2606822683718779669L;
	private MapRepresentation myMap;
	private int transition = 0;
	private boolean original;

	public SendMapBehaviour(final AbstractDedaleAgent myAgent, boolean original) {
		super(myAgent);
		this.original = original;
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
		ACLMessage checkDone = agent.receive(mt);
		if(checkDone != null) {
			transition = 1;
			if(original)
				System.out.println(agent.getLocalName()+" - transition to ReceiveFusedMAP");
			else
				System.out.println(agent.getLocalName()+" - transition to Exploration");
			return;
		}
		
		myMap = agent.getMap();
		ACLMessage sendMap = new ACLMessage(ACLMessage.PROPAGATE);
		sendMap.setSender(myAgent.getAID());
		sendMap.addReceiver(agent.getLastReceive().getSender());
		try {
			System.out.println(myAgent.getLocalName()+" - SEND MAP !!!!!");
			sendMap.setContentObject(myMap.serialize());
			((AbstractDedaleAgent) myAgent).sendMessage(sendMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
