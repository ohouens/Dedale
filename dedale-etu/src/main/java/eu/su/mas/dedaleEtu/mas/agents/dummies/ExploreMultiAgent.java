package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import eu.su.mas.dedaleEtu.mas.behaviours.AckPingMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.CoalitionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.HuntBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PlanBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SwitchBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SynchronizationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.TargetBehaviour;
import eu.su.mas.dedaleEtu.mas.dataTools.Pair;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.tools.sniffer.Agent;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class ExploreMultiAgent extends AbstractDedaleAgent{
	private static final long serialVersionUID = -8829443829282917888L;
	
	public static final int MEMORYSIZE = 10;
	public static final int SHARELOCK = 3;
	
	public enum State{
		target, explo, coalition, hunt
	}
	
	private MapRepresentation myMap;
	private ACLMessage lastReceive;
	private ACLMessage lastSend;
	private List<String> openNodes = new ArrayList<>();;
	private Set<String> closedNodes = new HashSet<String>();
	private List<String> agents;
	private List<String> positionMemory = new ArrayList<>();
	private int motionCounterMemory = 0;
	private int lastShareMemory = 0;
	private int lockCountdown = 0;
	private int timer = 0;
	private State currentState = State.explo;
	private List<State> stateMemory = new ArrayList<>(Arrays.asList(State.explo));
	private List<String> behaviourMemory = new ArrayList<>();
	private boolean toExplo = false;
	private boolean lastMove = false;
	private boolean exploDone = false;
	private List<String> route;
	private int routeCursor = 0;
	private int maxSpace = 0;
	private Pair lastOdor; 
	private List<String> golemZones = new ArrayList<String>(); //list of positions where more than one golem was smelled at once
	
	
	private String target;
	private List<String> tunnel = new ArrayList<>();
	private List<String> leaf = new ArrayList<>();
	private boolean tunnelFlag = false;
	
	private int bufferSizeBuffer;
	private boolean exploDoneBuffer;
	
	private MapRepresentation toSend;
	private boolean inFormation=false;
	private String rdv;
	
	private String placeBuffer;
	private ArrayList<String> golemBuffer = new ArrayList<>();
	
	private FSMBehaviour fsm;
	
	public void setup() {
		super.setup();
		final Object[] args = getArguments();
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/********
		 * Registration on the DF
		 ********/
		
		DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() ); 
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "Explo" );
        sd.setName(getLocalName());
        dfd.addServices(sd);
        
        try {  
            DFService.register(this,dfd);
            System.out.println("The agent "+ getLocalName() + " has been registered");
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/

		fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}
		};

		fsm.registerFirstState(new SwitchBehaviour(this), "Switch");
		fsm.registerState(new PingMapBehaviour(this), "PingMap");
		fsm.registerState(new AckPingMapBehaviour(this), "AckPingMap");
		fsm.registerState(new SynchronizationBehaviour(this), "Synchronization");
		fsm.registerState(new SendMapBehaviour(this, true), "SendOriginalMap");
		fsm.registerState(new SendMapBehaviour(this, false), "SendFusedMap");
		fsm.registerState(new ReceiveMapBehaviour(this, true), "ReceiveOriginalMap");
		fsm.registerState(new ReceiveMapBehaviour(this, false), "ReceiveFusedMap");
		fsm.registerState(new PlanBehaviour(this), "Planification");
		fsm.registerState(new HuntBehaviour(this), "Hunting");
		fsm.registerState(new TargetBehaviour(this), "Target");
		fsm.registerState(new CoalitionBehaviour(this), "Coalition");
		fsm.registerState(new ExploMultiBehaviour(this), "Exploration");
		
		String[] all = {"Switch", "Synchronization", "SendOriginalMap", "SendFusedMap", "ReceiveOriginalMap", "ReceiveFusedMap", "Planification", "Hunting", "Target", "Exploration", "Coalition"};

		fsm.registerTransition("Switch", "Planification", 0, all);
		fsm.registerTransition("Switch", "AckPingMap", 1, all);
		fsm.registerTransition("Switch", "Synchronization", 2, all);
		fsm.registerTransition("Switch", "PingMap", 4, all);
		
		fsm.registerTransition("PingMap", "Switch", 0, all);
		
		fsm.registerTransition("AckPingMap", "Switch", 0, all);
		
		fsm.registerTransition("Synchronization", "ReceiveOriginalMap", 0, all);
		fsm.registerTransition("Synchronization", "SendOriginalMap", 1, all);
		fsm.registerTransition("Synchronization", "Switch", 2, all);

		fsm.registerTransition("SendOriginalMap", "SendOriginalMap", 0, all);
		fsm.registerTransition("SendOriginalMap", "ReceiveFusedMap", 1, all);
		fsm.registerTransition("SendOriginalMap", "Switch", 2, all);
		fsm.registerTransition("ReceiveOriginalMap", "ReceiveOriginalMap", 0, all);
		fsm.registerTransition("ReceiveOriginalMap", "SendFusedMap", 1, all);
		fsm.registerTransition("ReceiveOriginalMap", "Switch", 2, all);
		
		fsm.registerTransition("SendFusedMap", "SendFusedMap", 0, all);
		fsm.registerTransition("SendFusedMap", "Planification", 1, all);
		fsm.registerTransition("SendFusedMap", "Switch", 2, all);
		fsm.registerTransition("ReceiveFusedMap", "ReceiveFusedMap", 0, all);
		fsm.registerTransition("ReceiveFusedMap", "Planification", 1, all);
		fsm.registerTransition("ReceiveFusedMap", "Switch", 2, all);
		
		fsm.registerTransition("Planification", "Exploration", 0, all);
		fsm.registerTransition("Planification", "Target", 1, all);
		fsm.registerTransition("Planification", "Hunting", 2, all);
		fsm.registerTransition("Planification", "Coalition", 3, all);

		fsm.registerTransition("Exploration", "Exploration", 0, all);
		fsm.registerTransition("Exploration", "Switch", 1, all);
		fsm.registerTransition("Target", "Target", 0, all);
		fsm.registerTransition("Target", "Switch", 1, all);
		fsm.registerTransition("Hunting", "Hunting", 0, all);
		fsm.registerTransition("Hunting", "Switch", 1, all);
		fsm.registerTransition("Coalition", "Coalition", 0, all);
		fsm.registerTransition("Coalition", "Switch", 1, all);
		
		lb.add(fsm);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		addBehaviour(new startMyBehaviours(this, lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	public String getRdv() {
		return rdv;
	}
	
	public void setRdv(String s) {
		rdv = s;
	}
	
	public boolean isInFormation() {
		return inFormation;
	}
	
	public void setInFormation(boolean b) {
		inFormation = b;
	}
	
	public void block() {
		fsm.block();
	}
	
	public void setToSend(MapRepresentation map) {
		toSend = map;
	}
	
	public MapRepresentation getToSend() {
		return toSend;
	}
	
	public int getBufferSizeBuffer() {
		return bufferSizeBuffer;
	}
	
	public boolean getExploDoneBuffer() {
		return exploDoneBuffer;
	}
	
	public String getPlaceBuffer() {
		return placeBuffer;
	}
	
	public ArrayList<String> getGolemBuffer() {
		return golemBuffer;
	}
	
	public boolean getTunnelFlag() {
		return tunnelFlag;
	}
	
	public void setTunnelFlag(boolean b) {
		tunnelFlag = b;
	}
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String t) {
		target = t;
	}
	
	public List<String> getTunnel(){
		return tunnel;
	}
	
	public List<String> getLeaf(){
		return leaf;
	}
	
	public boolean getExploDone() {
		return exploDone;
	}
	
	public void setExploDone(boolean done) {
		exploDone = done;
	}
	
	public boolean toExplo() {
		if(toExplo)
			toExplo = false;
		else
			toExplo = true;
		return toExplo;
	}
	
	public MapRepresentation getMap() {
		if(myMap == null) {
			System.out.println(getLocalName()+" - INIT MAP");
			myMap = new MapRepresentation();
			myMap.show();
			myMap.initPartial(getTeamates());
		}
		return myMap;
	}
	
	public void setMap(MapRepresentation map) {
		myMap = map;
	}
	
	public ACLMessage getLastReceive() {
		return lastReceive;
	}
	
	public void setLastReceive(ACLMessage last) {
		lastReceive = last;
	}
	
	public ACLMessage getLastSend() {
		return lastSend;
	}
	
	public void setLastSend(ACLMessage last) {
		lastSend = last;
	}
	
	public List<String> getOpenNodes(){
		return openNodes;
	}
	
	public Set<String> getClosedNodes(){
		return closedNodes;
	}
	
	public List<String> getTeamates(){
		if(agents == null)
			agents = getListAgents();
		return agents;
	}
	
	public void setTeamates(List<String> tm) {
		agents = tm;
	}
	
	public List<String> getPositionMemory(){
		return positionMemory;
	}
	
	public void updatePositionMemory(String position){
		positionMemory.add(position);
		if(positionMemory.size() > MEMORYSIZE)
			positionMemory.remove(0);
	}
	
	public List<String> getBehaviourMemory(){
		return behaviourMemory;
	}
	
	public void updateBehaviourMemory(String behaviour){
		behaviourMemory.add(behaviour);
		if(behaviourMemory.size() > MEMORYSIZE)
			behaviourMemory.remove(0);
	}
	
	public int getMCM() {
		return motionCounterMemory;
	}
	
	public void updateMCM() {
		motionCounterMemory ++;
	}
	
	public int getLSM() {
		return lastShareMemory;
	}
	
	public void updateLSM() {
		lastShareMemory = motionCounterMemory;
	}
	
	public int getLockCountdown() {
		return lockCountdown;
	}
	
	public void setLockCoundown(int i) {
		lockCountdown = i;
	}
	
	public void updateLC() {
		lockCountdown --;
	}
	
	public void updateTimer() {
		timer++;
	}
	
	public int getTimer() {
		return timer;
	}
	
	public State getCurrentState() {
		return currentState;
	}
	
	public void changeState(State s) {
		stateMemory.add(s);
		if(stateMemory.size() > MEMORYSIZE)
			stateMemory.remove(0);
		currentState = s;
	}
	
	public List<State> getStateMemory(){
		return stateMemory;
	}
	
	public void printMemory() {
		printPositionMemory();
		printStateMemory();
		printBehaviourMemory();
	}
	
	public void printBehaviourMemory() {
		System.out.println(getLocalName()+" - behaviourMemory:\t"+behaviourMemory);
	}

	public void printPositionMemory() {
		System.out.println(getLocalName()+" - positionMemory:"+positionMemory);
	}
	
	public void printStateMemory() {
		System.out.println(getLocalName()+" - stateMemory:\t"+stateMemory);
	}
	
	public List<String> getRoute(){
		return route;
	}
	
	public void setRoute(List<String> r) {
		route = r;
	}
	
	public int getRouteCursor() {
		return routeCursor;
	}
	
	public void setRouteCursor(int i) {
		routeCursor = i;
	}
	
	public String getRouteWay() {
		if(!getLastMove()) {
			routeCursor--;
			if(routeCursor < 0)
				routeCursor = route.size()-1;
		}
		String result = route.get(routeCursor);
		routeCursor++;
		if(routeCursor >= route.size())
			routeCursor = 0;
		return result;
	}
	
	public int getMaxSpace() {
		return maxSpace;
	}
	
	public void setMaxSpace(int ms) {
		maxSpace = ms;
	}
	
	public List<String> getListAgents() {
		List<String> agents = new ArrayList<>();
		
		DFAgentDescription dfd = new DFAgentDescription();
	    //dfd.setName(getAID());
	    ServiceDescription sd = new ServiceDescription();
	    //sd.setType("Explo");
	    //sd.setName(getLocalName());

	    dfd.addServices(sd);
	    
	    try {
		    DFAgentDescription[] result = DFService.search(this, dfd);
		    if (result.length > 0) {
		    	for (int i = 0; i < result.length; i++) {
		    		if(!result[i].getName().getLocalName().equals(getLocalName())) {
		    			String name = result[i].getName().getLocalName();
			    		agents.add(name);
//			    		System.out.println(this.getLocalName()+" FOUND AGENT "+result[i].getName().getLocalName());
		    		}
		    	}
		    	
		    }
	    }catch(FIPAException fe) {fe.printStackTrace();}
	    
	    return agents;
	}
	
	public void ping(int type, String content, AID receiver) {
		ACLMessage ack = new ACLMessage(type);
		ack.setSender(getAID());
		ack.addReceiver(receiver);
		ack.setContent(content);
		sendMessage(ack);
		System.out.println(getLocalName()+" - PING: "+content);
	}
	
	public void ping(int type, String content, List<String> receivers) {
		ACLMessage ack = new ACLMessage(type);
		ack.setSender(getAID());
		for(String teamate : receivers)
			ack.addReceiver(new AID(teamate,AID.ISLOCALNAME));
		ack.setContent(content);
		sendMessage(ack);
		System.out.println(getLocalName()+" - PING: "+content);
	}
	
	public void updateView() {
		getMap();
		String myPosition = getCurrentPosition();
		if(!closedNodes.contains(myPosition))
			myMap.addNode(myPosition, MapAttribute.closed);
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=observe();//myPosition
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
		while(iter.hasNext()){
			String nodeId=iter.next().getLeft();
			if (!closedNodes.contains(nodeId) && !myPosition.equals(nodeId)){
				if (!openNodes.contains(nodeId)){
					openNodes.add(nodeId);
					myMap.addNode(nodeId, MapAttribute.open);
					myMap.addEdge(myPosition, nodeId);
				}else{
					//the node exist, but not necessarily the edge
					myMap.addEdge(myPosition, nodeId);
				}
			}
		}
	}
	
	public void move(String nextNode) {
		try {
			doWait(500);
//			if(exploDone)
//			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(nextNode != null) {
			System.out.println(getLocalName()+" - current: "+getCurrentPosition()+", next: "+nextNode);
			if(!nextNode.equals(getCurrentPosition()))
			setLastMove(moveTo(nextNode));
			System.out.println(getLocalName()+" - moveTo "+nextNode+" : "+getLastMove());
			updatePositionMemory(getCurrentPosition());
		}
	}
	
	public boolean getLastMove() {
		return lastMove;
	}
	
	private void setLastMove(boolean lm) {
		lastMove = lm;
	}
	
	public void initCoalition() {
		if(getRoute() != null)
			setRouteCursor(getRoute().indexOf(getCurrentPosition()));
	}
	
	public String compressInfo(String agent) {
		int nb = getMap().getBuffer(agent).getAllNodes().size();
		return nb+","+exploDone;
	}
	
	public void decompressInfo(String info) {
		String[] compress = info.split(",");
		bufferSizeBuffer = Integer.valueOf(compress[0]);
		exploDoneBuffer = Boolean.valueOf(compress[1]);
	}
	
	public String compressCoalition(String golem) {
		return getCurrentPosition()+","+golem;
	}
	
	public void decompressCoalition(String coalition) {
		String[] compress = coalition.split(",");
		placeBuffer = compress[0];
		String interGolem = compress[1];
		if(!interGolem.equals("_")) {
			for(String s : interGolem.split(":"))
				golemBuffer.add(s);
		}
	}
	

	public boolean isTunnel(List<String> path) {
		for(String s : path) {
			if(!getTunnel().contains(s) && !getLeaf().contains(s)) {
				setTunnelFlag(false);
				System.out.println(getLocalName()+" - tunnelFlag down");
			}
		}
		setTunnelFlag(true);
		System.out.println(getLocalName()+" - tunnelFlag raised");
		return true;
	}
	
	public boolean isBlocked() {
		String myPos = getCurrentPosition();
		if(positionMemory.size() < 3)
			return false;
		if (positionMemory.get(positionMemory.size()-1).equals(myPos) && positionMemory.get(positionMemory.size()-2).equals(myPos) && positionMemory.get(positionMemory.size()-3).equals(myPos)) {
			System.out.println(getLocalName()+" - Blocked because: " + positionMemory);
			return true;
		}
		return false;
	}
	
	public void randomTarget() {
		String target = "";
		Random r = new Random();
		if(openNodes.size() > 2) {
			target = openNodes.get(r.nextInt(openNodes.size()));
		}else{
			int cursor = r.nextInt(closedNodes.size());
			int i = 0;
			for(String s : closedNodes) {
				if(i == cursor) {
					target = s;
					break;
				}
				i++;
			}
		}
		setTarget(target);
	}
	
	public void setLastOdor(String stenchPos) {
		lastOdor = new Pair(this.timer, stenchPos);
		myMap.putOdor(stenchPos, timer);
	}
	
	public Pair getLastOdor() {
		return lastOdor;
	}
	
	public List<String> getGolemZones(){
		return this.golemZones;
	}
}
