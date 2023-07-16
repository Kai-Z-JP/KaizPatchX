var renderClass = "jp.ngt.rtm.render.VehiclePartsRenderer";
importPackage(Packages.org.lwjgl.opengl);
importPackage(Packages.jp.ngt.rtm.render);
importPackage(Packages.jp.ngt.rtm.entity.train.util);

function init(par1, par2) {
    head_F = renderer.registerParts(new Parts("head_F"));
    head_B = renderer.registerParts(new Parts("head_B"));
    body = renderer.registerParts(new Parts("ctrl_F", "body_D", "body_W",
        "th_door_F", "con_door_F", "yaneue",
        "seat1_1", "seat1_2", "seat1_3", "seat2", "amidana1", "amidana2", "turikawa1", "turikawa2", "yukasita_L", "yukasita_R", "skirt_F",
        "tenjo1", "tenjo2", "koukoku",
        "con_sw_box_RF", "con_sw_box_LF"));
    body_tc = renderer.registerParts(new Parts("ctrl_B", "th_door_B", "con_door_B",
        "yukasita_B", "skirt_B", "con_sw_box_RB", "con_sw_box_LB"));
    body_oc = renderer.registerParts(new Parts("body_B", "seat_B", "amidana_B", "turikawa_B",
        "tenjo_B", "koukoku_B", "watariita", "cover"));
    window = renderer.registerParts(new Parts("window_F", "window_S", "body_D", "th_door_F", "con_door_F"));
    window_tc = renderer.registerParts(new Parts("window_B", "th_door_B", "con_door_B"));
    window_oc = renderer.registerParts(new Parts("window_B2"));
    light = renderer.registerParts(new Parts("room_light"));
    light_oc = renderer.registerParts(new Parts("room_light_B"));
    door_LF = renderer.registerParts(new Parts("door_LF"));
    door_RF = renderer.registerParts(new Parts("door_RF"));
    door_LB = renderer.registerParts(new Parts("door_LB"));
    door_RB = renderer.registerParts(new Parts("door_RB"));
    panto_unf = renderer.registerParts(new Parts("panto_unf"));
    panto_unb = renderer.registerParts(new Parts("panto_unb"));
    panto_upf = renderer.registerParts(new Parts("panto_upf"));
    panto_upb = renderer.registerParts(new Parts("panto_upb"));
    panto_up = renderer.registerParts(new Parts("panto_up"));
    panto_waku = renderer.registerParts(new Parts("panto_waku"));

    //以下右クリック操作対象パーツ登録
    //ActionTypeはTOGGLE, DRAG_X, DRAG_Y
    mascon_F = renderer.registerParts(new ActionParts(ActionType.DRAG_X, "mascon_F"));
    mascon_B = renderer.registerParts(new ActionParts(ActionType.DRAG_X, "mascon_B"));
    brake_F = renderer.registerParts(new ActionParts(ActionType.DRAG_X, "brake_F"));
    brake_B = renderer.registerParts(new ActionParts(ActionType.DRAG_X, "brake_B"));
    con_sw_RF = renderer.registerParts(new ActionParts(ActionType.TOGGLE, "con_sw_RF"));
    con_sw_LF = renderer.registerParts(new ActionParts(ActionType.TOGGLE, "con_sw_LF"));
    con_sw_RB = renderer.registerParts(new ActionParts(ActionType.TOGGLE, "con_sw_RB"));
    con_sw_LB = renderer.registerParts(new ActionParts(ActionType.TOGGLE, "con_sw_LB"));

    //モデル名によって描画切替
    var name = par1.getConfig().getName();
    hasPantograph = (name === "ohmi220") || (name === "ohmi800m");
    hasOneCab = name.indexOf("ohmi800") >= 0;
}

