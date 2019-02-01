package com.wsb;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();


    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                }
                    else if ("date".equalsIgnoreCase(cmd)){
                    hanlderData();
                    }

                else if ("time".equalsIgnoreCase(cmd)){
                    hanlderTime();
                }

                else if ("shave".equalsIgnoreCase(cmd)){
                    hanlderShaver();
                }
                else if ("f*ck".equalsIgnoreCase(cmd)){
                    hanlderfck();
                }
                else if ("tie".equalsIgnoreCase(cmd)){
                    hanlderTie();
                }
                else if ("remove-pregnancy".equalsIgnoreCase(cmd)){
                    hanlderRemover();
                }

                    else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }



    private void hanlderShaver() throws IOException{
        String shave = "> OK... relax and hold still... \n";
        outputStream.write(shave.getBytes());
    }

    private void hanlderfck() throws IOException{
        String fck = "> Ooooh Mama!\n> Hubba hubba!\n";
        outputStream.write(fck.getBytes());
    }

    private void hanlderTie() throws IOException{
        String tie = "> Go ask your father\n";
        outputStream.write(tie.getBytes());
    }

    private void hanlderRemover() throws IOException{
        String remove = "> Duuude...that's illegal!\n";
        outputStream.write(remove.getBytes());
    }



    private void hanlderData() throws IOException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    Date date = new Date();
    String showDate = sdf.format(date) + "\n";
    outputStream.write(showDate.getBytes());

}

    private void hanlderTime() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String showDate = sdf.format(date) + "\n";
        outputStream.write(showDate.getBytes());

    }


    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }


    //format: "msg" "nickname" msgText...
    // format: "msg" "#topic" msgText
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String msgText = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = sendTo + " " + login + ": " + msgText + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = login + ": " + msgText + "\n";
                    worker.send(outMsg);
                }
            }
        }


    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);

        List<ServerWorker> workerList = server.getWorkerList();


        //send other online users urrent users status
        String onlineMsg = "> offline " + login + "\n";
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }

        clientSocket.close();
    }


    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 2) {
            String login = tokens[1];
            //String password = tokens[2];

            //hardcoded logins
            if (login.equals("tom") || login.equals("jim") || login.equals("test")) {
                String msg = "> welcome!\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("> User:<" + login + "> logged in succesfully \n");


                List<ServerWorker> workerList = server.getWorkerList();

                //send current user all other online loggins
                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "> Online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }

                }
                //send other online users urrent users status
                String onlineMsg = "> online " + login + "\n";
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }

            } else {
                String msg = "> invalid login or password!\n";
                outputStream.write(msg.getBytes());
            }
        }
    }
    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}


