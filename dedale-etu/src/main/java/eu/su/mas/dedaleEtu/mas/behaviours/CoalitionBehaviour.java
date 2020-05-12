package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
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
					if(y==0)
						golem = stenchPos;
					else
						golem += ":"+stenchPos;
					y++;
				}
			}
		}
		
		if(!agent.isInFormation()) {
			System.out.println(agent.getLocalName()+" - route: "+agent.getRoute());
			System.out.println(agent.getLocalName()+" - cursor: "+agent.getRouteCursor());
			System.out.println(agent.getLocalName()+" - maxSpace: "+agent.getMaxSpace());
			agent.move(agent.getRouteWay());
			System.out.println(agent.getLocalName()+" - transition to SWITCH");
		}else {
			boolean hasMove = false;
			String myPos = agent.getCurrentPosition();
			MessageTemplate mt = MessageTemplate.MatchPerformative(777777);
			ACLMessage msgCoa = myAgent.receive(mt);
			agent.getGolemBuffer().clear();
			if(msgCoa != null)
				agent.decompressCoalition(msgCoa.getContent());
			for(String g : agent.getGolemBuffer()) {
				if(agent.getMap().getEdges(myPos).contains(g)) {
					agent.move(g);
					if(agent.getLastMove()) {
						hasMove = true;
						break;
					}
				}
			}
			if(!hasMove && agent.getPlaceBuffer() != null) {
				for(String p : agent.getMap().getEdges(agent.getPlaceBuffer())) {
					if(agent.getMap().getEdges(myPos).contains(p)) {
						agent.move(p);
						if(agent.getLastMove()) {
							hasMove = true;
							break;
						}
					}
				}
			}
			for(String team : agent.getTeamates())
				agent.ping(777777, agent.compressCoalition(golem), new AID(team, AID.ISLOCALNAME));
			if(golem.equals("_") && !hasMove)
				agent.updateLC();
			
			if(agent.getLockCountdown() <= 0) {
				System.out.println(agent.getLocalName()+" - Exit in Coalition");
				agent.setInFormation(false);
				transition = 1;
			}else {
				System.out.println(agent.getLocalName()+" - Stay in Coalition");
				transition = 0;
			}
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}

}
