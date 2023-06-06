package com.mycompany.chatclient;

import static com.mycompany.chatclient.ChatScreen.txt_roomName;
import static com.mycompany.chatclient.ChatScreen.y;
import game.Message;
import static game.Message.Message_Type.Name;
import static game.Message.Message_Type.ParticipantAdded;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

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
                            ChatRoom.participantListModel.addElement(received.userList.get(i));
                        }
                        break;

                    case CreateRoom:
                        String roomInfo = received.content.toString();
                        ClientRoom cr = new ClientRoom(roomInfo);
                        Client.rooms.add(cr);
                        Client.addRoomButton(cr);
                        break;

                    case ParticipantAdded:
                        String roomInformation = received.content.toString();
                        String[] participantInfo = received.content.toString().split(" ");
                        if (Client.name.equals(participantInfo[1])) {
                            ClientRoom addedClient = new ClientRoom(roomInformation);
                            Client.rooms.add(addedClient);
                            Client.addRoomButton(addedClient);
                            break;
                        }
                    case File:
                        String fileMessage = received.content.toString();
                        Client.cr.txta_rcvd.append(fileMessage + "\n");
                        break;
                    case Text:
                        Client.cr.txta_rcvd.append(received.content.toString() + "\n");
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
    public static String selectedClient;
    public static ArrayList<String> participants = new ArrayList<>();
    public static String name;//Client name
    public static ArrayList<ClientRoom> rooms = new ArrayList<>();
    public static ChatRoom cr;

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

    public static void SendFile(String roomName, File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String fileName = file.getName();
            FileInfo fileInfo = new FileInfo(roomName, fileName, fileBytes);
            Message fileMessage = new Message(Message.Message_Type.File);
            fileMessage.content = fileInfo;
            Client.output.writeObject(fileMessage);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void addRoomButton(ClientRoom room) {
        JButton chatRoom = new JButton();
        chatRoom.setBackground(Color.pink);
        ChatScreen.jPanel1.add(chatRoom);
        y += 50;
        chatRoom.setBounds(400, y, 150, 40);
        chatRoom.setText(room.roomName);

        chatRoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                cr = new ChatRoom(room.roomName, room.participants);
                cr.setVisible(true);
            }
        });

    }
}
