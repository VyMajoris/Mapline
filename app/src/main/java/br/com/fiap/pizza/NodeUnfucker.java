package br.com.fiap.pizza;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class NodeUnfucker {

    public ArrayList<LatLng> getLatLngList() {
        return latLngList;
    }

    public void setLatLngList(ArrayList<LatLng> latLngList) {
        this.latLngList = latLngList;
    }

    ArrayList<LatLng> latLngList = new ArrayList<>();
    int color;

    public JsonNode convertJSONToNode(String json) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        return jsonNode;
    }

    public ArrayList<LatLng> getLatLngs(JsonNode root) {
        return doGetLatLngs(null, root);
    }

    private ArrayList<LatLng> doGetLatLngs(String nodename, JsonNode node) {


        // System.out.println("doGetLatLngs - node name: " + nameToPrint);
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();

            ArrayList<Map.Entry<String, JsonNode>> nodesList = Lists.newArrayList(iterator);
            //System.out.println("Walk Tree - root:" + node + ", elements keys:" + nodesList);
            //System.out.println("NODE NAME 1: "+node);
            // System.out.println("NODE LIST 1: "+nodesList);

            for (Map.Entry<String, JsonNode> nodEntry : nodesList) {
                String name = nodEntry.getKey();
                JsonNode newNode = nodEntry.getValue();

                //System.out.println("  entry - key: " + name + ", value:" + node);
                doGetLatLngs(name, newNode);
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> arrayItemsIterator = node.elements();
            ArrayList<JsonNode> arrayItemsList = Lists.newArrayList(arrayItemsIterator);
            for (JsonNode arrayNode : arrayItemsList) {
                doGetLatLngs("array item", arrayNode);
            }
        } else {
            if (node.isValueNode()) {
                //  System.out.println(" Value key> " + node.canConvertToInt());
                System.out.println("  valueNode: " + node.asText());



                if (!node.canConvertToInt()) {

                    latLngList.add(new Gson().fromJson(node.asText(), LatLng.class));
                }
            } else {
                // System.out.println("  node some other type");
            }
        }

        return latLngList;
    }

    public int getColor(JsonNode root) {
        return doGetColor(null, root);
    }





    private int doGetColor(String nodename, JsonNode node) {


        String nameToPrint = nodename != null ? nodename : "must_be_root";
        // System.out.println("doGetLatLngs - node name: " + nameToPrint);
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();

            ArrayList<Map.Entry<String, JsonNode>> nodesList = Lists.newArrayList(iterator);
            //System.out.println("Walk Tree - root:" + node + ", elements keys:" + nodesList);
            //System.out.println("NODE NAME 1: "+node);
            // System.out.println("NODE LIST 1: "+nodesList);

            for (Map.Entry<String, JsonNode> nodEntry : nodesList) {
                String name = nodEntry.getKey();
                JsonNode newNode = nodEntry.getValue();

                //System.out.println("  entry - key: " + name + ", value:" + node);
                doGetLatLngs(name, newNode);
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> arrayItemsIterator = node.elements();
            ArrayList<JsonNode> arrayItemsList = Lists.newArrayList(arrayItemsIterator);
            for (JsonNode arrayNode : arrayItemsList) {
                doGetLatLngs("array item", arrayNode);
            }
        } else {
            if (node.isValueNode()) {
                //  System.out.println(" Value key> " + node.canConvertToInt());
                System.out.println("  valueNode: " + node.asText());


                if (node.canConvertToInt()) {
                    color = Integer.valueOf(node.asText());

                }
            } else {
                // System.out.println("  node some other type");
            }
        }

        return color;
    }

}