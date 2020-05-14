package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent.State;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CoalitionBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	
	private int transition=1;
	
	public CoalitionBehaviour(ExploreMultiAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		
		String golem = "_";
		String hunt = null;
		int y = 0;
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
					if(hunt == null)
						hunt = stenchPos;
					if(y==0)
						golem = stenchPos;
					else
						golem += ":"+stenchPos;
					y++;
				}
			}
		}
		
		System.out.println(agent.getLocalName()+" - route: "+agent.getRoute());
		System.out.println(agent.getLocalName()+" - cursor: "+agent.getRouteCursor());
		System.out.println(agent.getLocalName()+" - maxSpace: "+agent.getMaxSpace());
		if(hunt!=null) {
			agent.changeState(State.hunt);
			System.out.println(agent.getLocalName()+" - transition to HUNTING");
		}
		agent.move(agent.getRouteWay());
		System.out.println(agent.getLocalName()+" - transition to SWITCH");
	}
	
	@Override
	public int onEnd() {
		return transition;
	}

}
