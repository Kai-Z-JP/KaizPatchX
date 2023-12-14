var renderClass = "jp.ngt.rtm.render.NPCPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    head = renderer.registerParts(new Parts("head"));
    body = renderer.registerParts(new Parts("body"));
    leftArm = renderer.registerParts(new Parts("armL"));
    rightArm = renderer.registerParts(new Parts("armR"));
    leftLeg = renderer.registerParts(new Parts("legL"));
    rightLeg = renderer.registerParts(new Parts("legR"));
}

function render(entity, pass, partialTick) {
    GL11.glPushMatrix();

    if (pass == 0) {
        renderer.setRotationAngles(entity, partialTick);
        //0.0, 1.5, 0.0
        renderer.rotateAndRender(head, 0.0, 1.5, 0.0, renderer.headAngleX, renderer.headAngleY, renderer.headAngleZ);
        //0.0, 1.5, 0.0
        renderer.rotateAndRender(body, 0.0, 1.5, 0.0, renderer.bodyAngleX, renderer.bodyAngleY, renderer.bodyAngleZ);
        //-0.25, 1.375, 0.0
        renderer.rotateAndRender(rightArm, -0.3125, 1.375, 0.0, renderer.rightArmAngleX, renderer.rightArmAngleY, renderer.rightArmAngleZ);
        //0.25, 1.375, 0.0
        renderer.rotateAndRender(leftArm, 0.3125, 1.375, 0.0, renderer.leftArmAngleX, renderer.leftArmAngleY, renderer.leftArmAngleZ);
        //-0.125, 0.75, 0.0
        renderer.rotateAndRender(rightLeg, -0.11875, 0.75, 0.0, renderer.rightLegAngleX, renderer.rightLegAngleY, renderer.rightLegAngleZ);
        //0.125, 0.75, 0.0
        renderer.rotateAndRender(leftLeg, 0.11875, 0.75, 0.0, renderer.leftLegAngleX, renderer.leftLegAngleY, renderer.leftLegAngleZ);
    }

    GL11.glPopMatrix();
}
