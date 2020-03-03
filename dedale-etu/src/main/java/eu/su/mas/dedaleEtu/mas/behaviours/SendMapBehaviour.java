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
	private List<String> receivers;
	private boolean finished=false;
	private int transition = 0;
	private boolean receiveMerged;

	public SendMapBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap, List<String> receivers, boolean receiveMerged) {
		super(myAgent);
		this.myMap = myMap;
		this.receivers = receivers;
		this.receiveMerged = receiveMerged;
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		if(myMap == null) {
			System.out.println("Petit probleme de map ici");
			myMap = agent.getMap();
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
			transition = 1;
			if(receiveMerged)
				System.out.println(agent.getLocalName()+" - transition to RECEIVEMAP");
			else
				System.out.println(agent.getLocalName()+" - transition to Explo");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
