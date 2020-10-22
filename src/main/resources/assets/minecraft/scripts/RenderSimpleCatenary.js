var renderClass = "jp.ngt.rtm.render.WirePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.ngtlib.io);

function init(par1, par2) {
    ;
}

function renderWireStatic(tileEntity, connection, vec, par8) {
    ;
}

function renderWireDynamic(tileEntity, connection, vec, par8) {
    var x = vec.getX();
    var y = vec.getY();
    var z = vec.getZ();

    GL11.glPushMatrix;

    var tessellator = NGTTessellator.instance;
    tessellator.startDrawingQuads();
    var split = 8;
    for (var i = 0; i < split; ++i) {
        var ft = i / split;
        var ft2 = (i - 4) / split;
        var fh = (ft2 * ft2 - 0.25) * 1.2;
        var ftNext = (i + 1) / split;
        var ftn2 = ((i + 1) - 4) / split;
        var fhn = (ftn2 * ftn2 - 0.25) * 1.2;

        //接続元から接続先までの線分を分割し、たわみを考慮し描画

        tessellator.addVertexWithUV(x * ftNext, y * ftNext - 0.8125, z * ftNext, 1.0, 1.0);
        tessellator.addVertexWithUV(x * ftNext, y * ftNext + fhn, z * ftNext, 1.0, 0.0);
        tessellator.addVertexWithUV(x * ft, y * ft + fh, z * ft, 0.0, 0.0);
        tessellator.addVertexWithUV(x * ft, y * ft - 0.8125, z * ft, 0.0, 1.0);
    }
    tessellator.draw();


    /////////////////////////

    /* lengthSq = x * x + z * z;
    var split = lengthSq * 0.5 * 0.5;//=1/2
    for(var i = 0; i < split; ++i)
    {
    	tessellator.addVertex(x * ft, y * ft + fh + 0.025, z * ft);
        tessellator.addVertex(x * ft, y * ft + fh - 0.025, z * ft);
        tessellator.addVertex(x * ft, y * ft + fh + 0.025, z * ft);
        tessellator.addVertex(x * ft, y * ft + fh - 0.025, z * ft);
    }*/

    GL11.glPopMatrix;
}
