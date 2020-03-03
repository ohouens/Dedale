package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.CyclicBehaviour;
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
	private boolean sendMergedMap;
	
	public ReceiveMapBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap, boolean sendMergedMap) {
		super(myAgent);
		this.myMap = myMap;
		this.sendMergedMap = sendMergedMap;
	}
	
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
		ACLMessage msgMap = myAgent.receive(mt);
		if(msgMap != null) {
			transition = 1;
			if(sendMergedMap)
				System.out.println(agent.getLocalName()+" - transition from RECEIVEMAP to SendMap");
			else
				System.out.println(agent.getLocalName()+" - transition from RECEIVEMAP to Explo");
			if(myMap == null) {
				System.out.println(agent.getLocalName()+" - Petit probleme de map");
				myMap = agent.getMap();
			}
			SerializableSimpleGraph<String,MapAttribute> inter;
			System.out.println(myAgent.getLocalName()+" RECEIVE MAP FROM "+msgMap.getSender().getLocalName());
			try {
				inter = (SerializableSimpleGraph<String,MapAttribute>) msgMap.getContentObject();
				myMap.merge(inter);
				System.out.println(myAgent.getLocalName()+" - MAP MERGED !!!");
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
}
