import java.io.File;
import java.io.FileNotFoundException;
import java.util.PriorityQueue;
import java.util.Scanner;

// 201352048 김종권
/* OSPF 알고리즘 (최소힙을 이용한 다익스트라 알고리즘)
 	1. 클래스 구성  
 	  1) OSPF : OSPF알고리즘을 실행하는 주요 클래스
 	  2) Pair(Comparable 상속받은 클래스) : 우선순위 큐(최소힙)에 들어갈 자료형 정의 
 	  
    2. OSPF클래스 구성(메소드)
   	  1) fopen : 파일 입력
   	  2) dijkstra : 다익스트라 알고리즘
   	  3) path : 다익스트라 과정시 나온 방문 흔적 배열을 통해 경로 역추적
   	  4) format : 다익스트라 나온 결과를 바탕으로 출력하기 위해 형식화 하는 메소드
   	  5) printPath : 출발 노드와 도착 노드를 입력 받아서 경로를 출력
   	  6) printTable : 라우팅 테이블 결과 출력
   	  
   	3. Pair 클래스 : Comparable 인터페이스 implements
   	  1) 최소힙 구현을 위해 compareTo 재정의
 */
public class OSPF {
	static int n; // 노드의 수
	static int w[][]; // 인접 행렬
	final static int INF = 99999; // infinite weight
	static PriorityQueue<Pair> pq = new PriorityQueue(); // Minimum Heap : for optimization in time complexity

	// 세 가지 변수 : 각 노드별 최단거리 / 각 노드별 경로 / 각 노드별 라우팅 테이블에 쓰일 next hop
	static int result_cost[][];
	static String result_path[][];
	static String result_nextHop[][];

	public static void main(String[] args) {

		// 파일 입력 메소드
		fopen();

		// 각 노드별 라우팅 테이블 값을 구하기위해서 for문 사용
		// i+1 : 출발 노드
		for (int i = 0; i < n; i++) {
			// 다익스트라에 사용될 변수
			int touch[] = new int[n]; // path 경로 저장
			int length[] = new int[n]; // 최단 거리가 저장될 배열
			pq.clear(); // 최소힙 초기화

			/*
			 * 다익스트라 메소드의 인수 : 인접행렬, 노드의 개수, 출발 노드, 방문 흔적이 저장될 배열, 출발 노드에서 나머지 노드까지의 최단 거리가
			 * 저장될 배열
			 */
			dijkstra(w, n, i, touch, length);
			for (int j = 0; j < n; j++) {

				// path문자열을 입력
				String path = path(i, j, "", touch);

				/*
				 * 결과로나온 path와 각 노드까지의 최단 경로 값을 가지고 next hop, cost, path를 형식화 해서 저장하는 메소드
				 */
				format(path, length, i, j);
			}
		}

		// 출발노드와 도착노드를 입력받아서, 경로를 출력
		printPath();
		
		// ospf결과 테이블 출력
		printTable();

		// test용
/*		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++) {
				System.out.println((i+1)+"->"+(j+1)+" = "+result_path[i][j]);
			}
		}*/
	}