function render(entity, pass, par3) {
    GL11.glPushMatrix();

    if (pass === RenderPass.NORMAL.id) {
        head_F.render(renderer);
        body.render(renderer);
        renderController(entity, pass, par3); //通常描画用
        renderConSw(entity, pass, par3);
        renderDoor(entity, pass, par3);

        if (hasOneCab) {
            body_oc.render(renderer);
        } else {
            head_B.render(renderer);
            body_tc.render(renderer);
        }

        if (hasPantograph) {
            renderPantograph(entity, pass, par3);
        }
    } else if (pass === RenderPass.TRANSPARENT.id) {
        window.render(renderer);
        renderDoor(entity, pass, par3);

        if (hasOneCab) {
            window_oc.render(renderer);
        } else {
            window_tc.render(renderer);
        }
    } else if (pass >= RenderPass.LIGHT.id && pass <= RenderPass.LIGHT_BACK.id) {
        if (pass === RenderPass.LIGHT.id) {
            light.render(renderer);
            if (hasOneCab) {
                light_oc.render(renderer);
            }
        }

        head_F.render(renderer);
        if (!hasOneCab) {
            head_B.render(renderer);
        }
        renderController(entity, pass, par3); //輪郭線描画用
        renderConSw(entity, pass, par3);
    } else if (pass === RenderPass.PICK.id) {
        renderController(entity, pass, par3); //右クリック操作判定用
        renderConSw(entity, pass, par3);
    }

    GL11.glPopMatrix();
}

function onRightClick(entity, parts) {
    var doorState = entity.getTrainStateData(TrainState.TrainStateType.State_Door.id);
    var dirForward = (entity.getTrainDirection() === 0);
    if (parts.equals(con_sw_RF) || parts.equals(con_sw_RB)) {
        doorState ^= (dirForward ? 1 : 2);
    } else if (parts.equals(con_sw_LF) || parts.equals(con_sw_LB)) {
        doorState ^= (dirForward ? 2 : 1)
    }
    entity.setTrainStateData(TrainState.TrainStateType.State_Door.id, doorState);
    entity.syncTrainStateData(TrainState.TrainStateType.State_Door.id, doorState);
}

//move:右クリック開始時を0としたマウスの相対移動量
function onRightDrag(entity, parts, move) {
    var notch = entity.getNotch();
    var dataMap = entity.getResourceState().getDataMap();
    if (move === 0) {
        dataMap.setInt("start_notch", notch, 0);
        return;
    }
    var startNotch = dataMap.getInt("start_notch");
    var newNotch = startNotch + Math.floor(-move / 20); //マウス動き反転 & 20pxlごとに1ノッチ変更
    if (parts.equals(mascon_F) || parts.equals(mascon_B)) {
        newNotch = newNotch < 0 ? 0 : (newNotch > 5 ? 5 : newNotch);
    } else if (parts.equals(brake_F) || parts.equals(brake_B)) {
        newNotch = newNotch < -8 ? -8 : (newNotch > 0 ? 0 : newNotch);
    }
    entity.syncNotch(newNotch - notch);
}

function renderController(entity, pass, par3) {
    var rotationMF = 0.0;
    var rotationMB = 0.0;
    var rotationBF = 0.0;
    var rotationBB = 0.0;
    if (entity != null && entity.isControlCar()) {
        var dirForward = (entity.getCabDirection() === 0);
        var notch = entity.getNotch();
        var notchM = (notch < 0 ? 0 : notch) / 5;
        var notchB = ((notch > 0 ? 0 : notch) + 8) / 8;
        rotationMF = (dirForward ? (notchM * -126.0) : 0.0) + 90.0;
        rotationMB = (!dirForward ? (notchM * -126.0) : 0.0) + 90.0;
        rotationBF = dirForward ? (notchB * -47.0) : 0.0;
        rotationBB = !dirForward ? (notchB * -47.0) : 0.0;
    }

    GL11.glPushMatrix();
    GL11.glTranslatef(1.2, 0.0, 8.85);
    GL11.glRotatef(rotationMF, 0.0, 1.0, 0.0);
    GL11.glTranslatef(-1.2, 0.0, -8.85);
    mascon_F.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.7318, 0.0, 8.9228);
    GL11.glRotatef(rotationBF, 0.0, 1.0, 0.0);
    GL11.glTranslatef(-0.7318, 0.0, -8.9228);
    brake_F.render(renderer);
    GL11.glPopMatrix();

    if (!hasOneCab) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-1.2, 0.0, -8.85);
        GL11.glRotatef(rotationMB, 0.0, 1.0, 0.0);
        GL11.glTranslatef(1.2, 0.0, 8.85);
        mascon_B.render(renderer);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.7318, 0.0, -8.9228);
        GL11.glRotatef(rotationBB, 0.0, 1.0, 0.0);
        GL11.glTranslatef(0.7318, 0.0, 8.9228);
        brake_B.render(renderer);
        GL11.glPopMatrix();
    }
}

