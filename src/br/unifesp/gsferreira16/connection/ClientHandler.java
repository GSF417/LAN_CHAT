package br.unifesp.gsferreira16.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import br.unifesp.gsferreira16.userinterface.UserInterface;

public class ClientHandler implements Runnable{

	private Thread thread;
	
	private UserInterface UI;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	private boolean isRunning;
	
	public static ClientHandler establishClient(UserInterface UI, String hostName, int hostPort) {
		ClientHandler client = new ClientHandler();
		client.UI = UI;
		try {
			client.clientSocket = new Socket(hostName, hostPort);
			client.out = new PrintWriter(client.clientSocket.getOutputStream(), true);
			client.in = new BufferedReader(new InputStreamReader (client.clientSocket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		client.isRunning = true;
		client.thread = new Thread(client);
		client.thread.start();
		
		return client;
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
	
	public void sendToServer(String message) {
		System.out.println("Sent message: "+message);
		out.println(message);
	}

	@Override
	public void run() {
		System.out.println("Client connection established.");
		while (isRunning) {
			
			String inputLine = "";
			try {
				if (in.ready()) {
					inputLine = in.readLine();
					if (inputLine != null) {
						UI.postServerMessage(inputLine);
					}
				}
			} catch (IOException e) {
				System.out.println("Connection lost!");
				stopConnection();
			}
		}
		stopConnection();
		return;
	}
}
