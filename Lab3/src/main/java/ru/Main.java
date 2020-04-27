package ru;

import bt.Bt;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.torrent.TorrentSessionState;
import com.google.inject.Module;
import sun.net.www.protocol.http.HttpURLConnection;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = new File("torrent.torrent");

        File downloadDirectory = new File("Download");
        if(!downloadDirectory.exists()){
            downloadDirectory.mkdir();
        }
        Module dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });

        BtClient client = Bt.client()
                            .torrent(file.toURI().toURL())
                            .stopWhenDownloaded()
                            .storage(new FileSystemStorage(downloadDirectory))
                            .module(dhtModule)
                            .autoLoadModules()
                            .build();


        final boolean[] showPeer = {true};
        final int[] downloadedPercent = {-1};
        long time = System.currentTimeMillis();
        long downloadedSpace = 0;
        client.startAsync(new Consumer<TorrentSessionState>() {
            @Override
            public void accept(TorrentSessionState torrentSessionState) {
                if(torrentSessionState.getPiecesRemaining() == 0){
                    System.out.println("File skipped: " + torrentSessionState.getPiecesSkipped());
                    System.out.println("File incompleted: " + torrentSessionState.getPiecesIncomplete());
                }
                else{
                    if(showPeer[0]){
                        if(torrentSessionState.getConnectedPeers().size() > 0) {
                            torrentSessionState.getConnectedPeers().forEach(System.out::println);
                            showPeer[0] = false;
                        }
                    }
                    int downloaded = (torrentSessionState.getPiecesTotal() - torrentSessionState.getPiecesRemaining()) * 100 / torrentSessionState.getPiecesTotal();
                    if(downloaded > downloadedPercent[0]) {
                        if(downloaded > 0){
                            System.out.println("Remaining " + (System.currentTimeMillis() - time) * (100 - downloaded) / downloaded / 1000);
                            System.out.println(torrentSessionState.getDownloaded() * 1000 / (System.currentTimeMillis() - time) / 1024 / 1024 + " Mb/s");
                        }
                        System.out.println("File Downloaded: " + downloaded + " %");
                        downloadedPercent[0] = downloaded;
                    }
                }
            }
        }, 1000).join();



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
