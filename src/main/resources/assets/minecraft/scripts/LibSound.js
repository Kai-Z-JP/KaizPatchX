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