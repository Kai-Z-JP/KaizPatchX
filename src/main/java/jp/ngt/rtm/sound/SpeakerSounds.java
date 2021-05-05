package jp.ngt.rtm.sound;

import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTJson;
import jp.ngt.ngtlib.io.NGTText;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.network.PacketNotice;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class SpeakerSounds {
    private static final SpeakerSounds INSTANCE_CLIENT = new SpeakerSounds(false);

    private static final SpeakerSounds INSTANCE_SERVER = new SpeakerSounds(true);

    private static final String SAVE_FILE = "rtm/speaker_sounds.json";

    public static final int MAX_SOUND_ID = 64;

    private String[] sounds = new String[64];

    private final boolean sideServer;

    private SpeakerSounds(boolean par1) {
        this.sideServer = par1;
        if (NGTUtil.isServer()) {
            loadSoundList();
        }
    }

    public static SpeakerSounds getInstance(boolean server) {
        return server ? INSTANCE_SERVER : INSTANCE_CLIENT;
    }

    private void loadSoundList() {
        File file = new File(NGTFileLoader.getModsDir().get(0), SAVE_FILE);
        if (file.exists()) {
            try {
                this.sounds = (String[]) NGTJson.getObjectFromJson(NGTText.readText(file, false, ""), String[].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void syncSoundList() {
        IntStream.range(0, this.sounds.length).forEach(i -> setSound(i + 1, this.sounds[i], true));
    }

    private void saveSoundList() {
        File file = new File(NGTFileLoader.getModsDir().get(0), SAVE_FILE);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        NGTJson.writeToJson(NGTJson.getJsonFromObject(this.sounds), file);
    }

    public ResourceLocation getSound(int id) {
        String name = this.sounds[id - 1];
        return name == null ? null : (name.contains(":") ? new ResourceLocation(name.split(":")[0], name.split(":")[1]) : new ResourceLocation(name));
    }

    public void setSound(int id, String sound, boolean sync) {
        this.sounds[id - 1] = sound;
        String msg = String.format("speaker,%d,%s", id, sound);
        if (this.sideServer) {
            saveSoundList();
            RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice((byte) 1, msg));
        } else if (sync) {
            RTMCore.NETWORK_WRAPPER.sendToServer(new PacketNotice((byte) 0, msg));
        }
    }

    public void onGetPacket(String msg, boolean sync) {
        String[] sa = msg.split(",");
        int id = Integer.parseInt(sa[1]);
        setSound(id, sa[2], sync);
    }
}