package jp.ngt.rtm.modelpack;

import cpw.mods.fml.relauncher.Side;
import jp.ngt.ngtlib.io.IProgressWatcher;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTJson;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.texture.TextureManager;
import jp.ngt.rtm.network.PacketModelPack;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.crash.CrashReport;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * モデルパックのロードを行う, Client/Server共通
 */
public final class ModelPackLoadThread extends Thread implements IProgressWatcher {
    private final Side threadSide;
    private final boolean displayWindow;

    private JFrame mainFrame;
    private JProgressBar[] bars;
    private JLabel[] labels;
    private int[] maxValue;

    public boolean finished;

    private int count;

    public ModelPackLoadThread(Side par1) {
        super("RTM ModelPack Load");
        this.threadSide = par1;

        //Linux(X11)でDISPLAY変数が設定されてないとSwing使えないのでそのチェック
        this.displayWindow = (par1 == Side.CLIENT) && !GraphicsEnvironment.isHeadless();

        if (this.displayWindow) {
            this.bars = new JProgressBar[2];
            this.labels = new JLabel[2];
            this.maxValue = new int[2];

            this.mainFrame = new JFrame("RealTrainMod");
            this.mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            this.mainFrame.setSize(320, 180);
            this.mainFrame.setLocationByPlatform(true);
            this.mainFrame.setResizable(false);
            //this.mainFrame.setUndecorated(true);

            JPanel[] panels = new JPanel[4];

            this.labels[0] = new JLabel("Start Loading");
            panels[0] = new JPanel();
            panels[0].add(this.labels[0]);

            this.bars[0] = new JProgressBar();
            this.bars[0].setValue(0);
            panels[1] = new JPanel();
            panels[1].add(this.bars[0]);

            this.labels[1] = new JLabel("Ready");
            panels[2] = new JPanel();
            panels[2].add(this.labels[1]);

            this.bars[1] = new JProgressBar();
            this.bars[1].setValue(0);
            panels[3] = new JPanel();
            panels[3].add(this.bars[1]);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            Arrays.stream(panels).forEach(panel::add);
            this.mainFrame.getContentPane().add(panel);
        }
    }

    @Override
    public void run() {
        if (this.displayWindow) {
            this.mainFrame.setVisible(true);
        }

        try {
            this.runThread();
        } catch (Throwable e) {
            this.finish();
            if (this.threadSide == Side.CLIENT) {
                CrashReport crashReport = CrashReport.makeCrashReport(e, "Loading RTM ModelPack");
                crashReport.makeCategory("Initialization");
                crashReport = NGTUtilClient.getMinecraft().addGraphicsAndWorldToCrashReport(crashReport);
                NGTUtilClient.getMinecraft().displayCrashReport(crashReport);
            } else {
                e.printStackTrace();
            }
        } finally {
            if (this.displayWindow) {
                this.mainFrame.dispose();
            }
        }
    }

    private void runThread() throws InterruptedException {
        this.setMaxValue(0, 8, "");

        if (this.threadSide == Side.CLIENT && RTMCore.useServerModelPack) {
            this.setText(0, "Waiting for connecting to Server");
            this.setText(1, "You can start game");

            //サーバーと接続するまで待機
            while (RTMCore.proxy.getConnectionState() == 0)//!ConnectionManager.INSTANCE.isConnectedToServer())
            {
                RTMCore.NETWORK_WRAPPER.sendToServer(new PacketNotice(PacketNotice.Side_SERVER, "getModelPack"));
                sleep(500L);
            }

            //ダウンロード終了まで待機
            while (!PacketModelPack.writer.finish) {
                sleep(500L);
            }
        }

        long l0 = System.nanoTime();
        this.loadModelFromConfig();
        long time = System.nanoTime() - l0;
        NGTLog.debug("Load time:" + time);

        this.finish();
    }

    private void loadModelFromConfig() {
        this.setValue(0, 1, "Loading Train Models");
        List<File> fileList0 = NGTFileLoader.findFile((file) -> {
            String name = file.getName();
            return name.endsWith(".json") && name.startsWith("Model");
        });

        List<File> fileList1 = TextureManager.INSTANCE.loadTextures(this);
        List<File> fileList2 = TextureManager.INSTANCE.loadRailRoadSigns(this);

        this.setValue(0, 4, "Registering All Models");
        this.setMaxValue(1, fileList0.size(), "");

        ExecutorService executor;
        switch (RTMCore.loadSpeed) {
            case 1:
                executor = Executors.newSingleThreadExecutor();
                break;
            case 3:
                executor = Executors.newWorkStealingPool();
                break;
            default:
                executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 3);
                break;
        }
        List<Future<?>> list;
        try {
            list = fileList0.stream().map(file -> executor.submit(() -> {
                String json = NGTJson.readFromJson(file);
                String type = file.getName().split("_")[0];
                try {
                    String s = ModelPackManager.INSTANCE.registerModelset(type, json);
                    this.addValue(1, s);
                } catch (ModelPackException e) {
                    throw e;//そのまま投げる
                } catch (Throwable e) {
                    throw new ModelPackException("Can't load model", file.getAbsolutePath(), e);
                }
            })).collect(Collectors.toList());

            TextureManager.INSTANCE.registerTextures(this, fileList1, executor, list);
            TextureManager.INSTANCE.registerRailRoadSigns(this, fileList2, executor, list);
        } finally {
            executor.shutdown();
        }


        list.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new ModelPackException("Can't load model", future.toString(), e);
            }
        });
    }

    @Override
    public void finish() {
        this.finished = true;
    }

    @Override
    public void setMaxValue(int id, int value, String label) {
        if (!this.displayWindow) {
            return;
        }

        if (id < 2) {
            this.maxValue[id] = value;
            if (label != null && label.length() > 0) {
                this.labels[id].setText(label);
            }
        }
    }

    @Override
    public void addMaxValue(int id, int value) {
        if (!this.displayWindow) {
            return;
        }

        if (id < 2) {
            this.maxValue[id] += value;
        }
    }

    @Override
    public void setValue(int id, int value, String label) {
        if (!this.displayWindow) {
            return;
        }

        if (id < 2) {
            int i = (int) ((float) value / (float) this.maxValue[id] * 100.0F);
            this.bars[id].setValue(i);
            if (label != null && label.length() > 0) {
                this.labels[id].setText(label);
            }
        }
    }

    public void addValue(int id, String label) {
        if (!this.displayWindow) {
            return;
        }

        if (id < 2) {
            int i = (int) ((float) ++this.count / (float) this.maxValue[id] * 100.0F);
            this.bars[id].setValue(i);
            if (label != null && label.length() > 0) {
                this.labels[id].setText(label);
            }
        }
    }

    @Override
    public void setText(int id, String label) {
        if (!this.displayWindow) {
            return;
        }

        if (id < 2) {
            if (label != null && label.length() > 0) {
                this.labels[id].setText(label);
            }
        }
    }
}