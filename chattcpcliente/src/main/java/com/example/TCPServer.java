package com.example;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;

public class TCPServer {

    private static final int PORT = 7777;
    private static ServerSocket serverSocket;
    private static ArrayList<ClientThread> activeClients = new ArrayList<ClientThread>();

    public static ArrayList<ClientThread> getActiveClients() {
        return activeClients;
    }

    public static void main(String args[]) {
        openPort(PORT);
        handleEntry();

    }

    static void openPort(int port) {
        try {

            serverSocket = new ServerSocket(port);
            System.out.println();
            System.out.println(InetAddress.getLocalHost());
            System.out.printf("server listening on port %s...\n\n", port);

        } catch (Exception e) {
            System.out.println("unable to set up port!!");
            System.out.println("server closed.");

            System.exit(1);
        }
    }

    static void handleEntry() {
        ListHandler listHandler = new ListHandler();
        listHandler.start();

        try {
            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("new client connected: " + clientSocket.getInetAddress());

                activeClients.add(new ClientThread(clientSocket));
                activeClients.get(activeClients.size() - 1).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("unable to connect to the client.");
        }
    }

}

class ClientThread extends Thread {

    private Socket clientSocket;
    private Scanner input;
    private PrintWriter output;
    private String userName = "";
    private Instant lastBeat;
    private volatile boolean running = true;

    public ClientThread(Socket socket) {

        clientSocket = socket;
        lastBeat = Instant.now();

        try {
            input = new Scanner(clientSocket.getInputStream());
            output = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String received;

        while (running) {
            try {

                received = input.nextLine();
                System.out.printf("received from (%s): %s\n", clientSocket.getInetAddress(), received);

                handleMessage(received);

                System.out.println("------------------------------------------------->");

            } catch (Exception e) {

                stopRunning();
            }
        }
    }

    private boolean checkUsername(String name) {

        for (ClientThread clientThread : TCPServer.getActiveClients()) {
            if (clientThread.getUserName().equalsIgnoreCase(name))
                return false;
        }

        return true;
    }

    private void handleMessage(String message) {
        Scanner messageScanner = new Scanner(message);
        String key = messageScanner.next();

        switch (key) {

            case "JOIN": {

                String name = message.substring(message.indexOf(" ") + 1, message.indexOf(","));

                if (checkUsername(name)) {
                    output.println("J_OK");
                    this.setLastBeat(Instant.now());

                    String users = "";

                    for (ClientThread clientThread : TCPServer.getActiveClients()) {
                        if (!clientThread.getUserName().equals("")) {
                            users += clientThread.getUserName() + " ";
                        }
                    }

                    users += name;

                    for (ClientThread clientThread : TCPServer.getActiveClients()) {
                        clientThread.getOutput().printf("LIST %s\n", users);
                    }

                    this.setUserName(name);

                    System.out.println(name + " has joined successfully.");

                } else {
                    output.println("J_ERR");

                    System.out.println(name + " was rejected.");
                }
                break;
            }

            case "DATA": {
                if (message.length() > 250) {
                    output.println("The message is too long.");
                }

                else {

                    for (ClientThread clientThread : TCPServer.getActiveClients()) {
                        if (!clientThread.getUserName().equals("")) {
                            clientThread.getOutput().println(message);
                        }
                    }
                    System.out.println("data sent to all active clients.");
                }
                break;
            }

            case "QUIT": {
                System.out.println(this.getUserName() + " left the conversation.");
                ListHandler.removeThread(this);
                break;
            }

            case "ALVE": {
                System.out.println(this.getUserName() + " is beating.");
                this.setLastBeat(Instant.now());
                break;
            }

            default: {
                System.out.println(this.getUserName() + " sent a weird message");
                output.println("J_ERR");
                break;
            }
        }
        messageScanner.close();
    }

    public void stopRunning() {
        running = false;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public PrintWriter getOutput() {
        return output;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Instant getLastBeat() {
        return lastBeat;
    }

    public void setLastBeat(Instant lastBeat) {
        this.lastBeat = lastBeat;
    }

}

class ListHandler extends Thread {

    public static void removeThread(ClientThread client) {
        try {
            TCPServer.getActiveClients().remove(client);
            client.getClientSocket().close();
            client.stopRunning();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TCPServer.getActiveClients().isEmpty()) {
            String users = "";

            for (ClientThread clientThread : TCPServer.getActiveClients()) {
                if (!clientThread.getUserName().equals("")) {
                    users += clientThread.getUserName() + " ";
                }
            }

            for (ClientThread clientThread : TCPServer.getActiveClients()) {
                clientThread.getOutput().printf("LIST %s\n", users);
            }
        }

        client = null;
    }

    public void run() {
        long duration;

        while (true) {
            if (!TCPServer.getActiveClients().isEmpty()) {
                try {
                    for (ClientThread clientThread : new ArrayList<ClientThread>(TCPServer.getActiveClients())) {

                        duration = Duration.between(clientThread.getLastBeat(), Instant.now()).getSeconds();

                        if (duration > 60) {
                            System.out.println(clientThread.getClientSocket().getInetAddress() +
                                    "(" + clientThread.getUserName() + ") is no longer active, closing connection..");

                            System.out.println(clientThread.getUserName() + " dropped due to timeout.");
                            removeThread(clientThread);
                            System.out.println("------------------------------------------------->");
                        }
                    }
                    Thread.sleep(20000);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    Thread.sleep(30000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
