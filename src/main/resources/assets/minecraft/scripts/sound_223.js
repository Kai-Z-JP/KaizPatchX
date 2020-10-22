function onUpdate(su) {

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
        su.playSound('rtm', 'train.313_cp', vol, pitch);
    } else {
        su.stopSound('rtm', 'train.313_cp');
    }

    var notch = su.getNotch();
    var speed = su.getSpeed();
    var maxSp = 120;

    if (notch == 0) {
        su.stopSound('rtm', 'train.223_s0');
        su.stopSound('rtm', 'train.223_s1');
        su.stopSound('rtm', 'train.223_s2');
    } else {
        if (speed > 0.0) {
            su.stopSound('rtm', 'train.223_air');

            if (speed < 20.0) {
                var vol = 1.0;
                if (speed < 5.0) {
                    vol = speed / 5.0;
                } else if (speed > 10.0) {
                    vol = (20.0 - speed) / 10.0;
                }
                su.playSound('rtm', 'train.223_s0', vol, 1.0);
            } else {
                su.stopSound('rtm', 'train.223_s0');
            }

            var pitch = 1.0;

            if (speed >= 8.0) {
                var vol = 1.0;
                if (speed < 12.0) {
                    vol = (speed - 8.0) / 4.0;
                }
                pitch = (speed - 8.0) / (maxSp - 8.0) + 0.8;
                su.playSound('rtm', 'train.223_s1', vol, pitch);
            } else {
                su.stopSound('rtm', 'train.223_s1');
            }

            if (speed >= 12.0) {
                pitch = (speed - 12.0) / (maxSp - 12.0) + 0.9;
                su.playSound('rtm', 'train.223_s2', 2.0, pitch);

                var vol = (speed - 12.0) / (maxSp - 12.0);
                if (su.inTunnel()) {
                    su.stopSound('rtm', 'train.223_run');
                    su.playSound('rtm', 'train.223_run_tunnel', vol, 1.0);
                } else {
                    su.stopSound('rtm', 'train.223_run_tunnel');
                    su.playSound('rtm', 'train.223_run', vol, 1.0);
                }
            } else {
                su.stopSound('rtm', 'train.223_s2');
                su.stopSound('rtm', 'train.223_run_tunnel');
                su.stopSound('rtm', 'train.223_run');
            }
        } else {
            su.stopSound('rtm', 'train.223_s0');
            su.playSound('rtm', 'train.223_air', 1.0, 1.0);
        }
    }
}
