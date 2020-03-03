package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceivePositionBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1522841734399779465L;

	public ReceivePositionBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap) {
		super(myAgent);
	}
	
	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = myAgent.receive(msgTemplate);
		if(msg != null) {
			System.out.println(myAgent.getLocalName()+" RECEIVE POSITION "+msg.getContent());
			String currentPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			String forbiddenPosition = msg.getContent();
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iterBis=lobs.iterator();
			System.out.println(myAgent.getLocalName()+" - "+forbiddenPosition+" EQUAAALS "+currentPosition);
			while(iterBis.hasNext() && (forbiddenPosition.equals(currentPosition) || currentPosition.equals(currentPosition))) {
				System.out.println(myAgent.getLocalName()+" - "+forbiddenPosition+" EQUAAAAAALS "+currentPosition);
				currentPosition = iterBis.next().getLeft();
			}
			if(currentPosition.equals(forbiddenPosition))currentPosition = null;
			/************************************************
			 * 				END API CALL ILUSTRATION
			 *************************************************/
			if(currentPosition != null) {
				System.out.println("current:"+currentPosition+", next:"+currentPosition);
				((AbstractDedaleAgent) myAgent).moveTo(currentPosition);
			}else{
				System.out.println("BOOOOOOOOOOOOOOOOOOOOOOH");
			}
		}
	}
}
