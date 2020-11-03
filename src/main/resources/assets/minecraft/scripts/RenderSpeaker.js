var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    base = renderer.registerParts(new Parts("fixture"));
    body = renderer.registerParts(new Parts("body"));
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

    GL11.glTranslatef(0.0, 0.25, 0.0);

    if (pass == 0) {
        base.render(renderer);

        renderer.rotate(pitch, 'X', 0.0, 0.0, 0.0);
        body.render(renderer);
    }

    GL11.glPopMatrix();
}
