var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);
importPackage(Packages.jp.ngt.rtm.block.tileentity);

function init(par1, par2) {
    partMain = renderer.registerParts(new Parts("partMain"));
    xp = renderer.registerParts(new Parts("partXP"));
    xn = renderer.registerParts(new Parts("partXN"));
    zp = renderer.registerParts(new Parts("partZP"));
    zn = renderer.registerParts(new Parts("partZN"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, -0.5, 0.0);

    if (pass == 0) {
        var color = renderer.getColor(entity);
        if (color <= 0) {
            color = 0xFFFFFF;
        }
        NGTRenderHelper.setColor(color);

        if (entity != null) {
            var x = entity.getX();
            var y = entity.getY();
            var z = entity.getZ();
            var world = renderer.getWorld(entity);

            var b0 = true;
            if (entity instanceof TileEntityScaffold) {
                b0 = entity.getDir() === 0;
            }

            //なし:0, 足場Z:1, 足場X:2, 階段:3, 立方体:4
            var flag0 = BlockScaffold.getConnectionType(world, x + 1, y, z, 0);
            var flag1 = BlockScaffold.getConnectionType(world, x - 1, y, z, 0);
            var flag2 = BlockScaffold.getConnectionType(world, x, y, z + 1, 1);
            var flag3 = BlockScaffold.getConnectionType(world, x, y, z - 1, 1);
            var flagXP = (b0 && flag0 == 0) || (!b0 && flag0 == 0 && (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3));
            var flagXN = (b0 && flag1 == 0) || (!b0 && flag1 == 0 && (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3));
            var flagZP = (!b0 && flag2 == 0) || (b0 && flag2 == 0 && (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3));
            var flagZN = (!b0 && flag3 == 0) || (b0 && flag3 == 0 && (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3));

            if (flagXP) {
                xp.render(renderer);
            }

            if (flagXN) {
                xn.render(renderer);
            }

            if (flagZP) {
                zp.render(renderer);
            }

            if (flagZN) {
                zn.render(renderer);
            }
        } else {
            xp.render(renderer);
            xn.render(renderer);
        }

        partMain.render(renderer);

        NGTRenderHelper.setColor(0xFFFFFF);
    }

    GL11.glPopMatrix();
}
