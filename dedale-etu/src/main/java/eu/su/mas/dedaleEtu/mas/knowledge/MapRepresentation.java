package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;
import eu.su.mas.dedaleEtu.mas.dataTools.Pair;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import javafx.application.Platform;

/**
 * <pre>
 * This simple topology representation only deals with the graph, not its content.
 * The knowledge representation is not well written (at all), it is just given as a minimal example.
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * </pre>
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {
		agent,open,closed,add
	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle_add = "node.add {"+"fill-color: orange;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open+nodeStyle_add;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration
	
	private Set<String> nodes = new HashSet<>();
	private HashMap<String, Set<String>> graph = new HashMap<>();
	
	private HashMap<String, MapRepresentation> buffer = new HashMap<>();
	private List<String> agents;
	
	private Set<String> closedNodes = new HashSet<>();
	private String nodeMax;
	private HashMap<String, Integer> odors = new HashMap<>();


	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

//		Platform.runLater(() -> {
//			openGui();
//		});
	
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}
	
	public void show() {
		Platform.runLater(() -> {
			openGui();
		});
	}
	
	public void initPartial(List<String> agents) {
		this.agents = agents;
		for(String name : agents)
    		buffer.put(name, new MapRepresentation());
	}
	
	public MapRepresentation getBuffer(String name) {
		return buffer.get(name);
	}
	
	public void putOdor(String pos, int timer){
		odors.put(pos, timer);
		if(buffer.size() > 0) {
			for(String name : agents)
				buffer.get(name).putOdor(pos, timer);
		}
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id Id of the node
	 * @param mapAttribute associated state of the node
	 */
	public void addNode(String id,MapAttribute mapAttribute){
		//Partial sharing
		if(buffer.size() > 0) {
			for(String name : agents)
				buffer.get(name).addNode(id, mapAttribute);
		}
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
			
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
		nodes.add(id);
		//adding closed node
		if(mapAttribute == MapAttribute.closed && !this.closedNodes.contains(id)) {
			this.closedNodes.add(id);
//			System.out.println(id+"  CLOOOOOOOOOSEDD");
		}
	}

	/**
	 * Add the edge if not already existing.
	 * @param idNode1 one side of the edge
	 * @param idNode2 the other side of the edge
	 */
	public void addEdge(String idNode1,String idNode2){
		try {
			//Partial sharing
			if(buffer.size() > 0) {
				for(String name : agents) 
					buffer.get(name).addEdge(idNode1, idNode2);
			}
			//Security for post delete buffer
			if(!nodes.contains(idNode1))
				addNode(idNode1, MapAttribute.open);
			if(!nodes.contains(idNode2))
				addNode(idNode2, MapAttribute.open);
			//Actual edge adding
			this.nbEdges++;
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
			if(graph.get(idNode1) == null)
				graph.put(idNode1, new HashSet<>());
			if(graph.get(idNode2) == null)
				graph.put(idNode2, new HashSet<>());
			graph.get(idNode1).add(idNode2);
			graph.get(idNode2).add(idNode1);
		}catch (EdgeRejectedException e){
			//Do not add an already existing one
			this.nbEdges--;
		}
	}

	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow
	 */
	public List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if(shortestPath.size() > 0)
			shortestPath.remove(0);//remove the current position
		return shortestPath;
	}

	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),(MapAttribute)n.getAttribute("ui.class"));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}

		closeGui();

		this.g=null;

	}
	
	public String serialize() {
		String result = "";
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			if(closedNodes.contains(n.getId())) 
				result += n.getId()+":closed,";
			else
				result += n.getId()+":open,";
		}
		
		result += "|";
		
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			result += sn.getId()+":"+tn.getId()+",";
		}
		
		result += "|";
		
		Iterator<Entry<String, Integer>> iterO = this.odors.entrySet().iterator();
		while(iterO.hasNext()) {
			Entry<String, Integer> o = iterO.next();
			result += o.getKey()+":"+o.getValue()+",";
		}
//		System.out.println("result::::::::: "+result);
		return result;
	}
	
	public void merge(String sm, Set<String> closedNodes, List<String> openNodes) {
		String[] tab = sm.split("\\|");
		String listNodes = ",";
		String listEdges = ",";
		String listOdors = ",";
		if(tab.length >= 1)
			listNodes = tab[0];
		if(tab.length >= 2)
			listEdges = tab[1];
		if(tab.length >= 3)
			listOdors = tab[2];
		
		for(String node : listNodes.split(",")) {
			if(!node.equals("")) {
				String position = node.split(":")[0];
				System.out.println("MERGE - pos:"+position+","+node.split(":")[1]+" cn:"+closedNodes+" on:"+openNodes);
				if(node.split(":")[1].equals("closed")) {
					if(!closedNodes.contains(position)) {
						closedNodes.add(position);
						openNodes.remove(position);
						addNode(position, MapAttribute.closed);
						System.out.println("MERGE - HIT CN");
					}else System.out.println("MERGE - MISS");
				}else{
					if(!closedNodes.contains(position) && !openNodes.contains(position)) {
						openNodes.add(position);
						addNode(position, MapAttribute.open);
						System.out.println("MERGE - HIT ON");
					}else System.out.println("MERGE - MISS");
				}
			}
		}

		System.out.println("MERGE - edges: "+listEdges);
		for(String edge : listEdges.split(",")){
			if(!edge.equals("")) {
				addEdge(edge.split(":")[0], edge.split(":")[1]);
			}
		}
		
		for(String odor : listOdors.split(",")) {
			if(!odor.contentEquals("")) {
				String id = odor.split(":")[0];
				int time = Integer.valueOf(odor.split(":")[1]);
				System.out.println("MERGE - odor:"+id+","+time);
				if(!odors.containsKey(id)) {
					odors.put(id, time);
					System.out.println("MERGE - HIT");
				}
				else {
					if(odors.get(id) < time) {
						odors.put(id,time);
						System.out.println("MERGE - HIT");
					}else  System.out.println("MERGE - MISS");
				}
			}
		}
	}
	
	public Set<String> getAllNodes() {
		return new HashSet<>(nodes);
	}
	
	public Iterator<String> getNeighbor(String node){
		return graph.get(node).iterator();
	}
	
	public Set<String> getEdges(String node){
		return graph.get(node);
	}

	
	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);////GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);
		g.display();
	}

	public void resetBuffer(String name) {
		buffer.put(name, new MapRepresentation());
	}
}