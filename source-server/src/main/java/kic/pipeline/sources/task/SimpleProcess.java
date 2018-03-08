package kic.pipeline.sources.task;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class SimpleProcess {
    private ProcessBuilder processBuilder;

    public SimpleProcess(String[] command) {
        this.processBuilder = new ProcessBuilder(command);
    }

    public ProcessResult execute(byte[] stdIn) throws IOException {
        Process process = processBuilder.start();

        process.getOutputStream().write(stdIn);
        process.getOutputStream().close();
        byte[] stdOut = IOUtils.toByteArray(process.getInputStream());
        byte[] stdErr = IOUtils.toByteArray(process.getErrorStream());

        try {
            return new ProcessResult(stdOut, stdErr, process.waitFor());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public static class ProcessResult {
        public final byte[] stdOut;
        public final byte[] stdErr;
        public final int returnCode;

        public ProcessResult(byte[] stdOut, byte[] stdErr, int returnCode) {
            this.stdOut = stdOut;
            this.stdErr = stdErr;
            this.returnCode = returnCode;
        }
    }
}
