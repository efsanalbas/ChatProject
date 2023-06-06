/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatserver;

import game.Message;
import static game.Message.Message_Type.AddParticipant;
import static game.Message.Message_Type.CreateRoom;
import static game.Message.Message_Type.File;
import static game.Message.Message_Type.ParticipantAdded;
import static game.Message.Message_Type.Text;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClient extends Thread {

    Socket socket;               //Socket oluşturmak için kullanılacak.
    ObjectOutputStream output; //Client verilerini okuyabilmek için tanımlandı.
    ObjectInputStream input;  //Clienta mesaj gönderebilmek için tanımlandı.
    boolean isListening;     //Dinleme kontrolü için tanımlandı.
    public ArrayList<ServerClient> pairedClients = new ArrayList<>();
    public ArrayList<ServerRoom> serverRooms = new ArrayList<>();

    public String clientName;
    public static ArrayList<String> clientList = new ArrayList<>();
    public ServerClient currentPaired;

    public ServerClient(Socket soket) throws IOException { //Clientın mesajlarını serverda karşılamak için ServerClient objesi oluşturmam gerekiyordu ve burada constructor oluşturdum.
        this.socket = soket;
        this.output = new ObjectOutputStream(socket.getOutputStream()); //Mesaj gönderebilmemiz için outputStream oluşturmalıyız.
        this.input = new ObjectInputStream(socket.getInputStream());   // Mesajları okuyabilmemiz için inputStream oluşturmalıyız.
        this.isListening = false;
    }

    public void Listen() {   //Clientı dinlememizi sağlıyor.

        this.isListening = true; //True olduğu sürece clienttan gelen veri dinlenecek.
        this.start();    //run fonksiyonunu çalıştırır ve clienttan gelen mesajlar dinlenmeye başlar.
    }

    public void Stop() {  //Clientı dinlemeyi durdurmak için çalışır.
        try {
            this.isListening = false;  //Clientı dinlemeyi durdurur.
            this.output.close();       //mesaj gönderimi engellenir.
            this.input.close();        //mesaj alımı engellenir.
            this.socket.close();       //socket kapatılır.
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);//yukarıdaki işlemlerde hata meydana geldiğinde, hata burada yakalanır.
        }
    }

    public void SendMessage(Message.Message_Type type, ArrayList<String> content) { //Clienta serverdan Arraylist parametresiyle mesaj göndermek için bu metodu tanımladım.
        try {
            Message message = (Message) new Message(type); //Message tipinde bir mesaj objesi oluşturur.
            message.userList = content; //contentini atar.
            output.writeObject(message); //write object metodu ile mesajı gönderir.
        } catch (IOException ex) { //Yukarıdaki işlemlerde bir hata oluşması durumunda bu kısımda yakalanır.
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void SendMessage(Message.Message_Type type, Object content) {//Object parametresiyle mesaj göndermek için SendMessage metodunun overload fonksiyonu olarak bu metodu tanımladım.
        try {
            Message message = new Message(type);
            message.content = content;
            output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {//start metodu çalıştığında thread burayı çalıştırır.
        Message received = null;
        while (this.isListening) { //isListening true olduğu sürece gelen mesajları dinler.
            try {
                received = (Message) (input.readObject()); //message nesnesini alır ve türüne göre işlemleri gerçekleştirir.
                switch (received.type) {
                    case Name:    //Message tipi Name ise,
                        this.clientName = received.content.toString();
                        Server.clientList.add(this.clientName);
                        System.out.println("Name:" + this.clientName); //Öncelikle bağlananın adını ekrana yazdırılır.
                        Server.SendBroadcast();
                        break;

                    case CreateRoom: //Client bağlananlar listesinden bir kullanıcı seçtiğinde oda oluşturulması için bu tarafa istek atar.
                        String[] roomInfo = received.content.toString().split(" ");//Oda bilgilerini alır.(Oda adı, kullanıcı adı ve oda oluşturmak için seçilen kullanıcı alınır.)
                        ArrayList<ServerClient> participants = new ArrayList<>();//Odanın katılımcıları bu listede tutuldu.
                        for (ServerClient client : Server.clients) {//Kullanıcılar üzerinde döngü oluşturdum.
                            if ((client.clientName.equals(roomInfo[1]) || client.clientName.equals(roomInfo[2]))
                                    && !participants.contains(client)) {
                                participants.add(client);//Her iki clientın da participant listesine client eklendi.(Client ve seçilen client)
                            }
                        }

                        ServerRoom sr = new ServerRoom(roomInfo[0], participants); //Bir serverRoom nesnesi oluşturdum. 
                        //Odanın kullanıcıları ve ismini alan bir constructor'a sahip.
                        List<ServerRoom> tempRooms = new ArrayList<>(Server.rooms); // Geçici bir List oluşturulur

                        boolean roomExists = false;
                        for (ServerRoom room : tempRooms) {
                            if (room.roomName.equals(sr.roomName)) { // Aynı isimde bir oda mevcutsa
                                roomExists = true;
                                break;
                            }
                        }
                        if (!roomExists) { //Oda yoksa
                            Server.rooms.add(sr);
                            System.out.println("Room Added.");
                        } else {
                            System.out.println("There is a room with the same name.");
                        }
                        for (ServerClient client : sr.participants) { //Clientlara oda adı ve katılımcı bilgileri gönderilerek oda oluşturmaları sağlanır.
                            String participant = sr.participants.get(0).clientName;
                            for (int i = 1; i < sr.participants.size(); i++) {
                                participant += " " + sr.participants.get(i).clientName;
                            }
                            client.SendMessage(CreateRoom, sr.roomName + " " + participant);
                        }
                        break;

                    case AddParticipant: //Odaya yeni kullanıcı eklemek için client chatRoomda add participant butonuna bastığında, mesaj burada alınır.
                        String[] addInfo = received.content.toString().split("-123-"); //Kullanıcı adı ve oda adı alındı.
                        String roomName = addInfo[0]; //oda adı
                        String participantName = addInfo[1]; //eklenmek istenen katılımcının adı.
                        ServerClient newClient = null; //Eklenmek istenen client için ServerClient objesi.
                        for (ServerClient client : Server.clients) {
                            if (client.clientName.equals(participantName)) { //Servera bağlanan clientlardan birinin adı eklenmek istenen clienta eşitse,
                                newClient = client; //yeni clienta atandı.
                            }
                        }

                        for (ServerRoom room : Server.rooms) { //Yeni kullanıcı bu kısımda odaya eklendi
                            if (room != null && room.roomName.equals(addInfo[0]) && !room.participants.contains(newClient)) {
                                room.participants.add(newClient);
                                System.out.println("Eklendi.");
                                for (ServerClient participant : room.participants) { //Odaya eklendiğine dair odanın katılımcılarına mesaj gönderildi.
                                    participant.SendMessage(ParticipantAdded, roomName + " " + newClient.clientName);
                                    //Burada mesaj göndermemde asıl amacım yeni eklenen clientın ekranında room oluşması.
                                }
                            }
                        }
                        break;
                    case Text://Kullanıcı odadaki diğer clientlara mesaj göndermek istediğinde server mesajı yönlendirir.
                        String[] receivedMessage = received.content.toString().split("-123-"); //text areadaki mesaj buraya gönderildi.
                        //receivedMessage[0]: roomName
                        //receivedMessage[1]: Gönderilecek mesaj
                        for (ServerRoom room : Server.rooms) {
                            if (room != null && room.roomName.equals(receivedMessage[0])) {
                                for (ServerClient participant : room.participants) {
                                    participant.SendMessage(Text, receivedMessage[1]);//odadaki kullanıcıların hepsine bu mesaj gönderildi.
                                }
                            }
                        }
                        break;
                }
            } catch (EOFException ex) {
                System.out.println("Server connection closed.");
                this.Stop(); //Bu durumda socket kapatılır ve dinlemeyi bırakır.
                break;
            } catch (IOException ex) {
                System.out.println("Error: Message couldn't get.");//Mesaj alınamadığında client oyundan çıkmıştır.
                this.Stop(); //Bu durumda socket kapatılır ve dinlemeyi bırakır.
            } catch (ClassNotFoundException ex) {//input.readObject() kısmında oluşabilecek hatalar burada yakalanır.
                this.Stop();//Bu durumda socket kapatılır ve dinlemeyi bırakır.
            }
        }
    }

}
