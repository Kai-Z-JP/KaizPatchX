var renderClass = "jp.ngt.rtm.render.RailPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.rail.util);
importPackage(Packages.jp.ngt.ngtlib.math);

//include <scripts/LibRenderRail.js>

TONG_MOVE = 0.35;
TONG_POS = 1.0 / 10.0;
HALF_GAUGE = 0.5647;
/**レール長で割る*/
YAW_RATE = 450.0;

patternGrass = "grass";

function init(par1, par2) {
    staticParts = renderer.registerParts(new Parts("base"));
    leftParts = renderer.registerParts(new Parts("railL", "sideL"));
    rightParts = renderer.registerParts(new Parts("railR", "sideR"));
    tongFL = renderer.registerParts(new Parts("L0"));
    tongBL = renderer.registerParts(new Parts("L1"));
    tongFR = renderer.registerParts(new Parts("R0"));
    tongBR = renderer.registerParts(new Parts("R1"));
    grass1 = renderer.registerParts(new Parts("grass1"));
    grass2 = renderer.registerParts(new Parts("grass2"));
    grass3 = renderer.registerParts(new Parts("grass3"));
    grass4 = renderer.registerParts(new Parts("grass4"));
    grass5 = renderer.registerParts(new Parts("grass5"));
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
    if (objName.indexOf(patternGrass) !== -1) {
        return (NGTMath.RANDOM.nextInt(4) == 0)
    } else if (renderer.isSwitchRail(tileEntity))//分岐レール
    {
        //可動部パーツは除外
        return staticParts.containsName(objName);
    } else {
        return staticParts.containsName(objName) || leftParts.containsName(objName) || rightParts.containsName(objName);
    }
}
