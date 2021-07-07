package jp.ngt.rtm.command;


import cpw.mods.fml.common.event.FMLServerStartingEvent;

public final class RTMCommand {
    public static void init(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRTM());
        event.registerServerCommand(new CommandTRec());
        event.registerServerCommand(new CommandMCtrl());
    }
}