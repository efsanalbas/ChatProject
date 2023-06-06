/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatserver;

import java.util.ArrayList;

/**
 *
 * @author nurefsanalbas
 */
public class ServerRoom { //Client chat odası oluşturmak istediğinde oda adı ve katılımcı bilgileriyle odayı oluşturur.

    public String roomName;
    public ArrayList<ServerClient> participants;

    public ServerRoom(String roomName, ArrayList<ServerClient> participants) {
        this.roomName = roomName;
        this.participants = participants;
    }

}
