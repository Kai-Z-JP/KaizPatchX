package jp.ngt.rtm;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTFileLoadException;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTJson;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.ngtlib.util.PackInfo;
import jp.ngt.ngtlib.util.VersionChecker;
import jp.ngt.rtm.block.RenderBlockLiquid;
import jp.ngt.rtm.block.RenderVariableBlock;
import jp.ngt.rtm.block.tileentity.*;
import jp.ngt.rtm.electric.*;
import jp.ngt.rtm.entity.*;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.entity.npc.RenderNPC;
import jp.ngt.rtm.entity.train.*;
import jp.ngt.rtm.entity.train.parts.*;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.entity.vehicle.EntityVehicle;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.entity.vehicle.IUpdateVehicle;
import jp.ngt.rtm.entity.vehicle.RenderVehicleBase;
import jp.ngt.rtm.event.RTMEventHandlerClient;
import jp.ngt.rtm.event.RTMKeyHandlerClient;
import jp.ngt.rtm.event.RTMTickHandlerClient;
import jp.ngt.rtm.gui.camera.Camera;
import jp.ngt.rtm.item.RenderItemWithModel;
import jp.ngt.rtm.modelpack.ModelPackLoadThread;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.*;
import jp.ngt.rtm.modelpack.modelset.*;
import jp.ngt.rtm.rail.*;
import jp.ngt.rtm.sound.MovingSoundEntity;
import jp.ngt.rtm.sound.MovingSoundTileEntity;
import jp.ngt.rtm.sound.SoundUpdaterTrain;
import jp.ngt.rtm.sound.SoundUpdaterVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    public static final byte ViewMode_Artillery = 0;
    public static final byte ViewMode_SR = 1;
    public static final byte ViewMode_AMR = 2;
    public static final byte ViewMode_NVD = 3;
    public static final byte ViewMode_Camera = 4;

    private final ModelBase missing = new ModelMissing();
    private static final ResourceLocation texture = new ResourceLocation("rtm", "textures/missing.png");
    private byte connectionState = 0;

    private final FormationManager fmClient = new FormationManager(true);

    private final List<TileEntityLargeRailCore> unloadedRails = new ArrayList<>();

    @Override
    public void preInit() {
        this.versionCheck();

        RenderingRegistry.registerEntityRenderingHandler(EntityTrain.class, RenderVehicleBase.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityFreightCar.class, RenderVehicleBase.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityTanker.class, RenderVehicleBase.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityBogie.class, new RenderBogie());
        RenderingRegistry.registerEntityRenderingHandler(EntityFloor.class, new RenderSeat());
        RenderingRegistry.registerEntityRenderingHandler(EntityATC.class, RenderEntityInstalledObject.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityTrainDetector.class, RenderEntityInstalledObject.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityBumpingPost.class, RenderEntityInstalledObject.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityContainer.class, new RenderContainer());
        RenderingRegistry.registerEntityRenderingHandler(EntityArtillery.class, new RenderArtillery());
        RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, RenderBullet.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityTie.class, new RenderTie());
        RenderingRegistry.registerEntityRenderingHandler(EntityMMBoundingBox.class, new RenderMMBB());
        RenderingRegistry.registerEntityRenderingHandler(EntityVehicle.class, RenderVehicleBase.INSTANCE);
        RenderingRegistry.registerEntityRenderingHandler(EntityNPC.class, new RenderNPC());

//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluorescent.class, new RenderFluorescent());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLargeRailNormalCore.class, RenderLargeRail.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLargeRailSwitchCore.class, RenderLargeRail.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLargeRailSlopeCore.class, RenderLargeRail.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurnTableCore.class, new RenderTurntable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInsulator.class, RenderElectricalWiring.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnector.class, RenderElectricalWiring.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySignal.class, new RenderSignal());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRailroadSign.class, new RenderRailroadSign());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySignBoard.class, new RenderSignBoard());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEffect.class, new RenderEffect());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMarker.class, new RenderMarkerBlock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStation.class, new RenderStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMovingMachine.class, new RenderMovingMachine());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurnstile.class, RenderMachine.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPoint.class, RenderMachine.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrossingGate.class, RenderMachine.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTicketVendor.class, RenderMachine.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLight.class, RenderMachine.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpeaker.class, RenderMachine.INSTANCE);

