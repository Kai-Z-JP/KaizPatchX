var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    blades = renderer.registerParts(new Parts("blade0", "blade1", "blade2", "blade3", "blades", "obj2"));
    lod = renderer.registerParts(new Parts("lod"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass === 0) {
        lod.render(renderer);

        var tick = (renderer.getTick(entity) % 12);
        var rotation = tick * 30.0;
        renderer.rotate(rotation, 'Z', 0.0, 0.9, 0.0);
        blades.render(renderer);
    }

    GL11.glPopMatrix();
}
