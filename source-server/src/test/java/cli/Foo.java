package cli;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

public class Foo {
    public static void main(String[] args) throws Exception {
        Process pA = new ProcessBuilder("java", "-version").start();
        Process pB = new ProcessBuilder("bash", "-c", "cat").start();

        InputStream stdErrA = pA.getErrorStream();
        OutputStream stdInB = pB.getOutputStream();
        ByteArrayOutputStream cmdInB2 = new ByteArrayOutputStream();

        OutputStream tee = new TeeOutputStream(stdInB, stdInB);
        IOUtils.copy(stdErrA, tee);

        pA.waitFor();
        stdInB.close();
        pB.waitFor();

        System.out.println(IOUtils.toString(pB.getInputStream(), "UTF-8"));
        System.out.println(cmdInB2.toString("UTF-8"));
    }
}
