var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);

var tmpArray = [0.0, 0.0, 0.0];

function init(par1, par2) {
    base = renderer.registerParts(new Parts("base"));
    cover = renderer.registerParts(new Parts("cover"));
    light = renderer.registerParts(new Parts("mirror", "light"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    var state = renderer.getLightState(entity);
    var tick = renderer.getTick(entity);
    var rotation = (tick % 24) * 15.0;

    if (pass === 0) {
        base.render(renderer);
        if (state === 1) {
            GL11.glRotatef(rotation, 0.0, 1.0, 0.0);
            light.render(renderer);
        } else if (state === -1) {
            light.render(renderer);
        }
    } else if (pass === 1) {
        cover.render(renderer);
    } else if (pass === 2) {
        if (state === 1)//light ON
        {
            GL11.glRotatef(rotation, 0.0, 1.0, 0.0);
            //light.render(renderer);

            GL11.glTranslatef(0.0, 0.4, 0.0);

            for (var i = 0; i < 2; i++) {
                GL11.glPushMatrix();
                if (i === 1) {
                    GL11.glRotatef(180.0, 0.0, 1.0, 0.0);
                }
                GL11.glTranslatef(0.0, 0.0, 0.25);

                //var yaw = renderer.getYaw(entity);
                //var normal = renderer.getNormal(entity, 0.0, 0.0, 1.0, 0.0, yaw + rotation);
                //var lPos = renderer.getLightPos(entity, 0.0, 0.4, 0.25, 0.0, yaw + rotation);

                renderer.renderLightEffect(
                    null, tmpArray,
                    0.75, 0.0625, 5.0, 0xFF0000, 1, false
                );

                renderer.renderLightEffect(
                    null, tmpArray,
                    0.75, 0.0, 0.0, 0xFF0000, 0, false
                );

                GL11.glPopMatrix();
            }
        }
        /*else if(state == -1)
        {
            light.render(renderer);
        }*/
    }

    GL11.glPopMatrix();
}
