var renderClass = "jp.ngt.rtm.render.RailPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.rail.util);

TONG_MOVE = 0.35;
TONG_POS = 1.0 / 10.0;
HALF_GAUGE = 0.5647;
/**レール長で割る*/
YAW_RATE = 450.0;

function init(par1, par2) {
    staticParts = renderer.registerParts(new Parts("base"));
    leftParts = renderer.registerParts(new Parts("railL", "sideL"));
    rightParts = renderer.registerParts(new Parts("railR", "sideR"));
    tongFL = renderer.registerParts(new Parts("L0"));
    tongBL = renderer.registerParts(new Parts("L1"));
    tongFR = renderer.registerParts(new Parts("R0"));
    tongBR = renderer.registerParts(new Parts("R1"));
}

function renderRailStatic(tileEntity, posX, posY, posZ, par8, pass) {
    renderer.renderStaticParts(tileEntity, posX, posY, posZ);
}

function renderRailDynamic(tileEntity, posX, posY, posZ, par8, pass) {
    if (renderer.isSwitchRail(tileEntity)) {
        renderRailDynamic2(tileEntity, posX, posY, posZ);
    }
}

function shouldRenderObject(tileEntity, objName, len, pos) {
    if (renderer.isSwitchRail(tileEntity))//分岐レール
    {
        //可動部パーツは除外
        return staticParts.containsName(objName);
    } else {
        return staticParts.containsName(objName) || leftParts.containsName(objName) || rightParts.containsName(objName);
    }
}

function renderRailDynamic2(tileEntity, par2, par4, par6) {
    if (tileEntity.getSwitch() == null) {
        return;
    }

    GL11.glPushMatrix();
    var rp = tileEntity.getRailPositions()[0];
    var x = rp.posX - rp.blockX;
    //var y = rp.posY - rp.blockY;
    var z = rp.posZ - rp.blockZ;
    GL11.glTranslatef(par2 + x, par4, par6 + z);

    renderer.bindTexture(renderer.getModelObject().textures[0].material.texture);

    //分岐レールの各頂点-中間点までを描画
    var pArray = tileEntity.getSwitch().getPoints();
    for (var i = 0; i < pArray.length; ++i) {
        renderPoint(tileEntity, pArray[i]);
    }

    GL11.glPopMatrix();
}

function renderPoint(tileEntity, point) {
    if (point.branchDir == RailDir.NONE)//分岐なし部分
    {
        var rm = point.rmMain;
        var max = Math.floor(rm.getLength() * 2.0);
        var halfMax = Math.floor(max / 2);
        var startIndex = point.mainDirIsPositive ? 0 : halfMax;
        var endIndex = point.mainDirIsPositive ? halfMax : max;
        renderer.renderRailMapStatic(tileEntity, rm, max, startIndex, endIndex, leftParts, rightParts);
    } else {
        var tongIndex = Math.floor(point.rmMain.getLength() * 2.0 * TONG_POS);//どの位置を末端モデルで描画
        var move = point.getMovement() * TONG_MOVE;
        renderRailMapDynamic(tileEntity, point.rmMain, point.branchDir,
            point.mainDirIsPositive, move, tongIndex);

        move = (1.0 - point.getMovement()) * TONG_MOVE;
        renderRailMapDynamic(tileEntity, point.rmBranch, point.branchDir.invert(),
            point.branchDirIsPositive, move, tongIndex);
    }
}

/**
 * RailMapごとの描画
 * @param move 開通時:0.0
 */
function renderRailMapDynamic(tileEntity, rms, dir, par3, move, tongIndex) {
    var railLength = rms.getLength();
    var max = Math.floor(railLength * 2.0);
    var halfMax = Math.floor(max / 2);
    var startIndex = par3 ? 0 : halfMax;
    var endIndex = par3 ? halfMax : max;

    var origPos = rms.getRailPos(max, 0);
    var startPos = tileEntity.getStartPoint();
    var revXZ = RailPosition.REVISION[tileEntity.getRailPositions()[0].direction];
    //当RailMapのレール全体の始点に対する移動差分
    var coreX = startPos[0] + 0.5 + revXZ[0];
    var coreZ = startPos[2] + 0.5 + revXZ[1];
    var moveX = origPos[1] - coreX;
    var moveZ = origPos[0] - coreZ;
    //向きによって移動量を反転させる
    var dirFixture = ((par3 && dir == RailDir.LEFT) || (!par3 && dir == RailDir.RIGHT)) ? -1.0 : 1.0;

    //頂点-中間点
    for (var i = startIndex; i <= endIndex; ++i) {
        var p1 = rms.getRailPos(max, i);
        var x0 = moveX + (p1[1] - origPos[1]);
        var z0 = moveZ + (p1[0] - origPos[0]);
        var yaw = rms.getRailRotation(max, i);
        var brightness = renderer.getBrightness(
            renderer.getWorld(tileEntity),
            p1[1], renderer.getY(tileEntity), p1[0]);

        GL11.glPushMatrix();
        GL11.glTranslatef(x0, 0.0, z0);
        GL11.glRotatef(yaw, 0.0, 1.0, 0.0);
        renderer.setBrightness(brightness);

        //分岐してない側のレール
        //開始位置が逆の場合は左右反対側のパーツを描画
        if ((par3 && dir == RailDir.LEFT) || (!par3 && dir == RailDir.RIGHT)) {
            rightParts.render(renderer);
        } else {
            leftParts.render(renderer);
        }

        //トング部分の離れ度合い(0.0-1.0)
        var separateRate = (par3 ? i : max - i) / halfMax;
        separateRate = (1.0 - sigmoid2(separateRate)) * move * dirFixture;
        var halfGaugeMove = dirFixture * HALF_GAUGE;
        GL11.glTranslatef(separateRate - halfGaugeMove, 0.0, 0.0);
        var yaw2 = separateRate * YAW_RATE / railLength * (par3 ? -1.0 : 1.0);
        GL11.glRotatef(yaw2, 0.0, 1.0, 0.0);
        GL11.glTranslatef(halfGaugeMove, 0.0, 0.0);

        //分岐してる側のレール
        if (dir == RailDir.LEFT) {
            if (par3)//始点を共有
            {
                if (i == tongIndex) {
                    tongBL.render(renderer);//トングレール
                } else if (i > tongIndex) {
                    leftParts.render(renderer);//リードレール
                }
            } else//終点を共有
            {
                if (i == max - tongIndex) {
                    tongFR.render(renderer);//トングレール
                } else if (i < max - tongIndex) {
                    rightParts.render(renderer);//リードレール
                }
            }
        } else//dir == RailDir.RIGHT
        {
            if (par3)//始点を共有
            {
                if (i == tongIndex) {
                    tongBR.render(renderer);//トングレール
                } else if (i > tongIndex) {
                    rightParts.render(renderer);//リードレール
                }
            } else//終点を共有
            {
                if (i == max - tongIndex) {
                    tongFL.render(renderer);//トングレール
                } else if (i < max - tongIndex) {
                    leftParts.render(renderer);//リードレール
                }
            }
        }

        GL11.glPopMatrix();
    }
}

function sigmoid2(x) {
    var d0 = x * 3.5;
    var d1 = d0 / Math.sqrt(1.0 + d0 * d0);//シグモイド関数
    return d1 * 0.75 + 0.25;
}
