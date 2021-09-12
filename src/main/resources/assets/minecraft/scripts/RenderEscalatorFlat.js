var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

function init(par1, par2) {
    step = renderer.registerParts(new Parts("step"));
    cover_F = renderer.registerParts(new Parts("cover_F"));
    cover_B = renderer.registerParts(new Parts("cover_B"));
    part = [renderer.registerParts(new Parts("partL")), renderer.registerParts(new Parts("partR"))];
    belt = [renderer.registerParts(new Parts("beltL")), renderer.registerParts(new Parts("beltR"))];
    belt_FS = [renderer.registerParts(new Parts("beltL_FS")), renderer.registerParts(new Parts("beltR_FS"))];
    belt_BS = [renderer.registerParts(new Parts("beltL_BS")), renderer.registerParts(new Parts("beltR_BS"))];
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

            var x = entity.getX();
            var y = entity.getY();
            var z = entity.getZ();
            var world = renderer.getWorld(entity);
            var dir = entity.getDir();
            var yaw = 180.0 - (dir * 90.0);
            GL11.glRotatef(yaw, 0.0, 1.0, 0.0);

            //0:なし, 1:足場Z, 2:足場X, 3:階段, 4:立方体
            var conXP = BlockScaffold.getConnectionType(world, x + 1, y, z, entity.getDir());
            var conXN = BlockScaffold.getConnectionType(world, x - 1, y, z, entity.getDir());
            var conZP = BlockScaffold.getConnectionType(world, x, y, z + 1, entity.getDir());
            var conZN = BlockScaffold.getConnectionType(world, x, y, z - 1, entity.getDir());
            var conF = !((dir == 0 && conZN == 0) || (dir == 1 && conXP == 0) || (dir == 2 && conZP == 0) || (dir == 3 && conXN == 0));
            var conB = !((dir == 0 && conZP == 0) || (dir == 1 && conXN == 0) || (dir == 2 && conZN == 0) || (dir == 3 && conXP == 0));

            if ((dir == 0 && conXN == 0) || (dir == 1 && conZN == 0) || (dir == 2 && conXP == 0) || (dir == 3 && conZP == 0)) {
                renderSide(entity, pass, par3, 0, color, conF, conB);//L
            }

            if ((dir == 0 && conXP == 0) || (dir == 1 && conZP == 0) || (dir == 2 && conXN == 0) || (dir == 3 && conZN == 0)) {
                renderSide(entity, pass, par3, 1, color, conF, conB);//R
            }

            if (!conF) {
                cover_F.render(renderer);
            }

            if (!conB) {
                cover_B.render(renderer);
            }

            renderStairs(entity, pass, par3, conF, conB);
        } else {
            renderSide(entity, pass, par3, 0, color, true, true);
            renderSide(entity, pass, par3, 1, color, true, true);
            cover_F.render(renderer);
            cover_B.render(renderer);
            renderStairs(entity, pass, par3, false, false);
        }
    }

    GL11.glPopMatrix();
}

function renderSide(entity, pass, par3, side, color, conF, conB) {
    part[side].render(renderer);

    if (conF && conB) {
        NGTRenderHelper.setColor(color);
        belt[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    } else if (conF) {
        NGTRenderHelper.setColor(color);
        belt[side].render(renderer);
        belt_BS[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    } else if (conB) {
        NGTRenderHelper.setColor(color);
        belt[side].render(renderer);
        belt_FS[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    } else {
        NGTRenderHelper.setColor(color);
        belt[side].render(renderer);
        belt_FS[side].render(renderer);
        belt_BS[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    }
}

function renderStairs(entity, pass, par3, conF, conB) {
    var timeMil = renderer.getSystemTimeMillis();
    var partCount = 3;

    for (var i = 0; i < partCount; ++i) {
        var time = (timeMil + 1000 * i) % (partCount * 1000);//端から端まで7sec
        var x = time / 2000.0;//移動速度:0.5m/sec
        var moveY = 0.0;
        var moveZ = x - 0.5;

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, moveY + 0.01, moveZ);//Y:端でのZファイト防止に
        step.render(renderer);
        GL11.glPopMatrix();
    }
}
