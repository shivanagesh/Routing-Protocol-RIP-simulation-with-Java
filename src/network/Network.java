/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Shivanagesh Chandra
 */
public class Network {

    /**
     * These are real IP address of computer which is running router program
     */
    protected  String srcId;
    protected  String srcPort;

    public  String getSrcId() {
        return srcId;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    protected String routerIp;
    protected String routerPort;

    protected DatagramSocket socket;

    public Network(String routerIp, String routerPort, String srcPort) {

        this.routerIp = routerIp;
        this.routerPort = routerPort;
        this.srcPort = srcPort;
        try {
            socket = new DatagramSocket(Integer.parseInt(srcPort));
        } catch (Exception ex) {
            System.out.println("network.Network.<init>() :  Socket is alredy in use");
        }

        getIdFromRouter();

    }

    public void getIdFromRouter() {

        try {
            String reg = "RegClientToRouter#$";
            byte[] data = reg.getBytes();
            InetAddress router = InetAddress.getByName(routerIp);
            DatagramPacket theOutput
                    = new DatagramPacket(data, data.length, router, Integer.parseInt(routerPort));
            socket.send(theOutput);

        } catch (Exception ex) {
            System.err.println("Error in while registring with router");
        }

    }

    public void regRespnseFromRouter(DatagramPacket recvPacket) {

        byte[] data = recvPacket.getData();

        try {
            String reponseString = new String(data, "UTF-8").trim();
            String[] splitArray = reponseString.split("#\\$");
            srcId = splitArray[1];
            System.out.println("network.Network.regRespnseFromRouter() Source Id "+srcId);
            
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

  

    public byte[] BuildPacket(String data, String destinationId, String destinationPort) {

        
//        System.out.println("network.Network.BuildPacket()"+destinationId);
//        System.out.println("network.Network.BuildPacket()"+destinationPort);
//        System.out.println("network.Network.BuildPacket()"+srcId);
//        System.out.println("network.Network.BuildPacket()"+srcPort);
        int addresslength = destinationId.length() + destinationPort.length() + srcId.length() + srcPort.length();

        byte[] bytes = new byte[addresslength + data.length()];
        
        System.out.println("network.Network.BuildPacket()"+bytes.length);

        //Destination address inserting
        bytes[0] = (byte) destinationId.charAt(0);
        bytes[1] = (byte) destinationId.charAt(1);
        bytes[2] = (byte) destinationPort.charAt(0);
        bytes[3] = (byte) destinationPort.charAt(1);
        bytes[4] = (byte) destinationPort.charAt(2);
        bytes[5] = (byte) destinationPort.charAt(3);
        bytes[6] = (byte) destinationPort.charAt(4);
       
        //Source destination 
        bytes[7] = (byte) srcId.charAt(0);
        bytes[8] = (byte) srcId.charAt(1);
        bytes[9] = (byte) srcPort.charAt(0);
        bytes[10] = (byte) srcPort.charAt(1);
        bytes[11] = (byte) srcPort.charAt(2);
        bytes[12] = (byte) srcPort.charAt(3);
        bytes[13] = (byte) srcPort.charAt(4);
        
        
        byte []dataBytes = data.getBytes();
        for(int i = 14; i-14< dataBytes.length ; i++ ) {
            System.out.println("network.Network.BuildPacket()"+i + (i-14));
            bytes[i] = dataBytes[i-14];
        }
        
        return bytes;
    }

    public String receiveMessage(DatagramPacket packet) {
        
         byte[] revData = packet.getData();
         
         String data = "";
         
         for(int i = 14; i < revData.length ; i++ ){
             data = data + (char) (revData[i] & 0xFF); 
         }

        //To handle the recive of data
        return data;
    }

}
