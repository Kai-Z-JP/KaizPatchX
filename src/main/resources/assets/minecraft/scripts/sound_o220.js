function onUpdate(su) {

    playComplessorSound(su, "rtm", "train.cp_A");

    var speed = su.getSpeed();
    var notch = su.getNotch();

    if (notch == 0) {
        su.stopSound("rtm", "train.tsurikake");
        su.stopSound("rtm", "train.tsurikake_x2");

        if (speed > 0) {
            var vol = 1.0;
            if (speed > 10) {
                vol = (speed / 62) * 0.5 + 0.5;
            } else {
                vol = (speed / 10) * 0.5;
            }
            var pitch3 = (speed / 72) * 0.25 + 1.0;
            su.playSound("rtm", "train.tsurikake_n", vol, pitch3);
        } else {
            su.stopSound("rtm", "train.tsurikake_n");
        }
    } else {
        if (speed > 0.0) {
            su.stopSound("rtm", "train.223_air");

            var vol = 1.0;
            if (speed < 10) {
                vol = (speed / 10);
            }

            if (speed >= 36) {
                su.stopSound("rtm", "train.tsurikake");
                var pitch0 = sigmoid((speed - 36) / 36);
                su.playSound("rtm", "train.tsurikake_x2", vol, pitch0);
            } else {
                su.stopSound("rtm", "train.tsurikake_x2");
                var pitch1 = sigmoid(speed / 36);
                su.playSound("rtm", "train.tsurikake", vol, pitch1);
            }

            vol = (speed / 72);
            var pitch3 = vol * 0.25 + 1.0;
            su.playSound("rtm", "train.tsurikake_n", vol, pitch3);

            if (speed >= 12.0) {
                var vol = (speed - 12.0) / (72.0 - 12.0);
                if (su.inTunnel()) {
                    su.playSound("rtm", "train.223_run_tunnel", vol, 1.0);
                } else {
                    su.stopSound("rtm", "train.223_run_tunnel");
                }
            } else {
                su.stopSound("rtm", "train.223_run_tunnel");
            }
        } else {
            su.playSound("rtm", "train.223_air", 1.0, 1.0);
            su.stopSound("rtm", "train.tsurikake");
            su.stopSound("rtm", "train.tsurikake_x2");
            su.stopSound("rtm", "train.tsurikake_n");
        }
    }
}

function sigmoid(p1) {
    return p1 + 1.0;
}

function playComplessorSound(su, soundDomain, soundName) {
    if (su.isComplessorActive()) {
        var count = su.complessorCount();
        var c0 = 50;
        var vol = 1.0;
        if (count < c0) {
            var c1 = c0 * c0;
            vol = -(((count - c0) * (count - c0)) + c1) / c1;
        }
        var pitch = 1.0;
        if (count < c0) {
            pitch = (vol * 0.5) + 0.5;
        }
        su.playSound(soundDomain, soundName, vol, pitch);
    } else {
        su.stopSound(soundDomain, soundName);
    }
}