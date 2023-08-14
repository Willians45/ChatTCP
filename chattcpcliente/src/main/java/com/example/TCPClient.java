package com.example;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {

    private static InetAddress serverAddress;
    private static Socket socket;
    private static String userName;
    private static MessageListener messageListener;

    private static Scanner chatInput;
    private static PrintWriter chatOutput;

    public static Scanner getChatInput() {
        return chatInput;
    }

    public static void initialize(ClientController client) {

        Thread alive = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                        chatOutput.println("ALVE i am " + userName + ".");
                        System.out.println("-->ALVE message sent.");
                        System.out.println("------------------------------------------------->");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        alive.start();

        messageListener = new MessageListener(client);
        messageListener.start();

    }

    public static boolean connectToServer(String serverIP, int serverPort, String user,
            IntroController introController) {

        boolean connectedSuccessful = false;

        boolean isUserNameOK;

        do {
            isUserNameOK = checkUserName(user);

            if (isUserNameOK) {

                try {

                    serverAddress = InetAddress.getByName(serverIP);
                    socket = new Socket(serverAddress, serverPort);

                    chatOutput = new PrintWriter(socket.getOutputStream(), true);
                    chatInput = new Scanner(socket.getInputStream());

                } catch (Exception e) {
                    try {

                        introController.showWarningAlert("Dirección errónea", "Server address not found.\n" +
                                "Intente de nuevo.");

                        System.out.println("-->No se consigue la dirección.");
                        System.out.println("------------------------------------------------->");
                        return connectedSuccessful;

                    } catch (Exception f) {
                        System.out.println(f);
                        System.out.println("------------------------------------------------->");
                        return connectedSuccessful;
                    }
                }

                chatOutput.printf("JOIN %s, %s:%s\n", userName, serverAddress, serverPort);
                String response = chatInput.nextLine(); //

                switch (response) {

                    case "J_OK": // server accepted the new client
                        System.out.println("-->Conexión exitosa.");
                        System.out.println("------------------------------------------------->");
                        connectedSuccessful = true;
                        break;

                    case "J_ERR":
                        introController.showWarningAlert("Nombre en uso", "Su usuario ya se encuentra usado.\n" +
                                "Pruebe otro nombre.");

                        System.out.println("-->Nombre en uso.");
                        System.out.println("------------------------------------------------->");
                        break;
                }

                return connectedSuccessful;

            } else {
                // show GUI alert
                introController.showWarningAlert("Wrong input!",
                        "Your username should be max 12 character long and should only contain " +
                                "chars, digits, '-' and '_'");

                return connectedSuccessful;
            }

        } while (!isUserNameOK);
    }

    public static void sendButton(String message) {
        String dataMessage;

        dataMessage = "DATA " + userName + ": " + message;

        chatOutput.println(dataMessage);
    }

    public static void exit() {

        System.out.println("-->client exit.");
        System.out.println("-->closing connection..");
        System.out.println("------------------------------------------------->");

        messageListener.stopRunning();

        chatOutput.println("QUIT i am " + userName + ".");

        System.exit(1);
    }

    private static boolean checkUserName(String user) {
        userName = user;

        if (userName.length() < 13 && userName.matches("^[a-zA-Z0-9_-]+$")) {
            return true;

        } else {
            System.out.println("-->wrong username input.");
            System.out.println("------------------------------------------------->");
            return false;
        }
    }
}

class MessageListener extends Thread {

    private String response, key;
    private Scanner keyScanner;
    private ClientController clientController;
    private int getMessageFails = 0;
    private volatile boolean running = true;

    public MessageListener(ClientController clientController) {
        this.clientController = clientController;
    }

    @Override
    public void run() {
        while (running) {
            try {

                response = TCPClient.getChatInput().nextLine();

                keyScanner = new Scanner(response);
                key = keyScanner.next();

                switch (key) {
                    case "DATA":

                        clientController.handleChatField(response.substring(response.indexOf(" ") + 1));

                        System.out.println("-->data message received.");
                        System.out.println("--<<" + response.substring(response.indexOf(" ") + 1));
                        System.out.println("------------------------------------------------->");
                        break;

                    case "LIST":
                        String userList = response.substring(response.indexOf(" ") + 1);

                        clientController.handleActiveUsersField(userList);

                        System.out.println("-->list message received.");
                        System.out.println("--<<" + userList);
                        System.out.println("------------------------------------------------->");
                        break;

                    case "J_ERR":

                        clientController.handleChatField("\nThe server thinks you're shit..");
                        clientController.handleChatField("-->closing connection..");

                        System.out.println("-->J_ERR message received.");
                        System.out.println("-->closing connection..");
                        System.out.println("------------------------------------------------->");

                        stopRunning();
                        break;

                    default:
                        clientController.handleChatField("-->Server system message: " + response);

                        System.out.println("-->#server system message received.");
                        System.out.println("-->#" + response);
                        System.out.println("------------------------------------------------->");
                }
            } catch (Exception e) {
                getMessageFails++;
                if (getMessageFails > 1) {
                    stopRunning();

                    System.out.println("-->La conexión se ha perdido.");
                    System.out.println("------------------------------------------------->");

                    System.exit(1);
                }
            }
        }
    }

    public void stopRunning() {
        running = false;
    }

}
