var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    ball = renderer.registerParts(new Parts("chino_ball"));
}

function render(entity, pass, par3) {
    if (pass != 0) {
        return;
    }

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 16.0, 0.0);
    var tick = renderer.getTick(entity);
    var t0 = tick * 0.125;
    var ri = NGTMath.getSin(t0) * 8.0 + 8.0;

    for (var i = 0; i < 4; ++i) {
        GL11.glPushMatrix();
        var t1 = t0 + i * 10.0;
        var rz = NGTMath.getSin(t1) * 45.0;
        var rx = NGTMath.getCos(t1) * 45.0;
        GL11.glRotatef(rz, 0.0, 0.0, 1.0);
        GL11.glRotatef(rx, 1.0, 0.0, 0.0);
        for (var j = 0; j < 4; ++j) {
            GL11.glPushMatrix();
            var f1 = t0 * (i + 1) + (j * 90);
            GL11.glRotatef(f1, 0.0, 1.0, 0.0);
            GL11.glTranslatef(0.0, 0.0, ri);
            GL11.glRotatef(-f1, 0.0, 1.0, 0.0);
            GL11.glRotatef(-rx, 1.0, 0.0, 0.0);
            GL11.glRotatef(-rz, 0.0, 0.0, 1.0);
            ball.render(renderer);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    GL11.glPopMatrix();
}
