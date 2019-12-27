import java.io.File;
import java.io.FileNotFoundException;
import java.util.PriorityQueue;
import java.util.Scanner;

// 201352048 ������
/* OSPF �˰��� (�ּ����� �̿��� ���ͽ�Ʈ�� �˰���)
 	1. Ŭ���� ����  
 	  1) OSPF : OSPF�˰����� �����ϴ� �ֿ� Ŭ����
 	  2) Pair(Comparable ��ӹ��� Ŭ����) : �켱���� ť(�ּ���)�� �� �ڷ��� ���� 
 	  
    2. OSPFŬ���� ����(�޼ҵ�)
   	  1) fopen : ���� �Է�
   	  2) dijkstra : ���ͽ�Ʈ�� �˰���
   	  3) path : ���ͽ�Ʈ�� ������ ���� �湮 ���� �迭�� ���� ��� ������
   	  4) format : ���ͽ�Ʈ�� ���� ����� �������� ����ϱ� ���� ����ȭ �ϴ� �޼ҵ�
   	  5) printPath : ��� ���� ���� ��带 �Է� �޾Ƽ� ��θ� ���
   	  6) printTable : ����� ���̺� ��� ���
   	  
   	3. Pair Ŭ���� : Comparable �������̽� implements
   	  1) �ּ��� ������ ���� compareTo ������
 */
public class OSPF {
	static int n; // ����� ��
	static int w[][]; // ���� ���
	final static int INF = 99999; // infinite weight
	static PriorityQueue<Pair> pq = new PriorityQueue(); // Minimum Heap : for optimization in time complexity

	// �� ���� ���� : �� ��庰 �ִܰŸ� / �� ��庰 ��� / �� ��庰 ����� ���̺� ���� next hop
	static int result_cost[][];
	static String result_path[][];
	static String result_nextHop[][];

	public static void main(String[] args) {

		// ���� �Է� �޼ҵ�
		fopen();

		// �� ��庰 ����� ���̺� ���� ���ϱ����ؼ� for�� ���
		// i+1 : ��� ���
		for (int i = 0; i < n; i++) {
			// ���ͽ�Ʈ�� ���� ����
			int touch[] = new int[n]; // path ��� ����
			int length[] = new int[n]; // �ִ� �Ÿ��� ����� �迭
			pq.clear(); // �ּ��� �ʱ�ȭ

			/*
			 * ���ͽ�Ʈ�� �޼ҵ��� �μ� : �������, ����� ����, ��� ���, �湮 ������ ����� �迭, ��� ��忡�� ������ �������� �ִ� �Ÿ���
			 * ����� �迭
			 */
			dijkstra(w, n, i, touch, length);
			for (int j = 0; j < n; j++) {

				// path���ڿ��� �Է�
				String path = path(i, j, "", touch);

				/*
				 * ����γ��� path�� �� �������� �ִ� ��� ���� ������ next hop, cost, path�� ����ȭ �ؼ� �����ϴ� �޼ҵ�
				 */
				format(path, length, i, j);
			}
		}

		// ��߳��� ������带 �Է¹޾Ƽ�, ��θ� ���
		printPath();
		
		// ospf��� ���̺� ���
		printTable();

		// test��
/*		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++) {
				System.out.println((i+1)+"->"+(j+1)+" = "+result_path[i][j]);
			}
		}*/
	}

