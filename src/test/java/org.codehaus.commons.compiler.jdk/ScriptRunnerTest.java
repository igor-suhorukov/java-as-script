package org.codehaus.commons.compiler.jdk;

import com.github.igorsuhorukov.codehaus.plexus.util.IOUtil;
import org.github.suhorukov.java.as.script.ScriptRunner;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ScriptRunnerTest {


    private static String valueFromScript;

    @Test
    public void testClassloader() throws Exception {
        valueFromScript = null;
        String scriptText = IOUtil.toString(ScriptRunnerTest.class.getResourceAsStream("/SimpleScriptWithDependency.java"));
        new ScriptRunner().runScript(scriptText, null);
        assertNotNull(valueFromScript);
    }

    public static void setValueFromScript(String valueFromScript) {
        ScriptRunnerTest.valueFromScript = valueFromScript;
    }
}
