/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Shivanagesh Chandra
 */
public class Router {

    public static AtomicInteger nextId = new AtomicInteger();
    public static AtomicInteger routerNextId = new AtomicInteger();

    //For threads
    private boolean isRunning = true;

    //For router frequency update
    private HashMap<InetAddress, Integer> CurrentFrequency;
    private HashMap<InetAddress, Integer> PreviousFrequency;

    private int port;
    private InetAddress IPaddress;

    private RouterTable routingTable;

    private HashMap<InetAddress, Integer> neighbors;

    private DatagramSocket routerSocket;

    public int getPort() {
        return port;
    }

    public RouterTable getRoutingTable() {
        return routingTable;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public DatagramSocket getRouterSocket() {
        return routerSocket;
    }

    public boolean isIsRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void addNeighborRouter(InetAddress add, Integer port) {
        neighbors.put(add, port);
    }

    public void removeNeighborRouter(InetAddress add) {
        neighbors.remove(add);
    }

    public void setRouterSocket(DatagramSocket routerSocket) {
        this.routerSocket = routerSocket;
    }

    public Router(int port, String IPaddress) throws SocketException, Exception {
        this.port = port;
        try {
            this.IPaddress = InetAddress.getByName(IPaddress);
        } catch (UnknownHostException ex) {
            System.err.println("Host not found " + ex.getMessage());
        }
        routingTable = new RouterTable(routerNextId.incrementAndGet());

        neighbors = new HashMap<InetAddress, Integer>();

        CurrentFrequency = new HashMap<InetAddress, Integer>();
        PreviousFrequency = new HashMap<InetAddress, Integer>();

        routerSocket = new DatagramSocket(port, this.IPaddress);
        (new Thread(new receivePacket())).start();
        (new Thread(new CheckingNeighborsAlive())).start();
        (new Thread(new routerUpdate())).start();
    }

    public Router(int port) throws SocketException, Exception {
        this.port = port;
        System.out.println("network.Router.<init>()" + routerNextId);
        routingTable = new RouterTable(routerNextId.incrementAndGet());

        CurrentFrequency = new HashMap<InetAddress, Integer>();
        PreviousFrequency = new HashMap<InetAddress, Integer>();

        neighbors = new HashMap<InetAddress, Integer>();
        routerSocket = new DatagramSocket(port);
        (new Thread(new receivePacket())).start();
        (new Thread(new routerUpdate())).start();
        (new Thread(new CheckingNeighborsAlive())).start();

    }

    class receivePacket implements Runnable {

        public void run() {
            while (isRunning) {
                try {
//                    for (Integer name : routingTable.getTable().keySet()) {
//                        String key = name.toString();
//                        String value = routingTable.getTable().get(name).toString();
//                        System.out.println(key + " " + value);
//
//                    }
                    handlePacket();
                } catch (IOException e) {
                    System.err.println("Run packet" + e.getMessage());
                    continue;
                }
            }
        }

    }

    class routerUpdate implements Runnable {

        public void run() {
            while (isRunning) {
                try {
//                    for (InetAddress add : neighbors.keySet()) {
//                        String key = add.toString();
//                        String value = neighbors.get(add).toString();
//                        System.out.println(key + " " + value);
//
//                    }
                    Thread.sleep(15000);
                    sendRouterTableToNeighbors();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    continue;
                }
            }
        }
    }

    class CheckingNeighborsAlive implements Runnable {

        public void run() {
            while (isRunning) {
                try {
                    //  System.out.println("network.Router.CheckingNeighborsAlive.run() ");
                    for (InetAddress n : CurrentFrequency.keySet()) {
                        if (PreviousFrequency.get(n) != null) {
                            if (Objects.equals(PreviousFrequency.get(n), CurrentFrequency.get(n))) {
                                System.err.println("Router is not responding removing entries from router " + n + " " + PreviousFrequency.get(n) + " " + CurrentFrequency.get(n));
                                //removeRouteToRouter(n);
                            } else {
                                PreviousFrequency.put(n, CurrentFrequency.get(n));
                            }
                        } else {
                            PreviousFrequency.put(n, CurrentFrequency.get(n));
                        }
                    }

                } catch (Exception ex) {
                    System.err.println("network.Router.CheckingNeighborsAlive.run()" + ex.getMessage());
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                    System.out.println("network.Router.CheckingNeighborsAlive.run() thread exception" + ex.getMessage());
                    continue;
                }
            }
        }
    }

    public void removeRouteToRouter(InetAddress n) throws UnknownHostException {
        int port = neighbors.get(n);

        for (Integer name : routingTable.getTable().keySet()) {
            List<String> value = routingTable.getTable().get(name);
            InetAddress add = null;
            try {
                add = InetAddress.getByName(value.get(0));
            } catch (UnknownHostException ex) {
                System.out.println("network.Router.removeRouteToRouter()" + ex.getMessage());
            }

            System.err.println("network.Router.removeRouteToRouter() ------------------");
            System.out.println("network.Router.removeRouteToRouter()" + name);
            System.out.println("network.Router.removeRouteToRouter()" + add);
            System.out.println("network.Router.removeRouteToRouter()" + n);

            System.out.println("network.Router.removeRouteToRouter()" + port);
            System.out.println("network.Router.removeRouteToRouter()" + Integer.parseInt(value.get(2)));

            System.out.println("network.Router.removeRouteToRouter()" + Objects.equals(add, InetAddress.getByName("127.0.1.1")));
            System.out.println("network.Router.removeRouteToRouter()" + Objects.equals(add, n));
            System.out.println("network.Router.removeRouteToRouter()" + (add != null && Objects.equals(add, n) && port == Integer.parseInt(value.get(2))));
            System.err.println("network.Router.removeRouteToRouter() *******************");
            try {
                if (Objects.equals(add, InetAddress.getByName("127.0.1.1"))) {
                    System.out.println("network.Router.removeRouteToRouter() Special case");
                    if (port == Integer.parseInt(value.get(2))) {
                        routingTable.remove(name);
                    }
                } else if (Objects.equals(n, InetAddress.getByName("127.0.1.1"))) {
                    if (port == Integer.parseInt(value.get(2))) {
                        routingTable.remove(name);
                    }
                } else if (add != null && Objects.equals(add, n) && port == Integer.parseInt(value.get(2))) {
                    routingTable.remove(name);
                }
            } catch (Exception ex) {

            }

        }

    }

    public void sendRouterTableToNeighbors() {
        for (InetAddress add : neighbors.keySet()) {
            try {
                if (add == null) {
                    continue;
                }
                InetAddress address = add;
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream(60000);
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                os.flush();
                os.writeObject(routingTable);
                os.flush();
                //retrieves byte array
                byte[] token = "RTQ#R#".getBytes();
                byte[] sendBuf = byteStream.toByteArray();
                byte[] data = new byte[token.length + sendBuf.length];

                for (int i = 0; i < data.length; ++i) {
                    data[i] = i < token.length ? token[i] : sendBuf[i - token.length];
                }

                //   System.out.println("network.Router.sendRouterTableToNeighbors()" + add);
                //   System.out.println("network.Router.sendRouterTableToNeighbors()" + neighbors.get(add));
                //Actually datagram for mutiple computers
                DatagramPacket packet = new DatagramPacket(
                        data, data.length, address, neighbors.get(add));

                int byteCount = packet.getLength();
                routerSocket.send(packet);
                os.close();
            } catch (UnknownHostException e) {
                System.err.println("Exception:  " + e);
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void handlePacket() throws IOException {
        DatagramPacket recvPacket = receivePacket();
        byte[] data = recvPacket.getData();
        //Checking whether is registration packet 
        String str = new String(data, "UTF-8").trim();
        if (str.equals("RegClientToRouter#$")) {
            registration(recvPacket);
        } else if ((char) (data[0] & 0xFF) == 'R' && (char) (data[1] & 0xFF) == 'T' && (char) (data[2] & 0xFF) == 'Q' && (data[3] & 0xFF) == '#' && (data[4] & 0xFF) == 'R' && (data[5] & 0xFF) == '#') {

            for (InetAddress n : neighbors.keySet()) {
                int port = neighbors.get(n);
                InetAddress ipaddress = recvPacket.getAddress();
                int remoteRouterPort = recvPacket.getPort();
//                System.out.println("network.Router.handlePacket()" + n);
//                System.out.println("network.Router.handlePacket()" + InetAddress.getByName("127.0.1.1"));
//                System.out.println("network.Router.handlePacket()" + ipaddress);
//                System.out.println("network.Router.handlePacket()" + port);
//                System.out.println("network.Router.handlePacket()" + remoteRouterPort);
//                System.out.println("network.Router.handlePacket()" + (Objects.equals(n, ipaddress) && port == remoteRouterPort));
                //Special case for Local router running on same machine 
                if (Objects.equals(n, InetAddress.getByName("127.0.1.1"))) {
                    if (port == remoteRouterPort) {
                        //    System.out.println("network.Router.handlePacket() found match");
                        if (CurrentFrequency.get(n) != null) {
                            CurrentFrequency.replace(n, CurrentFrequency.get(n) + 1);
                        } else {
                            CurrentFrequency.put(n, 1);
                        }
                    }
                } else if (Objects.equals(n, ipaddress) && port == remoteRouterPort) {
//                    System.out.println("network.Router.handlePacket() found match" + CurrentFrequency);
//                    System.out.println("network.Router.handlePacket() found match" + CurrentFrequency.get(n));
                    if (CurrentFrequency.get(n) != null) {
                        CurrentFrequency.replace(n, CurrentFrequency.get(n) + 1);
                    } else {
                        CurrentFrequency.put(n, 1);
                    }
                }

            }
            handleRouterUpdate(recvPacket);

        } else {
            //Get destination address 
            String detstinationId = "" + (char) (data[0] & 0xFF) + (char) (data[1] & 0xFF);
            String detstinationPort = "" + (char) (data[2] & 0xFF) + (char) (data[3] & 0xFF) + (char) (data[4] & 0xFF) + (char) (data[5] & 0xFF) + (char) (data[6] & 0xFF);
            int destId = Integer.parseInt(detstinationId);
            int destPort = Integer.parseInt(detstinationPort);
            // System.out.println("network.Router.handlePacket()" + destId);
            List<String> lookaddress = routingTable.get(destId);
            //    System.out.println("network.Router.handlePacket()" + lookaddress);
            InetAddress destIp = InetAddress.getByName(lookaddress.get(0));
            if (Integer.parseInt(lookaddress.get(1)) > 0) {
                destPort = Integer.parseInt(lookaddress.get(2));
            }

//            System.out.println("network.Router.handlePacket()" + destId);
//            System.out.println("network.Router.handlePacket()" + destIp);
//            System.out.println("network.Router.handlePacket()" + destPort);
            DatagramPacket theOutput
                    = new DatagramPacket(data, data.length, destIp, destPort);
            routerSocket.send(theOutput);
        }

    }

    public void handleRouterUpdate(DatagramPacket packet) {
        try {
            // System.out.println("network.Router.handleRouterUpdate() got update for " + routingTable.getRouter_number());
            int byteCount = packet.getLength();
            //  System.out.println("network.Router1.handleRouterUpdate()" + byteCount);
            // System.out.println("network.Router1.handleRouterUpdate()" + packet.getData().length);
            byte[] data = new byte[packet.getData().length];
            data = packet.getData();
            byte[] routingTableInBytes = new byte[data.length];
            for (int i = 6; i < data.length; i++) {
                routingTableInBytes[i - 6] = data[i];
            }
            ByteArrayInputStream byteStream = new ByteArrayInputStream(routingTableInBytes);
            ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
            RouterTable rt = (RouterTable) is.readObject();
            is.close();
            // System.err.println("From router  " + rt.getRouter_number());
            // Updating other client entries thi router table
//            System.out.println("Before Update");
//            for (Integer name : routingTable.getTable().keySet()) {
//                String key = name.toString();
//                List<String> value = routingTable.getTable().get(name);
//                System.out.println(key + " " + value.get(0) + " " + value.get(1) + " " + value.get(2));
//            }

            for (Integer name : rt.getTable().keySet()) {

                if (routingTable.get(name) == null) {
                    List<String> remoteTableVlaue = rt.get(name);
//                    System.out.println("network.Router.handleRouterUpdate()"+ Integer.parseInt(remoteTableVlaue.get(2)) );
//                    System.out.println("network.Router.handleRouterUpdate()" + routerSocket.getLocalPort());
//                    System.out.println("network.Router.handleRouterUpdate()"+(Integer.parseInt(remoteTableVlaue.get(2)) != routerSocket.getLocalPort()));
                    if (Integer.parseInt(remoteTableVlaue.get(2)) != routerSocket.getLocalPort()) {
                        int cost = Integer.parseInt(remoteTableVlaue.get(1)) + 1;
                        remoteTableVlaue.remove(1);
                        remoteTableVlaue.remove(1);
                        remoteTableVlaue.add(Integer.toString(cost));
                        remoteTableVlaue.add(Integer.toString(packet.getPort()));
                        routingTable.put(name, remoteTableVlaue);
                    }
                } else {

                    List<String> currentTableValue = routingTable.get(name);
                    List<String> remoteTableVlaue = rt.get(name);
                    if (Integer.parseInt(remoteTableVlaue.get(1)) < 0) {
                        continue;
                    }
                    if (Integer.parseInt(currentTableValue.get(1)) > (Integer.parseInt(remoteTableVlaue.get(1)) + 1)) {
                        int cost = Integer.parseInt(remoteTableVlaue.get(1)) + 1;
                        remoteTableVlaue.remove(1);
                        remoteTableVlaue.remove(1);
                        remoteTableVlaue.add(Integer.toString(cost));
                        remoteTableVlaue.add(Integer.toString(packet.getPort()));
                        routingTable.update(name, remoteTableVlaue);
                    }
                }

                for (Integer value : routingTable.getTable().keySet()) {
                    List<String> cuurentTableValue = routingTable.get(value);
//                    System.out.println("network.Router.handleRouterUpdate()"+ value);
//                    System.out.println("network.Router.handleRouterUpdate()" + InetAddress.getByName(cuurentTableValue.get(0)));
//                    System.out.println("network.Router.handleRouterUpdate()" + packet.getAddress());
//                    System.out.println("network.Router.handleRouterUpdate()" + Integer.parseInt(cuurentTableValue.get(2)));
//                    System.out.println("network.Router.handleRouterUpdate()" +  packet.getPort());
//                    System.out.println("network.Router.handleRouterUpdate()" + (Objects.equals(InetAddress.getByName(cuurentTableValue.get(0)),packet.getAddress()) && Integer.parseInt(cuurentTableValue.get(2)) == packet.getPort()));
                    if (Objects.equals(InetAddress.getByName(cuurentTableValue.get(0)),packet.getAddress()) && Integer.parseInt(cuurentTableValue.get(2)) == packet.getPort()) {
                        System.out.println("network.Router.handleRouterUpdate() Inside"+rt.get(value));
                        if (rt.get(value) == null) {
                            routingTable.remove(value);
                        }
                    }

                }

            }

//            System.out.println("After Update");
//            for (Integer name : routingTable.getTable().keySet()) {
//                String key = name.toString();
//                List<String> value = routingTable.getTable().get(name);
//                System.out.println(key + " " + value.get(0) + " " + value.get(1) + " " + value.get(2));
//            }
        } catch (IOException e) {
            System.err.println("Exception:  " + e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void registration(DatagramPacket regPacket) throws IOException {

        InetAddress cliecntAdd = regPacket.getAddress();
        int clientId = nextId.incrementAndGet();
        System.out.println("network.Router.registration()" + cliecntAdd.getHostName());
        routingTable.add(clientId, cliecntAdd.getHostName(), 0);
        String responseString;
        if (clientId < 10) {
            responseString = "RegRouterToClient#$0" + clientId;
        } else {
            responseString = "RegRouterToClient#$" + clientId;
        }
        byte[] reponseBytes = responseString.getBytes();
        System.out.println("network.Router.registration() client address " + cliecntAdd + " " + regPacket.getPort());
        DatagramPacket theOutput
                = new DatagramPacket(reponseBytes, reponseBytes.length, cliecntAdd, regPacket.getPort());
        routerSocket.send(theOutput);

    }

    private DatagramPacket receivePacket() throws IOException {
        byte[] buffer = new byte[60000];

        DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
        routerSocket.receive(recvPacket);

        return recvPacket;
    }

//    public static void main(String[] args) {
//
//        System.out.print("Please router number port number: ");
//
//        int port_number = 0;
//
//        Scanner scan = new Scanner(System.in);
//        port_number = scan.nextInt();
//        scan.close();
//
//        System.out.println();
//
//        Router router = null;
//        Router router2 = null;
//        Router router3 = null;
//        Router router4= null;
//        Router router5= null;
//        Router router6= null;
//
//        try {
//            router = new Router(port_number);
//               System.out.print("Router " + router.routingTable.getRouter_number() + " is up and running :D");
//            Thread.sleep(5000);
//            router2 = new Router(4110);
//              System.out.print("\nRouter " + router2.routingTable.getRouter_number() + " is up and running :D");
//              Thread.sleep(5000);
//            router3 = new Router(4112);
//              System.out.print("\nRouter " + router3.routingTable.getRouter_number() + " is up and running :D");
//              Thread.sleep(5000);
//            router4 = new Router(4114);
//              System.out.print("\nRouter " + router4.routingTable.getRouter_number() + " is up and running :D");
//              Thread.sleep(5000);
//            router5 = new Router(4116);
//             System.out.print("\nRouter " + router5.routingTable.getRouter_number() + " is up and running :D");
//              Thread.sleep(5000);
//            router6 = new Router(4117);
//          
//             System.out.print("\nRouter " + router6.routingTable.getRouter_number() + " is up and running :D");
//            
////            //Router 1
////            router.getRoutingTable().add(2, InetAddress.getByName("127.0.0.1"), 0);
////            router.getRoutingTable().add(3, InetAddress.getByName("127.0.0.1"), 0);
////            
////            //Router 2
////            router2.getRoutingTable().add(4, InetAddress.getByName("127.0.0.1"), 0);
////            router2.getRoutingTable().add(3, InetAddress.getByName("127.0.0.1"), 0);
////            
////            //Router 3
////            router3.getRoutingTable().add(5, InetAddress.getByName("127.0.0.1"), 0);
////            router3.getRoutingTable().add(6, InetAddress.getByName("127.0.0.1"), 0);
////             
////            //Router 4
////            router4.getRoutingTable().add(7, InetAddress.getByName("127.0.0.1"), 0);
////            router4.getRoutingTable().add(8, InetAddress.getByName("127.0.0.1"), 0);
////            
////              //Router 5
////            router5.getRoutingTable().add(9, InetAddress.getByName("127.0.0.1"), 0);
////            router5.getRoutingTable().add(10, InetAddress.getByName("127.0.0.1"), 0);
////            
////              //Router 6
////            router6.getRoutingTable().add(11, InetAddress.getByName("127.0.0.1"), 0);
////            router6.getRoutingTable().add(12, InetAddress.getByName("127.0.0.1"), 0);
//            
//            
//            //Router 1 Neighbors
//            router.addNeighborRouter(InetAddress.getByName("127.0.0.1"), router2.getRouterSocket().getLocalPort());
//            router.addNeighborRouter(InetAddress.getByName("127.0.1.1"), router3.getRouterSocket().getLocalPort());
//            
//            //Router 2 Neighbors
//            router2.addNeighborRouter(InetAddress.getByName("127.0.0.1"), router.getRouterSocket().getLocalPort());
//            router2.addNeighborRouter(InetAddress.getByName("127.0.1.1"), router4.getRouterSocket().getLocalPort());
//            
//             //Router 3 Neighbors
//            router3.addNeighborRouter(InetAddress.getByName("127.0.0.1"), router.getRouterSocket().getLocalPort());
//            router3.addNeighborRouter(InetAddress.getByName("127.0.1.1"), router5.getRouterSocket().getLocalPort());
//            
//            
//            //Router 4 Neighbors
//            router4.addNeighborRouter(InetAddress.getByName("127.0.0.1"), router2.getRouterSocket().getLocalPort());
//          
//            //Router 5 Neighbors
//            router5.addNeighborRouter(InetAddress.getByName("127.0.0.1"), router3.getRouterSocket().getLocalPort());
//            router5.addNeighborRouter(InetAddress.getByName("127.0.1.1"), router6.getRouterSocket().getLocalPort());
//            
//             //Router 6 Neighbors
//            router6.addNeighborRouter(InetAddress.getByName("127.0.0.1"), router5.getRouterSocket().getLocalPort());
//            
//           
//        } catch (SocketException se) {
//            String message = "Port in use";
//            System.err.println(message);
//        } catch (Exception se) {
//            System.err.println(se.getMessage());
//
//        }
//
//    }
}
