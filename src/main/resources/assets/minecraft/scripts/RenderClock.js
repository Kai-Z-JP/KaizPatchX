var renderClass = "jp.ngt.rtm.render.SignalPartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);

var useMCTime;

function init(par1, par2) {
    body = renderer.registerParts(new Parts("body", "scale1", "scale2"));
    hand_h = renderer.registerParts(new Parts("hand_h"));
    hand_m = renderer.registerParts(new Parts("hand_m"));

    useMCTime = (renderer.getModelName().equals("Clock01MC"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass === 0) {
        if (renderer.isOpaqueCube(entity)) {
            GL11.glTranslatef(0.0, 0.0, 0.25);
            renderClock(entity);
        } else {
            renderClock(entity);
            GL11.glRotatef(180.0, 0.0, 1.0, 0.0);
            renderClock(entity);
        }
    }

    GL11.glPopMatrix();
}

function renderClock(entity) {
    body.render(renderer);

    var minute = (useMCTime ? renderer.getMCMinute() : renderer.getSystemMinute()) * -6.0;
    var hour = (useMCTime ? renderer.getMCHour() : renderer.getSystemHour()) * -30.0 + (minute / 12.0);

    GL11.glPushMatrix();
    renderer.rotate(hour, 'Z', 0.0, 0.5, 0.0);
    hand_h.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    renderer.rotate(minute, 'Z', 0.0, 0.5, 0.0);
    hand_m.render(renderer);
    GL11.glPopMatrix();
}
