function onUpdate(selector, executer) {

}

function onTrainPass(transponder, train, executer) {
    var dataMap = transponder.getResourceState().getDataMap();
    if (dataMap.getBoolean("facingDirectionOnly") && !transponder.isTrainFacingDirection(train)) {
        return;
    }

    var formation = train.getFormation();
    if (dataMap.getBoolean("frontCarOnly") && formation != null && !formation.isFrontCar(train)) {
        return;
    }

    var signalLevel = dataMap.getInt("signalLevel");
    train.setSignal2(signalLevel);
}
