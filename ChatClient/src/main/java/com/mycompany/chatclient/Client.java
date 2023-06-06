package com.mycompany.chatclient;

import static com.mycompany.chatclient.ChatScreen.y;
import game.Message;
import static game.Message.Message_Type.ParticipantAdded;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;

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
                    case ConnectedClients: //Clientlar bağlandığında ChatScreende listeye eklenir.
                        for (int i = 0; i < received.userList.size(); i++) {//Bağlanan kullanıcılar arrayList olarak Client'a gönderildi.
                            if (!ChatScreen.listModel.contains(received.userList.get(i))) { //Eğer bu kullanıcı önceden eklendiyse her durumda yeniden eklenmemesi için bu durumu kontrol ettim.
                                ChatScreen.listModel.addElement(received.userList.get(i)); //Listeye bağlanan clientları ekledim.
                                ChatRoom.participantListModel.addElement(received.userList.get(i));
                                //Aynı zamanda chatRoom tarafında da bağlanan clientlar arasından 
                                //odaya ekleme yapılabilmesi için participantListModel' de bağlı clientları gösterdim. 
                            }
                        }
                        break;
                    case CreateRoom: //Serverdan gelen istek doğrultusunda oda oluşturulur.
                        String roomInfo = received.content.toString();//Katılımcı ve oda bilgileri serverdan alındı.
                        ClientRoom cr = new ClientRoom(roomInfo); //Oda oluşturuldu. Burada clientRoom, server tarafındaki ServerRoom gibi katılımcı ve oda adını içerir.
                        Client.rooms.add(cr);//Clientın odalarının bulunduğu listeye ClientRoom nesnesi eklendi.
                        Client.addRoomButton(cr); //Client ekranına butonu eklememi sağlayan fonksiyon.
                        break;
                    case ParticipantAdded: //Client odaya kullanıcı eklemek istediğinde,
                        String roomInformation = received.content.toString(); //oda bilgisi ve eklenecek yeni client serverdan alınır.
                        String[] participantInfo = received.content.toString().split(" ");
                        if (Client.name.equals(participantInfo[1])) {
                            ClientRoom addedClient = new ClientRoom(roomInformation); //Oda nesnesi oluşturuldu.
                            Client.rooms.add(addedClient);//Clientın odalarına eklendi.
                            Client.addRoomButton(addedClient); //Client ekranında yeni room gösterildi.
                            break;
                        }
                    case Text: //Text gelediğinde ekrana yazdırıldı.
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

    public static void addRoomButton(ClientRoom room) { //Oda butonunu clientın ekranına eklemek için bu fonksiyonu oluşturdum.
        JButton chatRoom = new JButton(); //Butonu oluşturdu.
        chatRoom.setBackground(Color.pink);
        ChatScreen.jPanel1.add(chatRoom); //panele ekledi.
        y += 50;
        chatRoom.setBounds(400, y, 150, 40);//Ekrana ekledi.
        chatRoom.setText(room.roomName);

        chatRoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                cr = new ChatRoom(room.roomName, room.participants); //ChatRooma tıklandığında oda ekranı görüntülendi.
                cr.setVisible(true);
            }
        });

    }
}
