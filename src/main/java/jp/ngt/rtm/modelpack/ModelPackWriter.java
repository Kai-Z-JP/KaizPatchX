package jp.ngt.rtm.modelpack;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ModelPackWriter {
    private final ModelPackDownloadThread thread;
    private final File modsDir;
    public boolean finish;

    public ModelPackWriter() {
        this.thread = null;
        //this.thread = new ModelPackDownloadThread(this);
        //this.thread.start();
        this.modsDir = NGTFileLoader.getModsDir().get(0);
        this.finish = false;
    }

    public void onPacket(String par1, long par2, byte[] par3) {
        if (par1.startsWith("start_file")) {
            int index = par1.indexOf(":");
            this.deleteModelPack(par1.substring(index + 1));
        } else if (par1.equals("finish")) {
            this.finish = true;
        } else {
            this.writeModelPack(par1, par2, ByteBuffer.wrap(par3));
        }

		/*if(par1.startsWith("start_file"))
		{
			int index = par1.indexOf(":");
			this.startWriting(par1.substring(index + 1));
		}
		else if(par1.equals("end_file"))
		{
			this.endWriting();
		}
		else if(par1.equals("finish"))
		{
			this.finishWriting();
		}
		else
		{
			this.writeBytes(par2, par3);
		}*/
    }

    /**
     * モデルパックを書き込み(追記)
     */
    public void writeModelPack(String par1, long par2, ByteBuffer par3) {
        try {
            @SuppressWarnings("resource")
            FileChannel channel = new FileOutputStream(new File(this.modsDir, "Temp#" + par1)).getChannel();
            channel.write(par3);
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * モデルパックが存在する場合は消去
     */
    public void deleteModelPack(String par1) {
        String name = "Temp#" + par1;
        File file = new File(this.modsDir, name);
        if (file.exists()) {
            file.delete();
            NGTLog.debug("[RTM](Client) Delete ModelPack : " + name);
        }
    }

    public synchronized void startWriting(String par1Name) {
        while (this.thread.writingStatus == 1 || this.thread.writingStatus == 2) {
            try {
                NGTLog.debug("wait (start writing)");
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }

        NGTLog.debug("start writing");
        this.thread.writingStatus = 1;
        this.thread.fileName = "Temp_" + par1Name;
    }

    public synchronized void writeBytes(long par2, ByteBuffer par3) {
        while (this.thread.writingStatus == 0 || this.thread.writingStatus == 1) {
            try {
                NGTLog.debug("wait (write bytes)");
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }

        par3.position(0);

        try {
            this.thread.channel.write(par3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void endWriting() {
        while (this.thread.writingStatus == 0 || this.thread.writingStatus == 1) {
            try {
                NGTLog.debug("wait (end writing)");
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }

        NGTLog.debug("end writing");
        this.thread.writingStatus = 0;
    }

    public synchronized void finishWriting() {
        while (this.thread.writingStatus == 1 || this.thread.writingStatus == 2) {
            try {
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }

        NGTLog.debug("finish writing");
        this.thread.writingStatus = 3;
    }

    public synchronized void resume() {
        this.notify();
    }
}