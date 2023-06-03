/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game;

import java.util.ArrayList;

/**
 *
 * @author nurefsanalbas
 */

public class Message implements java.io.Serializable {
    public static enum Message_Type {Name,ConnectedClients,Pair,Text,File}
    
    public Message_Type type;
    public Object content;
    public ArrayList <String> userList= new ArrayList<>();
    public Message(Message_Type t)
    {
        this.type=t;
        }
 
}