function renderConSw(entity, pass, par3) {
    var stateRF = 0;
    var stateLF = 0;
    var stateRB = 0;
    var stateLB = 0;
    if (entity != null) {
        var doorState = entity.getTrainStateData(TrainState.TrainStateType.State_Door.id);
        var dirForward = (entity.getTrainDirection() === 0);
        var doorROpen = (doorState & 1) === 1;
        var doorLOpen = (doorState & 2) === 2;
        stateRF = (dirForward && doorROpen) ? 1 : 0;
        stateLF = (dirForward && doorLOpen) ? 1 : 0;
        stateRB = (!dirForward && doorROpen) ? 1 : 0;
        stateLB = (!dirForward && doorLOpen) ? 1 : 0;
    }

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.03 * stateRF, 0.0);
    con_sw_RF.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.03 * stateLF, 0.0);
    con_sw_LF.render(renderer);
    GL11.glPopMatrix();

    if (!hasOneCab) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 0.03 * stateRB, 0.0);
        con_sw_RB.render(renderer);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 0.03 * stateLB, 0.0);
        con_sw_LB.render(renderer);
        GL11.glPopMatrix();
    }
}

function renderDoor(entity, pass, par3) {
    var moveL = (entity == null ? 0.0 : renderer.sigmoid(renderer.getDoorMovementL(entity)));
    var moveR = (entity == null ? 0.0 : renderer.sigmoid(renderer.getDoorMovementR(entity)));

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, 0.72 * moveL);
    door_LF.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, -0.72 * moveL);
    door_LB.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, 0.72 * moveR);
    door_RF.render(renderer);
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 0.0, -0.72 * moveR);
    door_RB.render(renderer);
    GL11.glPopMatrix();
}

function renderPantograph(entity, pass, par3) {
    var move = (entity == null ? 0.0 : renderer.getPantographMovementFront(entity));

    panto_waku.render(renderer);

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 3.14, 7.13);
    GL11.glRotatef(36.0 * move, 1.0, 0.0, 0.0);
    GL11.glTranslatef(-0.0, -3.14, -7.13);
    panto_unf.render(renderer);
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 3.966, 8.13);
        GL11.glRotatef(-60.0 * move, 1.0, 0.0, 0.0);
        GL11.glTranslatef(-0.0, -3.966, -8.13);
        panto_upf.render(renderer);
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0, 5.01, 6.435);
            GL11.glRotatef(24.0 * move, 1.0, 0.0, 0.0);
            GL11.glTranslatef(-0.0, -5.01, -6.435);
            panto_up.render(renderer);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }
    GL11.glPopMatrix();

    GL11.glPushMatrix();
    GL11.glTranslatef(0.0, 3.14, 5.67);
    GL11.glRotatef(-36.0 * move, 1.0, 0.0, 0.0);
    GL11.glTranslatef(-0.0, -3.14, -5.67);
    panto_unb.render(renderer);
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0, 3.966, 4.67);
        GL11.glRotatef(60.0 * move, 1.0, 0.0, 0.0);
        GL11.glTranslatef(-0.0, -3.966, -4.67);
        panto_upb.render(renderer);
        GL11.glPopMatrix();
    }
    GL11.glPopMatrix();
}