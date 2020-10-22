var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    body = renderer.registerParts(new Parts("body1", "body2", "jikuuke", "jikuuke2"));
    tank = renderer.registerParts(new Parts("tank", "kuchi"));
}

function render(entity, pass, par3) {
    if (pass != 0) {
        return;
    }

    GL11.glPushMatrix();
    body.render(renderer);
    GL11.glTranslatef(0.0, 1.6, 0.0);
    tank.render(renderer);
    GL11.glPopMatrix();
}
