var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

function init(par1, par2) {
    partMain = renderer.registerParts(new Parts("partMain"));
    partR = renderer.registerParts(new Parts("partR"));
    partL = renderer.registerParts(new Parts("partL"));
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
            var yaw = 180.0 - (dir * 90.0);
            GL11.glRotatef(yaw, 0.0, 1.0, 0.0);

            //0:なし, 1:足場Z, 2:足場X, 3:階段, 4:立方体
            var flag0 = BlockScaffoldStairs.getConnectionType(world, x + 1, y, z, entity.getDir());
            var flag1 = BlockScaffoldStairs.getConnectionType(world, x - 1, y, z, entity.getDir());
            var flag2 = BlockScaffoldStairs.getConnectionType(world, x, y, z + 1, entity.getDir());
            var flag3 = BlockScaffoldStairs.getConnectionType(world, x, y, z - 1, entity.getDir());

            if ((dir == 0 && flag1 < 3) || (dir == 1 && flag3 < 3) || (dir == 2 && flag0 < 3) || (dir == 3 && flag2 < 3)) {
                partL.render(renderer);
            }

            if (!((dir == 0 && flag0 >= 3) || (dir == 1 && flag2 >= 3) || (dir == 2 && flag1 >= 3) || (dir == 3 && flag3 >= 3))) {
                partR.render(renderer);
            }
        } else {
            partL.render(renderer);
            partR.render(renderer);
        }

        partMain.render(renderer);

        NGTRenderHelper.setColor(0xFFFFFF);
    }

    GL11.glPopMatrix();
}
