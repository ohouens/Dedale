package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class TargetBehaviour extends OneShotBehaviour{
	
	private int transition;

	public TargetBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
	}
	
	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		transition = 1;
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		
		String nextNode=null;
		if(agent.getLockCountdown() <= 0) {
			agent.changeState(ExploreMultiAgent.State.explo);
			agent.initCoalition();
			return;
		}
		
		agent.updateLC();
		
		/************************************************
		 * 				END API CALL ILUSTRATION
		 *************************************************/
		agent.move(nextNode);
		if(transition == 1)
			System.out.println(agent.getLocalName()+" - transition to SWITCH");
		else
			System.out.println(agent.getLocalName()+" - Stay in TARGET");
	}

	public int onEnd() {
		return transition;
	}
}
