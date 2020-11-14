var renderClass = "jp.ngt.rtm.render.MachinePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

var turnR;

function init(par1, par2) {
    main = renderer.registerParts(new Parts('pole', 'light_a', 'light_b', 'body1', 'body2', 'body3', 'dirB', 'body4', 'base', 'dirR', 'dirL'));
    lightL = renderer.registerParts(new Parts('light_L', 'dirR'));
    lightR = renderer.registerParts(new Parts('light_R', 'dirL'));
    bar = renderer.registerParts(new Parts('bar0', 'bar1'));

    turnR = (renderer.getModelName().equals("CrossingGate01R"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    var light = renderer.getLightState(entity);

    if (pass === 0) {
        main.render(renderer);

        GL11.glPushMatrix();
        var move = renderer.getMovingCount(entity) * (turnR ? 90.0 : -90.0);
        renderer.rotate(move, 'Z', 0.0, 0.5337, -0.24);
        bar.render(renderer);
        GL11.glPopMatrix();

        switch (light) {
            case 0:
                lightL.render(renderer);
                break;
            case 1:
                lightR.render(renderer);
                break;
        }
    } else if (pass === 2)//発光
    {
        switch (light) {
            case 0:
                lightR.render(renderer);
                break;
            case 1:
                lightL.render(renderer);
                break;
        }
    }

    GL11.glPopMatrix();
}
