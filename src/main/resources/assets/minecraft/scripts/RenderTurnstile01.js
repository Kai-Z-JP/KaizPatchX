var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    main = renderer.registerParts(new Parts("body", "touchPanel", "sidePanel", "door_n"));
    doorL = renderer.registerParts(new Parts("door_L"));
    doorR = renderer.registerParts(new Parts("door_R"));
    sign = renderer.registerParts(new Parts("sign_F", "sign_B"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass === 0) {
        main.render(renderer);

        var state = renderer.getMovingCount(entity);
        if (state > 0.0) {
            GL11.glPushMatrix();
            renderer.rotate(-90.0, 'Y', -0.4125, 0.625, -0.385);
            doorL.render(renderer);
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            renderer.rotate(90.0, 'Y', 0.3125, 0.625, -0.385);
            doorR.render(renderer);
            GL11.glPopMatrix();
        } else {
            doorL.render(renderer);
            doorR.render(renderer);
        }
    } else if (pass === 2) {
        sign.render(renderer);
    }

    GL11.glPopMatrix();
}