//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipe.class, new RenderPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConverterCore.class, new RenderConverter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPaint.class, new RenderPaint());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFlag.class, new RenderFlag());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluorescent.class, new RenderOrnament<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipe.class, new RenderOrnament<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScaffoldStairs.class, new RenderOrnament<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScaffold.class, new RenderOrnament<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPole.class, new RenderOrnament<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlantOrnament.class, new RenderOrnament<>());

        RenderingRegistry.registerBlockHandler(new RenderVariableBlock());
        RenderingRegistry.registerBlockHandler(new RenderSignalBaseBlock());
        RenderingRegistry.registerBlockHandler(new RenderBlockLiquid());
        RenderingRegistry.registerBlockHandler(new RenderBlockLargeRail());

        MinecraftForgeClient.registerItemRenderer(RTMItem.itemtrain, RenderItemWithModel.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(RTMItem.itemCargo, RenderItemWithModel.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(RTMItem.installedObject, RenderItemWithModel.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(RTMItem.itemLargeRail, RenderItemWithModel.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(RTMItem.itemSignal, RenderItemWithModel.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(RTMItem.itemVehicle, RenderItemWithModel.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(RTMItem.itemWire, RenderItemWithModel.INSTANCE);

        MinecraftForge.EVENT_BUS.register(new RTMEventHandlerClient(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new RTMParticles());

        RTMKeyHandlerClient.init();

        ModelPackManager.INSTANCE.registerType("ModelFirearm", FirearmConfig.class, ModelSetFirearmClient.class);
        ModelPackManager.INSTANCE.registerType("ModelRail", RailConfig.class, ModelSetRailClient.class);
        ModelPackManager.INSTANCE.registerType("ModelSignal", SignalConfig.class, ModelSetSignalClient.class);
        ModelPackManager.INSTANCE.registerType("ModelTrain", TrainConfig.class, ModelSetTrainClient.class);
        ModelPackManager.INSTANCE.registerType("ModelContainer", ContainerConfig.class, ModelSetContainerClient.class);
        ModelPackManager.INSTANCE.registerType("ModelVehicle", VehicleConfig.class, ModelSetVehicleClient.class);
        ModelPackManager.INSTANCE.registerType("ModelNPC", NPCConfig.class, ModelSetNPC.class);
        ModelPackManager.INSTANCE.registerType("ModelMachine", MachineConfig.class, ModelSetMachineClient.class);
        ModelPackManager.INSTANCE.registerType("ModelWire", WireConfig.class, ModelSetWireClient.class);
        ModelPackManager.INSTANCE.registerType("ModelConnector", ConnectorConfig.class, ModelSetConnectorClient.class);
        ModelPackManager.INSTANCE.registerType("ModelOrnament", OrnamentConfig.class, ModelSetOrnamentClient.class);

        ModelPackLoadThread thread = new ModelPackLoadThread(Side.CLIENT);
        thread.start();
        ImageIO.scanForPlugins();
    }

    private void versionCheck() {
        if (!RTMConfig.versionCheck) {
            return;
        }

        List<File> fileList = NGTFileLoader.findFile(file -> file.getName().equals("pack.json"));
        try {
            fileList.stream()
                    .map(NGTJson::readFromJson)
                    .map(json -> (PackInfo) NGTJson.getObjectFromJson(json, PackInfo.class))
                    .filter(Objects::nonNull)
                    .forEach(VersionChecker::addToCheckList);
        } catch (NGTFileLoadException e) {
            e.printStackTrace();
        }

        VersionChecker.addToCheckList(new PackInfo(RTMCore.metadata.name, RTMCore.metadata.url, RTMCore.metadata.updateUrl, RTMCore.metadata.version));
    }

    @Override
    public void init() {
        //preInitではMC.renderEngineが初期化されてない
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMirror.class, RenderMirror.INSTANCE);

        FMLCommonHandler.instance().bus().register(RTMKeyHandlerClient.INSTANCE);
        FMLCommonHandler.instance().bus().register(new RTMTickHandlerClient());
    }

    @Override
    public IUpdateVehicle getSoundUpdater(EntityVehicleBase vehicle) {
        if (vehicle instanceof EntityTrainBase) {
            return new SoundUpdaterTrain(NGTUtilClient.getMinecraft().getSoundHandler(), (EntityTrainBase) vehicle);
        } else {
            return new SoundUpdaterVehicle(NGTUtilClient.getMinecraft().getSoundHandler(), vehicle);
        }
    }

    @Override
    public byte getConnectionState() {
        return this.connectionState;
    }

    @Override
    public void setConnectionState(byte par1) {
        this.connectionState = par1;
        NGTLog.debug("[RTM](Client) Set connection state : " + par1);
    }

    @Override
    public void spawnModParticle(World world, double x, double y, double z, double mX, double mY, double mZ) {
        EntityMeltedMetalFX entityFX = new EntityMeltedMetalFX(world, x, y, z, mX, mY, mZ);
        entityFX.setParticleIcon(RTMParticles.getInstance().getIIcon(0));
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(entityFX);
    }

    @Override
    public void renderMissingModel() {
        NGTUtilClient.getMinecraft().renderEngine.bindTexture(texture);
        this.missing.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
    }

    public static class ModelMissing extends ModelBase {
        ModelRenderer shape1;

        public ModelMissing() {
            this.textureWidth = 64;
            this.textureHeight = 32;

            this.shape1 = new ModelRenderer(this, 0, 0);
            this.shape1.addBox(-8F, -8F, -8F, 16, 16, 16);
            this.shape1.setRotationPoint(0F, 0F, 0F);
            this.shape1.setTextureSize(64, 32);
            this.shape1.mirror = true;
            this.setRotation(this.shape1, 0F, 0F, 0F);
        }

        @Override
        public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
            super.render(null, f, f1, f2, f3, f4, f5);
            this.setRotationAngles(f, f1, f2, f3, f4, f5);
            this.shape1.render(f5);
        }

        private void setRotation(ModelRenderer model, float x, float y, float z) {
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }

        public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5) {
            super.setRotationAngles(f, f1, f2, f3, f4, f5, null);
        }
    }

    @Override
    public float getFov(EntityPlayer player, float fov) {
        switch (getViewMode()) {
            case ViewMode_Artillery:
            case ViewMode_AMR:
                return 0.1F;
            case ViewMode_SR:
                return 0.25F;
            case ViewMode_Camera:
                return Camera.INSTANCE.getFov();
            default:
                return fov;
        }
    }

    /**
     * 0:火砲, 1:狙撃銃, 2:AMR, 3:NVD
     */
    public static byte getViewMode() {
        EntityPlayer player = NGTUtilClient.getMinecraft().thePlayer;
        if (NGTUtilClient.getMinecraft().gameSettings.thirdPersonView == 0) {
            ItemStack helmet = NGTUtilClient.getMinecraft().thePlayer.inventory.armorItemInSlot(3);
            if (helmet != null && helmet.getItem() == RTMItem.nvd) {
                return ViewMode_NVD;
            }

            if (player.isRiding() && player.ridingEntity instanceof EntityArtillery) {
                ModelSetFirearm set = ((EntityArtillery) player.ridingEntity).getModelSet();
                if (set.getConfig().fpvMode) {
                    return ViewMode_Artillery;
                }
            }

            if (player.getCurrentEquippedItem() != null) {
                if (player.getCurrentEquippedItem().getItem() == RTMItem.sniper_rifle) {
                    return ViewMode_SR;
                } else if (player.getCurrentEquippedItem().getItem() == RTMItem.amr) {
                    return ViewMode_AMR;
                } else if (player.getCurrentEquippedItem().getItem() == RTMItem.camera) {
                    return ViewMode_Camera;
                }
            }
        }
        return -1;
    }

    @Override
    public void playSound(Entity entity, ResourceLocation sound, float vol, float pitch) {
        if (sound != null) {
            if (NGTUtil.isServer()) {
                super.playSound(entity, sound, vol, pitch);
            } else {
                MovingSoundEntity ms = new MovingSoundEntity(entity, sound, false);
                ms.setVolume(vol);
                ms.setPitch(pitch);
                ms.update();
                NGTUtilClient.playSound(ms);
            }
        }
    }

    @Override
    public void playSound(TileEntity entity, ResourceLocation sound, float vol, float pitch) {
        if (sound != null) {
            if (NGTUtil.isServer()) {
                super.playSound(entity, sound, vol, pitch);
            } else {
                MovingSoundTileEntity ms = new MovingSoundTileEntity(entity, sound, false);
                ms.setVolume(vol);
                ms.setPitch(pitch);
                ms.update();
                NGTUtilClient.playSound(ms);
            }
        }
    }

    @Override
    public FormationManager getFormationManager() {
        return NGTUtil.isServer() ? super.getFormationManager() : this.fmClient;
    }

    @Override
    public void reportCrash(CrashReport report) {
        this.crashReportHolder.set(report);
        NGTUtilClient.getMinecraft().addGraphicsAndWorldToCrashReport(report);
        NGTUtilClient.getMinecraft().displayCrashReport(report);
        this.postReportCrash();
    }
}