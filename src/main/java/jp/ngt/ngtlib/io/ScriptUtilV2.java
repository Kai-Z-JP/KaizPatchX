package jp.ngt.ngtlib.io;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import org.graalvm.polyglot.*;

import javax.script.ScriptException;
import java.io.IOException;

public final class ScriptUtilV2 {
    private static final Engine engine = Engine.newBuilder()
            .option("engine.WarnInterpreterOnly", "false")
            .build();

    private static final HostAccess hostAccess = HostAccess.newBuilder(HostAccess.ALL)
            .targetTypeMapping(Value.class, Float.class, Value::isNumber, value -> (float) value.asDouble())
            .targetTypeMapping(Value.class, Integer.class, Value::isNumber, value -> (int) value.asDouble())
            .build();

    private static Context createContext() {
        return Context.newBuilder()
                .allowExperimentalOptions(true)
                .allowHostAccess(hostAccess)
                .allowHostClassLoading(true)
                .allowHostClassLookup(name -> name.contains("."))
                .allowIO(true)
                .engine(engine)
                .hostClassLoader(Launch.classLoader)
                .option("js.nashorn-compat", "true")
                .build();
    }

    /**
     * スクリプトを評価し、Context を返す
     *
     * @param script JavaScript コード
     * @return スクリプト実行済みの Context
     */
    public static Context doScript(String script) {
        return doScript(script, "<eval>");
    }

    /**
     * スクリプトを評価し、Context を返す
     *
     * @param script JavaScript コード
     * @return スクリプト実行済みの Context
     */
    public static Context doScript(String script, String filename) {
        Context ctx = createContext();
        try {
            ctx.eval("js", "load(\"nashorn:mozilla_compat.js\");");
            Source src = Source.newBuilder("js", script, filename).build();
            ctx.eval(src);
            return ctx;
        } catch (PolyglotException pe) {
            throw new RuntimeException(buildStacktrace(pe), pe);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ResourceLocation からスクリプトを読み込んで評価し、Context を返す
     *
     * @param resource スクリプトリソース
     * @return スクリプト実行済みの Context
     */
    public static Context doScript(ResourceLocation resource) {
        try {
            String script = NGTText.getText(resource, true);
            return doScript(script);
        } catch (IOException e) {
            throw new RuntimeException("Script load error: " + resource.getResourcePath());
        }
    }

    /**
     * コンテキスト内で定義された関数を呼び出す
     *
     * @param ctx  作成済みの Context
     * @param func 呼び出す JavaScript 関数名
     * @param args 引数
     * @return 呼び出し結果
     */
    public static Object doScriptFunction(Context ctx, String func, Object... args) {
        try {
            Value function = ctx.getBindings("js").getMember(func);
            if (function == null || !function.canExecute()) {
                throw new ScriptException("Function not found: " + func);
            }
            return function.execute(args).as(Object.class);
        } catch (PolyglotException pe) {
            throw new RuntimeException(buildStacktrace(pe), pe);
        } catch (Exception e) {
            throw new RuntimeException("Invoke error: " + e.getMessage());
        }
    }

    /**
     * 関数呼び出しをエラー無視で行う
     */
    public static Object doScriptIgnoreError(Context ctx, String func, Object... args) {
        try {
            return doScriptFunction(ctx, func, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * グローバル変数を取得
     */
    public static Object getScriptField(Context ctx, String fieldName) {
        Value val = ctx.getBindings("js").getMember(fieldName);
        return val != null ? val.as(Object.class) : null;
    }

    private static String buildStacktrace(PolyglotException pe) {
        StringBuilder msg = new StringBuilder();
        SourceSection loc = pe.getSourceLocation();

        if (loc != null) {
            msg.append(String.format("Error at %s:%d%n",
                    loc.getSource().getName(),
                    loc.getStartLine()));
            msg.append(String.format("  > %s%n", pe.getMessage()));

            String allSrc = loc.getSource().getCharacters().toString();
            String[] lines = allSrc.split("\\r?\\n");
            int errLine = loc.getStartLine();
            if (errLine >= 1 && errLine <= lines.length) {
                String code = lines[errLine - 1];
                msg.append("    ").append(code).append("\n");

                int col = loc.getStartColumn();
                StringBuilder pointer = new StringBuilder("    ");
                for (int i = 1; i < col; i++) {
                    pointer.append(' ');
                }
                pointer.append("^\n");
                msg.append(pointer);
            }
        } else {
            msg.append("Error:\n")
                    .append("  > ")
                    .append(pe.getMessage())
                    .append("\n");
        }

        Value guest = pe.getGuestObject();
        if (guest != null && guest.hasMember("stack")) {
            msg.append("JS Stack:\n")
                    .append(guest.getMember("stack").asString())
                    .append("\n");
        }

        return msg.toString();
    }
}
