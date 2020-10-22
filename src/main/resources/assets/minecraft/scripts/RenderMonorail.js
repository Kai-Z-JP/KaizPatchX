var renderClass = "jp.ngt.rtm.render.RailPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.rail.util);
importPackage(Packages.jp.ngt.ngtlib.math);

function init(par1, par2) {
    allParts = renderer.registerParts(new Parts("body", "wire", "gaishi"));
}

function renderRailStatic(tileEntity, posX, posY, posZ, par8, pass) {
    if (!renderer.isSwitchRail(tileEntity)) {
        renderer.renderStaticParts(tileEntity, posX, posY, posZ);
    }
}

function renderRailDynamic(tileEntity, posX, posY, posZ, par8, pass) {
    if (renderer.isSwitchRail(tileEntity)) {
        if (tileEntity.getSwitch() == null) {
            return;
        }

        GL11.glPushMatrix();
        var rp = tileEntity.getRailPositions()[0];
        var x = rp.posX - rp.blockX;
        var z = rp.posZ - rp.blockZ;
        GL11.glTranslatef(posX + x, posY, posZ + z);

        renderer.bindTexture(renderer.getModelObject().textures[0].material.texture);

        //分岐レールの各頂点-中間点までを描画
        var pArray = tileEntity.getSwitch().getPoints();
        for (var i = 0; i < pArray.length; ++i) {
            renderPoint(tileEntity, pArray[i]);
        }

        GL11.glPopMatrix();
    }
}

function renderPoint(tileEntity, point) {
    if (point.branchDir == RailDir.NONE)//分岐なし部分
    {
        var rm = point.rmMain;
        var max = Math.floor(rm.getLength() * 2.0);
        var halfMax = max / 2;
        var startIndex = point.rmmDir ? 0 : halfMax;
        var endIndex = point.rmmDir ? halfMax : max;
        renderer.renderRailMapStatic(tileEntity, rm, max, startIndex, endIndex, allParts);
    } else {
        renderPointDynamic(tileEntity, point)
    }
}

function renderPointDynamic(tileEntity, point) {
    var rlMain = point.rmMain.getLength();
    var rlBranch = point.rmBranch.getLength();
    var max = Math.floor((rlMain > rlBranch ? rlMain : rlBranch) * 2.0);
    var halfMax = max / 2;

    var pmStart = point.rmMain.getRailPos(max, 0);
    var pbStart = point.rmBranch.getRailPos(max, 0);
    var pmEnd = point.rmMain.getRailPos(max, max);
    var pbEnd = point.rmBranch.getRailPos(max, max);

    var startPos = tileEntity.getStartPoint();
    var revXZ = RailPosition.REVISION[tileEntity.getRailPositions()[0].direction];
    //レール全体の始点からの移動差分
    var moveX = point.rpRoot.posX - (startPos[0] + 0.5 + revXZ[0]);
    var moveZ = point.rpRoot.posZ - (startPos[2] + 0.5 + revXZ[1]);

    //頂点-中間点
    for (var i = 0; i <= halfMax; ++i) {
        var im = point.mainDirIsPositive ? i : max - i;
        var ib = point.branchDirIsPositive ? i : max - i;

        var pmn = point.rmMain.getRailPos(max, im);
        if (point.mainDirIsPositive) {
            pmn[0] -= pmStart[0];
            pmn[1] -= pmStart[1];
        } else {
            pmn[0] -= pmEnd[0];
            pmn[1] -= pmEnd[1];
        }

        var pbn = point.rmBranch.getRailPos(max, ib);
        if (point.branchDirIsPositive) {
            pbn[0] -= pbStart[0];
            pbn[1] -= pbStart[1];
        } else {
            pbn[0] -= pbEnd[0];
            pbn[1] -= pbEnd[1];
        }

        var move = point.getMovement();
        var moveInv = 1.0 - move;
        var x0 = moveX + (pmn[1] * moveInv + pbn[1] * move);
        var z0 = moveZ + (pmn[0] * moveInv + pbn[0] * move);
        var yawm = NGTMath.normalizeAngle(point.rmMain.getRailRotation(max, im) + (point.mainDirIsPositive ? 0.0 : 180.0));
        var yawb = NGTMath.normalizeAngle(point.rmBranch.getRailRotation(max, ib) + (point.branchDirIsPositive ? 0.0 : 180.0));
        var yawdif = yawb - yawm;
        if (yawdif > 180.0) {
            yawdif -= 360.0;
        } else if (yawdif < -180.0) {
            yawdif += 360.0;
        }
        var yaw = yawm + yawdif * move;

        var brightness = renderer.getBrightness(
            renderer.getWorld(tileEntity),
            Math.round(renderer.getX(tileEntity) + x0),
            renderer.getY(tileEntity),
            Math.round(renderer.getZ(tileEntity) + z0));

        GL11.glPushMatrix();
        GL11.glTranslatef(x0, 0.0, z0);
        GL11.glRotatef(yaw, 0.0, 1.0, 0.0);
        renderer.setBrightness(brightness);
        allParts.render(renderer);
        GL11.glPopMatrix();
    }
}

function shouldRenderObject(tileEntity, objName, len, pos) {
    return true;
}