	// ������ �а� ����� ����� ������ ����� ���������� �ʱ�ȭ�ϴ� �޼ҵ�
	public static void fopen() {
		File file = new File("init.txt");
		try {
			Scanner sc = new Scanner(file);
			// ����� ���� �Է� �� �ʱ�ȭ
			n = Integer.parseInt(sc.nextLine());
			w = new int[n][n];
			result_cost = new int[n][n];
			result_path = new String[n][n];
			result_nextHop = new String[n][n];

			for (int i = 0; i < n; i++) {
				if (!sc.hasNextLine()) {
					System.err.println("** error : ����� �Է� �ʿ� (����)");
					System.exit(0);
				}

				String str = sc.nextLine();
				String[] s = (str).split(",| ");

				if (s.length != n) {
					System.err.println("** error : ����� �Է� �ʿ� (����)");
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

	// ���ͽ�Ʈ�� �˰���
	public static void dijkstra(int W[][], int n, int start, int touch[], int length[]) {

		boolean visited[] = new boolean[n]; // �湮�ߴ��� üũ �ϴ� ����

		// �湮 ���� �ʱ�ȭ�� ������� �ʱ�ȭ, �� �ڱ� �ڽ��� -1�� ����
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

		// �ּ����� �����Ͱ� ���� ������ �ݺ�
		while (!pq.isEmpty()) {
			Pair p = pq.poll(); // �ִܰŸ��� vertex
			int vnear = p.adjVertex; // ���� ���� ����

			// �̹� �湮�� vertex�� ���� vertexŽ��, ���Ѱ��� ���� ��찡 �ּ��� ���� ������ �� ���� ���(-1��)
			if (visited[vnear]) {
				if (length[vnear] == INF)
					touch[vnear] = -1;
				continue;
			}

			// �ִܰŸ��� vertex �湮
			visited[vnear] = true;

			// �湮�� vertex���� �ٸ� vertex�Ÿ� ������ ��
			for (int i = 0; i < n; i++) {
				// ��������� ������带 ���� (i+1)���� ���� ��� < ��������� �ٷ� (i+1)���� ���� ���
				// �� ��쿡 �ش�Ǹ� ���̸� ������ �� vnear���� �湮
				if (length[vnear] + W[vnear][i] < length[i]) {
					length[i] = length[vnear] + W[vnear][i];
					touch[i] = vnear;
					pq.offer(new Pair(i, length[i]));
				}
			}
		}
	}

	// ��θ� ������ �ϴ� �޼ҵ� (��� ȣ��)
	public static String path(int start, int end, String p, int touch[]) {

		// ���� ��� ��ġ�� �������� ��� => ���� �Ϸ�
		// ���� ��� ��ġ�� INF�� ��� => ������ �� ���� ���
		if (end == start || end == -1)
			return (end + 1) + "-" + p;
		return path(start, touch[end], (end + 1) + "-" + p, touch);
	}

	// ����γ��� path�� �� �������� �ִ� ��� ���� ������ next hop, cost, path�� ����ȭ �ؼ� �����ϴ� �޼ҵ�
	private static void format(String path, int length[], int i, int j) {
		String[] s = path.split("-");
		int cnt = s.length;
		
		// �ڱ� �ڽ��� ����� ���
		if (i == j) {
			path = "path : �ڱ� �ڽ� / Cost : 0";
			result_cost[i][j] = 0;
			result_nextHop[i][j] = "-";
		}
		
		// next_hop�� ������ ���
		else if (cnt==2) {
			path = path.substring(0, path.length() - 1);
			result_cost[i][j] = length[j];
			result_nextHop[i][j] = "-";
			path = "path : " + path + " / Cost : " + length[j];
		}

		// ����Ǿ� �������� ���
		else if (path.equals("")) {
			// System.out.println("path : ���޺Ұ� / Cost : ����");
			path = "path : ���޺Ұ� / Cost : ����";
			result_cost[i][j] = INF;
			result_nextHop[i][j] = "-";
		}

		// ��θ� ���������� ã�Ұ� �������� ���� ���
		else {
			path = path.substring(0, path.length() - 1);
			String tmp[] = path.split("-");
			result_nextHop[i][j] = tmp[1];
			path = "path : " + path + " / Cost : " + length[j];
			result_cost[i][j] = length[j];
		}

		result_path[i][j] = path;
	}

	// ������� ������ ��带 �Է¹޾Ƽ� ��θ� ����ϴ� �޼ҵ�
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
	
	// ospf��� ���̺� ����ϴ� �޼ҵ�
	private static void printTable() {
		for (int i = 0; i < n; i++) {
			System.out.println("��-----<< Forwarding table for node " + (i + 1) + " >>-----��");
			System.out.println("| destination node  |  Next hop  |  cost    |");
			for (int j = 0; j < n; j++) {
				if (result_cost[i][j] == INF)
					result_cost[i][j] = -1;
				System.out.println("|        " + (j + 1) + "          |       " + result_nextHop[i][j] + "    |   "
						+ result_cost[i][j] + "      |");
			}
			System.out.println("��-------------------------------------------��\n\n");
		}
	}
}

// �켱���� ť�� �� �ڷ���(���� ������ ����ġ��)
class Pair implements Comparable<Pair> {
	int adjVertex;
	int weight;

	public Pair(int av, int w) {
		adjVertex = av;
		weight = w;
	}

	// �ּ��� ������ ���� ���Լ�
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