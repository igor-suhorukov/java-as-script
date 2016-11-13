//dependency:mvn:/com.github.igor-suhorukov:phantomjs-runner:1.1
package org.github.suhorukov;

import com.github.igorsuhorukov.phantomjs.PhantomJsDowloader;
import org.codehaus.commons.compiler.jdk.ScriptRunnerTest;

class Script{

    public static void main(String[] args)  throws Exception{

        String phantomPath = PhantomJsDowloader.getPhantomJsPath();
        ScriptRunnerTest.setValueFromScript(phantomPath);
    }
}
