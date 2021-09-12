var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

var CYATHEA = 0;//ヘゴ
var CYCAS = 1;//ソテツ
var PHOENIX = 2;//カナリーヤシ

function init(par1, par2) {
    leaf = renderer.registerParts(new Parts("leaf"));
    stem = renderer.registerParts(new ActionParts(ActionType.DRAG_Y, "stem"));

    var name = par1.getConfig().getName();
    plantType = (name.indexOf("Cyathea") >= 0) ? CYATHEA : ((name.indexOf("Cycas") >= 0) ? CYCAS : PHOENIX);
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    var scale1 = 1.0;
    var scale2 = 1.0;
    if (entity != null) {
        scale1 = entity.getRandomScale();
        scale2 = entity.getResourceState().getDataMap().getDouble("scale");
        if (scale2 <= 0.0) {
            scale2 = 1.0;
        }
    }
    scale1 *= scale2

    if (pass == RenderPass.NORMAL.id) {
        renderStem(entity, scale1);
        if (plantType != CYATHEA) {
            renderLeaf(entity, scale1);
        }
    } else if (pass == RenderPass.TRANSPARENT.id) {
        if (plantType == CYATHEA) {
            renderLeaf(entity, scale1);//シダの葉のみアルファブレンド
        }
    } else if (pass == RenderPass.OUTLINE.id) {
        renderStem(entity, scale1);
    } else if (pass == RenderPass.PICK.id) {
        renderStem(entity, scale1);
    }

    GL11.glPopMatrix();
}

//move:右クリック開始時を0としたマウスの相対移動量
function onRightDrag(entity, parts, move) {
    var dataMap = entity.getResourceState().getDataMap();//上+下-
    var scale = dataMap.getDouble("scale");
    var oldScale = scale
    /*if(scale <= 0.0)
    {
        scale = 1.0;
    }*/

    scale = Math.pow(1.25, move / 60.0);

    if (scale != oldScale) {
        dataMap.setDouble("scale", scale, 3);
    }
}

function renderStem(entity, scale) {
    GL11.glPushMatrix();
    var scY = (plantType == CYATHEA || plantType == PHOENIX) ? 2.0 : 1.0;
    GL11.glScalef(scale, scale * scY, scale);
    stem.render(renderer);
    GL11.glPopMatrix();
}

function renderLeaf(entity, scale) {
    var count = 25;
    var rotation = (360.0 / 5) + 5;
    var scY = (plantType == CYATHEA || plantType == PHOENIX) ? 2.0 : 1.0;
    var leafPos = (plantType == CYATHEA || plantType == CYCAS) ? 0.5 : 0.6;//上からXmまで葉生やす
    var yDec = (leafPos / count) * scale;
    var stemThickness = 0.25;
    var leafRotation = (plantType == CYCAS) ? 70.0 : 100.0;

    var leafScale1 = (plantType == CYATHEA || plantType == CYCAS) ? 1.25 : 1.25;
    var leafScale2 = (plantType == CYATHEA || plantType == CYCAS) ? 0.75 : 0.75;

    var leafScale1 = plantType == CYATHEA ? 1.0 : (plantType == CYCAS ? 0.75 : 1.25);
    var leafScale2 = plantType == CYATHEA ? 1.0 : (plantType == CYCAS ? 0.75 : 0.75);

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 5.0 * scY * scale, 0.0);
    for (var i = 0; i < count; ++i) {
        var f0 = i / (count - 1);
        GL11.glPushMatrix();
        GL11.glRotatef(leafRotation * f0, 1.0, 0.0, 0.0);
        var s2 = (leafScale1 + leafScale2 * f0) * scale;//葉x2
        GL11.glScalef(s2, s2, s2);
        GL11.glTranslatef(0.0, stemThickness * f0 * scale * 0.5, 0.0);
        leaf.render(renderer);
        GL11.glPopMatrix();

        GL11.glRotatef(rotation, 0.0, 1.0, 0.0);
        GL11.glTranslatef(0.0, -yDec, 0.0);
    }
    GL11.glPopMatrix();
}
