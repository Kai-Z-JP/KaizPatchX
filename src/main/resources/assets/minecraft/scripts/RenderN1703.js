var renderClass = "jp.ngt.rtm.render.NPCPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    head = renderer.registerParts(new Parts("head"));
    body = renderer.registerParts(new Parts("jointH", "body1", "body2", "body3", "jointB", "backpack"));
    armL1 = renderer.registerParts(new Parts("armL1", "jointAL1"));
    armL2 = renderer.registerParts(new Parts("armL2", "handL"));
    armR1 = renderer.registerParts(new Parts("armR1", "jointAR1"));
    armR2 = renderer.registerParts(new Parts("armR2", "handR"));
    legL1 = renderer.registerParts(new Parts("legL1", "jointLL1"));
    legL2 = renderer.registerParts(new Parts("legL2", "jointLL2", "legL3"));
    legR1 = renderer.registerParts(new Parts("legR1", "jointLR1"));
    legR2 = renderer.registerParts(new Parts("legR2", "jointLR2", "legR3"));
}

function render(entity, pass, partialTick) {
    GL11.glPushMatrix();

    if (pass == 0) {
        renderer.setRotationAngles(entity, partialTick);

        var scale = 1.5;
        GL11.glScalef(scale, scale, scale);

        renderer.rotateAndRender(head, 0.0, 1.525, 0.0, renderer.headAngleX, renderer.headAngleY, renderer.headAngleZ);

        renderer.rotateAndRender(body, 0.0, 1.525, 0.0, renderer.bodyAngleX, renderer.bodyAngleY, renderer.bodyAngleZ);

        renderArm(armR1, armR2, -0.275, 1.425, 0.0, -0.325, 1.15, 0.0, renderer.rightArmAngleX, renderer.rightArmAngleY, renderer.rightArmAngleZ);

        renderArm(armL1, armL2, 0.275, 1.425, 0.0, 0.325, 1.15, 0.0, renderer.leftArmAngleX, renderer.leftArmAngleY, renderer.leftArmAngleZ);

        renderLeg(legR1, legR2, -0.1, 0.825, 0.0, -0.1, 0.55, 0.0, renderer.rightLegAngleX, renderer.rightLegAngleY, renderer.rightLegAngleZ);

        renderLeg(legL1, legL2, 0.1, 0.825, 0.0, 0.1, 0.55, 0.0, renderer.leftLegAngleX, renderer.leftLegAngleY, renderer.leftLegAngleZ);
    }

    GL11.glPopMatrix();
}

function renderArm(parts1, parts2, x1, y1, z1, x2, y2, z2, rotationX, rotationY, rotationZ) {
    GL11.glPushMatrix();
    GL11.glTranslatef(x1, y1, z1);
    var rX2 = NGTMath.toDegrees(rotationX);
    GL11.glRotatef(NGTMath.toDegrees(rotationZ), 0.0, 0.0, 1.0);
    GL11.glRotatef(NGTMath.toDegrees(rotationY), 0.0, 1.0, 0.0);
    GL11.glRotatef(rX2, 1.0, 0.0, 0.0);
    GL11.glTranslatef(-x1, -y1, -z1);
    parts1.render(renderer);

    if (rX2 < 0.0) {
        GL11.glTranslatef(x2, y2, z2);
        GL11.glRotatef(rX2, 1.0, 0.0, 0.0);
        GL11.glTranslatef(-x2, -y2, -z2);
    }
    parts2.render(renderer);

    GL11.glPopMatrix();
}

function renderLeg(parts1, parts2, x1, y1, z1, x2, y2, z2, rotationX, rotationY, rotationZ) {
    GL11.glPushMatrix();
    GL11.glTranslatef(x1, y1, z1);
    var rX2 = NGTMath.toDegrees(rotationX);
    GL11.glRotatef(NGTMath.toDegrees(rotationZ), 0.0, 0.0, 1.0);
    GL11.glRotatef(NGTMath.toDegrees(rotationY), 0.0, 1.0, 0.0);
    GL11.glRotatef(rX2, 1.0, 0.0, 0.0);
    GL11.glTranslatef(-x1, -y1, -z1);
    parts1.render(renderer);

    if (rX2 < 0.0) {
        GL11.glTranslatef(x2, y2, z2);
        GL11.glRotatef(-rX2, 1.0, 0.0, 0.0);
        GL11.glTranslatef(-x2, -y2, -z2);
    }
    parts2.render(renderer);

    GL11.glPopMatrix();
}
