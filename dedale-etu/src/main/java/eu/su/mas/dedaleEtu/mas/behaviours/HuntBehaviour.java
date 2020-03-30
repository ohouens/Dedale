package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;

public class HuntBehaviour extends OneShotBehaviour{
	private int transition;
	
	public HuntBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		agent.updateView();
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=agent.observe();//myPosition
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2 = lobs.iterator();
		String nextNode = null;
		transition = 0;
		
		while (iter2.hasNext()) {
			Couple <String, List<Couple<Observation, Integer>>> StrList = iter2.next();
			List <Couple<Observation, Integer>> listObsInt = StrList.getRight();
			System.out.println("OBSERVATIONS: " + listObsInt);
			for (int i = 0; i < listObsInt.size(); i++) {
				if (listObsInt.get(i).getLeft().toString().equals("Stench")){
					System.out.println("I can smell the golem from here!");
					String stenchPos = StrList.getLeft();
					System.out.println("Odor position: " + stenchPos);
					nextNode = stenchPos;
					// TODO: il faut s'assurer que la position de l'odeur est la position du golem pour mettre golemBlocked à true
					// càd on doit verifier que la case où on veut aller est occupée
				}
			}
		}
		
		if(nextNode == null) {
			transition = 1;
			agent.changeState(ExploreMultiAgent.State.explo);
			System.out.println(agent.getLocalName()+" - HUNTING Mode desactivated");
			System.out.println(agent.getLocalName()+" - transition to SWITCH");
			return;
		}
		
		//list of observations associated to the currentPosition
		System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
		
		/************************************************
		 * 				END API CALL ILUSTRATION
		 *************************************************/
		agent.move(nextNode);
		System.out.println(agent.getLocalName()+" - continue HUNTING");
	
	}

	public int onEnd() {
		return transition;
	}
}
