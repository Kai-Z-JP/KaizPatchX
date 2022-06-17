package jp.ngt.rtm.event;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.npc.macro.MacroRecorder;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.parts.EntityArtillery;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.entity.vehicle.EntityPlane;
import jp.ngt.rtm.entity.vehicle.EntityVehicle;
import jp.ngt.rtm.modelpack.modelset.ModelSetTrainClient;
import jp.ngt.rtm.network.PacketRTMKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class RTMKeyHandlerClient {
    private static final String CATG_RTM = "rtm.key";
    public static final RTMKeyHandlerClient INSTANCE = new RTMKeyHandlerClient();

    public static final KeyBinding KEY_HORN = new KeyBinding("rtm.horn", Keyboard.KEY_P, CATG_RTM);
    public static final KeyBinding KEY_CHIME = new KeyBinding("rtm.chime", Keyboard.KEY_I, CATG_RTM);
    public static final KeyBinding KEY_ATS = new KeyBinding("rtm.ats", Keyboard.KEY_COMMA, CATG_RTM);
    public static final KeyBinding KEY_EB = new KeyBinding("key.rtm.eb", Keyboard.KEY_5, CATG_RTM);
    public static final KeyBinding KEY_CHIME_NEXT = new KeyBinding("key.rtm.chime_next", Keyboard.KEY_RIGHT, CATG_RTM);
    public static final KeyBinding KEY_CHIME_PREV = new KeyBinding("key.rtm.chime_prev", Keyboard.KEY_LEFT, CATG_RTM);
    public static final KeyBinding KEY_REVERSER_FORWARD = new KeyBinding("key.rtm.reverser_forward", Keyboard.KEY_UP, CATG_RTM);
    public static final KeyBinding KEY_REVERSER_BACK = new KeyBinding("key.rtm.reverser_back", Keyboard.KEY_DOWN, CATG_RTM);

    private boolean sneaking;

    private int countPressingBackTicks = 0;
    private int countPressingForwardTicks = 0;

    private RTMKeyHandlerClient() {
    }

    public static void init() {
        ClientRegistry.registerKeyBinding(KEY_HORN);
        ClientRegistry.registerKeyBinding(KEY_CHIME);
        ClientRegistry.registerKeyBinding(KEY_ATS);
        ClientRegistry.registerKeyBinding(KEY_EB);
        ClientRegistry.registerKeyBinding(KEY_CHIME_NEXT);
        ClientRegistry.registerKeyBinding(KEY_CHIME_PREV);
        ClientRegistry.registerKeyBinding(KEY_REVERSER_FORWARD);
        ClientRegistry.registerKeyBinding(KEY_REVERSER_BACK);
    }

    public void onTickStart() {
		/*Minecraft mc = NGTUtilClient.getMinecraft();
		Entity entity = mc.thePlayer.ridingEntity;
		if(entity != null && entity instanceof EntityVehicle)
		{
			if(mc.gameSettings.keyBindJump.getIsKeyPressed())
			{
				this.sendKeyToServer(RTMCore.KEY_JUMP, "");
			}
			else if(mc.gameSettings.keyBindSneak.getIsKeyPressed())
			{
				if(entity instanceof EntityPlane && !entity.onGround)
				{
					this.sendKeyToServer(RTMCore.KEY_SNEAK, "");
					this.unpressKey(mc.gameSettings.keyBindSneak);
				}
			}
		}*/

        Minecraft mc = NGTUtilClient.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            if (player.isRiding() && player.ridingEntity instanceof EntityVehicle) {
                this.sendKeyToServer(RTMCore.KEY_JUMP, "");
            }
        } else if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            if (player.isRiding() && player.ridingEntity instanceof EntityPlane) {
                if (!player.ridingEntity.onGround) {
                    this.sendKeyToServer(RTMCore.KEY_SNEAK, "");
                }
            }
        } else if (player.isRiding() && player.ridingEntity instanceof EntityTrainBase) {
            if (mc.gameSettings.keyBindBack.getIsKeyPressed()) {
                countPressingBackTicks++;
            } else {
                countPressingBackTicks = 0;
            }

            if (mc.gameSettings.keyBindForward.getIsKeyPressed()) {
                countPressingForwardTicks++;
            } else {
                countPressingForwardTicks = 0;
            }

            if (mc.gameSettings.keyBindBack.isPressed() || countPressingBackTicks > 10) {
                this.sendKeyToServer(RTMCore.KEY_Forward, "");
                MacroRecorder.INSTANCE.recNotch(player.worldObj, 1);
            } else if (mc.gameSettings.keyBindForward.isPressed() || countPressingForwardTicks > 10) {
                this.sendKeyToServer(RTMCore.KEY_Back, "");
                MacroRecorder.INSTANCE.recNotch(player.worldObj, -1);
            }
        }
    }

    public void onTickEnd() {
		/*Minecraft mc = NGTUtilClient.getMinecraft();
		if(this.sneaking)
		{
			KeyBinding.setKeyBindState(p_74510_0_, true);
			this.sneaking = false;
		}*/
    }

    @SubscribeEvent
    public void keyDown(InputEvent event) {
        Minecraft mc = NGTUtilClient.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        Entity riding = player.ridingEntity;
        if (mc.gameSettings.keyBindSneak.getIsKeyPressed()) {
            if (player.isRiding() && riding instanceof EntityPlane) {
                if (((EntityPlane) riding).disableUnmount()) {
                    //this.sendKeyToServer(RTMCore.KEY_SNEAK, "");
                    this.unpressKey(mc.gameSettings.keyBindSneak);
                }
            }
        } else if (KEY_HORN.isPressed()) {
            if (player.isRiding()) {
                if (riding instanceof EntityTrainBase) {
                    this.playSound(player, RTMCore.KEY_Horn);
                } else if (riding instanceof EntityArtillery) {
                    this.sendKeyToServer(RTMCore.KEY_Fire, "");
                }
            }
        } else if (KEY_CHIME.isPressed()) {
            this.playSound(player, RTMCore.KEY_Chime);
        } else if (mc.gameSettings.keyBindInventory.getIsKeyPressed())//isPressedだとMinecraft1976が処理されない
        {
            if (player.isRiding() && riding instanceof EntityTrainBase) {
                mc.gameSettings.keyBindInventory.isPressed();
                this.sendKeyToServer(RTMCore.KEY_ControlPanel, "");
            }
        } else if (KEY_ATS.isPressed()) {
            this.sendKeyToServer(RTMCore.KEY_ATS, "");
        }

        if (player.isRiding() && (riding instanceof EntityTrainBase)) {
            EntityTrainBase train = (EntityTrainBase) riding;
            if (KEY_REVERSER_BACK.isPressed()) {
                byte data = train.getTrainStateData(TrainStateType.State_Direction.id);
                if (data < 2) {
                    player.playSound("rtm:train.lever", 1.0F, 1.0F);
                    train.syncTrainStateData(TrainStateType.State_Direction.id, ++data);
                    TrainState state = TrainState.getState(TrainStateType.State_Direction.id, data);
                    NGTLog.showChatMessage(new ChatComponentText("direction: " + state.stateName));
                }
            } else if (KEY_REVERSER_FORWARD.isPressed()) {
                byte data = train.getTrainStateData(TrainStateType.State_Direction.id);
                if (data > 0) {
                    player.playSound("rtm:train.lever", 1.0F, 1.0F);
                    train.syncTrainStateData(TrainStateType.State_Direction.id, --data);
                    TrainState state = TrainState.getState(TrainStateType.State_Direction.id, data);
                    NGTLog.showChatMessage(new ChatComponentText("direction: " + state.stateName));
                }
            } else if (KEY_EB.isPressed()) {
                train.syncTrainStateData(TrainStateType.State_Notch.id, (byte) -(train.getModelSet().getConfig().deccelerations.length - 1));
                this.playSound(player, RTMCore.KEY_Horn);
                NGTLog.showChatMessage(new ChatComponentText("Push EB"));
            } else if (KEY_CHIME_NEXT.isPressed()) {
                TrainStateType type = TrainStateType.State_Announcement;
                int i0 = train.getTrainStateData(type.id) + 1;
                i0 = i0 < type.min ? type.max : (i0 > type.max ? 0 : i0);
                train.syncTrainStateData(type.id, (byte) i0);
                NGTLog.showChatMessage(new ChatComponentText("Next chime"));
            } else if (KEY_CHIME_PREV.isPressed()) {
                TrainStateType type = TrainStateType.State_Announcement;
                int i0 = train.getTrainStateData(type.id) - 1;
                i0 = i0 < type.min ? type.max : (i0 > type.max ? 0 : i0);
                train.syncTrainStateData(type.id, (byte) i0);
                NGTLog.showChatMessage(new ChatComponentText("Prev chime"));
            }
        }
    }

    private void unpressKey(KeyBinding key) {
        NGTUtil.getMethod(KeyBinding.class, key, new String[]{"unpressKey", "func_74505_d"}, new Class[]{});
    }

    private void sendKeyToServer(byte keyCode, String sound) {
        EntityPlayer player = NGTUtilClient.getMinecraft().thePlayer;
        RTMCore.NETWORK_WRAPPER.sendToServer(new PacketRTMKey(player, keyCode, sound));
    }

    private void playSound(EntityPlayer player, byte key) {
        if (player.isRiding() && player.ridingEntity instanceof EntityTrainBase) {
            EntityTrainBase train = (EntityTrainBase) player.ridingEntity;
            ModelSetTrainClient modelset = (ModelSetTrainClient) train.getModelSet();
            if (modelset != null) {
                ResourceLocation sound = null;
                if (key == RTMCore.KEY_Horn) {
                    //ClientProxy.playSound(player, modelset.sound_Horn);
                    sound = modelset.sound_Horn;
                    MacroRecorder.INSTANCE.recHorn(player.worldObj);
                } else if (key == RTMCore.KEY_Chime) {
                    int index = train.getTrainStateData(9);
                    String[][] sa0 = modelset.getConfig().sound_Announcement;
                    if (sa0 != null && index < sa0.length) {
                        String[] sa1 = sa0[index][1].split(":");
                        //ClientProxy.playSound(player, new ResourceLocation(sa1[0], sa1[1]));
                        sound = new ResourceLocation(sa1[0], sa1[1]);
                        MacroRecorder.INSTANCE.recChime(player.worldObj, sa0[index][1]);
                    }
                }

                if (sound != null) {
                    this.sendKeyToServer(key, sound.getResourceDomain() + ":" + sound.getResourcePath());
                }
            }
        }
    }
}