	// 파일을 읽고 라우팅 결과의 정보가 저장될 전역변수를 초기화하는 메소드
	public static void fopen() {
		File file = new File("init.txt");
		try {
			Scanner sc = new Scanner(file);
			// 노드의 개수 입력 및 초기화
			n = Integer.parseInt(sc.nextLine());
			w = new int[n][n];
			result_cost = new int[n][n];
			result_path = new String[n][n];
			result_nextHop = new String[n][n];

			for (int i = 0; i < n; i++) {
				if (!sc.hasNextLine()) {
					System.err.println("** error : 충분한 입력 필요 (종료)");
					System.exit(0);
				}

				String str = sc.nextLine();
				String[] s = (str).split(",| ");

				if (s.length != n) {
					System.err.println("** error : 충분한 입력 필요 (종료)");
					System.exit(0);	 
				}
				for (int j = 0; j < n; j++) {
					int val = Integer.parseInt(s[j]);
					if (val == -1)
						val = INF;
					w[i][j] = val;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("error in fopen, " + e.getMessage());
		}
	}

	// 다익스트라 알고리즘
	public static void dijkstra(int W[][], int n, int start, int touch[], int length[]) {

		boolean visited[] = new boolean[n]; // 방문했는지 체크 하는 변수

		// 방문 흔적 초기화는 출발지로 초기화, 단 자기 자신은 -1로 지정
		for (int i = 0; i < n; i++) {
			if (i == start) {
				touch[i] = -1;
				visited[i] = true;
				length[i] = 0;
				continue;
			}

			touch[i] = start;
			length[i] = W[start][i];
			pq.offer(new Pair(i, length[i]));
		}

		// 최소힙에 데이터가 없을 때까지 반복
		while (!pq.isEmpty()) {
			Pair p = pq.poll(); // 최단거리인 vertex
			int vnear = p.adjVertex; // 근접 노드로 지정

			// 이미 방문한 vertex면 다음 vertex탐색, 무한값을 갖는 경우가 최소인 경우는 지나갈 수 없는 경로(-1값)
			if (visited[vnear]) {
				if (length[vnear] == INF)
					touch[vnear] = -1;
				continue;
			}

			// 최단거리인 vertex 방문
			visited[vnear] = true;

			// 방문한 vertex에서 다른 vertex거리 까지의 비교
			for (int i = 0; i < n; i++) {
				// 출발지에서 인접노드를 거쳐 (i+1)노드로 가는 경우 < 출발지에서 바로 (i+1)노드로 가는 경우
				// 이 경우에 해당되면 길이를 갱신한 후 vnear노드로 방문
				if (length[vnear] + W[vnear][i] < length[i]) {
					length[i] = length[vnear] + W[vnear][i];
					touch[i] = vnear;
					pq.offer(new Pair(i, length[i]));
				}
			}
		}
	}

	// 경로를 역추적 하는 메소드 (재귀 호출)
	public static String path(int start, int end, String p, int touch[]) {

		// 현재 출발 위치가 도착지인 경우 => 추적 완료
		// 현재 출발 위치가 INF인 경우 => 도달할 수 없는 경우
		if (end == start || end == -1)
			return (end + 1) + "-" + p;
		return path(start, touch[end], (end + 1) + "-" + p, touch);
	}

	// 결과로나온 path와 각 노드까지의 최단 경로 값을 가지고 next hop, cost, path를 형식화 해서 저장하는 메소드
	private static void format(String path, int length[], int i, int j) {
		String[] s = path.split("-");
		int cnt = s.length;
		
		// 자기 자신의 노드인 경우
		if (i == j) {
			path = "path : 자기 자신 / Cost : 0";
			result_cost[i][j] = 0;
			result_nextHop[i][j] = "-";
		}
		
		// next_hop이 인접한 경우
		else if (cnt==2) {
			path = path.substring(0, path.length() - 1);
			result_cost[i][j] = length[j];
			result_nextHop[i][j] = "-";
			path = "path : " + path + " / Cost : " + length[j];
		}

		// 연결되어 있지않은 경우
		else if (path.equals("")) {
			// System.out.println("path : 도달불가 / Cost : 무한");
			path = "path : 도달불가 / Cost : 무한";
			result_cost[i][j] = INF;
			result_nextHop[i][j] = "-";
		}

		// 경로를 정상적으로 찾았고 인접하지 않은 경우
		else {
			path = path.substring(0, path.length() - 1);
			String tmp[] = path.split("-");
			result_nextHop[i][j] = tmp[1];
			path = "path : " + path + " / Cost : " + length[j];
			result_cost[i][j] = length[j];
		}

		result_path[i][j] = path;
	}

	// 출발지와 도착지 노드를 입력받아서 경로를 출력하는 메소드
	public static void printPath() {
		Scanner sc = new Scanner(System.in);
		int start = sc.nextInt() - 1;
		int end = sc.nextInt() - 1;
		if (start > n || end < 0) {
			System.err.println("error, extends bounds ** exit");
			System.exit(0);
		}
		System.out.println(result_path[start][end] + "\n");
	}
	
	// ospf결과 테이블 출력하는 메소드
	private static void printTable() {
		for (int i = 0; i < n; i++) {
			System.out.println("●-----<< Forwarding table for node " + (i + 1) + " >>-----●");
			System.out.println("| destination node  |  Next hop  |  cost    |");
			for (int j = 0; j < n; j++) {
				if (result_cost[i][j] == INF)
					result_cost[i][j] = -1;
				System.out.println("|        " + (j + 1) + "          |       " + result_nextHop[i][j] + "    |   "
						+ result_cost[i][j] + "      |");
			}
			System.out.println("●-------------------------------------------●\n\n");
		}
	}
}

// 우선순위 큐에 들어갈 자료형(근접 정점과 가중치값)
class Pair implements Comparable<Pair> {
	int adjVertex;
	int weight;

	public Pair(int av, int w) {
		adjVertex = av;
		weight = w;
	}

	// 최소힙 구현을 위한 비교함수
	@Override
	public int compareTo(Pair p) {
		if (weight > p.weight)
			return 1;
		else if (weight < p.weight)
			return -1;
		else
			return 0;
	}
}