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
public class ServerRoom {

    public String roomName;
    public ArrayList<String> participants;

    public ServerRoom(String roomName, ArrayList<String> participants) {
        this.roomName = roomName;
        this.participants = participants;
    }

}
