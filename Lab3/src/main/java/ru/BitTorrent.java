package ru;

import bt.Bt;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.torrent.TorrentSessionState;
import com.google.inject.Module;

import java.io.File;
import java.net.MalformedURLException;
import java.util.function.Consumer;

public class BitTorrent {
    private File torrentFile;

    private File downloadDirectory;

    private BtClient client;

    public BitTorrent(String file, String downloadDirectory) throws MalformedURLException {
        this.torrentFile = new File(file);

        this.downloadDirectory = new File(downloadDirectory);
        if (!this.downloadDirectory.exists()){
            this.downloadDirectory.mkdir();
        }

        Module dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });

        this.client = Bt.client()
                .torrent(this.torrentFile.toURI().toURL())
                .stopWhenDownloaded()
                .storage(new FileSystemStorage(this.downloadDirectory))
                .module(dhtModule)
                .autoLoadModules()
                .build();
    }

    public void startAsync(){
        final boolean[] showPeer = {true};
        final int[] downloadedPercent = {-1};
        long time = System.currentTimeMillis();

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
}
