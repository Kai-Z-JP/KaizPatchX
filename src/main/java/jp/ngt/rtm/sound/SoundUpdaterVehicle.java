package jp.ngt.rtm.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.entity.vehicle.IUpdateVehicle;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBaseClient;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class SoundUpdaterVehicle implements IUpdateVehicle {
	protected final SoundHandler theSoundHandler;
	protected final EntityVehicleBase theVehicle;

	protected boolean silent;
	protected ResourceLocation prevSoundResource;
	protected ISound prevSound;

	protected List<MovingSoundVehicle> playingSounds = new ArrayList<MovingSoundVehicle>();
	protected Map<Integer, Object> dataMap;

	public SoundUpdaterVehicle(SoundHandler par1, EntityVehicleBase par2) {
		this.theSoundHandler = par1;
		this.theVehicle = par2;
	}

	@Override
	public void update() {
		ModelSetVehicleBaseClient modelset = (ModelSetVehicleBaseClient) this.theVehicle.getModelSet();
		if (modelset.se != null) {
			//RTMUtil.doScriptFunction(modelset.se, "onUpdate", this);
			ScriptUtil.doScriptIgnoreError(modelset.se, "onUpdate", this);
		} else {
			boolean flag = false;
			if (this.theVehicle.isDead) {
				if (this.prevSound != null) {
					((MovingSoundVehicle) this.prevSound).stop();
				}
				return;
			}

			ResourceLocation newSound = this.getSound(modelset);
			if (this.prevSoundResource == null || newSound == null || !newSound.equals(this.prevSoundResource)) {
				if (this.prevSound != null) {
					((MovingSoundVehicle) this.prevSound).stop();
				}
				this.prevSoundResource = newSound;
				this.silent = true;
			}

			if (this.silent && !this.theSoundHandler.isSoundPlaying(this.prevSound) && this.prevSoundResource != null) {
				MovingSoundVehicle sound = createMovingSound(this.theVehicle, this.prevSoundResource, true, this.changePitch());
				this.theSoundHandler.playSound(sound);
				this.prevSound = sound;
				this.silent = false;
				flag = true;
			}
		}
	}

	protected static MovingSoundVehicle createMovingSound(EntityVehicleBase vehicle, ResourceLocation sound, boolean par3, boolean par4) {
		if (vehicle instanceof EntityTrainBase) {
			return new MovingSoundTrain((EntityTrainBase) vehicle, sound, par3, par4);
		} else {
			return new MovingSoundVehicle(vehicle, sound, par3, par4);
		}
	}

	protected ResourceLocation getSound(ModelSetVehicleBaseClient modelset) {
		float speed = this.theVehicle.getSpeed();
		if (speed > 0) {
			return modelset.sound_Acceleration;
		}
		return modelset.sound_Stop;
	}

	protected boolean changePitch() {
		return true;
	}

	public float getSpeed() {
		return this.theVehicle.getSpeed() * 72.0F;
	}

	public boolean inTunnel() {
		World world = this.theVehicle.worldObj;
		int x = MathHelper.floor_double(this.theVehicle.posX);
		int y = MathHelper.floor_double(this.theVehicle.posY);
		int z = MathHelper.floor_double(this.theVehicle.posZ);
		return !world.canBlockSeeTheSky(x + 1, y, z + 1) &&
				!world.canBlockSeeTheSky(x - 1, y, z + 1) &&
				!world.canBlockSeeTheSky(x + 1, y, z - 1) &&
				!world.canBlockSeeTheSky(x - 1, y, z - 1);
	}

	/**
	 * リピートあり
	 */
	public void playSound(String domain, String path, float volume, float pitch) {
		this.playSound(domain, path, volume, pitch, true);
	}

	public void playSound(String domain, String path, float volume, float pitch, boolean repeat) {
		MovingSoundVehicle sound = this.getPlayingSound(domain, path);
		boolean flag = (sound == null);
		if (flag) {
			ResourceLocation resource = new ResourceLocation(domain, path);
			sound = new MovingSoundVehicle(this.theVehicle, resource, repeat, false);
		}
		sound.setVolume(volume);
		sound.setPitch(pitch);

		if (flag) {
			this.theSoundHandler.playSound(sound);
			this.playingSounds.add(sound);
		}
	}

	public void stopSound(String domain, String path) {
		MovingSoundVehicle playing = this.getPlayingSound(domain, path);
		if (playing != null) {
			playing.stop();
			this.playingSounds.remove(playing);
		}
	}

	public void stopAllSounds() {
		for (int i = 0; i < this.playingSounds.size(); ++i) {
			this.playingSounds.get(i).stop();
		}

		if (this.prevSound != null) {
			((MovingSoundVehicle) this.prevSound).stop();
		}
	}

	private MovingSoundVehicle getPlayingSound(String domain, String path) {
		for (int i = 0; i < this.playingSounds.size(); ++i) {
			MovingSoundVehicle sound = this.playingSounds.get(i);
			ResourceLocation resource = sound.getPositionedSoundLocation();
			if (resource.getResourceDomain().equals(domain) && resource.getResourcePath().equals(path)) {
				return sound;
			}
		}
		return null;
	}

	@Override
	public void onModelChanged() {
		this.stopAllSounds();
	}

	public Object getData(int id) {
		if (this.dataMap == null) {
			this.dataMap = new HashMap<Integer, Object>();
		}

		if (this.dataMap.containsKey(id)) {
			return this.dataMap.get(id);
		}
		return 0;
	}

	public void setData(int id, Object value) {
		if (this.dataMap == null) {
			this.dataMap = new HashMap<Integer, Object>();
		}
		this.dataMap.put(id, value);
	}

	public Entity getEntity() {
		return this.theVehicle;
	}
}