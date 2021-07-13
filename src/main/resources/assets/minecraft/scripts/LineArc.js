var len0;
var len1;
var r;
var yaw;
var cant
var endH;

var lenC;
var lenTotal;

//Required
function getDefaultArgs() {
    return "len0=10.0,len1=10.0,r=40.0,yaw=90.0,cant=15.0,endH=0.0";
}

//Required
function getLength() {
    if (lenC <= 0.0) {
        lenC = 2.0 * Math.PI * r * (Math.abs(yaw) / 360.0);
    }

    if (lenTotal <= 0.0) {
        var lenXZ = len0 + len1 + lenC;
        var lenY = endH;
        lenTotal = Math.sqrt(lenXZ * lenXZ + lenY * lenY);
    }

    return lenTotal;
}

//Required
function getNearlestPoint(split, x, z) {

}

//Required
function getPos(split, index) {
    var pos = [0.0, 0.0];//z,x

    var t = getStepLength(split, index);
    var sid = getSectionId(t);
    if (sid == 0) {
        pos[0] = t;
    } else if (sid == 1) {
        var angle = yaw * (t - len0) / lenC;
        pos = getCurvePos(angle);
        pos[0] += len0;
    } else {
        pos = getCurvePos(yaw);
        pos[0] += len0;

        var t2 = t - (len0 + lenC);
        pos[0] += Math.cos(yaw) * t2;
        pos[1] += Math.sin(yaw) * t2;
    }

    return pos;
}

function getCurvePos(angle) {
    var pos = [0.0, 0.0];
    if (angle > 0.0) {
        pos[0] = Math.sin(angle);
        pos[1] = Math.cos(angle) + r;
    } else if (angle < 0.0) {
        angle += 180.0;
        pos[0] = Math.sin(angle);
        pos[1] = Math.cos(angle) - r;
    }
    return pos;
}

//Required
function getHeight(split, index) {
    return endH * index / split;
}

//Required
function getYaw(split, index) {
    var t = getStepLength(split, index);
    var sid = getSectionId(t);
    if (sid == 0) {
        return 0.0;
    } else if (sid == 1) {
        var t1 = t - len0;
        return yaw * t1 / lenC;
    } else {
        return yaw;
    }
}

//Required
function getPitch(split, index) {
    return Math.atan2(endH, len0 + len1 + lenC);
}

//Required
function getRoll(split, index) {
    var t = getStepLength(split, index);
    var sid = getSectionId(t);
    if (sid == 0) {
        return cant * t / len0;
    } else if (sid == 1) {
        return cant;
    } else {
        var t1 = t - (len0 + lenC);
        return cant * (1.0 - (t1 / len1));
    }
}

/*=========================================*/

function getStepLength(split, index) {
    return (len0 + len1 + lenC) * index / split;
}

function getSectionId(t) {

    if (t < len0)//始点～カーブ
    {
        return 0;
    } else if (t < len0 + lenC)//カーブ
    {
        return 1;
    } else//カーブ～終点
    {
        return 2;
    }
}
