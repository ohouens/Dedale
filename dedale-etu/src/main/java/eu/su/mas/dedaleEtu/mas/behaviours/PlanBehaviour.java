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
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent.State;
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
		if(agent.isBlocked()) {
			agent.ping(ACLMessage.NOT_UNDERSTOOD, agent.getCurrentPosition(), agent.getTeamates());
			agent.doWait(1000);
			ACLMessage msg = new ACLMessage(ACLMessage.CANCEL);
			int i = 0;
			while(msg != null) {
				MessageTemplate interlock = MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);
				msg = myAgent.receive(interlock);
				if(msg != null) {
//					i++;
//					System.out.println(agent.getLocalName()+" - "+i);
					if(agent.getLastTry().equals(msg.getContent())) {
						agent.changeState(ExploreMultiAgent.State.target);
						agent.randomTarget();
						agent.setLockCoundown(5);
						transition = 1;
						System.out.println(agent.getLocalName()+" - INTERLOCKING with "+msg.getSender().getLocalName()+" in "+msg.getContent());
	//					System.out.println(agent.getLocalName()+" - myPos: "+agent.getCurrentPosition()+", lastTry:"+msg.getContent());
						return;
					}
				}
			}
		}
		MessageTemplate help = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
		ACLMessage msg = myAgent.receive(help);
		if(msg != null && agent.getLastMove() && agent.getExploDone()) {
			agent.changeState(ExploreMultiAgent.State.target);
			agent.setTarget(msg.getContent());
			agent.setLockCoundown(7);
			transition = 1;
			System.out.println(agent.getLocalName()+" - Answer HELP From"+msg.getSender().getLocalName()+" in "+msg.getContent());
			return;
		}
		switch(agent.getCurrentState()) {
			case target:
				transition = 1;
//				System.out.println(agent.getLocalName()+" - transition to TARGET");
				break;
			case hunt:
				transition = 2;
//				System.out.println(agent.getLocalName()+" - transition to HUNTING");
				break;
			default:
				if(!agent.getExploDone()) {
					transition = 0;
//					System.out.println(agent.getLocalName()+" - transition to EXPLORATION");
				}else {
					if(agent.getRoute() == null)
						agent.setRoute(makeRoute());
//					System.out.println(agent.getLocalName()+" - target "+agent.getTarget());
//					System.out.println(agent.getLocalName()+" - transition to TARGET");
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
//				System.out.println("rewind: "+rewind);
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
