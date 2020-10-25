package subway;

import java.util.List;
import java.util.ArrayList;

public class Graph {
	
	
	public List<Subway.Station> vertics = new ArrayList<>();
	public List<List<Edge>> adj = new ArrayList<>();
	
	public static class Edge{
		public int u, v;
		public Edge(int u, int v) {
			this.u = u;
			this.v = v;
		}
		
		public boolean equal(Edge edge) {
			return (this.u == edge.u && this.v == edge.v);
		}
	}
	
	public void createGraph(List<Subway.Station> vertics, List<Edge> edges) {
		this.vertics = vertics;
		for(int i=0; i<vertics.size(); i++) {
			adj.add(new ArrayList<Edge>());
		}
		for(Edge edge:edges) {
			adj.get(edge.u).add(edge);
		}
	}
	
	public Graph() {
		
	};
	
	public Graph(List<Subway.Station> vertics, List<Edge> edge) {
		createGraph(vertics, edge);
		
	};
	

	
	
}
