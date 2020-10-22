function onUpdate(su) {
    var speed = su.getSpeed();
    var notch = su.getNotch();
    var maxSp = 130.0;

    if (speed > 0.0) {
        var vol = 1.0;
        var s0 = 20;
        if (speed < s0) {
            vol = (-((speed - s0) * (speed - s0)) + 10) / 10.0;
        }
        var pitch = (speed / maxSp) * 2.5 - 0.5;
        su.playSound('rtm', 'train.run_trailer', vol, pitch);
    } else {
        su.stopSound('rtm', 'train.run_trailer');
    }
}
