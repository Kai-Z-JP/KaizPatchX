package jp.ngt.rtm.modelpack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.IProgressWatcher;

import javax.swing.*;

@SideOnly(Side.CLIENT)
public final class WindowThread extends Thread implements IProgressWatcher {
	private final JFrame mainFrame;
	private final JProgressBar[] bars;
	private final JLabel[] labels;
	private final int[] maxValue;
	private boolean shouldContinue;

	public WindowThread() {
		super("RTM Window");
		this.shouldContinue = true;
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
		for (int i = 0; i < panels.length; ++i) {
			panel.add(panels[i]);
		}
		this.mainFrame.getContentPane().add(panel);
	}

	@Override
	public void run() {
		this.mainFrame.setVisible(true);

		while (this.shouldContinue) {
			try {
				sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.mainFrame.dispose();
	}

	@Override
	public void finish() {
		this.shouldContinue = false;
	}

	@Override
	public void setMaxValue(int id, int value, String label) {
		if (id < 2) {
			this.maxValue[id] = value;
			if (label != null && label.length() > 0) {
				this.labels[id].setText(label);
			}
		}
	}

	@Override
	public void setValue(int id, int value, String label) {
		if (id < 2) {
			int i = (int) ((float) value / (float) this.maxValue[id] * 100.0F);
			this.bars[id].setValue(i);
			if (label != null && label.length() > 0) {
				this.labels[id].setText(label);
			}
		}
	}

	@Override
	public void setText(int id, String label) {
		if (id < 2) {
			if (label != null && label.length() > 0) {
				this.labels[id].setText(label);
			}
		}
	}
}