import java.io.*;
import java.net.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;
import java.util.HashMap;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalTime;

import utils.LamportClock;
import utils.Request;
import utils.Content;
import utils.FileIO;
import utils.Constants;
import utils.AggregatedContent;

public class AggregatorServer {
	PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<Request>(20, getClockComparator());
	HashMap<String, String> xmlContent = new HashMap<String, String>();
	private TreeMap<Integer, String> requestOrder = new TreeMap<Integer, String>();

	// Used for testing to generate grandtruth result and overwrote the
	// removeEldestEntry to keep most 20
	LinkedHashMap<Integer, String> lhm = new LinkedHashMap<Integer, String>() {
		protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
			return size() > 20;
		}
	};
	private AggregatedContent aggregatedContent;

	StringBuilder testAggregatedContent = new StringBuilder();
	LamportClock clock;

	private Thread producer;
	private Thread consumer = createConsumer();
	Set<Integer> usedClocks = new HashSet<Integer>();
	private boolean testMode;
	private boolean overWritten = true;

	public AggregatorServer(int port, boolean testMode, boolean verbose) throws Exception {
		this.testMode = testMode;
		clock = new LamportClock("Aggregator-Server", 0, verbose);
		File file = new File("aggregatedContent");
		if (file.exists() && !file.isDirectory()) {
			FileInputStream f = new FileInputStream(file);
			ObjectInputStream s = new ObjectInputStream(f);
			aggregatedContent = (AggregatedContent) s.readObject();
			testAggregatedContent = aggregatedContent.toSting();
			this.overWritten = false;
			s.close();
		} else {
			aggregatedContent = new AggregatedContent();
		}
		this.producer = createProducer(port);
	}

	// A thread to recieve requests
	private Thread createProducer(int port) {
		return new Thread(new Runnable() {
			public void run() {
				ServerSocket server = null;
				try {
					server = new ServerSocket(port);
					server.setReuseAddress(true);
					while (true) {
						Socket client = server.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						String line;
						String requestType = "";
						String serverID = "";
						boolean first = true;
						int recievedClock = 0;
						String file = "";
						LocalTime time = LocalTime.now();

						// Read requests from clients
						while ((line = in.readLine()) != null) {
							if (line.equals("")) {
								break;
							}
							String[] lines = line.replaceAll(":", "").split(" ");
							if (first) {
								requestType = lines[0];
								file = lines[1].substring(0, lines[1].indexOf('.'));
								first = false;
							}
							if (lines[0].equals("serverID")) {
								serverID = lines[1];
							}
							if (lines[0].equals("Lamport-Clock")) {
								recievedClock = Integer.parseInt(lines[1]);
							}
						}

						// Assign Lamport Clock for the requests
						int curClock = clock.compareAndSet(recievedClock,
								String.format("Recieving %s request from %s.",
										Constants.requestColors.get(requestType),
										Constants.getServerColor(requestType, serverID)));

						// Check if race condidtion in Lamport Clock
						if (usedClocks.contains(curClock)) {
							System.out
									.println("\033[4m\033[1m\u001b[31mDuplicate lamport clock. Test did not pass.");
							throw new Exception("Duplicate lamport clock.");
						} else {
							usedClocks.add(curClock);
						}

						// Add content for test if this is a PUT request
						if (requestType.equals("PUT")) {
							// Create new Content if is PUT request for later uploads
							aggregatedContent.addContent(serverID, curClock, time);
							if (testMode) {
								if (!file.equals("/invalid01") && !file.equals("/null01"))
									addTestContent(curClock, file);
							}
						}

						// Write grandtruth result if this is a GET request
						if (testMode && requestType.equals("GET")) {
							writeTestAggregatedContent(serverID);
						}

						// Write order of request
						if (testMode && overWritten) {
							requestOrder.put(curClock, serverID);
							writeRequests(requestOrder);
						}

						// Put request into priority queue
						requests.put(new Request(client, requestType, serverID, clock,
								curClock, aggregatedContent, time));

					}

				} catch (Exception e) {
					if (e.getMessage().equals("Duplicate lamport clock.")) {
						System.out.println("Duplicate lamport clock! ");
						System.exit(0);
					}
					e.printStackTrace();
				}

			}
		});
	}

	protected void writeRequests(TreeMap<Integer, String> requestOrder2) throws Exception {
		StringBuilder req = new StringBuilder();
		for (Map.Entry<Integer, String> entry : requestOrder2.entrySet()) {
			req.append("[" + entry.getKey() + ", " + entry.getValue() + "]" + "\n");
		}
		FileIO.writeTxt("requests", req.toString());
	}

	protected void writeTestAggregatedContent(String serverID) throws Exception {
		StringBuilder allTestContent = new StringBuilder();
		Map<Integer, String> map = new TreeMap<>(lhm);
		for (String s : map.values()) {
			allTestContent.append(s + "\n");
		}
		FileIO.writeTxt("Grandtruth-" + serverID, allTestContent.toString());
	}

	protected void addTestContent(int curClock, String fileName) throws Exception {
		String fileDir = "./contents" + fileName + ".txt";
		String xmlContent = FileIO.readTxt(fileDir);
		lhm.put(curClock, xmlContent);
		testAggregatedContent.append(xmlContent);
		testAggregatedContent.append("\n");
	}

	// Executing request
	private Thread createConsumer() {
		return new Thread(new Runnable() {
			public void run() {
				while (true) {

					try {
						Request request = requests.take();
						request.start();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}
		});
	}

	private void startProducer() {
		clock.increment("Staring aggregator server, Initiating Producer.");
		this.producer.start();
	}

	private void startConsumer() {
		clock.increment("Staring aggregator server, Initiating Consumer.");
		this.consumer.start();
	}

	private Comparator<Request> getClockComparator() {
		return new Comparator<Request>() {
			@Override
			public int compare(Request o1, Request o2) {
				return o1.getClock() - o2.getClock();
			}
		};
	}

	public static void main(String[] args)
			throws Exception {
		int port = Integer.parseInt(args[0]);
		boolean testMode = Boolean.parseBoolean(args[1]);
		boolean verbose = Boolean.parseBoolean(args[2]);
		AggregatorServer AS = new AggregatorServer(port, testMode, verbose);
		AS.startProducer();
		AS.startConsumer();
	}

}
