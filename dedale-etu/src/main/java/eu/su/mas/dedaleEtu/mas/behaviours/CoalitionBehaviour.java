package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;

public class CoalitionBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	
	private int transition=1;
	
	public CoalitionBehaviour(ExploreMultiAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		System.out.println(agent.getLocalName()+" - route: "+agent.getRoute()+", cursor: "+agent.getRouteCursor());
		
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=agent.observe().iterator();
		while(iter.hasNext()){
			Couple<String, List<Couple<Observation, Integer>>> couple = iter.next();
			String nodeId=couple.getLeft();
			List <Couple<Observation, Integer>> listObsInt = couple.getRight();
			
			System.out.println("OBSERVATIONS: " + listObsInt);
			for (int i = 0; i < listObsInt.size(); i++) {
				if (listObsInt.get(i).getLeft().toString().equals("Stench")){
					System.out.println(agent.getLocalName()+" - I can smell the golem from here!");
					String stenchPos = couple.getLeft();
					System.out.println(agent.getLocalName()+" - Odor position: " + stenchPos);
					agent.changeState(ExploreMultiAgent.State.hunt);
					System.out.println(agent.getLocalName()+" - HUNTING Mode activated");
					System.out.println(agent.getLocalName()+" - transition to SWITCH");
					return;
				}
			}
		}
		
		/************************************************
		 * 				END API CALL ILUSTRATION
		 *************************************************/
		agent.move(agent.getRouteWay());
		if(transition == 1)
			System.out.println(agent.getLocalName()+" - transition to SWITCH");
		else
			System.out.println(agent.getLocalName()+" - Stay in Coalition");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}

}
