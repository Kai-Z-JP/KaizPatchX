package jp.ngt.rtm.modelpack;

import jp.ngt.rtm.RTMCore;
import net.minecraft.crash.CrashReport;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class CrashableFutureTask<T> extends FutureTask<T> {
    public CrashableFutureTask(@NotNull Callable<T> callable) {
        super(callable);
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);

        CrashReport crashReport = CrashReport.makeCrashReport(t, "Loading RTM ModelPack");
        crashReport.makeCategory("Initialization");
        RTMCore.proxy.reportCrash(crashReport);
    }
}
