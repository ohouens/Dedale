package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveMapBehaviour extends OneShotBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1495684385089527428L;
	private MapRepresentation myMap;
	private int transition = 0;
	private boolean original;
	
	public ReceiveMapBehaviour(final AbstractDedaleAgent myAgent, boolean original) {
		super(myAgent);
		this.original = original;
	}
	
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
		ACLMessage msgMap = myAgent.receive(mt);
		if(msgMap != null) {
			transition = 1;
			myMap = agent.getMap();
			SerializableSimpleGraph<String,MapAttribute> inter;
			System.out.println(myAgent.getLocalName()+" RECEIVE MAP FROM "+msgMap.getSender().getLocalName());
			
			try {
				inter = (SerializableSimpleGraph<String,MapAttribute>) msgMap.getContentObject();
				if(original) {	
					agent.setMap(myMap.merge(inter));
					System.out.println(myAgent.getLocalName()+" - MAP MERGED !!!");
				}else{
					MapRepresentation map = new MapRepresentation();
					agent.setMap(map.merge(inter));
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			ACLMessage ackMap = new ACLMessage(ACLMessage.CONFIRM);
			ackMap.setSender(agent.getAID());
			ackMap.addReceiver(msgMap.getSender());
			ackMap.setContent("ackMap");
			agent.sendMessage(ackMap);
			System.out.println(agent.getLocalName()+" - AckMap");
			if(original)
				System.out.println(agent.getLocalName()+" - transition to SendFusedMap");
			else
				System.out.println(agent.getLocalName()+" - transition to Exploration");
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
