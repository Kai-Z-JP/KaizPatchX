var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

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
            var dir = entity.getDir();
            var b0 = (dir == 0 || dir == 2);

            //なし:0, 足場Z:1, 足場X:2, 階段:3, 立方体:4
            var flag0 = BlockScaffold.getConnectionType(world, x + 1, y, z, 1);
            var flag1 = BlockScaffold.getConnectionType(world, x - 1, y, z, 1);
            var flag2 = BlockScaffold.getConnectionType(world, x, y, z + 1, 0);
            var flag3 = BlockScaffold.getConnectionType(world, x, y, z - 1, 0);
            var flagXP = !(flag0 >= 1 && flag0 <= 3) && (b0 || (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3));
            var flagXN = !(flag1 >= 1 && flag1 <= 3) && (b0 || (flag2 == 1 || flag3 == 1 || flag2 == 3 || flag3 == 3));
            var flagZP = !(flag2 >= 1 && flag2 <= 3) && (!b0 || (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3));
            var flagZN = !(flag3 >= 1 && flag3 <= 3) && (!b0 || (flag0 == 2 || flag1 == 2 || flag0 == 3 || flag1 == 3));

            if (flagXP && flag0 != 3) {
                xp.render(renderer);
            }

            if (flagXN && flag1 != 3) {
                xn.render(renderer);
            }

            if (flagZP && flag2 != 3) {
                zp.render(renderer);
            }

            if (flagZN && flag3 != 3) {
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
