var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

function init(par1, par2) {
    main = renderer.registerParts(new Parts("partMain"));
    xp = renderer.registerParts(new Parts("partXP"));
    xn = renderer.registerParts(new Parts("partXN"));
    yp = renderer.registerParts(new Parts("partYP"));
    yn = renderer.registerParts(new Parts("partYN"));
    zp = renderer.registerParts(new Parts("partZP"));
    zn = renderer.registerParts(new Parts("partZN"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, -0.5, 0.0);

    if (pass == 0) {
        if (entity != null) {
            var x = entity.getX();
            var y = entity.getY();
            var z = entity.getZ();
            var world = renderer.getWorld(entity);

            var side = entity.getAttachedSide();

            if (side == 4 || side == 5)//X
            {
                xp.render(renderer);
                xn.render(renderer);
            } else if (side == 0 || side == 1)//Y
            {
                yp.render(renderer);
                yn.render(renderer);
            } else if (side == 2 || side == 3)//Z
            {
                zp.render(renderer);
                zn.render(renderer);
            }
        } else {
            yp.render(renderer);
            yn.render(renderer);
            main.render(renderer);
        }
    }

    GL11.glPopMatrix();
}
