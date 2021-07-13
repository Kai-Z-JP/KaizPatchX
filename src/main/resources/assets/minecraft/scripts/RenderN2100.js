var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.ngtlib.renderer);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.entity.train.util);

function init(par1, par2) {
    var name = par1.getConfig().getName();
    isCtrlCar = (name.indexOf("N2100") >= 0);

    body = renderer.registerParts(new Parts("m_car", "m_panel_L", "m_panel_R"));

    if (isCtrlCar) {
        head = renderer.registerParts(new Parts("m_car_C"));
        light_h_off = renderer.registerParts(new Parts("light_h_off"));
        light_h_on = renderer.registerParts(new Parts("light_h_on"));
        light_t_off = renderer.registerParts(new Parts("light_t_off"));
        light_t_on = renderer.registerParts(new Parts("light_t_on"));
    } else {
        head = renderer.registerParts(new Parts("m_car_T"));
    }

    door_bl = renderer.registerParts(new Parts("door_LB"));
    door_br = renderer.registerParts(new Parts("door_RB"));
    door_fl = renderer.registerParts(new Parts("door_LF"));
    door_fr = renderer.registerParts(new Parts("door_RF"));
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass == RenderPass.NORMAL.id) {
        body.render(renderer);
        head.render(renderer);
        renderDoor(entity, pass, par3);
        if (isCtrlCar) {
            renderHeadLight(entity, pass, par3);
        }
    } else if (pass == RenderPass.TRANSPARENT.id) {
        body.render(renderer);
        head.render(renderer);
        renderDoor(entity, pass, par3);
    } else if (pass >= RenderPass.LIGHT.id && pass <= RenderPass.LIGHT_BACK.id) {
        if (pass == RenderPass.LIGHT.id) {
            body.render(renderer);
            head.render(renderer);
            if (isCtrlCar) {
                renderHeadLight(entity, pass, par3);
            }
        }
    }

    GL11.glPopMatrix();
}

function renderDoor(entity, pass, par3) {
    var moveL = (entity == null ? 0.0 : renderer.sigmoid(renderer.getDoorMovementL(entity)));
    var moveR = (entity == null ? 0.0 : renderer.sigmoid(renderer.getDoorMovementR(entity)));
    var size = 0.68;

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, size * moveL);
    door_fl.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, size * -moveL);
    door_bl.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, size * moveR);
    door_fr.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, size * -moveR);
    door_br.render(renderer);
    GL11.glPopMatrix();
}

function renderHeadLight(entity, pass, par3) {
    var headLight = false;
    var tailLight = false;

    if (entity != null) {
        mode = entity.getTrainStateData(TrainState.TrainStateType.State_Light);////0:消灯,1:前照灯,2:尾灯

        if (mode > 0) {
            dir = entity.getTrainDirection();
            emptyF = entity.getConnectedTrain(dir) == null;//front空き
            emptyB = entity.getConnectedTrain(1 - dir) == null;//back空き

            headLight = ((mode == 1) && (dir == 0) && emptyF) || ((mode == 2) && (dir == 1) && emptyB);
            tailLight = ((mode == 2) && (dir == 0) && emptyB) || ((mode == 1) && (dir == 1) && emptyF);
        }
    }

    if (headLight) {
        light_h_on.render(renderer);
    } else {
        light_h_off.render(renderer);
    }

    if (tailLight) {
        light_t_on.render(renderer);
    } else {
        light_t_off.render(renderer);
    }
}
