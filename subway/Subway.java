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
	
	
	static class Station{ //存放站点信息
		String stationName;
		
		List<String> Lines;
		
		boolean isTransfer = false;
	}
	
	static Graph graph; // 存放该地铁图信息
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
		System.out.println("   for examples: java Subway -map subway.txt -q 国家图书馆 木樨地 [-o routine.txt]");
		
	}
	
	public static void loadSubwayMap(String filePath) {
		
		System.out.println("Loading your map!....." + filePath);
		
		List<Station> vertics = new ArrayList<>();
		List<Graph.Edge> edges = new ArrayList<>();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8"));
			String lineTxt = null;
			
			int stationNumber = 0; // 用于给每个站标注id
			
			while((lineTxt = bufferedReader.readLine())!=null) {
				String[] list = lineTxt.split(" ");
				List<Station> stations = new ArrayList<>();
				String lineName = list[0]; 
				
				int pre = 0; //pre和now用于记录相连的两个点，利用pre和now可以记录相应的边信息
				int now = 0;
				for(int i=1; i<list.length; i++) {
					String stationName = list[i];
					Station station;
					if(!stationNameToId.containsKey(stationName)) { // 若映射空间中没有对应站名则说明该站是第一次遇见，则需要给该站进行初始化
						station = new Station();
						stationNameToStation.put(stationName, station);
						station.stationName = stationName;
						station.Lines = new ArrayList<>();
						station.Lines.add(lineName);
						vertics.add(station);
						
						stationNameToId.put(stationName, stationNumber); //同时给相应的映射更新
						stationIdToName.put(stationNumber, stationName);
						stationNumber += 1;
						
					}else { //若该站早已存在则只需要给该站的线路信息进行更新
						station = stationNameToStation.get(stationName);
						station.Lines.add(lineName);
					}
					if(station.Lines.size() > 1) { //当该站的线路多于1时说明该站是可换乘站
						station.isTransfer = true;
					}
					stations.add(station);
					
					if(i == 1) {
						now = stationNameToId.get(stationName); //记录开头的站点id
					}else { // 顺次将铁路中边的信息记录下来
						pre = now;
						now = stationNameToId.get(stationName); 
						edges.add(new Graph.Edge(pre, now));
						edges.add(new Graph.Edge(now, pre));
					}
					
				}
				linesMap.put(lineName, stations); //构建线路名到站点列表的映射，用于后续的线路信息查询
				
			}
			bufferedReader.close();
		}catch(Exception e) {
			System.out.println("Read Error: " + e);
			System.exit(0);
		}
		graph = new Graph(vertics, edges); //把所有站点信息和边信息构成图
		System.out.println("Load map successfully!");
		
	}
	
	public static void getLineInfo(String lineName, String savePath){
		try {
			if(!linesMap.containsKey(lineName)) { //判断该站点是否存在
				throw new Exception("There is no such a line !!!");
			}
			System.out.println("The result of the query: \n");
			List<Station> stations = linesMap.get(lineName);  //通过线路名到站点列表的映射得到该线路的信息
			String result = lineName + " : ";
			for(Station station: stations) {
				result += station.stationName + " ";
			}
			
			WriteFile.write(result, savePath); //写入数据到指定的savePath中
			System.out.println("Query line info successfully!");
			
		}catch(Exception e) {
			System.out.println(e);
			System.exit(0);
		}
		
		
		
	}
	
	public static List<String> returnLine(String stationA, String stationB) { //根据常识：相连的两个站不可能同时拥有多个线路，因此可以这么做
		List<String> l1 = stationNameToStation.get(stationA).Lines; //首先分别获得站点A和B的线路信息
		List<String> l2 = stationNameToStation.get(stationB).Lines;
		List<String> exists = new ArrayList(l2); 
		List<String> notexists = new ArrayList(l2);
		exists.removeAll(l1); // 相当于从l2中删去l2和l1共有的线路
		notexists.removeAll(exists); //相当于删除掉l2和l1不共有的元素，此时得到就是站点A和B的共有线路
		return notexists;
	}
	
	public static void getShortestPath(String begin, String end, String savePath) throws Exception {
		//首先判断要查询的两个站点是否都存在、是否为同一站点
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
		int[] dist = new int[totalV];  //记录起始站点到所有站点的最短距离
		int[] transCount = new int[totalV];  //记录起始站点到所有站点的最少换乘数
		String[] line = new String[totalV];  //记录起始站点到所有站点的路径中，各个站点所乘坐的线路名
		int[] parent = new int[totalV];  //记录路径，即每个站点的前一个站点id
		boolean[] isVis = new boolean[totalV];  //记录是否已访问过该站点
		
		for(int i=0; i<totalV; i++) {  //数据初始化，将距离和换乘数都设置为0
			dist[i] = Integer.MAX_VALUE;
			transCount[i] = 0;
		}
		dist[stationNameToId.get(begin)] = 0;  //起始点距离设置0， 其父站点不存在，设置为-1
		parent[stationNameToId.get(begin)] = -1;  
		
		int count = 0;  //记录处理过的站点个数
		while(count < totalV) {
			int u = -1;  //当前查找到的最近站点
			int mindist = Integer.MAX_VALUE;
			for(int i=0; i<totalV; i++) {  //选择当前距离最近的站点
				if(!isVis[i] && dist[i]<mindist) {
					u = i;
					mindist = dist[i];
				}
			}
			isVis[u] = true;  //将该站点设置为已访问
			count ++;
			List<Graph.Edge> adjEdges = graph.adj.get(u); //获得该点的邻接关系
			for(Graph.Edge edge: adjEdges) {  //遍历该点的邻接信息，优先筛选出站数最少的路径，其次选出换乘数最少的路径
				if(!isVis[edge.v] && dist[u]+1<dist[edge.v]) {
					List<String> common = returnLine(stationIdToName.get(edge.u), stationIdToName.get(edge.v)); //获得相邻两点的公共线路，即为当前所乘坐的线路
					line[edge.v] = common.get(0); //将邻近的点所在线路的更新
					if(!stationIdToName.get(edge.u).equals(begin)) { //判断之前选出的最近点是否为起始点，若不是则判断该对点是否在同一个线路上，若不是则该邻接点的换乘数+1，反之不变
						if((line[edge.u] != line[edge.v])  ) {
							transCount[edge.v] = transCount[edge.u] + 1;
						}else {
							transCount[edge.v] = transCount[edge.u];
						}
					}
					dist[edge.v] = dist[u] + 1; //更新该点距离和父子关系
					parent[edge.v] = edge.u;
				}
				else if(!isVis[edge.v] && dist[u]+1==dist[edge.v]) { //若所花站数一致，则需考虑换乘数
					List<String> common = returnLine(stationIdToName.get(edge.u), stationIdToName.get(edge.v));
					line[edge.v] = common.get(0);
					if(!stationIdToName.get(edge.u).equals(begin)) { 
						if((line[edge.u] != line[edge.v])  ) {  //判断两个站点是否在同一线路中后
							if(transCount[edge.u] + 1<transCount[edge.v]) {  //若两站点不是同一线路且当前找到的线路中换乘数少则将该点信息更新
								transCount[edge.v] = transCount[edge.u] + 1;
								dist[edge.v] = dist[u] + 1;
								parent[edge.v] = edge.u;
							}
						}else {
							if(transCount[edge.u] < transCount[edge.v]) {  //若两站点在同一线路中也同样进行判断，只不过不需要将其+1
								transCount[edge.v] = transCount[edge.u];
								dist[edge.v] = dist[u] + 1;
								parent[edge.v] = edge.u;
							}
						}
					}
				}
			}
			
		}
		int stationCount = 0;  //记录路径中经过的站点数量（不包括起始点）
		ArrayList<Integer> path = new ArrayList<>();  //因为parent中记录的顺序是反着的，该path用于记录逆序的顺序
		int next = stationNameToId.get(end);
		while(parent[next] != -1) {  //得到逆序的乘坐线路
			stationCount++;
			path.add(next);
			next = parent[next];
		}
		//查看路径中的站点
//		for(int i=path.size()-1; i>=0; i--) {
//			System.out.println(stationIdToName.get(path.get(i)));
//		}
		String str = begin + "  " +stationIdToName.get(path.get(path.size()-1));  //str中用于记录整个乘坐线路
		String nowLine = returnLine(begin, stationIdToName.get(path.get(path.size()-1))).get(0); // 获得初始线路
		
		// 得到整个乘坐线路以及换乘信息
		for(int i=path.size()-2; i>=0; i--) {
			String name = stationIdToName.get(path.get(i));
			String last = stationIdToName.get(path.get(i+1));
			
			List<String> common = returnLine(name, last); // 得到当前的站点和上一个站点的共同线路
			
//			System.out.println(notexists+"  "+ name+"  "+ last+"  " + notexists);
			if(!common.get(0).equals(nowLine)) {  //判断线路是否发生变化，通过之前得到的两个站点的共同线路和先前记录过的当前线路可知线路是否发生变化
				str += "\n换乘 " + common.get(0) + "\n";  //若发生变化则需要打印其信息并且更新当前线路
				nowLine = common.get(0);  
			}
			str += "  " + name;
				
			
		}
		str += "\n共经过： " + stationCount +" 站（不包含起始站点）\n";
		str += "共换乘： " + transCount[stationNameToId.get(end)] + " 次";
		WriteFile.write(str, savePath);  //写入savePath中
		System.out.println("Query the shortest path between " + begin + " and " + end + " successfully!");
//		System.out.println(str);
//		System.out.println("共经过： " + stationCount +" 站（不包含起始站点）");
//		System.out.println("共换乘： " + transCount[stationNameToId.get(end)] + " 次");
		
		
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		List<String> argslist = Arrays.asList(args);
		
		if(!argslist.contains("-map")) {  //判断是否有读入的地铁信息
			System.out.println("You have to load your subway map!");
			printInfo();
		}else if(argslist.contains("-q") && argslist.contains("-l")){ // 判断是否一个命令中出现不同查询要求
			printInfo();
		}else if(argslist.contains("-o") && !argslist.get(argslist.size()-1).contains(".txt")) {  //判断在有保存要求的情况下，保存的文件是否为.txt格式
			System.out.println("Your save Path should be in .txt format and in the last!");
			printInfo();
		}else if(argslist.indexOf("-q") + 2 >= argslist.size() || argslist.indexOf("-map") != 0){  //判断读入地铁信息的命令是否在开头以及查询最短路径时是否只输入了一个站点
			printInfo();
		}else {  //正确命令下的执行
			loadSubwayMap(args[1]);  // 读入地铁信息并构建成图
			
			if(argslist.contains("-l")) {  // 判断是查询线路信息还是最短路径
				int start = argslist.indexOf("-l") + 1;
				for(int i=start; i<argslist.size() && !argslist.get(i).equals("-o"); i++) {  //查询线路信息时允许一次性查询多个线路的信息，直到 -o前都是线路信息
					if(argslist.contains("-o")) {  // 判断是否要求保存，有的话则需要输入该路径，反之输入空
						getLineInfo(argslist.get(i), argslist.get(argslist.size()-1));
					}else {
						getLineInfo(argslist.get(i), "");
					}
				}
				
			}else if(argslist.contains("-q")) {
				int indexQ = argslist.indexOf("-q");  //获取-q位置用于得到所要查询的两个站点所在的位置
				if(argslist.contains("-o")) {
					getShortestPath(argslist.get(indexQ+1), argslist.get(indexQ+2), argslist.get(argslist.size()-1));
				}else {
					getShortestPath(argslist.get(indexQ+1), argslist.get(indexQ+2), "");
				}
			}
		}
		
	}

}
