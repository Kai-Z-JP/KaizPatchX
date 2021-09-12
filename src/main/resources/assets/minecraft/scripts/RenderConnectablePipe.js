var renderClass = "jp.ngt.rtm.render.OrnamentPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.block);

function init(par1, par2) {
    main = renderer.registerParts(new Parts("partMain"));
    xp = renderer.registerParts(new Parts("partXP"));
    xn = renderer.registerParts(new Parts("partXN"));
    yp = renderer.registerParts(new Parts("partYP"));
    yn = renderer.registerParts(new Parts("partYN"));
    zp = renderer.registerParts(new Parts("partZP"));
    zn = renderer.registerParts(new Parts("partZN"));
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
            NGTRenderHelper.setColor(color);
            var x = entity.getX();
            var y = entity.getY();
            var z = entity.getZ();
            var world = renderer.getWorld(entity);

            var side = entity.getAttachedSide();
            var conXP = entity.isConnected(5);
            var conXN = entity.isConnected(4);
            var conYP = entity.isConnected(1);
            var conYN = entity.isConnected(0);
            var conZP = entity.isConnected(3);
            var conZN = entity.isConnected(2);
            var noCon = !conXP && !conXN && !conZP && !conZN;
            var straightCon = (conXP && conXN) || (conYP && conYN) || (conZP && conZN);

            if (conXP || side == 4) {
                xp.render(renderer);
            }

            if (conXN || side == 5) {
                xn.render(renderer);
            }

            if (conYP || side == 0) {
                yp.render(renderer);
            }

            if (conYN || side == 1) {
                yn.render(renderer);
            }

            if (conZP || side == 2) {
                zp.render(renderer);
            }

            if (conZN || side == 3) {
                zn.render(renderer);
            }

            if (noCon || !straightCon) {
                main.render(renderer);
            }
            NGTRenderHelper.setColor(0xFFFFFF);
        } else {
            yp.render(renderer);
            yn.render(renderer);
            main.render(renderer);
        }
    }

    GL11.glPopMatrix();
}
