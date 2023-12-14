var renderClass = "jp.ngt.rtm.render.NPCPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    head = renderer.registerParts(new Parts("head"));
    body = renderer.registerParts(new Parts("body"));
    arm_lu = renderer.registerParts(new Parts("arm_lu"));
    arm_ld = renderer.registerParts(new Parts("arm_ld"));
    arm_ru = renderer.registerParts(new Parts("arm_ru"));
    arm_rd = renderer.registerParts(new Parts("arm_rd"));
    leg_lu = renderer.registerParts(new Parts("leg_lu"));
    leg_ld = renderer.registerParts(new Parts("leg_ld"));
    leg_ru = renderer.registerParts(new Parts("leg_ru"));
    leg_rd = renderer.registerParts(new Parts("leg_rd"));
}

function render(entity, pass, partialTick) {
    vArmLU = PooledVec3.create(0.3125, 1.375, 0.0);
    vArmLD = PooledVec3.create(0.375, 1.125, 0.125);
    vArmRU = PooledVec3.create(-0.3125, 1.375, 0.0);
    vArmRD = PooledVec3.create(-0.375, 1.125, 0.125);

    vLegLU = PooledVec3.create(0.11875, 0.75, 0.0);
    vLegLD = PooledVec3.create(0.125, 0.375, -0.125);
    vLegRU = PooledVec3.create(-0.11875, 0.75, 0.0);
    vLegRD = PooledVec3.create(-0.125, 0.375, -0.125);

    GL11.glPushMatrix();

    if (pass == 0) {
        var time = renderer.getSystemTimeMillis() % 3600000;//1h区切り

        renderer.setRotationAngles(entity, partialTick);

        //walk(time);
        dance(time);
    }

    GL11.glPopMatrix();
}

function walk(time) {
    renderer.rotateAndRender(head, 0.0, 1.5, 0.0, renderer.headAngleX, renderer.headAngleY, renderer.headAngleZ);
    renderer.rotateAndRender(body, 0.0, 1.5, 0.0, renderer.bodyAngleX, renderer.bodyAngleY, renderer.bodyAngleZ);

    var armLXD = renderer.leftArmAngleX < 0.0 ? renderer.leftArmAngleX : 0.0;
    renderLimb(arm_lu, arm_ld, vArmLU, vArmLD,
        renderer.leftArmAngleX, renderer.leftArmAngleY, renderer.leftArmAngleZ,
        armLXD, 0.0, 0.0);

    var armRXD = renderer.rightArmAngleX < 0.0 ? renderer.rightArmAngleX : 0.0;
    renderLimb(arm_ru, arm_rd, vArmRU, vArmRD,
        renderer.rightArmAngleX, renderer.rightArmAngleY, renderer.rightArmAngleZ,
        armRXD, 0.0, 0.0);

    var legLXD = renderer.leftLegAngleX < 0.0 ? -renderer.leftLegAngleX : 0.0;
    renderLimb(leg_lu, leg_ld, vLegLU, vLegLD,
        renderer.leftLegAngleX, renderer.leftLegAngleY, renderer.leftLegAngleZ,
        legLXD, 0.0, 0.0);

    var legRXD = renderer.rightLegAngleX < 0.0 ? -renderer.rightLegAngleX : 0.0;
    renderLimb(leg_ru, leg_rd, vLegRU, vLegRD,
        renderer.rightLegAngleX, renderer.rightLegAngleY, renderer.rightLegAngleZ,
        legRXD, 0.0, 0.0);
}

