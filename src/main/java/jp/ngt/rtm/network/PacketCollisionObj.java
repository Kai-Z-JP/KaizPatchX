package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.entity.util.ColFace;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;

/**
 * 当たり判定用面情報をサーバーへ送信<br>
 * ※パーツごと送ると容量オーバー<br>
 */
public class PacketCollisionObj implements IMessage, IMessageHandler<PacketCollisionObj, IMessage> {
    public PacketCollisionObj() {
    }

    private String type;
    private String modelName;
    private String partsName;
    private ColFace face;
    /**
     * 1:面終了, 2:パーツ終了
     */
    private byte status;

    public PacketCollisionObj(String type, String model, String parts, ColFace face, byte status) {
        this.type = type;
        this.modelName = model;
        this.partsName = parts;
        this.face = face;
        this.status = status;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.type);
        ByteBufUtils.writeUTF8String(buffer, this.modelName);
        ByteBufUtils.writeUTF8String(buffer, this.partsName);
        buffer.writeByte(this.status);

		/*List<ColFace> list = this.parts.faces;
		buffer.writeInt(list.size());
		for(ColFace face : list)
		{
			buffer.writeInt(face.vertices.length);
			for(Vec3 vec : face.vertices)
			{
				buffer.writeFloat((float)vec.getX());
				buffer.writeFloat((float)vec.getY());
				buffer.writeFloat((float)vec.getZ());
			}
			buffer.writeFloat((float)face.normal.getX());
			buffer.writeFloat((float)face.normal.getY());
			buffer.writeFloat((float)face.normal.getZ());
		}*/

        buffer.writeInt(this.face.vertices.length);
        for (Vec3 vec : this.face.vertices) {
            buffer.writeFloat((float) vec.getX());
            buffer.writeFloat((float) vec.getY());
            buffer.writeFloat((float) vec.getZ());
        }
        buffer.writeFloat((float) this.face.normal.getX());
        buffer.writeFloat((float) this.face.normal.getY());
        buffer.writeFloat((float) this.face.normal.getZ());
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.type = ByteBufUtils.readUTF8String(buffer);
        this.modelName = ByteBufUtils.readUTF8String(buffer);
        this.partsName = ByteBufUtils.readUTF8String(buffer);
        this.status = buffer.readByte();

		/*this.parts = new ColParts(partsName);
		int faceCount = buffer.readInt();
		for(int i = 0; i < faceCount; ++i)
		{
			int vtxCount = buffer.readInt();
			ColFace face = new ColFace();
			face.vertices = new Vec3[vtxCount];
			for(int j = 0; j < vtxCount; ++j)
			{
				float x = buffer.readFloat();
				float y = buffer.readFloat();
				float z = buffer.readFloat();
				face.vertices[j] = new Vec3(x, y, z);
			}
			float x = buffer.readFloat();
			float y = buffer.readFloat();
			float z = buffer.readFloat();
			face.normal = new Vec3(x, y, z);
			face.init();
			this.parts.faces.add(face);
		}*/

        int vtxCount = buffer.readInt();
        this.face = new ColFace();
        this.face.vertices = new Vec3[vtxCount];
        for (int j = 0; j < vtxCount; ++j) {
            float x = buffer.readFloat();
            float y = buffer.readFloat();
            float z = buffer.readFloat();
            this.face.vertices[j] = new Vec3(x, y, z);
        }
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();
        this.face.normal = new Vec3(x, y, z);
        this.face.init();
    }

    @Override
    public IMessage onMessage(PacketCollisionObj message, MessageContext ctx) {
        ModelSetBase set = ModelPackManager.INSTANCE.getModelSet(message.type, message.modelName);
        if (set != null) {
            set.addColFace(message.partsName, message.face, message.status);
        }
        return null;
    }
}
