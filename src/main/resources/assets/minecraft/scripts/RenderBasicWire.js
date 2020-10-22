var renderClass = "jp.ngt.rtm.render.WirePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.renderer);

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

    GL11.glPushMatrix();
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    var length = Math.sqrt(x * x + z * z);
    var x1 = x / length;
    var z1 = z / length;
    var split = 16.0;

    var tessellator = NGTTessellator.instance;
    //X-Z
    tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
    tessellator.setColorOpaque_I(0);
    for (var j = 0; j <= split; ++j) {
        var ft = j / split;
        var f2 = (j - 8) / split;
        var fh = (f2 * f2 - 0.25) * 1.5;
        tessellator.addVertex((x * ft - 0.025 * z1), y * ft + fh, (z * ft + 0.025 * x1));
        tessellator.addVertex((x * ft + 0.025 * z1), y * ft + fh, (z * ft - 0.025 * x1));
    }
    tessellator.draw();

    //Y
    tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
    tessellator.setColorOpaque_I(0);
    for (var j = 0; j <= split; ++j) {
        var ft = j / split;
        var f2 = (j - 8) / split;
        var fh = (f2 * f2 - 0.25) * 1.5;
        tessellator.addVertex(x * ft, y * ft + fh + 0.025, z * ft);
        tessellator.addVertex(x * ft, y * ft + fh - 0.025, z * ft);
    }
    tessellator.draw();

    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glPopMatrix();
}
