package kic.pipeline.sources.task;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import static java.util.stream.Collectors.*;

// TODO this class need to operate on streams insted of this heap intense byte arrays
// however for the moment this makes devlopment much easier
// for every new line on the key and on the value extractor should be pused to the topic immediatly
public class SimpleProcess {
    private ProcessBuilder processBuilder;

    public SimpleProcess(String[] command) {
        this.processBuilder = new ProcessBuilder(command);
    }

    public ProcessResult execute(byte[] stdIn) {
        return execute(null, stdIn);
    }

    public ProcessResult execute(File workingDirectory, byte[] stdIn) {
        byte[] stdOut = new byte[0];
        byte[] stdErr = new byte[0];

        try {
            processBuilder.directory(workingDirectory);
            Process process = processBuilder.start();

            try {
                process.getOutputStream().write(stdIn);
                process.getOutputStream().close();
            } catch (IOException ioe) {}

            stdOut = IOUtils.toByteArray(process.getInputStream());
            stdErr = IOUtils.toByteArray(process.getErrorStream());

            return new ProcessResult(getCommandString(), stdOut, stdErr, process.waitFor(), null);
        } catch (Exception e) {
            return new ProcessResult(getCommandString(), stdOut, stdErr, Integer.MIN_VALUE, e);
        }
    }

    public String getCommandString() {
        return processBuilder.command().stream().collect(joining(" "));
    }

    public static class ProcessResult {
        public final String command;
        public final byte[] stdOut;
        public final byte[] stdErr;
        public final int returnCode;
        public final Exception exception;

        public ProcessResult(String command, byte[] stdOut, byte[] stdErr, int returnCode, Exception exception) {
            this.command = command;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
            this.returnCode = returnCode;
            this.exception = exception;
        }

        public void retrowExceptionIfCaughtWithMsg(String msg) throws ProcessException {
            if (exception != null) {
                throw new ProcessException(command, exception, msg, new String(stdOut), new String(stdErr));
            }
        }

        @Override
        public String toString() {
            return "ProcessResult{" +
                    "stdOut=" + new String(stdOut).replaceAll("^\\s+|\\s+$", "") +
                    ", stdErr=" + new String(stdErr).replaceAll("^\\s+|\\s+$", "") +
                    ", returnCode=" + returnCode +
                    ", exception=" + (exception == null ? "" : ExceptionUtils.getStackTrace(exception)) +
                    '}';
        }
    }

    public static class ProcessException extends IOException {
        public final String msg;
        public final String stdOut;
        public final String stdErr;

        public ProcessException(String command, Exception exception, String msg, String stdOut, String stdErr) {
            super(command + "\nmsg:" + msg +
                            "\nout:" + new String(stdOut) +
                            "\nerr:" + new String(stdErr),
                  exception);
            this.msg = msg;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }
    }
}
