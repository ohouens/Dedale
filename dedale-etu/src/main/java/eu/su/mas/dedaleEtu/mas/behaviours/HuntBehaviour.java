package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class HuntBehaviour extends OneShotBehaviour{
	private int transition;
	
	public HuntBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		if(!agent.getExploDone())
			agent.updateView();
		
		boolean isBlocked=true;
		for(String pos : agent.getPositionMemory()) {
			if(!pos.equals(agent.getCurrentPosition())) {
				isBlocked=false;
				break;
			}
		}
		
		String preOdor = null;
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=agent.observe();//myPosition
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2 = lobs.iterator();
		String nextNode = null;
		while (iter2.hasNext()) {
			Couple <String, List<Couple<Observation, Integer>>> StrList = iter2.next();
			List <Couple<Observation, Integer>> listObsInt = StrList.getRight();
			System.out.println("OBSERVATIONS: " + listObsInt);
			for (int i = 0; i < listObsInt.size(); i++) {
				if (listObsInt.get(i).getLeft().toString().equals("Stench")){
//					System.out.println("I can smell the golem from here!");
					String stenchPos = StrList.getLeft();
//					System.out.println("Odor position: " + stenchPos);
					preOdor = nextNode;
					nextNode = stenchPos;
					// TODO: il faut s'assurer que la position de l'odeur est la position du golem pour mettre golemBlocked à true
					// càd on doit verifier que la case où on veut aller est occupée
				}
			}
		}
		
		if(isBlocked && nextNode!=null) {
			agent.ping(ACLMessage.PROXY, "DEAD:"+nextNode, agent.getTeamates());
			System.out.println(agent.getLocalName()+" - HUNTING done");
			transition = 0;
			return;
		}
		
		if(preOdor != null)
			agent.ping(ACLMessage.PROXY, "HELP:"+preOdor, agent.getTeamates());
		
		if(nextNode == null) {
			transition = 1;
			agent.changeState(ExploreMultiAgent.State.explo);
			agent.initCoalition();
			System.out.println(agent.getLocalName()+" - HUNTING Mode desactivated");
			System.out.println(agent.getLocalName()+" - transition to SWITCH");
			return;
		}
		
		//list of observations associated to the currentPosition
		System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
		
		
		agent.move(nextNode);
		System.out.println(agent.getLocalName()+" - continue HUNTING");
		transition = 0;
	}

	public int onEnd() {
		return transition;
	}
}
