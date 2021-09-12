var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

function init(par1, par2) {
    body = renderer.registerParts(new Parts("body_main"));
    light = renderer.registerParts(new Parts("body_light"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    var move = 0.4375;

    if (entity == null) {
        GL11.glTranslatef(0.0, move, 0.0);
    } else {
        switch (entity.getDir()) {
            case 0:
                GL11.glTranslatef(0.0, move, 0.0);
                break;
            case 1:
                GL11.glTranslatef(0.0, 0.0, move);
                break;
            case 2:
                GL11.glTranslatef(0.0, -move, 0.0);
                break;
            case 3:
                GL11.glTranslatef(0.0, 0.0, -move);
                break;
            case 4:
                GL11.glTranslatef(0.0, move, 0.0);
                GL11.glRotatef(90.0, 0.0, 1.0, 0.0);
                break;
            case 5:
                GL11.glTranslatef(move, 0.0, 0.0);
                GL11.glRotatef(90.0, 0.0, 1.0, 0.0);
                break;
            case 6:
                GL11.glTranslatef(0.0, -move, 0.0);
                GL11.glRotatef(90.0, 0.0, 1.0, 0.0);
                break;
            case 7:
                GL11.glTranslatef(-move, 0.0, 0.0);
                GL11.glRotatef(90.0, 0.0, 1.0, 0.0);
                break;
        }
    }

    if (pass == 0) {
        body.render(renderer);
    } else if (pass == 2) {
        light.render(renderer);
    }

    GL11.glPopMatrix();
}
