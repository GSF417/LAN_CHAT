package br.unifesp.gsferreira16.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler implements Runnable {

	private Thread thread;
	private ServerSocket serverSocket;
	private List<ClientConnector> clients;
	private List<ClientConnector> finishedClients;
	
	private boolean isRunning;
	
	private String[] nameList = {
		"ALPHA",
		"BRAVO",
		"CHARLIE",
		"DELTA",
		"ECHO",
		"FOXTROT",
		"GOLF",
		"HOTEL",
		"INDIA",
		"JULIETT",
		"KILO",
		"LIMA",
		"MIKE",
		"NOVEMBER",
		"OSCAR",
		"PAPA",
		"QUEBEC",
		"ROMEO",
		"SIERRA",
		"TANGO",
		"UNIFORM",
		"VICTOR",
		"WHISKEY",
		"XRAY",
		"YANKEE",
		"ZULU"
	};
	private int[] nameUseCount = new int[nameList.length];
	
	private String generateNewName(int x) {
		if (nameUseCount[x] == 0) {
			nameUseCount[x]++;
			return nameList[x];
		}
		return nameList[x]+nameUseCount[x];
	}
	
	public static ServerHandler establishServer(int hostPort) {
		ServerHandler server = new ServerHandler();
		
		server.clients = new ArrayList<ClientConnector>();
		server.finishedClients = new ArrayList<ClientConnector>();
		try {
			server.serverSocket = new ServerSocket(hostPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		server.isRunning = true;
		server.thread = new Thread(server);
		server.thread.start();
		return server;
	}
	
	public void stop() {
		isRunning = false;
		try {
			for (int i = 0; i < clients.size(); i++) {
				ClientConnector client = clients.get(i);
				client.stopConnection();
			}
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendToClients(String name, String message) {
		// Purge all finished Clients
		for (int i = 0; i < finishedClients.size(); i++) {
			ClientConnector client = finishedClients.get(i);
			clients.remove(client);
		}
		finishedClients.clear();
		System.out.println("Attempting to send ["+message+"] from client "+name);
		// Now send the message to all remaining clients
		for (int i = 0; i < clients.size(); i++) {
			ClientConnector client = clients.get(i);
			if (client.isClosed()) {
				finishedClients.add(client);
				continue;
			}
			System.out.println(i);
			try {
				System.out.println("Sending message ["+message+"] from client "+name);
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				out.println(name+": "+message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		Thread t = null;
		while (isRunning) {
			Socket client;
			try {
				client = serverSocket.accept();
				String name = generateNewName(clients.size());
				ClientConnector connector = ClientConnector.establishClientConnection(name, this, client);
				clients.add(connector);
				System.out.println("Server establishing new connection with client of name "+name);
				t = new Thread(connector);
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Connection lost!");
		}
		stop();
		return;
	}
	
	private static class ClientConnector implements Runnable {
		
		private boolean isRunning;
		
		private String name;
		private ServerHandler handler;
		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;
		
		public static ClientConnector establishClientConnection(String name, ServerHandler handler, Socket socket) {
			ClientConnector connector = new ClientConnector();
			connector.name = name;
			connector.handler = handler;
			connector.clientSocket = socket;
			connector.isRunning = true;
			
			try {
				connector.out = new PrintWriter(connector.clientSocket.getOutputStream(), true);
				connector.in = new BufferedReader(new InputStreamReader(connector.clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return connector;
		}
		
		public boolean isClosed() {
			return clientSocket.isClosed();
		}
		
		public OutputStream getOutputStream() throws IOException {
			return clientSocket.getOutputStream();
		}
		
		public void stopConnection() {
			try {
				isRunning = false;
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			System.out.println("Running client of name "+name);
			while (isRunning) {
				String inputLine = null;
				try {
					while ((inputLine = in.readLine()) != null) {
						System.out.println("Detected message: "+inputLine);
						if (inputLine.equals("/end")) {
							out.println("Server: Connection ended.");
							stopConnection();
							break;
						}
						handler.sendToClients(name, inputLine);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			stopConnection();
			return;
		}
		
	}
}
