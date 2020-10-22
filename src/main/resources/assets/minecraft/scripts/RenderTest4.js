var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    body = renderer.registerParts(new Parts("body1", "body2", "body3", "box1"));
    propeller = renderer.registerParts(new Parts("propeller1", "propeller2", "propeller3", "propeller4"));
    wheel1 = renderer.registerParts(new Parts("wheel1"));
    wheel2 = renderer.registerParts(new Parts("wheel2"));
    wheel3 = renderer.registerParts(new Parts("wheel3"));
    rod1R = renderer.registerParts(new Parts("rod1R"));
    rod2R = renderer.registerParts(new Parts("rod2R"));
    rod3R = renderer.registerParts(new Parts("rod3R"));
    rod1L = renderer.registerParts(new Parts("rod1L"));
    rod2L = renderer.registerParts(new Parts("rod2L"));
    rod3L = renderer.registerParts(new Parts("rod3L"));
}

function render(entity, pass, par3) {
    if (pass != 0) {
        return;
    }

    GL11.glPushMatrix();

    body.render(renderer);

    var f0 = renderer.getWheelRotationR(entity);
    var r = 0.6;
    var c = 3.6;//ロッド長

    var radL = NGTMath.toRadians(f0);
    var hL = NGTMath.getSin(radL) * r;
    var xL = Math.sqrt(c * c - hL * hL);
    var roRodL = NGTMath.getSin(-radL / 2.0) - NGTMath.toDegrees(Math.atan2(hL, xL));
    var strokeL = 2.0 * r - (c + r - (xL - NGTMath.getCos(radL) * r));

    var f0R = f0 + 90.0;
    var radR = NGTMath.toRadians(f0R);
    var hR = NGTMath.getSin(radR) * r;
    var xR = Math.sqrt(c * c - hR * hR);
    var roRodR = NGTMath.getSin(-radR / 2.0) - NGTMath.toDegrees(Math.atan2(hR, xR));
    var strokeR = 2.0 * r - (c + r - (xR - NGTMath.getCos(radR) * r));

    GL11.glPushMatrix();
    renderer.rotate(f0 * 5.0, 'Z', 0.0, 1.9, 6.4);
    propeller.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    renderer.rotate(f0, 'X', 0.0, -0.2, 1.8);
    wheel1.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    renderer.rotate(f0, 'X', 0.0, -0.2, -1.8);
    wheel3.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    renderer.rotate(f0, 'X', 0.0, -0.2, 0.0);
    wheel2.render(renderer);
    renderer.rotate(-f0, 'X', 0.0, -0.2, -0.6);
    rod1L.render(renderer);
    renderer.rotate(-roRodL, 'X', 0.0, -0.2, -0.6);
    rod2L.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, strokeL);
    rod3L.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    renderer.rotate(f0R, 'X', 0.0, -0.2, 0.0);
    renderer.rotate(-f0R, 'X', 0.0, -0.2, -0.6);
    rod1R.render(renderer);
    renderer.rotate(-roRodR, 'X', 0.0, -0.2, -0.6);
    rod2R.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, strokeR);
    rod3R.render(renderer);
    GL11.glPopMatrix();

    GL11.glPopMatrix();
}
