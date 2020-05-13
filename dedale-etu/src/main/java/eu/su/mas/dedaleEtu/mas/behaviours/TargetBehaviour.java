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
	private ExploreMultiAgent agent;

	public TargetBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
		agent = (ExploreMultiAgent)myAgent;
	}
	
	@Override
	public void action() {
		transition = 1;
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		String position = agent.getCurrentPosition();
		agent.updateView();
		
		System.out.println(agent.getLocalName()+" - target: "+agent.getTarget()+", lockdown: "+agent.getLockCountdown());
		
		if(agent.getLockCountdown() <= 0) {
			System.out.println(agent.getLocalName()+" - Target not reach");
			agent.changeState(ExploreMultiAgent.State.explo);
			agent.initCoalition();
			return;
		}
		
		if(agent.getTarget().equals(position)) {
			System.out.println(agent.getLocalName()+" - Target reach !");
			agent.changeState(ExploreMultiAgent.State.explo);
			agent.initCoalition();
			return;
		}

		String nextNode=null;
		List<String> openNodes = agent.getOpenNodes();
		List<String> sp = agent.getMap().getShortestPath(position, agent.getTarget());
		nextNode = sp.get(0);
		
		agent.move(nextNode);
		agent.updateLC();
		agent.initCoalition();
		System.out.println(agent.getLocalName()+" - transition to SWITCH");
	}

	public int onEnd() {
		return transition;
	}
}
