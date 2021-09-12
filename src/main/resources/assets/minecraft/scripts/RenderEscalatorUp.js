var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

function init(par1, par2) {
    step = renderer.registerParts(new Parts("step"));
    cover_F = renderer.registerParts(new Parts("cover_F"));
    cover_B = renderer.registerParts(new Parts("cover_B"));
    part_F = [renderer.registerParts(new Parts("partL_F")), renderer.registerParts(new Parts("partR_F"))];
    part_FS = [renderer.registerParts(new Parts("partL_FS")), renderer.registerParts(new Parts("partR_FS"))];
    part_B = [renderer.registerParts(new Parts("partL_B")), renderer.registerParts(new Parts("partR_B"))];
    part_BS = [renderer.registerParts(new Parts("partL_BS")), renderer.registerParts(new Parts("partR_BS"))];
    belt = [renderer.registerParts(new Parts("beltL")), renderer.registerParts(new Parts("beltR"))];
    belt_FS = [renderer.registerParts(new Parts("beltL_FS")), renderer.registerParts(new Parts("beltR_FS"))];
    belt_BS = [renderer.registerParts(new Parts("beltL_BS")), renderer.registerParts(new Parts("beltR_BS"))];
}

function render(entity, pass, par3) {
    renderEscalator(entity, pass, par3, false);
}

function renderEscalator(entity, pass, par3, reverse) {
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
            var conXP = BlockScaffoldStairs.getConnectionType(world, x + 1, y, z, entity.getDir());
            var conXN = BlockScaffoldStairs.getConnectionType(world, x - 1, y, z, entity.getDir());
            var conZP = BlockScaffoldStairs.getConnectionType(world, x, y, z + 1, entity.getDir());
            var conZN = BlockScaffoldStairs.getConnectionType(world, x, y, z - 1, entity.getDir());
            var conF = !((dir == 0 && conZN != 3) || (dir == 1 && conXP != 3) || (dir == 2 && conZP != 3) || (dir == 3 && conXN != 3));
            var conB = !((dir == 0 && conZP != 3) || (dir == 1 && conXN != 3) || (dir == 2 && conZN != 3) || (dir == 3 && conXP != 3));

            if ((dir == 0 && conXN < 3) || (dir == 1 && conZN < 3) || (dir == 2 && conXP < 3) || (dir == 3 && conZP < 3)) {
                renderSide(entity, pass, par3, 0, color, conF, conB);//L
            }

            if ((dir == 0 && conXP < 3) || (dir == 1 && conZP < 3) || (dir == 2 && conXN < 3) || (dir == 3 && conZN < 3)) {
                renderSide(entity, pass, par3, 1, color, conF, conB);//R
            }

            if (!conF) {
                cover_F.render(renderer);
            }

            if (!conB) {
                cover_B.render(renderer);
            }

            renderStairs(entity, pass, par3, conF, conB, reverse);
        } else {
            renderSide(entity, pass, par3, 0, color, true, true);
            renderSide(entity, pass, par3, 1, color, true, true);
            cover_F.render(renderer);
            cover_B.render(renderer);
            renderStairs(entity, pass, par3, false, false, reverse);
        }
    }

    GL11.glPopMatrix();
}

function renderSide(entity, pass, par3, side, color, conF, conB) {
    if (conF && conB) {
        part_F[side].render(renderer);
        part_B[side].render(renderer);

        NGTRenderHelper.setColor(color);
        belt[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    } else if (conF) {
        part_F[side].render(renderer);
        part_BS[side].render(renderer);

        NGTRenderHelper.setColor(color);
        belt[side].render(renderer);
        belt_BS[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    } else if (conB) {
        part_FS[side].render(renderer);
        part_B[side].render(renderer);

        NGTRenderHelper.setColor(color);
        belt_FS[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    } else {
        part_FS[side].render(renderer);
        part_BS[side].render(renderer);

        NGTRenderHelper.setColor(color);
        belt_FS[side].render(renderer);
        belt_BS[side].render(renderer);
        NGTRenderHelper.setColor(0xFFFFFF);
    }
}

function renderStairs(entity, pass, par3, conF, conB, reverse) {
    var timeMil = renderer.getSystemTimeMillis();
    var partCount = 7;

    for (var i = 0; i < partCount; ++i) {
        var time = (timeMil + 1000 * i) % (partCount * 1000);//端から端まで7sec
        if (reverse) {
            time = (partCount * 1000) - time - 1;//時間の進みを逆転
        }
        var x = time / 2000.0;//移動速度:0.5m/sec
        var moveY = 0.0;
        var moveZ = x - 1.5;
        if (conF && conB) {
            if (time < 2000 || time > 5000) {
                continue;
            }
            moveY = x - 1.5;
        } else if (conF) {
            if (time > 5000) {
                continue;
            }

            if (time < 1000) {
                moveY = -0.5;
            } else if (time < 3000) {
                var x2 = x - 1.5;
                moveY = x2 + (x2 * x2 * 0.5);
            } else {
                moveY = x - 1.5;
            }
        } else if (conB) {
            if (time < 2000) {
                continue;
            }

            if (time < 3000) {
                moveY = x - 1.5;
            } else if (time < 5000) {
                var x2 = x - 1.5;
                moveY = x2 - (x2 * x2 * 0.5);
            } else {
                moveY = 0.5;
            }
        } else {
            if (time < 1000) {
                moveY = -0.5;
            } else if (time < 3000) {
                var x2 = x - 1.5;
                moveY = x2 + (x2 * x2 * 0.5);
            } else if (time < 5000) {
                var x2 = x - 1.5;
                moveY = x2 - (x2 * x2 * 0.5);
            } else {
                moveY = 0.5;
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, moveY + 0.01, moveZ);//Y:端でのZファイト防止に
        step.render(renderer);
        GL11.glPopMatrix();
    }
}
