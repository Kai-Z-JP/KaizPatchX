package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import net.minecraftforge.client.model.ModelFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SideOnly(Side.CLIENT)
public class MqozModel extends MqoModel {
	protected MqozModel(InputStream[] is, String name, int mode, VecAccuracy par3) throws ModelFormatException {
		super(is, name, mode, par3);
	}

	@Override
	protected void init(InputStream[] is) throws ModelFormatException {
		ZipInputStream zis = new ZipInputStream(is[0]);
		try {
			ZipEntry zEntry = zis.getNextEntry();//zip内にmqoは1つとして処理
			super.init(new InputStream[]{zis});
			zis.close();
		} catch (IOException e) {
			throw new ModelFormatException("Exception on reading MQOZ.", e);
		}
	}

	@Override
	public FileType getType() {
		return FileType.MQOZ;
	}
}
