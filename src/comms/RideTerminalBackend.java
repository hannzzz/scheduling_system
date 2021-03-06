package comms;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

import mainMenu.swapManagament.Swap;

public class RideTerminalBackend implements Runnable{
	private String name;
	private ThreadedSocket managerTerminal = null;
	private boolean running = false;
	private LinkedList<String> currentWorkers;
	
	private LinkedList<Swap> swaps;
	private SwapParser swapParser;
	
	public RideTerminalBackend(String name) {
		this.name = name;
		currentWorkers = new LinkedList<String>();
		swaps = new LinkedList<Swap>();
		swapParser = new SwapParser();
	}
	
	public void run() {
		running = true;
		connect("127.0.0.1", 5555);
		while (running) {
			String newMessage = managerTerminal.readMessage();
			if (newMessage == null) {
				stop();
				return;
			}
			
			processMessage(newMessage);
		}
	}
	
	public void login(String user) {
		if (currentWorkers.contains(user)) {
			//replace with a pop up window?
			System.out.println("Error, user " + user + " is already logged in");
			return;
		}
		currentWorkers.add(user);
		try {
			managerTerminal.writeMessage("login," + user);
		} catch (IOException e) {}
	}
	
	public void logout(String user) {
		if (!currentWorkers.contains(user)) {
			//replace with a pop up window?
			System.out.println("Error, user " + user + " is not logged in");
			return;
		}
		currentWorkers.remove(user);
		try {
			managerTerminal.writeMessage("logout," + user);
		} catch (IOException e) {}
	}
	
	private void connect(String terminalAddress, int terminalPort) {
		try {
			Socket serverSocket = new Socket(terminalAddress, terminalPort);
			managerTerminal = new ThreadedSocket(serverSocket);
		}
		catch (IOException e) {
			System.out.println("Could not connect to the manager terminal, stopping application");
			System.out.println(e);
			stop();
		}
		managerTerminal.start();
		connectionHandshake();
	}
	
	public void stop() {
		if (running) {
			running = false;
			managerTerminal.stop();
		}
	}
	
	private void connectionHandshake() {
		try {
			managerTerminal.writeMessage("terminal," + name);
		} catch (IOException e) {}
	}
	
	private void processMessage(String message) {
		String[] elements = message.split(",");
		if (elements[0] == "swap") {
			addSwap(message);
		}
	}
	
	private void addSwap(String swapString) {
		Swap newSwap = swapParser.fromString(swapString);
		swaps.add(newSwap);
	}
}
