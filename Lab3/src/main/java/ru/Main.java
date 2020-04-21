package ru;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = new File("torrent.torrent");

        byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

        BDecoder decoder = new BDecoder();
        Map dict = decoder.decodeStream(in);
        dict.forEach(mapConsumer);

    }

    private static BiConsumer mapConsumer = new BiConsumer() {
        @Override
        public void accept(Object o, Object o2) {
            if (o2 instanceof Map){
                Map m = (Map) o2;
                m.forEach(this);
            }
            else if(o2 instanceof List) {
                System.out.println(o);
                ((List) o2).forEach(listConsumer);
            }
            else if(o2 instanceof byte[]){
                String o2String = new String((byte[]) o2);
                System.out.println(o + " " + o2String);
            }
            else{
                System.out.println(o + " " + o2);
            }

        }
    };

    private static Consumer listConsumer = new Consumer() {
        @Override
        public void accept(Object o) {
            if(o instanceof List){
                ((List) o).forEach(this);
            }
            else if(o instanceof Map){
                ((Map) o).forEach(mapConsumer);
            }
            else if(o instanceof byte[]){
                System.out.println(new String((byte[]) o));
            }
            else{
                System.out.println(o);
            }
        }
    };
}
