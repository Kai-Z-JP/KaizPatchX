var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    base = renderer.registerParts(new Parts("base", "jiku"));
    body = renderer.registerParts(new Parts("body"));
    body_L = renderer.registerParts(new Parts("mirror", "light"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    var meta = renderer.getMetadata(entity);
    var pitch = renderer.getPitch(entity);
    switch (meta) {
        case 0:
            pitch = -pitch;
            break;
        case 1:
            break;
        case 2:
            pitch -= 90.0;
            break;
        case 3:
            pitch -= 90.0;
            break;
        case 4:
            pitch -= 90.0;
            break;
        case 5:
            pitch -= 90.0;
            break;
    }

    var state = renderer.getLightState(entity);

    if (pass === 0) {
        base.render(renderer);

        renderer.rotate(90.0 + pitch, 'X', 0.0, 0.6, 0.0);
        body.render(renderer);

        if (state === -1) {
            body_L.render(renderer);
        }
    } else if (pass === 2) {
        if (state === 1) {
            renderer.rotate(90.0 + pitch, 'X', 0.0, 0.6, 0.0);
            body_L.render(renderer);

            GL11.glRotatef(-90.0, 1.0, 0.0, 0.0);
            //GL11.glTranslatef(0.0, 0.0, 0.4);
            GL11.glTranslatef(0.0, 0.0, 0.91);

            var yaw = renderer.getYaw(entity);
            var normal = renderer.getNormal(entity, 0.0, 0.0, 1.0, pitch, yaw);

            renderer.renderLightEffect(
                normal,
                renderer.getLightPos(entity, 0.0, 0.5, -0.2, pitch, yaw),
                8.0, 0.7, 256.0, 0xFFFFFF, 1, false
            );

            //GL11.glTranslatef(0.0, 0.0, 0.52);

            renderer.renderLightEffect(
                normal,
                renderer.getLightPos(entity, 0.0, 0.5, 0.31, pitch, yaw),
                0.8, 0.0, 0.0, 0xFFFFFF, 0, false
            );
        }
    }

    GL11.glPopMatrix();
}
