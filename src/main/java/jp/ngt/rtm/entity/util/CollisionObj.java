package jp.ngt.rtm.entity.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.PooledVec3;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.renderer.NGTTessellator;
import jp.ngt.ngtlib.renderer.model.Face;
import jp.ngt.ngtlib.renderer.model.GroupObject;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.modelpack.cfg.ModelConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.network.PacketCollisionObj;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class CollisionObj {
    private static final float Y_LIMIT = NGTMath.sin(45.0F);//ある法線角度以上の面を当たり判定に使用
    private static final float PLAYER_R = 0.5F;

    private final List<ColParts> partsList = new ArrayList<>();
    private AxisAlignedBB box;

    public CollisionObj() {
        ;
    }

    @SideOnly(Side.CLIENT)
    public CollisionObj(IModelNGT model, ModelConfig cfg) {
        this.initFaceList(model, cfg.collisionParts);
        this.box = this.initAABB();
    }

    @SideOnly(Side.CLIENT)
    public void syncCollisionObj(String type, ModelSetBase modelSet, Thread thread) {
        for (int i = 0; i < this.partsList.size(); ++i) {
            ColParts parts = this.partsList.get(i);
            for (int j = 0; j < parts.faces.size(); ++j) {
                ColFace face = parts.faces.get(j);
                byte status = (byte) ((i == this.partsList.size() - 1) ? 2 : ((j == parts.faces.size() - 1) ? 1 : 0));
                RTMCore.NETWORK_WRAPPER.sendToServer(
                        new PacketCollisionObj(type, modelSet.getConfig().getName(), parts.name, face, status));
                try {
                    thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addColFace(String partsName, ColFace face, byte status) {
        ColParts target = null;
        for (ColParts parts : this.partsList) {
            if (parts.name.equals(partsName)) {
                target = parts;
            }
        }

        if (target == null) {
            target = new ColParts(partsName);
            this.partsList.add(target);
        }

        target.faces.add(face);

        if (status == 1) {
            this.box = this.initAABB();
        }
    }

    //モデルのoffset, scale未反映

    /**
     * 衝突判定に使用する面を抽出<br>
     * 条件:<br>
     * ・collisionParts[]に含まれる or "-all"指定<br>
     * ・normalが一定角度以上<br>
     */
    @SideOnly(Side.CLIENT)
    private void initFaceList(IModelNGT model, String[] names) {
        boolean addAll = false;
        List<String> nameList = new ArrayList<>();
        if (names != null) {
            NGTUtil.addArray(nameList, names);
            addAll = names[0].equals("-all");
        }

        for (GroupObject go : model.getGroupObjects()) {
            if (addAll || nameList.contains(go.name)) {
                ColParts parts = new ColParts(go.name);
                for (Face face : go.faces) {
                    if (face.faceNormal.getY() > -Y_LIMIT) {
                        ColFace cFace = new ColFace();
                        cFace.setData(face);
                        parts.faces.add(cFace);
                    }
                }
                this.partsList.add(parts);
            }
        }
    }

    private AxisAlignedBB initAABB() {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        for (ColParts parts : this.partsList) {
            for (ColFace face : parts.faces) {
                for (Vec3 vtx : face.vertices) {
                    minX = Math.min(vtx.getX(), minX);
                    maxX = Math.max(vtx.getX(), maxX);
                    minY = Math.min(vtx.getY(), minY);
                    maxY = Math.max(vtx.getY(), maxY);
                    minZ = Math.min(vtx.getZ(), minZ);
                    maxZ = Math.max(vtx.getZ(), maxZ);
                }
            }
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * 衝突判定Box取得
     *
     * @param target     適用対象
     * @param myself     当オブジェクトの持ち主
     * @param playerAABB 適用対象のAABB
     * @param list
     */
    public void applyCollison(Entity target, Entity myself, AxisAlignedBB playerAABB, List<AxisAlignedBB> boxList, List<String> exclusionParts) {
        double px = target.posX;
        double py = target.posY;
        double pz = target.posZ;

        double offsetY = (myself instanceof EntityVehicleBase) ? myself.getYOffset() : 0.0D;
        Vec3 vt = PooledVec3.create(px - myself.posX, py + PLAYER_R - myself.posY - offsetY, pz - myself.posZ);
        //ヒット結果(回転適用済)
        HitResult[] hitResults = this.getCollisionPoints(vt, myself, exclusionParts);
        this.hit = hitResults;
        boolean hitted = false;

        for (HitResult result : hitResults) {
            if (result == null) {
                continue;
            }

            AxisAlignedBB hitAABB = result.face.toBox(myself);
            if (hitAABB.intersectsWith(playerAABB)) {
                boxList.add(hitAABB);
                hitted = true;
            }
        }

        if (hitted && myself instanceof EntityVehicleBase) {
            //this.applyVehicleCollision(target, (EntityVehicleBase)myself);
        }
    }

    //うまく動かないので未使用
    private void applyVehicleCollision(Entity target, EntityVehicleBase vehicle) {
        Vec3 vec = PooledVec3.create(0.0D, 0.0D, vehicle.getSpeed());
        vec = vec.rotateAroundY(MathHelper.wrapAngleTo180_float(vehicle.rotationYaw));
        target.motionX += vec.getX();
        target.motionZ += vec.getZ();
    }

    /**
     * XYZ方向のヒットした点
     */
    public HitResult[] getCollisionPoints(Vec3 vt, Entity myself, @Nullable List<String> exclusionParts) {
        vt = vt.rotateAroundX(-myself.rotationPitch);
        vt = vt.rotateAroundY(-myself.rotationYaw);
        Vec3 pointX = null, pointY = null, pointZ = null;
        ColFace faceX = null, faceY = null, faceZ = null;
        for (int idx = 0; idx < this.partsList.size(); ++idx)//ConcurrentModificationException防止
        {
            ColParts parts = this.partsList.get(idx);

            if (exclusionParts != null && exclusionParts.contains(parts.name)) {
                continue;
            }

            for (int i = 0; i < parts.faces.size(); ++i)//ConcurrentModificationException対策
            {
                ColFace face = parts.faces.get(i);
                Vec3 hit = face.getCollisionVec(vt, PLAYER_R);
                if (!hit.equals(Vec3.ZERO)) {
                    boolean[] flags = this.checkActiveHitDir(hit, vt);
                    boolean hitY = face.normal.getY() > Y_LIMIT;

                    if (flags[0] && !hitY) {
                        if (pointX == null || (Math.abs(vt.getX() - hit.getX()) < Math.abs(vt.getX() - pointX.getX()))) {
                            pointX = hit;
                            faceX = face;
                            //face.addColor(0xFF0000);
                            face.color = 0xFF0000;
                        }
                    }

                    if (flags[1] && hitY) {
                        if (pointY == null || (Math.abs(vt.getY() - hit.getY()) < Math.abs(vt.getY() - pointY.getY()))) {
                            pointY = hit;
                            faceY = face;
                            //face.addColor(0x00FF00);
                            face.color = 0x00FF00;
                        }
                    }

                    if (flags[2] && !hitY) {
                        if (pointZ == null || (Math.abs(vt.getZ() - hit.getZ()) < Math.abs(vt.getZ() - pointZ.getZ()))) {
                            pointZ = hit;
                            faceZ = face;
                            //face.addColor(0x0000FF);
                            face.color = 0x0000FF;
                        }
                    }
                }
            }
        }

        HitResult[] result = new HitResult[3];
        result[0] = (pointX != null) ? this.applyRotation(pointX, faceX, myself) : null;
        result[1] = (pointY != null) ? this.applyRotation(pointY, faceY, myself) : null;
        result[2] = (pointZ != null) ? this.applyRotation(pointZ, faceZ, myself) : null;

        return result;
    }

    private HitResult applyRotation(Vec3 point, ColFace face, Entity myself) {
        point = point.rotateAroundY(myself.rotationYaw);
        point = point.rotateAroundX(myself.rotationPitch);

        face = face.rotateAroundY(myself.rotationYaw);
        face = face.rotateAroundX(myself.rotationPitch);

        return new HitResult(point, face);
    }

    /**
     * @return {x, y, z} 各成分方向の判定が有効か
     */
    private boolean[] checkActiveHitDir(Vec3 hit, Vec3 target) {
        double difX = target.getX() - hit.getX();
        double difY = target.getY() - hit.getY();
        double difZ = target.getZ() - hit.getZ();
        Vec3 difN = (PooledVec3.create(difX, difY, difZ)).normalize();
        double limit = 0.5D;
        return new boolean[]{Math.abs(difN.getX()) > limit, Math.abs(difN.getY()) > limit, Math.abs(difN.getZ()) > limit};
    }

    public AxisAlignedBB getSizeBox() {
        return this.box;
    }

    public class HitResult {
        public final Vec3 point;
        public final ColFace face;

        public HitResult(Vec3 par1, ColFace par2) {
            this.point = par1;
            this.face = par2;
        }
    }

    //render////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final List<AxisAlignedBB> aabbList = new ArrayList<>();
    private HitResult[] hit;
    private boolean rendering;

    //回転後に描画
    @SideOnly(Side.CLIENT)
    public void checkAndRenderCollision(EntityPlayer player, Entity myself, List<String> exclusionParts) {
        this.aabbList.clear();

        AxisAlignedBB box = player.boundingBox;
        this.rendering = true;
        this.applyCollison(player, myself, box, this.aabbList, exclusionParts);
        this.rendering = false;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        float prevPointSize = GL11.glGetFloat(GL11.GL_POINT_SIZE);
        float prevLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        GL11.glPointSize(10.0F);
        GL11.glLineWidth(5.0F);

        GL11.glPushMatrix();
        this.renderFrame(myself, exclusionParts);
        //AABBは回転適用済みのため、もとに戻して描画
        float pitch = myself.prevRotationPitch;
        GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
        float yaw = myself.prevRotationYaw;
        GL11.glRotatef(-yaw, 0.0F, 1.0F, 0.0F);
        this.renderHits();
        this.renderCollisionBox(myself);
        GL11.glPopMatrix();

        GL11.glPointSize(prevPointSize);
        GL11.glLineWidth(prevLineWidth);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @SideOnly(Side.CLIENT)
    private void renderFrame(Entity myself, List<String> exclusionParts) {
        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        for (ColParts parts : this.partsList) {
            if (!exclusionParts.contains(parts.name)) {
                for (ColFace face : parts.faces) {
                    face.renderFrame(tessellator, true);
                    face.renderNormal(tessellator);
                }
            }
        }
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    private void renderHits() {
        if (this.hit == null) {
            return;
        }

        NGTTessellator tessellator = NGTTessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        int color = 0xFF0000;
        float size = PLAYER_R;
        for (HitResult result : this.hit) {
            if (result != null) {
                float x = (float) result.point.getX();
                float y = (float) result.point.getY();
                float z = (float) result.point.getZ();
                tessellator.setColorRGBA_I(color, 0xFF);
                tessellator.addVertex(x - size, y, z);
                tessellator.addVertex(x + size, y, z);
                tessellator.addVertex(x, y - size, z);
                tessellator.addVertex(x, y + size, z);
                tessellator.addVertex(x, y, z - size);
                tessellator.addVertex(x, y, z + size);

                result.face.color = color;
                result.face.renderFrame(tessellator, true);
            }
            color = (color >> 8) & 0xFFFFFF;
        }
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    private void renderCollisionBox(Entity myself) {
        double py = myself.posY;
    	/*if(myself instanceof EntityVehicleBase)
    	{
    		py -= ((EntityVehicleBase)myself).getVehicleYOffset();
    	}*/
        int color = 0xA0A0A0;//0xFF0000;
        //color = (color >> 8) & 0xFFFFFF;
        this.aabbList.forEach(aabb -> NGTRenderer.renderFrame(
                (float) (aabb.minX - myself.posX), (float) (aabb.minY - py), (float) (aabb.minZ - myself.posZ),
                (float) (aabb.maxX - aabb.minX), (float) (aabb.maxY - aabb.minY), (float) (aabb.maxZ - aabb.minZ),
                color, 0xFF));
    }
}
