var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    body = renderer.registerParts(new Parts("body", "rod"));
    washer = renderer.registerParts(new Parts("washer"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass == 0) {
        body.render(renderer);

        var state = renderer.getMovingCount(entity);
        if (state == 1.0) {
            var tick = renderer.getTick(entity);
            var rotation = (tick % 15) * 24.0;
            renderer.rotate(rotation, 'Y', 0.0, 0.0, 0.15);
        }
        washer.render(renderer);
    }

    GL11.glPopMatrix();
}
