package jp.ngt.rtm.modelpack;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.rtm.RTMConfig;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketModelPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class ModelPackUploadThread extends Thread {
    private final ByteBuffer buffer = ByteBuffer.allocate(RTMCore.PacketSize);

    public ModelPackUploadThread() {
        super("RTM ModelPack Upload");
    }

    public static void startThread() {
        if (!RTMConfig.useServerModelPack) {
            return;
        }
        ModelPackUploadThread thread = new ModelPackUploadThread();
        thread.start();
    }

    @Override
    public void run() {
        NGTLog.debug("[RTM](UploadThread) Start uploading ModelPack");
        List<File> fileList = NGTFileLoader.findFile((file) -> {
            String name = file.getName();
            return name.startsWith("ModelPack_") && name.endsWith(".zip");
        });
        fileList.forEach(file -> {
            try {
                NGTLog.debug("[RTM](UploadThread) Start uploading " + file.getName());
                RTMCore.NETWORK_WRAPPER.sendToAll(new PacketModelPack("start_file:" + file.getName(), 0, ByteBuffer.allocate(RTMCore.PacketSize)));

                @SuppressWarnings("resource")
                FileChannel channel = new FileInputStream(file).getChannel();
                long size = channel.size();
                while (channel.read(this.buffer) >= 0) {
                    this.buffer.flip();
                    RTMCore.NETWORK_WRAPPER.sendToAll(new PacketModelPack(file.getName(), size, this.buffer));
                    this.buffer.clear();
                    sleep(100);
                }
                channel.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        RTMCore.NETWORK_WRAPPER.sendToAll(new PacketModelPack("finish", 0, ByteBuffer.allocate(RTMCore.PacketSize)));
        NGTLog.debug("[RTM](UploadThread) Finish uploading ModelPack");
    }
}