package com.compiler.javacompiler.service;

import com.compiler.javacompiler.entity.Input;
import com.compiler.javacompiler.entity.Output;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CompilerServiceUtil {

    private final String SUCCESS = "SUCCESS";
    private final String COMPILE_TIME_ERROR = "COMPILE TIME ERROR OR WARNING";
    private final String RUNTIME_ERROR = "RUNTIME ERROR OR WARNING";
    private final String LARGE_OUTPUT_ERROR = "LARGE OUTPUT ERROR";
    private final String TIME_LIMIT_EXCEEDED = "TIME LIMIT EXCEEDED";
    private final String JUDGE_FAILURE = "JUDGE FAILURE";

    private final int SUCCESS_CODE = 0;
    private final int COMPILE_TIME_ERROR_CODE = -1;
    private final int RUNTIME_ERROR_CODE = -2;
    private final int LARGE_OUTPUT_ERROR_CODE = -3;
    private final int TIME_LIMIT_EXCEEDED_CODE = -4;
    private final int JUDGE_FAILURE_CODE = -5;

    private final String EXE_DIR;
    private final String SRC_PATH;
    private final String STDIN;

    private final Map<String, String> ENV_ARGS;

    private String[] LINUX_COMPILE_ARGS = {
        "/usr/bin/javac", "{src_path}", "-d", "{exe_dir}", "-encoding", "UTF8"
    };
    private String[] LINUX_RUN_ARGS = {
        "/usr/bin/java", "-cp", "{exe_dir}", "-Xss1M", "-Xms16M", "-Xmx128M", "-Djava.security.manager",
        "-Djava.security.policy==/java_ide/java_policy", "-Djava.awt.headless=true", "Main"
    };

    private String[] WINDOWS_COMPILE_ARGS = {
        "javac", "{src_path}", "-d", "{exe_dir}", "-encoding", "UTF8"
    };
    private String[] WINDOWS_RUN_ARGS = {
        "java", "-cp", "{exe_dir}", "-Xss1M", "-Xms16M", "-Xmx128M", "-Djava.security.manager",
        "-Djava.security.policy==D:/java_ide/win_java_policy", "-Djava.awt.headless=true", "Main"
    };

    private final int MAX_TIME = 5;
    private final int MAX_FILE_SIZE = 51200;
    private final String CHARSET = "UTF-8";
    private final Output testCaseOutput;

    private File SRC_FILE;
    private File STDIN_FILE;

    public CompilerServiceUtil(Input testCaseInput) {
        EXE_DIR = (SystemUtils.IS_OS_WINDOWS ? "D:" : "") + "/java_ide/"
                + RandomStringUtils.randomAlphanumeric(30)
                + System.currentTimeMillis();

        SRC_PATH = EXE_DIR + "/Main.java";
        STDIN = EXE_DIR + "/input.txt";

        LINUX_RUN_ARGS[2] = EXE_DIR;
        LINUX_COMPILE_ARGS[1] = SRC_PATH;
        LINUX_COMPILE_ARGS[3] = EXE_DIR;

        WINDOWS_RUN_ARGS[2] = EXE_DIR;
        WINDOWS_COMPILE_ARGS[1] = SRC_PATH;
        WINDOWS_COMPILE_ARGS[3] = EXE_DIR;

        ENV_ARGS = new HashMap<>();
        ENV_ARGS.put("LANG", "en_US.UTF-8");
        ENV_ARGS.put("LANGUAGE", "en_US:en");
        ENV_ARGS.put("LC_ALL", "en_US.UTF-8");

        testCaseOutput = new Output();

        mkdir();
        createFiles(testCaseInput.getCode(), testCaseInput.getInput());
    }

    public Output execute() {
        compile();
        if (testCaseOutput.getStatusCode() == SUCCESS_CODE) run();
        //rmdir();
        return testCaseOutput;
    }

    private String trimPath(String text) {
        String sub;
        if (SystemUtils.IS_OS_WINDOWS) {
            sub = EXE_DIR.replace('/', '\\');
        } else {
            sub = EXE_DIR;
        }
        return text.replace(sub, "~/path/usr~");
    }

    private Output compile() {
        String text = "";
        try {
            ProcessBuilder pb = new ProcessBuilder(getCompileArgs());

            Map<String, String> env = pb.environment();
            ENV_ARGS.forEach((key, value) -> env.put(key, value));

            Process process = pb.start();

            if (process.waitFor(MAX_TIME, TimeUnit.SECONDS)) {
                text = IOUtils.toString(process.getErrorStream(), CHARSET).trim();

                if (text.length() > 0) {
                    // Compile Time Error.
                    text = trimPath(text);
                    testCaseOutput.setStatusCode(COMPILE_TIME_ERROR_CODE);
                    testCaseOutput.setStatusMessage(COMPILE_TIME_ERROR);
                    testCaseOutput.setOutputText(text);
                } else {
                    // Success.
                    testCaseOutput.setStatusCode(SUCCESS_CODE);
                    testCaseOutput.setStatusMessage(SUCCESS);
                    testCaseOutput.setOutputText(text);
                }
            } else {
                // Time limit exceeded.
                process.destroy();
                process.waitFor();

                text = "Compilation took too long to finish.";

                testCaseOutput.setStatusCode(JUDGE_FAILURE_CODE);
                testCaseOutput.setStatusMessage(JUDGE_FAILURE);
                testCaseOutput.setOutputText(text);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            text = "Judge failed due to an exception.";

            testCaseOutput.setStatusCode(JUDGE_FAILURE_CODE);
            testCaseOutput.setStatusMessage(JUDGE_FAILURE);
            testCaseOutput.setOutputText(text);
        }
        return testCaseOutput;
    }

    private Output run() {
        String text = "";
        try {
            ProcessBuilder pb = new ProcessBuilder(getRunArgs());

            Map<String, String> env = pb.environment();
            ENV_ARGS.forEach((key, value) -> env.put(key, value));

            pb.redirectInput(STDIN_FILE);
            Process process = pb.start();

            if (process.waitFor(MAX_TIME, TimeUnit.SECONDS)) {
                text = IOUtils.toString(process.getErrorStream(), CHARSET).trim();

                if (text.length() > 0) {
                    // Runtime Error.
                    text = trimPath(text);
                    testCaseOutput.setStatusCode(RUNTIME_ERROR_CODE);
                    testCaseOutput.setStatusMessage(RUNTIME_ERROR);
                    testCaseOutput.setOutputText(text);
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder sb = new StringBuilder();

                    while ((text = br.readLine()) != null) {
                        sb.append(text);
                        sb.append("\n");
                        if (sb.length() > MAX_FILE_SIZE) break;
                    }

                    br.close();

                    if (sb.length() > MAX_FILE_SIZE) {
                        // Large output error.
                        text = "Output is too large to display.";
                        testCaseOutput.setStatusCode(LARGE_OUTPUT_ERROR_CODE);
                        testCaseOutput.setStatusMessage(LARGE_OUTPUT_ERROR);
                        testCaseOutput.setOutputText(text);
                    } else {
                        // Success.
                        text = sb.toString();
                        testCaseOutput.setStatusCode(SUCCESS_CODE);
                        testCaseOutput.setStatusMessage(SUCCESS);
                        testCaseOutput.setOutputText(text);
                    }
                }
            } else {
                // Time limit exceeded.
                process.destroy();
                process.waitFor();

                text = "Your code took too long to execute.";

                testCaseOutput.setStatusCode(TIME_LIMIT_EXCEEDED_CODE);
                testCaseOutput.setStatusMessage(TIME_LIMIT_EXCEEDED);
                testCaseOutput.setOutputText(text);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            text = "Judge failed due to an exception.";

            testCaseOutput.setStatusCode(JUDGE_FAILURE_CODE);
            testCaseOutput.setStatusMessage(JUDGE_FAILURE);
            testCaseOutput.setOutputText(text);
        }
        return testCaseOutput;
    }

    private String[] getCompileArgs() {
        return SystemUtils.IS_OS_WINDOWS ? WINDOWS_COMPILE_ARGS : LINUX_COMPILE_ARGS;
    }

    private String[] getRunArgs() {
        return SystemUtils.IS_OS_WINDOWS ? WINDOWS_RUN_ARGS : LINUX_RUN_ARGS;
    }

    private void createFiles(String code, String input) {
        try {
            SRC_FILE = new File(SRC_PATH);
            STDIN_FILE = new File(STDIN);

            SRC_FILE.createNewFile();
            STDIN_FILE.createNewFile();

            FileUtils.writeStringToFile(SRC_FILE, code, CHARSET, true);
            FileUtils.writeStringToFile(STDIN_FILE, input, CHARSET, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mkdir() {
        try {
            Path path = Paths.get(EXE_DIR);
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rmdir() {
        try {
            FileUtils.deleteDirectory(new File(EXE_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