function dance(time)//新○島
{
    var time = time % 4000;
    var tTho = (time % 1000) < 750 ? ((time % 1000) / 750.0) : 1.0;

    var LEG_X = 18.435;
    var LEG_Z = 10.0;

    var bodyZ = 0.0;
    var armX = tTho * 2.0 - 1.0;//-1.0~1.0
    var armY = tTho;
    var legRX = 0.0;
    var legLX = 0.0;
    var legRZ = 0.0;
    var legLZ = 0.0;
    var leg2RX = 0.0;
    var leg2LX = 0.0;

    if (time >= 0 && time < 1000) {
        bodyZ = 0.25 * tTho;
        legRX = -LEG_X * tTho;//0.25/0.75
        legLX = LEG_X * tTho;
        legRZ = LEG_Z * tTho;
        legLZ = 0.0;
        leg2RX = (tTho < 0.5) ? tTho * 2.0 : 2.0 - (tTho * 2.0);
    } else if (time >= 1000 && time < 2000) {
        bodyZ = 0.5 * tTho + 0.25;
        armX *= -1.0;
        armY = 1.0 - armY;
        legRX = (LEG_X * 2.0) * tTho - LEG_X;
        legLX = -(LEG_X * 2.0) * tTho + LEG_X;
        legRZ = LEG_Z;
        legLZ = -LEG_Z * tTho;
        leg2LX = (tTho < 0.5) ? tTho * 2.0 : 2.0 - (tTho * 2.0);
    } else if (time >= 2000 && time < 3000) {
        bodyZ = 0.25 * -tTho + 0.75;
        legRX = (33.690 - LEG_X) * tTho + LEG_X;//0.5/0.75
        legLX = (-33.690 + LEG_X) * tTho - LEG_X;
        legRZ = LEG_Z * (1.0 - tTho);
        legLZ = -LEG_Z;
        leg2RX = (tTho < 0.5) ? tTho * 2.0 : 2.0 - (tTho * 2.0);
    } else//(time >= 3000 && time < 4000)
    {
        bodyZ = 0.5 * -tTho + 0.5;
        armX *= -1.0;
        armY = 1.0 - armY;
        legRX = 33.690 * (1.0 - tTho);
        legLX = -33.690 * (1.0 - tTho);
        legRZ = 0.0;
        legLZ = -LEG_Z * (1.0 - tTho);
        leg2LX = (tTho < 0.5) ? tTho * 2.0 : 2.0 - (tTho * 2.0);
    }

    GL11.glTranslatef(0.0, 0.0, bodyZ);

    renderer.rotateAndRender(head, 0.0, 1.5, 0.0, 0.0, 0.0, 0.0);
    renderer.rotateAndRender(body, 0.0, 1.5, 0.0, 0.0, 0.0, 0.0);

    renderLimb(arm_lu, arm_ld, vArmLU, vArmLD,
        NGTMath.toRadians(-45.0 * armX), NGTMath.toRadians(-40.0 * armY), NGTMath.toRadians(20.0),
        NGTMath.toRadians(-35.0 * (armX + 1.0) - 20.0), 0.0, 0.0);

    renderLimb(arm_ru, arm_rd, vArmRU, vArmRD,
        NGTMath.toRadians(-45.0 * -armX), NGTMath.toRadians(40.0 * (1.0 - armY)), NGTMath.toRadians(-20.0),
        NGTMath.toRadians(-35.0 * (-armX + 1.0) - 20.0), 0.0, 0.0);

    renderLimb(leg_lu, leg_ld, vLegLU, vLegLD,
        NGTMath.toRadians(legLX), 0.0, NGTMath.toRadians(legLZ),
        NGTMath.toRadians(20.0 * leg2LX), 0.0, 0.0);

    renderLimb(leg_ru, leg_rd, vLegRU, vLegRD,
        NGTMath.toRadians(legRX), 0.0, NGTMath.toRadians(legRZ),
        NGTMath.toRadians(20.0 * leg2RX), 0.0, 0.0);
}

function dance2(time)//腕振り
{
    var sec2 = (time % 1000) / 1000;
    var hz = Math.sin(sec2 * 2.0 * Math.PI) * 5.0;
    renderer.rotateAndRender(head, 0.0, 1.5, 0.0, 0.0, 0.0, NGTMath.toRadians(hz));
    renderer.rotateAndRender(body, 0.0, 1.5, 0.0, 0.0, 0.0, 0.0);

    var sin1 = Math.sin(sec2 * 2.0 * Math.PI);
    var sin2 = Math.sin((sec2 + 0.5) * 2.0 * Math.PI);

    var axl = sin1 * -45.0;
    renderLimb(arm_lu, arm_ld, vArmLU, vArmLD,
        NGTMath.toRadians(-10.0), 0.0, 0.0,
        NGTMath.toRadians(axl - 90.0), 0.0, 0.0);

    var axr = sin2 * -45.0;
    renderLimb(arm_ru, arm_rd, vArmRU, vArmRD,
        NGTMath.toRadians(-10.0), 0.0, 0.0,
        NGTMath.toRadians(axr - 90.0), 0.0, 0.0);

    var lxl = (sin2 + 1.0) * -7.5;
    renderLimb(leg_lu, leg_ld, vLegLU, vLegLD,
        NGTMath.toRadians(lxl), 0.0, 0.0,
        NGTMath.toRadians(-lxl * 2.0), 0.0, 0.0);

    var lxr = (sin1 + 1.0) * -7.5;
    renderLimb(leg_ru, leg_rd, vLegRU, vLegRD,
        NGTMath.toRadians(lxr), 0.0, 0.0,
        NGTMath.toRadians(-lxr * 2.0), 0.0, 0.0);
}

function renderLimb(partsU, partsD, vecU, vecD, rXU, rYU, rZU, rXD, rYD, rZD) {
    GL11.glPushMatrix();
    GL11.glTranslatef(vecU.getX(), vecU.getY(), vecU.getZ());
    var rXUd = NGTMath.toDegrees(rXU);
    GL11.glRotatef(NGTMath.toDegrees(rZU), 0.0, 0.0, 1.0);
    GL11.glRotatef(NGTMath.toDegrees(rYU), 0.0, 1.0, 0.0);
    GL11.glRotatef(rXUd, 1.0, 0.0, 0.0);
    GL11.glTranslatef(-vecU.getX(), -vecU.getY(), -vecU.getZ());
    partsU.render(renderer);

    GL11.glTranslatef(vecD.getX(), vecD.getY(), vecD.getZ());
    GL11.glRotatef(NGTMath.toDegrees(rZD), 0.0, 0.0, 1.0);
    GL11.glRotatef(NGTMath.toDegrees(rYD), 0.0, 1.0, 0.0);
    GL11.glRotatef(NGTMath.toDegrees(rXD), 1.0, 0.0, 0.0);
    GL11.glTranslatef(-vecD.getX(), -vecD.getY(), -vecD.getZ());

    partsD.render(renderer);

    GL11.glPopMatrix();
}
