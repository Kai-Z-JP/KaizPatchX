var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

var isAuto;

function init(par1, par2) {
    main = renderer.registerParts(new Parts("body1", "body2"));
    body_parts = renderer.registerParts(new Parts("body3"));
    motor = renderer.registerParts(new Parts("motor"));
    lever = renderer.registerParts(new Parts("lever"));

    isAuto = (renderer.getModelName().equals("Point01A"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass === 0) {
        main.render(renderer);

        GL11.glPushMatrix();
        var state = renderer.getLodState(entity);
        if (state < 0) {
            GL11.glTranslatef(2.75, 0.0, 0.0);
        }
        body_parts.render(renderer);
        GL11.glPopMatrix();

        if (isAuto) {
            motor.render(renderer);
        } else {
            var move = (renderer.getMovingCount(entity) * 60.0) - 30.0;
            renderer.rotate(move, 'X', 0.0, 0.0, 0.0);
            lever.render(renderer);
        }
    }

    GL11.glPopMatrix();
}
