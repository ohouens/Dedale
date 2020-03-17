package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import eu.su.mas.dedaleEtu.mas.behaviours.AckPingMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.PingMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceivePositionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.behaviours.SendMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosition;
import eu.su.mas.dedaleEtu.mas.behaviours.SwitchBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SynchronizationBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
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
		rewind, explo, coalition
	}
	
	private MapRepresentation myMap;
	private ACLMessage lastReceive;
	private ACLMessage lastSend;
	private HashSet<String> closedNodes;
	private List<String> agents;
	private List<String> positionMemory = new ArrayList<>();
	private int motionCounterMemory = 0;
	private int lastShareMemory = 0;
	private int lockCountdown = 0;
	private State currentState;
	private List<State> stateMemory = new ArrayList<>(Arrays.asList(State.explo));
	
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

		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}
		};

		fsm.registerFirstState(new PingMapBehaviour(this), "PingMap");
		fsm.registerState(new SwitchBehaviour(this), "Switch");
		fsm.registerState(new SynchronizationBehaviour(this), "Synchronization");
		fsm.registerState(new AckPingMapBehaviour(this), "AckPingMap");
		fsm.registerState(new ExploMultiBehaviour(this), "Exploration");
		fsm.registerState(new SendMapBehaviour(this, true), "SendOriginalMap");
		fsm.registerState(new SendMapBehaviour(this, false), "SendFusedMap");
		fsm.registerState(new ReceiveMapBehaviour(this, true), "ReceiveOriginalMap");
		fsm.registerState(new ReceiveMapBehaviour(this, false), "ReceiveFusedMap");
		
		fsm.registerTransition("PingMap", "Switch", 0);

		fsm.registerTransition("Switch", "Exploration", 0);
		fsm.registerTransition("Switch", "AckPingMap", 1);
		fsm.registerTransition("Switch", "Synchronization", 2);
		
		fsm.registerTransition("AckPingMap", "Switch", 0);
		
		fsm.registerTransition("Synchronization", "ReceiveOriginalMap", 0);
		fsm.registerTransition("Synchronization", "SendOriginalMap", 1);
		
		fsm.registerTransition("ReceiveOriginalMap", "ReceiveOriginalMap", 0);
		fsm.registerTransition("ReceiveOriginalMap", "SendFusedMap", 1);
		fsm.registerTransition("SendFusedMap", "SendFusedMap", 0);
		fsm.registerTransition("SendFusedMap", "Exploration", 1);
		
		fsm.registerTransition("SendOriginalMap", "SendOriginalMap", 0);
		fsm.registerTransition("SendOriginalMap", "ReceiveFusedMap", 1);
		fsm.registerTransition("ReceiveFusedMap", "ReceiveFusedMap", 0);
		fsm.registerTransition("ReceiveFusedMap", "Exploration", 1);

		fsm.registerTransition("Exploration", "PingMap", 0);
		
		lb.add(fsm);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		addBehaviour(new startMyBehaviours(this, lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
	
	public MapRepresentation getMap() {
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
	
	public HashSet<String> getClosedNodes(){
		return closedNodes;
	}
	
	public void setClosedNodes(HashSet<String> cn) {
		closedNodes = cn;
	}
	
	public List<String> getTeamates(){
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
	
	public State getCurrentState() {
		return currentState;
	}
	
	public void changeState(State i) {
		stateMemory.add(currentState);
		if(stateMemory.size() > MEMORYSIZE)
			stateMemory.remove(0);
		currentState = i;
	}
	
	public List<State> getStateMemory(){
		return stateMemory;
	}
	
	public void printMemory() {
		printPositionMemory();
		printStateMemory();
	}
	
	public void printPositionMemory() {
		System.out.println(getLocalName()+" - positionMemory: "+positionMemory);
	}
	
	public void printStateMemory() {
		System.out.println(getLocalName()+" - stateMemory:"+stateMemory);
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
			    		agents.add(result[i].getName().getLocalName());
//			    		System.out.println(this.getLocalName()+" FOUND AGENT "+result[i].getName().getLocalName());
		    		}
		    	}
		    	
		    }
	    }catch(FIPAException fe) {fe.printStackTrace();}
	    
	    return agents;
	}
}
