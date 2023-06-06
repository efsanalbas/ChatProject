/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatclient;

import java.util.ArrayList;

/**
 *
 * @author nurefsanalbas
 */
public class ClientRoom { //ServerRoom clasının Client tarafındaki karşılayıcısı.

    public String roomName;
    public ArrayList<String> participants = new ArrayList<>();

    public ClientRoom(String roomInfo) {
        String[] roomInformation = roomInfo.split(" "); //Oda bilgilerini boşluklardan ayırır ve dizi haline getirir.
        this.roomName = roomInformation[0]; //Oda adı dizinin ilk elemanındadır.
        for (int i = 1; i < roomInformation.length; i++) { //Geri kalan elemanlar participantlardır.
            if (!participants.contains(roomInformation[i])) {
                participants.add(roomInformation[i]);
            }
        }
    }

}
