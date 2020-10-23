package jp.ngt.rtm.modelpack;

import jp.ngt.ngtlib.io.NGTJson;

import java.io.File;

public class ModelPackLoadFromConfig extends Thread {

	private final File file;
	private final ModelPackLoadThread thread;

	public ModelPackLoadFromConfig(ModelPackLoadThread m, File f) {
		this.file = f;
		this.thread = m;
	}

	@Override
	public void run() {
		String json = NGTJson.readFromJson(this.file);
		String type = this.file.getName().split("_")[0];
		try {
			String s = ModelPackManager.INSTANCE.registerModelset(type, json);
			this.thread.addValue(1, s);
		} catch (ModelPackException e) {
			throw e;//そのまま投げる
		} catch (Throwable e) {
			throw new ModelPackException("Can't load model", this.file.getAbsolutePath(), e);
		}
	}
}
