package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent.State;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HuntBehaviour extends OneShotBehaviour{
	private int transition;
	
	public HuntBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		ExploreMultiAgent agent = (ExploreMultiAgent)myAgent;
		String nextNode = null;
		if(!agent.getExploDone())
			agent.updateView();
		if(agent.getLastMove()) {
			if(agent.getDejaVu().size()>=23)
				agent.getDejaVu().clear();
			agent.getDejaVu().add(agent.getCurrentPosition());
		
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=agent.observe();//myPosition
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2 = lobs.iterator();
	//		String nextNode = null;
			while (iter2.hasNext()) {
				Couple <String, List<Couple<Observation, Integer>>> StrList = iter2.next();
				List <Couple<Observation, Integer>> listObsInt = StrList.getRight();
	//			System.out.println("OBSERVATIONS: " + listObsInt);
				for (int i = 0; i < listObsInt.size(); i++) {
					if (listObsInt.get(i).getLeft().toString().equals("Stench")){
	//					System.out.println("I can smell the golem from here!");
						String stenchPos = StrList.getLeft();
						if(!agent.getDejaVu().contains(stenchPos))
							nextNode = stenchPos;
						// TODO: il faut s'assurer que la position de l'odeur est la position du golem pour mettre golemBlocked à true
						// càd on doit verifier que la case où on veut aller est occupée
					}
				}
			}
		}else {
			nextNode = agent.getLastTry();
		}
		
		if(nextNode == null) {
			transition = 1;
			agent.changeState(ExploreMultiAgent.State.explo);
			agent.initCoalition();
			agent.getDejaVu().clear();
			System.out.println(agent.getLocalName()+" - HUNTING Mode desactivated");
//			System.out.println(agent.getLocalName()+" - transition to SWITCH");
			return;
		}
		
		if(agent.isBlocked()) {
			agent.ping(ACLMessage.INFORM_REF, nextNode, agent.getTeamates());
//			agent.move(agent.getGuess());
			System.out.println(agent.getLocalName()+" - GUESS in "+nextNode);
//				agent.doWait(2000);
//			transition=0;
		}
		
		//list of observations associated to the currentPosition
//		System.out.println(this.myAgent.getLocalName()+" - State of the observations : "+lobs);
		
//		System.out.println(agent.getLocalName()+" - continue HUNTING");
		transition = 1;
		agent.move(nextNode);
	}

	public int onEnd() {
		return transition;
	}
}
