var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    body = renderer.registerParts(new Parts("body"));
    wheel = renderer.registerParts(new Parts("wheel"));
    ball = renderer.registerParts(new Parts("ball"));
    rod = renderer.registerParts(new Parts("rod"));
}

function render(entity, pass, par3) {
    if (pass != 0) {
        return;
    }

    GL11.glPushMatrix();
    var sc = 0.5;
    GL11.glScalef(sc, sc, sc);
    var f0 = -renderer.getWheelRotationR(entity);
    var r = 2.0;
    var c = 5.0;

    GL11.glTranslatef(0.0, 3.5, 0.0);

    //wheel
    GL11.glPushMatrix();
    GL11.glRotatef(90.0, 0.0, 0.0, 1.0);
    GL11.glRotatef(f0 + 90.0, 0.0, 1.0, 0.0);
    GL11.glTranslatef(0.0, -3.5, 0.0);
    wheel.render(renderer);
    GL11.glPopMatrix();

    GL11.glTranslatef(0.0, -0.5, 0.0);
    body.render(renderer);
    GL11.glTranslatef(0.0, 8.5, 0.0);

    var radL = NGTMath.toRadians(f0 + 180.0);
    var yL = r * NGTMath.getSin(radL);
    var xL = 2.0 * c - r + r * NGTMath.getCos(radL);
    var rad1L = -Math.atan2(yL, xL);
    var c1L = Math.sqrt(yL * yL + xL * xL);
    var rad2L = -Math.acos(c1L * 0.5 / c);
    var rad3L = -2.0 * rad2L;

    //legL
    GL11.glPushMatrix();
    GL11.glTranslatef(-3.0, 0.0, 0.0);
    GL11.glRotatef(NGTMath.toDegrees(rad1L + rad2L), 1.0, 0.0, 0.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glTranslatef(0.0, 0.5, 0.0);
    GL11.glRotatef(NGTMath.toDegrees(rad3L), 1.0, 0.0, 0.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glPopMatrix();

    var radR = NGTMath.toRadians(f0);
    var yR = r * NGTMath.getSin(radR);
    var xR = 2.0 * c - r + r * NGTMath.getCos(radR);
    var rad1R = -Math.atan2(yR, xR);
    var c1R = Math.sqrt(yR * yR + xR * xR);
    var rad2R = -Math.acos(c1R * 0.5 / c);
    var rad3R = -2.0 * rad2R;

    //legR
    GL11.glPushMatrix();
    GL11.glTranslatef(3.0, 0.0, 0.0);
    GL11.glRotatef(NGTMath.toDegrees(rad1R + rad2R), 1.0, 0.0, 0.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glTranslatef(0.0, 0.5, 0.0);
    GL11.glRotatef(NGTMath.toDegrees(rad3R), 1.0, 0.0, 0.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glPopMatrix();

    GL11.glTranslatef(0.0, 11.0, 0.0);

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, -3.5, 5.0);
    var tick = renderer.getTick(entity);
    renderBall(tick % 60);
    renderBall((tick + 20) % 60);
    renderBall((tick + 40) % 60);
    GL11.glPopMatrix();

    //armL
    GL11.glPushMatrix();
    GL11.glTranslatef(-2.0, 0.0, 0.0);
    GL11.glRotatef(-45.0, 0.0, 0.0, 1.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glTranslatef(0.0, 0.5, 0.0);
    GL11.glRotatef(-90.0, 1.0, 0.0, 0.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glPopMatrix();

    //armR
    GL11.glPushMatrix();
    GL11.glTranslatef(2.0, 0.0, 0.0);
    GL11.glRotatef(45.0, 0.0, 0.0, 1.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glTranslatef(0.0, 0.5, 0.0);
    GL11.glRotatef(-90.0, 1.0, 0.0, 0.0);
    GL11.glTranslatef(0.0, -5.5, 0.0);
    rod.render(renderer);
    GL11.glPopMatrix();

    GL11.glPopMatrix();
}

function renderBall(tick) {
    GL11.glPushMatrix();
    if (tick < 40) {
        var t2 = (tick / 20.0) - 1.0;//-1~1
        var x = t2 * (3.5 + 2.0);
        var f0 = 20.0;
        var y = (-f0 * (t2 * t2)) + f0;
        GL11.glTranslatef(x, y, 0.0);
    } else {
        var move = -(((tick - 40) / 20.0) - 0.5) * (3.5 + 2.0) * 2.0;
        GL11.glTranslatef(move, 0.0, 0.0);
    }
    ball.render(renderer);
    GL11.glPopMatrix();
}
