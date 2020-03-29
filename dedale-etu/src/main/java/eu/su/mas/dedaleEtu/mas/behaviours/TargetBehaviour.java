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
		transition = 0;
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		
		String nextNode=null;
		if(agent.getLockCountdown() <= 0) {
			agent.changeState(ExploreMultiAgent.State.explo);
			transition=1;
			return;
		}
		int size = agent.getPositionMemory().size();
		List<String> pm = agent.getPositionMemory();
		if(size >= 2 && !pm.get(size-2).equals(pm.get(size-1)) && agent.getLastMove()) {
			nextNode = agent.getPositionMemory().remove((int)size-1);
			System.out.println(agent.getLocalName()+" - new REWIND position "+nextNode);
		}else {
			agent.getPositionMemory().clear();
			Random random = new Random();
			int index = random.nextInt(lobs.size());
			nextNode = lobs.get(index).getLeft(); 
			System.out.println(agent.getLocalName()+" - new STOCHASTIC position "+nextNode);
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
