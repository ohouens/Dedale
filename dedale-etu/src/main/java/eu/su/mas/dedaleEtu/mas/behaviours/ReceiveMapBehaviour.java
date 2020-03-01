package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveMapBehaviour extends SimpleBehaviour{
	private boolean finished=false;
	private MapRepresentation myMap;
	
	public ReceiveMapBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap) {
		super(myAgent);
		this.myMap = myMap;
	}
	
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
		ACLMessage msgMap = myAgent.receive(mt);
		if(msgMap != null) {
			if(myMap == null) {
				myMap = agent.getMap();
				return;
			}
			SerializableSimpleGraph<String,MapAttribute> inter;
			System.out.println(myAgent.getLocalName()+" RECEIVE MAP FROM "+msgMap.getSender().getLocalName());
			try {
				inter = (SerializableSimpleGraph<String,MapAttribute>) msgMap.getContentObject();
				myMap.merge(inter);
				System.out.println(myAgent.getLocalName()+" - MAP MERGED !!!");
				agent.setRole(-10);
				ACLMessage itOK = new ACLMessage(ACLMessage.CONFIRM);
				itOK.setSender(agent.getAID());
				itOK.addReceiver(msgMap.getSender());
				itOK.setContent("IT's OKOKOKOKOKOK");
				agent.sendMessage(itOK);
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
}
