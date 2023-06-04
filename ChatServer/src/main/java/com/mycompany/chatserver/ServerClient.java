/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatserver;

import game.Message;
import static game.Message.Message_Type.Pair;
import static game.Message.Message_Type.Room;
import static game.Message.Message_Type.Text;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClient extends Thread {

    Socket socket;               //Socket oluşturmak için kullanılacak.
    ObjectOutputStream output; //Client verilerini okuyabilmek için tanımlandı.
    ObjectInputStream input;  //Clienta mesaj gönderebilmek için tanımlandı.
    boolean isListening;     //Dinleme kontrolü için tanımlandı.
    public ArrayList<ServerClient> pairedClients = new ArrayList<>();
    public String clientName;
    public static ArrayList<String> clientList = new ArrayList<>();
    public ServerClient pairedCurrent;

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

    public void SendMessage(Message.Message_Type type, String content) { //Clienta serverdan mesaj göndermek için bu metodu tanımladım.
        try {
            Message message = (Message) new Message(type); //Message tipinde bir mesaj objesi oluşturur.
            message.content = content; //contentini atar.
            output.writeObject(message); //write object metodu ile mesajı gönderir.
        } catch (IOException ex) { //Yukarıdaki işlemlerde bir hata oluşması durumunda bu kısımda yakalanır.
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void SendMessage(Message.Message_Type type, ArrayList<String> content) { //Clienta serverdan mesaj göndermek için bu metodu tanımladım.
        try {
            Message message = (Message) new Message(type); //Message tipinde bir mesaj objesi oluşturur.
            message.userList = content; //contentini atar.
            output.writeObject(message); //write object metodu ile mesajı gönderir.
        } catch (IOException ex) { //Yukarıdaki işlemlerde bir hata oluşması durumunda bu kısımda yakalanır.
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
                        System.out.println("Name:" + this.clientName); //Öncelikle bağlananın adını ekrana yazdırır.
                        Server.SendBroadcast();
                        break;
                    case Pair:
                        for (int i = 0; i < Server.clients.size(); i++) {
                            if (Server.clients.get(i).clientName.equals(received.content.toString())) {
                                this.pairedClients.add(Server.clients.get(i));
                                // this.pairedCurrent = Server.clients.get(i);
                                Server.clients.get(i).pairedClients.add(this);
                                // Server.clients.get(i).pairedCurrent = this;

                            }
                        }
                        for (ServerClient pairedClient : this.pairedClients) {
                            if (pairedClient != null && pairedClient.clientName.equals(received.content.toString())) {
                                System.out.println("BURAYA GİRDİ.");
                                pairedClient.SendMessage(Pair, this.clientName);
                            }
                        }

                        break;
                    case Room:
                        String[] makeRoom = received.content.toString().split(" ");
                        for (ServerClient pairedClient : this.pairedClients) {
                            if (pairedClient != null && pairedClient.clientName.equals(makeRoom[0])) {
                                pairedClient.SendMessage(Room, makeRoom[1]);
                            }
                        }

                        break;
                    case Text:
                        String receivedMessage = received.content.toString();
                        for (ServerClient pairedClient : this.pairedClients) {
                             if (pairedClient != null ) {
                            this.SendMessage(Text, receivedMessage);
                            pairedClient.SendMessage(Text, receivedMessage);
                             }
                        }
                        break;
                }
            } catch (IOException ex) {
                System.out.println("Error: Message couldn't get.");//Mesaj alınamadığında client oyundan çıkmıştır.
                this.Stop(); //Bu durumda socket kapatılır ve dinlemeyi bırakır.
            } catch (ClassNotFoundException ex) {//input.readObject() kısmında oluşabilecek hatalar burada yakalanır.
                this.Stop();//Bu durumda socket kapatılır ve dinlemeyi bırakır.
            }
        }
    }

}
