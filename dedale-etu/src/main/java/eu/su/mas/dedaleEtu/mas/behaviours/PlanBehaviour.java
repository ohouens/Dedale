package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PlanBehaviour extends OneShotBehaviour{

	private int transition;
	private ExploreMultiAgent agent;
	
	public PlanBehaviour(AbstractDedaleAgent myAgent) {
		super(myAgent);
		agent = (ExploreMultiAgent)myAgent;
	}
	
	@Override
	public void action() {
		switch(agent.getCurrentState()) {
			case target:
				transition = 1;
				System.out.println(agent.getLocalName()+" - transition to TARGET");
				break;
			case hunt:
				transition = 2;
				System.out.println(agent.getLocalName()+" - transition to HUNTING");
				break;
			default:
				if(agent.isBlocked() && !agent.isInFormation()) {
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
					ACLMessage msgCoa = myAgent.receive(mt);
					if(msgCoa != null) {
						agent.setInFormation(true);
						System.out.println(agent.getLocalName()+" - Enter in coalition");
						transition = 3;
						agent.setLockCoundown(20);
						return;
					}
					transition = 1;
					System.out.println(agent.getLocalName()+" - Target mode activated");
					agent.changeState(ExploreMultiAgent.State.target);
					agent.randomTarget();
					agent.setLockCoundown(ExploreMultiAgent.SHARELOCK);
					System.out.println(agent.getLocalName()+" - target "+agent.getTarget());
					System.out.println(agent.getLocalName()+" - transition to TARGET");
					return;
				}
				if(!agent.getExploDone()) {
					transition = 0;
					System.out.println(agent.getLocalName()+" - transition to EXPLORATION");
				}else {
					if(agent.getRoute() == null)
						agent.setRoute(makeRoute());
					if(agent.getRdv() == null)
						agent.setRdv(agent.getMap().getNodeMax());
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROXY);
					ACLMessage msgHunt = myAgent.receive(mt);
					if(msgHunt != null) {
						String[] asking = msgHunt.getContent().split(":");
						String nature = asking[0];
						String target = asking[1];
						if(nature.equals("HELP")) {
							System.out.println(agent.getLocalName()+" - answer HELP in "+target);
							List<String> way = agent.getMap().getShortestPath(agent.getCurrentPosition(), target);
							if(way.size()>=1) {
								String nextMove = way.get(0);
								agent.move(nextMove);
							}
							transition = 2;
							return;
						}
//						if(nature.equals("DEAD")) {
//							System.out.println(agent.getLocalName()+" - Teamate have already DEAD this, need to move elsewhere");
//							agent.randomTarget();
//							agent.setLockCoundown(3);
//							transition = 1;
//							return;
//						}
					}
					transition = 3;
				}
				break;
		}
	}
	
	@Override
	public int onEnd() {
		return transition;
	}
	
	public List<String> makeRoute() {
		List<String> result = new ArrayList<>();
		Set<String> queue = agent.getMap().getAllNodes();
		String cursor = agent.getCurrentPosition();
		result.add(cursor);
		queue.remove(cursor);
		String origin = cursor;
		agent.setMaxSpace(1);
		while(!queue.isEmpty()) {
			Iterator<String> neighbor = agent.getMap().getNeighbor(cursor);
			String inter = null;
			while(neighbor.hasNext() && inter == null) {
				inter = neighbor.next();
				if(!queue.contains(inter))
					inter = null;
			}
			if(inter != null) {
				cursor = inter;
				result.add(cursor);
				queue.remove(cursor);
			}else {
				Collection<String> rewind = nearestLeaf(queue,result);
				queue.removeAll(rewind);
				System.out.println("rewind: "+rewind);
				if(rewind.size()+1 > agent.getMaxSpace())
					agent.setMaxSpace(rewind.size()+1);
				result.addAll(rewind);
				cursor = result.get(result.size()-1);
			}
		}
		String end = result.get(result.size()-1);
		if(!origin.equals(end)) {
			List<String> add = agent.getMap().getShortestPath(end, origin);
			add.remove(add.size()-1);
			result.addAll(add);
		}
		return result;
	}
	
	public Collection<String> rewind(Set<String> queue, List<String> result){
		List<String> buffer = new ArrayList<>();
		List<String> copy = new ArrayList<>(result);
		String cursor = null;
		String inter = null;
		int i = copy.size()-2;
		while(inter == null) {
			cursor = copy.get(i);
			buffer.add(cursor);
			Iterator<String> neighbor = agent.getMap().getNeighbor(cursor);
			while(neighbor.hasNext() && inter == null) {
				inter = neighbor.next();
				if(!queue.contains(inter))
					inter = null;
			}
			i--;
		}
		return buffer;
	}
	
	public Collection<String> nearestLeaf(Set<String> queue, List<String> result){
		List<String> buffer = new ArrayList<>();
		HashMap<List<String>, Integer> shortest = new HashMap<>();
		String cursor = result.get(result.size()-1);
		for(String node : queue) {
			List<String> l = agent.getMap().getShortestPath(cursor, node);
			shortest.put(l, l.size());
		}
		int min = -1;
		Iterator<Entry<List<String>, Integer>> iter = shortest.entrySet().iterator();
		while(iter.hasNext() && min != 2) {
			Entry<List<String>, Integer> current = iter.next();
			if((current.getValue() < min || min == -1) && current.getValue() > 0) {
				buffer = current.getKey();
				min = current.getValue();
			}
		}
		return buffer;
	}
}
