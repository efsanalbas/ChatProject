/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatclient;

import java.io.Serializable;

public class FileInfo implements Serializable {

    public String roomName;
    public String fileName;
    public byte[] fileBytes;
    public String filePath;

    public FileInfo(String roomName, String fileName, byte[] fileBytes,String filePath) {
        this.roomName = roomName;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.filePath = filePath;

    }
}
