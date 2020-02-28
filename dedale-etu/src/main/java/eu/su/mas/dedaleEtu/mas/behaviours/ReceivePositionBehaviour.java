package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceivePositionBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1522841734399779465L;
	
	private boolean finished=false;
	private MapRepresentation myMap;

	public ReceivePositionBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap) {
		super(myAgent);
		this.myMap = myMap;
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = ((ExploreMultiAgent) myAgent);
		MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = myAgent.receive(msgTemplate);
		if(msg != null) {
			((ExploreMultiAgent) myAgent).setRole(1);
			System.out.println(myAgent.getLocalName()+" RECEIVE POSITION "+msg.getContent());
			String currentPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			String forbiddenPosition = msg.getContent();
			String nextNode = ((ExploreMultiAgent)this.myAgent).getNextPosition();
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iterBis=lobs.iterator();
			System.out.println(myAgent.getLocalName()+" - "+forbiddenPosition+" EQUAAALS "+nextNode);
			while(iterBis.hasNext() && (forbiddenPosition.equals(nextNode) || currentPosition.equals(nextNode))) {
				System.out.println(myAgent.getLocalName()+" - "+forbiddenPosition+" EQUAAAAAALS "+nextNode);
				nextNode = iterBis.next().getLeft();
			}
			if(nextNode.equals(forbiddenPosition))nextNode = null;
			/************************************************
			 * 				END API CALL ILUSTRATION
			 *************************************************/
			if(nextNode != null) {
				agent.setNextPosition(nextNode);
				System.out.println("current:"+currentPosition+", next:"+nextNode);
				agent.move();
				agent.setRole(0);
				System.out.println(myAgent.getLocalName()+" - "+"ROLE IS TO JUKE TEAMATE ! Code: "+agent.getRole());
			}else{
				System.out.println("BOOOOOOOOOOOOOOOOOOOOOOH");
			}
		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
