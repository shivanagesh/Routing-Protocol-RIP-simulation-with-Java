/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Shivanagesh Chandra
 */
public class RouterTable implements Serializable {

    private int router_number;
    private HashMap<Integer, List<String>> table;

    public RouterTable(int id) {
        this.router_number = id;
        table = new HashMap<Integer, List<String>>();
    }

    public int getRouter_number() {
        return router_number;
    }

    public void setRouter_number(int router_number) {
        this.router_number = router_number;
    }

    public HashMap<Integer, List<String>> getTable() {
        return table;
    }

        public void add(int id, String add, int cost) {
        List<String> str = new ArrayList<String>();
        str.add(add);
        str.add(Integer.toString(cost));
        str.add("-1");
        table.put(id, str);
    }

    public void put(int id, List<String> str) {
        table.put(id, str);
    }

    public List<String> get(int id) {
        return table.get(id);

    }

    public void remove(int id) {
        table.remove(id);
    }

    public void update(int id, InetAddress add, int cost) {
        List<String> str = new ArrayList<String>();
        str.add(add.toString());
        str.add(Integer.toString(cost));
        table.replace(id, str);
    }
    public void update(int id, List<String> str) {
        table.replace(id, str);
    }

}
