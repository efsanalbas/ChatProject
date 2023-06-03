/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatserver;

/**
 *
 * @author nurefsanalbas
 */
public class ChatServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server(3000); //Serverı oluşturuyoruz ve 3000 nolu porttan dinlemeye başlıyor.
        server.Listen(); //server dinlemeye başlıyor.
    }

}