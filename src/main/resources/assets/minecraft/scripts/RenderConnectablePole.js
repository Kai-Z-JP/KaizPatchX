var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

function init(par1, par2) {
    main = renderer.registerParts(new Parts("partMain"));
    xp = renderer.registerParts(new Parts("partXP"));
    xn = renderer.registerParts(new Parts("partXN"));
    zp = renderer.registerParts(new Parts("partZP"));
    zn = renderer.registerParts(new Parts("partZN"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, -0.5, 0.0);

    if (pass == 0) {
        if (entity != null) {
            var color = renderer.getColor(entity);
            if (color <= 0) {
                color = 0xFFFFFF;
            }
            NGTRenderHelper.setColor(color);
            var x = entity.getX();
            var y = entity.getY();
            var z = entity.getZ();
            var world = renderer.getWorld(entity);
            var conXP = BlockLinePole.isConnected(world, x + 1, y, z, false);
            var conXN = BlockLinePole.isConnected(world, x - 1, y, z, false);
            var conYP = BlockLinePole.isConnected(world, x, y + 1, z, false);
            var conYN = BlockLinePole.isConnected(world, x, y - 1, z, false);
            var conZP = BlockLinePole.isConnected(world, x, y, z + 1, false);
            var conZN = BlockLinePole.isConnected(world, x, y, z - 1, false);
            var noCon = !conXP && !conXN && !conZP && !conZN;
            var rightAngleCon = (conXP || conXN) && (conZP || conZN);

            if (conXP) {
                xp.render(renderer);
            }

            if (conXN) {
                xn.render(renderer);
            }

            if (conZP) {
                zp.render(renderer);
            }

            if (conZN) {
                zn.render(renderer);
            }

            if (noCon || rightAngleCon || conYP || conYN) {
                main.render(renderer);
            }
            NGTRenderHelper.setColor(0xFFFFFF);
        } else {
            xp.render(renderer);
            xn.render(renderer);
            main.render(renderer);
        }
    }

    GL11.glPopMatrix();
}
