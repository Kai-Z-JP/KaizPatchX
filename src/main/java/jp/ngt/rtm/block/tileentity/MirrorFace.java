package jp.ngt.rtm.block.tileentity;

public enum MirrorFace {
    //右下から反時計回り
    //uava,uavi,uivi,uiva
    YNEG(MirrorVertex.PNN, MirrorVertex.PNP, MirrorVertex.NNP, MirrorVertex.NNN),
    YPOS(MirrorVertex.PPP, MirrorVertex.PPN, MirrorVertex.NPN, MirrorVertex.NPP),
    ZNEG(MirrorVertex.NNN, MirrorVertex.NPN, MirrorVertex.PPN, MirrorVertex.PNN),
    ZPOS(MirrorVertex.PNP, MirrorVertex.PPP, MirrorVertex.NPP, MirrorVertex.NNP),
    XNEG(MirrorVertex.NNP, MirrorVertex.NPP, MirrorVertex.NPN, MirrorVertex.NNN),
    XPOS(MirrorVertex.PNN, MirrorVertex.PPN, MirrorVertex.PPP, MirrorVertex.PNP);

    public final MirrorVertex[] vertices;

    MirrorFace(MirrorVertex... par1) {
        this.vertices = par1;
    }

    public static MirrorFace get(int par1) {
        return MirrorFace.values()[par1];
    }

    public enum MirrorVertex {
        NNN(-0.5F, -0.5F, -0.5F),
        NNP(-0.5F, -0.5F, 0.5F),
        NPN(-0.5F, 0.5F, -0.5F),
        PNN(0.5F, -0.5F, -0.5F),
        NPP(-0.5F, 0.5F, 0.5F),
        PNP(0.5F, -0.5F, 0.5F),
        PPN(0.5F, 0.5F, -0.5F),
        PPP(0.5F, 0.5F, 0.5F);

        public final float x;
        public final float y;
        public final float z;

        MirrorVertex(float par1, float par2, float par3) {
            this.x = par1;
            this.y = par2;
            this.z = par3;
        }
    }
}