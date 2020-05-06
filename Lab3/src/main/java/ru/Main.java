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
        BitTorrent torrent = new BitTorrent("torrent.torrent", "Download");
        torrent.startAsync();
    }
}
