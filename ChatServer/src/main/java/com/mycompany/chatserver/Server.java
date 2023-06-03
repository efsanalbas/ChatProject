/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatserver;

import game.Message;
import static game.Message.Message_Type.ConnectedClients;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nurefsanalbas
 */
public class Server extends Thread {

    ServerSocket serverSocket; //socket tanımladım.
    int port; //port numarasını tanımladım.
    boolean isListening; //dinleme durumunu tutması için tanımladım.
    public static ArrayList<ServerClient> clients; //bağlanan clientları bu arraylistte tuttum.
    public static ArrayList<String> clientList= new ArrayList<>();;

    public Server(int port) { //Server oluşturmak için constructor tanımladım.
        try {
            this.port = port; //sınıfın port değişkenini parametre olarak gönderilen port değişkenine atıyorum.
            this.serverSocket = new ServerSocket(port); //Server socket oluşturuyorum.
            this.isListening = false;//Dinleme değeri başlangıçta false olarak belirlendi.
            this.clients = new ArrayList<>(); //Bağlanan clientları eklemek için arraylist oluşturdum.
        } catch (IOException ex) { //Socket oluşturulurken oluşabilecek hataları yakalar.
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Listen() { //Server çalıştıktan sonra clientların bağlanmasını sağlar.
        this.isListening = true; //dinlemeye başlar.
        this.start(); //threadin run fonksiyonunu çalıştırır.

    }

    public void Stop() { //Socketi kapatır ve artık client kabul etmez.
        try {
            this.isListening = false; //Client bağlanmasını durdurur.
            this.serverSocket.close(); //serverSocketi kapatır.
        } catch (IOException ex) {//serverSocket kapatılırken oluşan hataları yakalar.
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addClient(ServerClient server_client) { //Arrayliste clientı ekler.
        this.clients.add(server_client);

    }

public static void SendBroadcast() {
    ArrayList<String> connectedUsers = new ArrayList<>(clientList);
    for (ServerClient client : clients) {
        client.SendMessage(Message.Message_Type.ConnectedClients, connectedUsers);
    }
}

    @Override
    public void run() {

        while (this.isListening) {//isListening true olduğu sürece döner.
            try {
                System.out.println("Client Bekleniyor...");
                Socket clientSocket = this.serverSocket.accept();//Clientı kabul eder.
                System.out.println("Client Geldi..");
                ServerClient nclient = new ServerClient(clientSocket); //Clienttan bir serverClient oluşturur,gelen mesajlar gönderilecek mesajlar bu obje üzerinden yapılır.
                this.addClient(nclient); //arrayliste client eklenir.
                nclient.Listen(); //ve client dinlemeye başlanır.
 

            } catch (IOException ex) {// Socket oluştururken ortaya çıkabilecek hataları yakalar.
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

//    public static void Send(ServerClient cl, Message msg) {//Serverın clienta mesaj göndermesini sağlar.
//
//        try {
//            cl.output.writeObject(msg);
//        } catch (IOException ex) {//output.writeObject(msg) metodundaki hataları yakalar.
//            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
}
