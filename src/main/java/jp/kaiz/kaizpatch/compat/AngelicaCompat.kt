package jp.kaiz.kaizpatch.compat

import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Angelica互換性レイヤー
 * GLRedirects APIを使用してスクリプト内のGL呼び出しをGLStateManagerにリダイレクトする
 */
object AngelicaCompat {
    private const val GL_REDIRECTS_CLASS = "com.gtnewhorizons.angelica.api.GLRedirects"
    private const val GLSM_PACKAGE = "com.gtnewhorizons.angelica.glsm"
    private const val IMPORT_STATEMENT = "importPackage(Packages.$GLSM_PACKAGE);\n"

    private var redirectPattern: Pattern? = null
    private var getTargetMethod: Method? = null

    @JvmStatic
    val isAvailable: Boolean
        get() = redirectPattern != null && getTargetMethod != null


    @JvmStatic
    fun init() {
        if (isAvailable) return

        runCatching {
            val clazz = Class.forName(GL_REDIRECTS_CLASS)
            redirectPattern = clazz.getMethod("getMethodRedirectPattern").invoke(null) as Pattern
            getTargetMethod = clazz.getMethod("getTargetMethodName", String::class.java)
        }.onSuccess {
            println("[KaizPatchX] Angelica GLRedirects API detected, script GL redirects enabled")
        }.onFailure { e ->
            if (e !is ClassNotFoundException) {
                System.err.println("[KaizPatchX] Failed to initialize Angelica GLRedirects API")
                e.printStackTrace()
            }
        }
    }

    /**
     * スクリプト内のGL呼び出しをGLStateManagerにリダイレクトする
     */
    @JvmStatic
    fun transformScript(script: String): String {
        val pattern = redirectPattern ?: return script
        val method = getTargetMethod ?: return script

        return runCatching {
            val result = StringBuffer()
            val matcher = pattern.matcher(script)
            var replaceCount = 0

            while (matcher.find()) {
                val targetMethodName = method.invoke(null, matcher.group()) as? String ?: continue
                matcher.appendReplacement(result, Matcher.quoteReplacement("GLStateManager.$targetMethodName"))
                replaceCount++
            }
            matcher.appendTail(result)

            if (replaceCount > 0) {
                IMPORT_STATEMENT + result.toString()
            } else {
                script
            }
        }.getOrElse { e ->
            System.err.println("[KaizPatchX] Failed to transform script for Angelica compatibility")
            e.printStackTrace()
            script
        }
    }
}
