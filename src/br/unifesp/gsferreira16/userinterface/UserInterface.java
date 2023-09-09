package br.unifesp.gsferreira16.userinterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import br.unifesp.gsferreira16.connection.*;

public class UserInterface implements ActionListener, KeyListener {

	private static final int WIDTH = 1080, HEIGHT = 720;
	
	private ServerHandler serverHandler;
	private ClientHandler clientHandler;
	
	private JFrame userFrame;
	private JTextField hostIP, hostPort, chatInput;
	private JTextArea chatLog;
	private JButton sendInput, connectButton, disconnectButton;
	private JCheckBox serverCheckbox;
	
	private String chatText = "";
	
	private boolean runServer;
	
	public static UserInterface create() {
		UserInterface UI = new UserInterface();
		UI.runServer = false;
		
		// Initialize frame
		UI.userFrame = new JFrame("LAN Chat Program");
		UI.userFrame.addKeyListener(UI);
		
		GroupLayout layout = new GroupLayout(UI.userFrame.getContentPane());
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		// IP area
		JPanel IPPanel = new JPanel();
		
		JLabel IPText = new JLabel("Host IP");
		IPPanel.add(IPText);
		
		UI.hostIP = new JTextField(30);
		IPPanel.add(UI.hostIP);
		
		// Port area
		JPanel portPanel = new JPanel();
		
		JLabel portText = new JLabel("Host Port");
		portPanel.add(portText);
		
		UI.hostPort = new JTextField(30);
		portPanel.add(UI.hostPort);
		
		// Chat log area
		JPanel chatPanel = new JPanel();
		
		UI.chatLog = new JTextArea(30, 80);
		UI.chatLog.setEditable(false);
		UI.chatLog.setHighlighter(null);
		chatPanel.add(UI.chatLog);
		
		// Input area
		JPanel inputPanel = new JPanel();
		
		UI.chatInput = new JTextField(30);
		UI.chatInput.addKeyListener(UI);
		inputPanel.add(UI.chatInput);
		
		UI.sendInput = new JButton("SEND");
		UI.sendInput.addActionListener(UI);
		inputPanel.add(UI.sendInput);
		
		UI.connectButton = new JButton("CONNECT");
		UI.connectButton.addActionListener(UI);
		
		UI.disconnectButton = new JButton("DISCONNECT");
		UI.disconnectButton.addActionListener(UI);
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.add(IPPanel);
		connectionPanel.add(portPanel);
		connectionPanel.add(UI.connectButton);
		connectionPanel.add(UI.disconnectButton);
		
		UI.serverCheckbox = new JCheckBox("Run as Server");
		UI.serverCheckbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				UI.runServer = e.getStateChange() == 1 ? true : false;
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(connectionPanel)
				.addComponent(UI.serverCheckbox)
				.addComponent(chatPanel)
				.addComponent(inputPanel)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(connectionPanel)
				.addComponent(UI.serverCheckbox)
				.addComponent(chatPanel)
				.addComponent(inputPanel)
		);
		
		UI.userFrame.getContentPane().setLayout(layout);
		UI.userFrame.pack();
		UI.userFrame.setLocationRelativeTo(null);
		UI.userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		UI.userFrame.setBounds(0, 0, WIDTH, HEIGHT);
		UI.userFrame.setVisible(true);
		return UI;
	}

	private void cleanChat() {
		chatText = "";
		chatLog.setText(chatText);
	}
	
	private void checkForCommands(String text) {
		switch (text) {
			case "/clean":
				cleanChat();
				break;
			case "/end":
				disconnect();
				break;
		}
	}
	
	public void postServerMessage(String message) {
		chatText = chatText + message + "\n";
		chatLog.setText(chatText);
	}
	
	private void establishConnection() {
		int port = Integer.parseInt(hostPort.getText());
		String IP = hostIP.getText();
		System.out.println(IP+":"+port);
		if (serverHandler != null) {
			System.out.println("Server already established!");
			return;
		}
		if (clientHandler != null) {
			System.out.println("Client already established!");
			return;
		}
		if (runServer) {
			serverHandler = ServerHandler.establishServer(port);
		}
		clientHandler = ClientHandler.establishClient(this, IP, port);
		System.out.println("Connections established.");
	}
	
	private void disconnect() {
		System.out.println("Commence disconnection.");
		if (clientHandler != null) {
			clientHandler.stopConnection();
			clientHandler = null;
		}
		System.out.println("Client disconnected successfully.");
		if (serverHandler != null) {
			serverHandler.stop();
			serverHandler = null;
		}
		System.out.println("Server disconnected successfully.");
	}
	
	private void sendMessageLocal(String text) {
		chatText = chatText + "LOCAL_USER: " + text + "\n";
		chatLog.setText(chatText);
	}
	
	private void sendMessageToServer(String text) {
		clientHandler.sendToServer(text);
	}
	
	private void sendChatInput() {
		String text = chatInput.getText();
		if (clientHandler != null) {
			sendMessageToServer(text);
		}
		else {
			sendMessageLocal(text);
		}
		chatInput.setText("");
		checkForCommands(text);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if (e.getActionCommand() == this.sendInput.getText()) {
			sendChatInput();
		}
		else if (e.getActionCommand() == this.connectButton.getText()) {
			establishConnection();
		}
		else if (e.getActionCommand() == this.disconnectButton.getText()) {
			disconnect();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		chatInput.requestFocus();
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER:
				sendChatInput();
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
