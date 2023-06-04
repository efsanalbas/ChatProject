package com.mycompany.chatclient;

import static com.mycompany.chatclient.ChatScreen.txt_roomName;
import static com.mycompany.chatclient.ChatScreen.y;
import game.Message;
import static game.Message.Message_Type.Name;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author nurefsanalbas
 */
class Listen extends Thread {

    public void run() {
        Message name = new Message(Message.Message_Type.Name); //İsim mesajı oluşturur.Oyun başladığında bağlanan oyuncuyu servera bildirmek için bu adımı ekledim.
        name.content = ChatScreen.username; //ismin içeriğine oyuncunun girdiği textfield değeri atandı.
        Client.Send(name); //Mesaj servera gönderildi.
        Client.name = ChatScreen.username; //Clientın adına da aynı şekilde textField değeri atandı.
        while (Client.socket.isConnected()) { //Client socket bağlı olduğu sürece bu döngü dönmeye devam etti.
            try {

                Message received = (Message) (Client.input.readObject()); //Alınan mesaj input.readObject() ile okundu ve mesaj objesine atandı.
                switch (received.type) { //Mesajın tipine göre aşağıdaki koşullar çalıştırılır.
                    case ConnectedClients:
                        ChatScreen.listModel.clear(); // Önceki kullanıcıları temizle
                        for (int i = 0; i < received.userList.size(); i++) {
                            ChatScreen.listModel.addElement(received.userList.get(i));
                        }
                        break;

                    case Pair:
                        if (received.content.toString() != null) {
                            System.out.println(received.content.toString());
                            Client.pairedClient = received.content.toString();
                            Client.pairedClients.add(Client.pairedClient);
                        }

                        break;
                    case Room:
                        Client.makeRoom = true;

                        System.out.println(Client.makeRoom);
                        if (Client.makeRoom) {
                            Client.participants.add(Client.name);
                            Client.participants.add(Client.pairedClient);
                            JButton chatRoom = new JButton();
                            chatRoom.setBackground(Color.pink);
                            ChatScreen.jPanel1.add(chatRoom);
                            y += 50;
                            chatRoom.setBounds(400, y, 150, 40);
                            chatRoom.setText(received.content.toString());
                            Client.roomName = received.content.toString();

                            String pairedCL = Client.pairedClient;
                            chatRoom.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    ArrayList<String> participant = new ArrayList<>();
                                    participant.add(Client.name);
                                    participant.add(pairedCL);
                                    ChatRoom CR = new ChatRoom(txt_roomName.getText(), participant);
                                    CR.setVisible(true);
                                }
                            });
                        }
                        Client.makeRoom = false;
                        System.out.println("Client:" + Client.makeRoom);

                        break;
                    case Text:
                        ChatRoom.txta_rcvd.append(received.content.toString() + "\n");//Eşleştiği clientın servera gönderdiği mesajı serverdan aldı.
                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}

public class Client {

    //her clientın bir soketi olmalı
    public static Socket socket;   //verileri almak için gerekli nesne
    public static ObjectInputStream input;  //verileri göndermek için gerekli nesne
    public static ObjectOutputStream output; //serverı dinleme thredi 
    public static Listen listenMe;
    public static String pairedClient;
    public static ArrayList<String> pairedClients = new ArrayList<>();
    public static ArrayList<String> participants = new ArrayList<>();
    public static String pair;
    public static String name;//Client name
    // public static ChatRoom chatRoom;
    public static String roomName;
    public static boolean makeRoom;

    public static void Start(String ip, int port) {
        try {
            Client.socket = new Socket(ip, port);//Client Soket nesnesi
            Client.input = new ObjectInputStream(Client.socket.getInputStream()); //input stream
            Client.output = new ObjectOutputStream(Client.socket.getOutputStream()); //output stream
            Client.listenMe = new Listen(); //Client için listen thread oluşturuldu.
            Client.listenMe.start();//thread başlatıldı.
        } catch (IOException ex) {//Socket oluştuktan sonra hata meydana gelirse burada yakalanır.
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Send(Message msg) {//mesaj gönderimi için bu fonksiyon kullanılır.
        try {
            Client.output.writeObject(msg);//output.writeObject(msg) ile mesaj server'a gönderilir.
        } catch (IOException ex) { //Gönderim sırasında hata oluşursa burada hata yakalanır.
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
