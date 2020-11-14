var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.ngtlib.math);

//[yaw, pitch]
rotationArray = [
    [0.0, 10.0],
    [36.0, 45.0],
    [72.0, -20.0],
    [108.0, 30.0],
    [144.0, -60.0],
    [180.0, 40.0],
    [-36.0, 50.0],
    [-72.0, -10.0],
    [-108.0, 30.0],
    [-144.0, -70.0],
];

function init(par1, par2) {
    ball = renderer.registerParts(new Parts("ball"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    /*var meta = renderer.getMetadata(entity);
    var pitch = renderer.getPitch(entity);
    switch(meta)
    {
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
    }*/

    GL11.glTranslatef(0.0, 0.5, 0.0);

    var state = renderer.getLightState(entity);
    var tick = renderer.getTick(entity);
    var rotation = (tick % 360) * 1.0;

    if (pass === 0) {
        if (state === 1) {
            GL11.glRotatef(rotation, 0.0, 1.0, 0.0);
            ball.render(renderer);
        } else if (state === -1) {
            ball.render(renderer);
        }
    } else if (pass === 2) {
        if (state === 1) {
            for (var i = 0; i < rotationArray.length; ++i) {
                GL11.glPushMatrix();

                var yaw = rotationArray[i][0];
                var pitch = rotationArray[i][1];
                GL11.glRotatef(yaw, 0.0, 1.0, 0.0);
                GL11.glRotatef(pitch, 1.0, 0.0, 0.0);
                GL11.glTranslatef(0.0, 0.0, 0.5);

                var yaw = renderer.getYaw(entity);
                var normal = renderer.getNormal(entity, 0.0, 0.0, 1.0, pitch, yaw);
                var pos = [entity.xCoord + 0.5, entity.yCoord + 0.5, entity.zCoord + 0.5];
                var brightness = (NGTMath.getSin(rotation + (i * 60)) * 0.5) + 0.5;

                renderer.renderLightEffect(
                    normal, pos,
                    0.75 * brightness, 0.0, 5.0 * brightness, 0xFFFFFF, 1, false
                );

                renderer.renderLightEffect(
                    normal, pos,
                    0.25 * brightness, 0.0, 0.0, 0xFFFFFF, 0, false
                );

                GL11.glPopMatrix();
            }
        }
    }

    GL11.glPopMatrix();
}
