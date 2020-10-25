package subway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class Subway {
	
	
	static class Station{ //���վ����Ϣ
		String stationName;
		
		List<String> Lines;
		
		boolean isTransfer = false;
	}
	
	static Graph graph; // ��Ÿõ���ͼ��Ϣ
	static Map<String, List<Station>> linesMap = new HashMap();
	static Map<String, Integer> stationNameToId = new HashMap();
	static Map<Integer, String> stationIdToName = new HashMap();
	static Map<String, Station> stationNameToStation = new HashMap();
	
	static void printInfo() {
		System.out.println("\n   You can not query the shortest path and info about line at the same time, and remember you have to -map first and then to use -q or -l"
				+ " and finally to -o.");
		System.out.println("       -map filepath: load the subway info at filepath");
		System.out.println("       -o filepath(.txt): save the query info at filepath(.txt)");
		System.out.println("       -q staionA stationB: query for the shortest routine between A and B");
		System.out.println("       -l line1 line2 ... linen: query for the line info");
		System.out.println("       -map filepath(.txt): load the subway info at filepath(.txt)");
		System.out.println("   for examples: java Subway -map subway.txt -q ����ͼ��� ľ�ص� [-o routine.txt]");
		
	}
	
	public static void loadSubwayMap(String filePath) {
		
		System.out.println("Loading your map!....." + filePath);
		
		List<Station> vertics = new ArrayList<>();
		List<Graph.Edge> edges = new ArrayList<>();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
			String lineTxt = null;
			
			int stationNumber = 0; // ���ڸ�ÿ��վ��עid
			
			while((lineTxt = bufferedReader.readLine())!=null) {
				String[] list = lineTxt.split(" ");
				List<Station> stations = new ArrayList<>();
				String lineName = list[0]; 
				
				int pre = 0; //pre��now���ڼ�¼�����������㣬����pre��now���Լ�¼��Ӧ�ı���Ϣ
				int now = 0;
				for(int i=1; i<list.length; i++) {
					String stationName = list[i];
					Station station;
					if(!stationNameToId.containsKey(stationName)) { // ��ӳ��ռ���û�ж�Ӧվ����˵����վ�ǵ�һ������������Ҫ����վ���г�ʼ��
						station = new Station();
						stationNameToStation.put(stationName, station);
						station.stationName = stationName;
						station.Lines = new ArrayList<>();
						station.Lines.add(lineName);
						vertics.add(station);
						
						stationNameToId.put(stationName, stationNumber); //ͬʱ����Ӧ��ӳ�����
						stationIdToName.put(stationNumber, stationName);
						stationNumber += 1;
						
					}else { //����վ���Ѵ�����ֻ��Ҫ����վ����·��Ϣ���и���
						station = stationNameToStation.get(stationName);
						station.Lines.add(lineName);
					}
					if(station.Lines.size() > 1) { //����վ����·����1ʱ˵����վ�ǿɻ���վ
						station.isTransfer = true;
					}
					stations.add(station);
					
					if(i == 1) {
						now = stationNameToId.get(stationName); //��¼��ͷ��վ��id
					}else { // ˳�ν���·�бߵ���Ϣ��¼����
						pre = now;
						now = stationNameToId.get(stationName); 
						edges.add(new Graph.Edge(pre, now));
						edges.add(new Graph.Edge(now, pre));
					}
					
				}
				linesMap.put(lineName, stations); //������·����վ���б��ӳ�䣬���ں�������·��Ϣ��ѯ
				
			}
			bufferedReader.close();
		}catch(Exception e) {
			System.out.println("Read Error: " + e);
			System.exit(0);
		}
		graph = new Graph(vertics, edges); //������վ����Ϣ�ͱ���Ϣ����ͼ
		System.out.println("Load map successfully!");
		
	}
	
	public static void getLineInfo(String lineName, String savePath){
		try {
			if(!linesMap.containsKey(lineName)) { //�жϸ�վ���Ƿ����
				throw new Exception("There is no such a line !!!");
			}
			System.out.println("The result of the query: \n");
			List<Station> stations = linesMap.get(lineName);  //ͨ����·����վ���б��ӳ��õ�����·����Ϣ
			String result = lineName + " : ";
			for(Station station: stations) {
				result += station.stationName + " ";
			}
			
			WriteFile.write(result, savePath); //д�����ݵ�ָ����savePath��
			System.out.println("Query line info successfully!");
			
		}catch(Exception e) {
			System.out.println(e);
			System.exit(0);
		}
		
		
		
	}
	
	public static List<String> returnLine(String stationA, String stationB) { //���ݳ�ʶ������������վ������ͬʱӵ�ж����·����˿�����ô��
		List<String> l1 = stationNameToStation.get(stationA).Lines; //���ȷֱ���վ��A��B����·��Ϣ
		List<String> l2 = stationNameToStation.get(stationB).Lines;
		List<String> exists = new ArrayList(l2); 
		List<String> notexists = new ArrayList(l2);
		exists.removeAll(l1); // �൱�ڴ�l2��ɾȥl2��l1���е���·
		notexists.removeAll(exists); //�൱��ɾ����l2��l1�����е�Ԫ�أ���ʱ�õ�����վ��A��B�Ĺ�����·
		return notexists;
	}
	
	public static void getShortestPath(String begin, String end, String savePath) throws Exception {
		//�����ж�Ҫ��ѯ������վ���Ƿ񶼴��ڡ��Ƿ�Ϊͬһվ��
		try {
			if(!stationNameToId.containsKey(begin)) {
				throw new Exception("There is no such an start station!!!");
				
			}
			if(!stationNameToId.containsKey(end)) {
				throw new Exception("There is no such an end station!!!");
			}
			if(begin.equals(end)) {
				throw new Exception("You are at your target place!!!");
			}
		}catch(Exception e) {
			System.out.println(e);
			System.exit(0);
		}
		
		int totalV = graph.vertics.size();
		int[] dist = new int[totalV];  //��¼��ʼվ�㵽����վ�����̾���
		int[] transCount = new int[totalV];  //��¼��ʼվ�㵽����վ������ٻ�����
		String[] line = new String[totalV];  //��¼��ʼվ�㵽����վ���·���У�����վ������������·��
		int[] parent = new int[totalV];  //��¼·������ÿ��վ���ǰһ��վ��id
		boolean[] isVis = new boolean[totalV];  //��¼�Ƿ��ѷ��ʹ���վ��
		
		for(int i=0; i<totalV; i++) {  //���ݳ�ʼ����������ͻ�����������Ϊ0
			dist[i] = Integer.MAX_VALUE;
			transCount[i] = 0;
		}
		dist[stationNameToId.get(begin)] = 0;  //��ʼ���������0�� �丸վ�㲻���ڣ�����Ϊ-1
		parent[stationNameToId.get(begin)] = -1;  
		
		int count = 0;  //��¼�������վ�����
		while(count < totalV) {
			int u = -1;  //��ǰ���ҵ������վ��
			int mindist = Integer.MAX_VALUE;
			for(int i=0; i<totalV; i++) {  //ѡ��ǰ���������վ��
				if(!isVis[i] && dist[i]<mindist) {
					u = i;
					mindist = dist[i];
				}
			}
			isVis[u] = true;  //����վ������Ϊ�ѷ���
			count ++;
			List<Graph.Edge> adjEdges = graph.adj.get(u); //��øõ���ڽӹ�ϵ
			for(Graph.Edge edge: adjEdges) {  //�����õ���ڽ���Ϣ������ɸѡ��վ�����ٵ�·�������ѡ�����������ٵ�·��
				if(!isVis[edge.v] && dist[u]+1<dist[edge.v]) {
					List<String> common = returnLine(stationIdToName.get(edge.u), stationIdToName.get(edge.v)); //�����������Ĺ�����·����Ϊ��ǰ����������·
					line[edge.v] = common.get(0); //���ڽ��ĵ�������·�ĸ���
					if(!stationIdToName.get(edge.u).equals(begin)) { //�ж�֮ǰѡ����������Ƿ�Ϊ��ʼ�㣬���������жϸöԵ��Ƿ���ͬһ����·�ϣ�����������ڽӵ�Ļ�����+1����֮����
						if((line[edge.u] != line[edge.v])  ) {
							transCount[edge.v] = transCount[edge.u] + 1;
						}else {
							transCount[edge.v] = transCount[edge.u];
						}
					}
					dist[edge.v] = dist[u] + 1; //���¸õ����͸��ӹ�ϵ
					parent[edge.v] = edge.u;
				}
				else if(!isVis[edge.v] && dist[u]+1==dist[edge.v]) { //������վ��һ�£����迼�ǻ�����
					List<String> common = returnLine(stationIdToName.get(edge.u), stationIdToName.get(edge.v));
					line[edge.v] = common.get(0);
					if(!stationIdToName.get(edge.u).equals(begin)) { 
						if((line[edge.u] != line[edge.v])  ) {  //�ж�����վ���Ƿ���ͬһ��·�к�
							if(transCount[edge.u] + 1<transCount[edge.v]) {  //����վ�㲻��ͬһ��·�ҵ�ǰ�ҵ�����·�л��������򽫸õ���Ϣ����
								transCount[edge.v] = transCount[edge.u] + 1;
								dist[edge.v] = dist[u] + 1;
								parent[edge.v] = edge.u;
							}
						}else {
							if(transCount[edge.u] < transCount[edge.v]) {  //����վ����ͬһ��·��Ҳͬ�������жϣ�ֻ��������Ҫ����+1
								transCount[edge.v] = transCount[edge.u];
								dist[edge.v] = dist[u] + 1;
								parent[edge.v] = edge.u;
							}
						}
					}
				}
			}
			
		}
		int stationCount = 0;  //��¼·���о�����վ����������������ʼ�㣩
		ArrayList<Integer> path = new ArrayList<>();  //��Ϊparent�м�¼��˳���Ƿ��ŵģ���path���ڼ�¼�����˳��
		int next = stationNameToId.get(end);
		while(parent[next] != -1) {  //�õ�����ĳ�����·
			stationCount++;
			path.add(next);
			next = parent[next];
		}
		//�鿴·���е�վ��
//		for(int i=path.size()-1; i>=0; i--) {
//			System.out.println(stationIdToName.get(path.get(i)));
//		}
		String str = begin + "  " +stationIdToName.get(path.get(path.size()-1));  //str�����ڼ�¼����������·
		String nowLine = returnLine(begin, stationIdToName.get(path.get(path.size()-1))).get(0); // ��ó�ʼ��·
		
		// �õ�����������·�Լ�������Ϣ
		for(int i=path.size()-2; i>=0; i--) {
			String name = stationIdToName.get(path.get(i));
			String last = stationIdToName.get(path.get(i+1));
			
			List<String> common = returnLine(name, last); // �õ���ǰ��վ�����һ��վ��Ĺ�ͬ��·
			
//			System.out.println(notexists+"  "+ name+"  "+ last+"  " + notexists);
			if(!common.get(0).equals(nowLine)) {  //�ж���·�Ƿ����仯��ͨ��֮ǰ�õ�������վ��Ĺ�ͬ��·����ǰ��¼���ĵ�ǰ��·��֪��·�Ƿ����仯
				str += "\n���� " + common.get(0) + "\n";  //�������仯����Ҫ��ӡ����Ϣ���Ҹ��µ�ǰ��·
				nowLine = common.get(0);  
			}
			str += "  " + name;
				
			
		}
		str += "\n�������� " + stationCount +" վ����������ʼվ�㣩\n";
		str += "�����ˣ� " + transCount[stationNameToId.get(end)] + " ��";
		WriteFile.write(str, savePath);  //д��savePath��
		System.out.println("Query the shortest path between " + begin + " and " + end + " successfully!");
//		System.out.println(str);
//		System.out.println("�������� " + stationCount +" վ����������ʼվ�㣩");
//		System.out.println("�����ˣ� " + transCount[stationNameToId.get(end)] + " ��");
		
		
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		List<String> argslist = Arrays.asList(args);
		
		if(!argslist.contains("-map")) {  //�ж��Ƿ��ж���ĵ�����Ϣ
			System.out.println("You have to load your subway map!");
			printInfo();
		}else if(argslist.contains("-q") && argslist.contains("-l")){ // �ж��Ƿ�һ�������г��ֲ�ͬ��ѯҪ��
			printInfo();
		}else if(argslist.contains("-o") && !argslist.get(argslist.size()-1).contains(".txt")) {  //�ж����б���Ҫ�������£�������ļ��Ƿ�Ϊ.txt��ʽ
			System.out.println("Your save Path should be in .txt format and in the last!");
			printInfo();
		}else if(argslist.indexOf("-q") + 2 >= argslist.size() || argslist.indexOf("-map") != 0){  //�ж϶��������Ϣ�������Ƿ��ڿ�ͷ�Լ���ѯ���·��ʱ�Ƿ�ֻ������һ��վ��
			printInfo();
		}else {  //��ȷ�����µ�ִ��
			loadSubwayMap(args[1]);  // ���������Ϣ��������ͼ
			
			if(argslist.contains("-l")) {  // �ж��ǲ�ѯ��·��Ϣ�������·��
				int start = argslist.indexOf("-l") + 1;
				for(int i=start; i<argslist.size() && !argslist.get(i).equals("-o"); i++) {  //��ѯ��·��Ϣʱ����һ���Բ�ѯ�����·����Ϣ��ֱ�� -oǰ������·��Ϣ
					if(argslist.contains("-o")) {  // �ж��Ƿ�Ҫ�󱣴棬�еĻ�����Ҫ�����·������֮�����
						getLineInfo(argslist.get(i), argslist.get(argslist.size()-1));
					}else {
						getLineInfo(argslist.get(i), "");
					}
				}
				
			}else if(argslist.contains("-q")) {
				int indexQ = argslist.indexOf("-q");  //��ȡ-qλ�����ڵõ���Ҫ��ѯ������վ�����ڵ�λ��
				if(argslist.contains("-o")) {
					getShortestPath(argslist.get(indexQ+1), argslist.get(indexQ+2), argslist.get(argslist.size()-1));
				}else {
					getShortestPath(argslist.get(indexQ+1), argslist.get(indexQ+2), "");
				}
			}
		}
		
	}

}
