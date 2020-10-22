var renderClass = "jp.ngt.rtm.render.RailPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    allParts = renderer.registerParts(new Parts("makuragi", "railL", "sideL", "railR", "sideR", "obj1", "obj2", "obj3", "obj4"));
}

function renderRailStatic(tileEntity, posX, posY, posZ, par8, pass) {
    renderer.renderStaticParts(tileEntity, posX, posY, posZ);
}

function renderRailDynamic(tileEntity, posX, posY, posZ, par8, pass) {
}

function shouldRenderObject(tileEntity, objName, len, pos) {
    if ((pos % 4 != 0) && (objName === "obj1" || objName === "obj2" || objName === "obj3")) {
        return false;
    }
    return true;
}
