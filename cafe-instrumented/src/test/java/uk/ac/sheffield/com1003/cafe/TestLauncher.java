package uk.ac.sheffield.com1003.cafe;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class TestLauncher {

    public static String TEST_DIRECTORY = "src/test/java/uk/ac/sheffield/com1003/cafe";
    public static String PACKAGE_NAME = "uk.ac.sheffield.com1003.cafe";
    public static String LOG_PATH = "log.txt";
    public static String METHOD_COVERAGE_PATH = "MethodCoverage";
    public static SummaryGeneratingListener listener = new SummaryGeneratingListener();

    public void runIndividual() {
        int indexStart;
        int indexEnd = 0;
        List<Map<String, List<Integer>>> indexList = new ArrayList<>();
        File log = new File(LOG_PATH);
        try {
            PrintWriter printWriter = new PrintWriter(log);
            printWriter.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (String method : listTestMethods()) {
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(selectMethod(method)).build();
            Launcher launcher = LauncherFactory.create();
            launcher.registerTestExecutionListeners(listener);
            launcher.execute(request);
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(log));
                indexStart = indexEnd;
                while ((bufferedReader.readLine()) != null) {
                    indexEnd += 1;
                }
                indexEnd -= indexStart;
                List<Integer> indexTmp = new ArrayList<>();
                indexTmp.add(indexStart);
                indexTmp.add(indexEnd);
                Map<String, List<Integer>> mapTmp = new HashMap<>();
                mapTmp.put(method, indexTmp);
                indexList.add(mapTmp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        migrateLog(indexList);
    }

    /**
     * Migrate the method execution trace of each test method from "log.txt" to individual files.
     *
     * @param indexList A list contains the names of the test methods and the indexes in "log.txt".
     */
    public void migrateLog(List<Map<String, List<Integer>>> indexList) {
        for (Map<String, List<Integer>> indexMap : indexList) {
            indexMap.forEach((key, value) -> {
                String methodCoveragePath = String.join("/", METHOD_COVERAGE_PATH, key + ".txt");
                FileWriter fileWriter;
                try {
                    fileWriter = new FileWriter(methodCoveragePath);
                    for (int i = value.get(0); i < value.get(1); i++) {
                        String line = Files.readAllLines(Paths.get(LOG_PATH)).get(i);
                        fileWriter.write(line + "\n");
                    }
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public List<String> listTestClasses() {
        String currentClass = this.getClass().getSimpleName();
        return Stream.of(Objects.requireNonNull(new File(TEST_DIRECTORY).listFiles())).map(File::getName).filter(name -> !name.contains(currentClass)).collect(Collectors.toList());
    }

    /**
     * Get the names of test methods in all test classes.
     *
     * @return A list of the names of test methods.
     */
    public List<String> listTestMethods() {
        List<String> classStrings = listTestClasses();
        List<String> selectMethods = new ArrayList<>();
        for (String classString : classStrings) {
            try {
                Class<?> testClass = Class.forName(String.join(".", PACKAGE_NAME, classString.replace(".java", "")));
                for (Method testMethod : testClass.getDeclaredMethods()) {
                    if (testMethod.isAnnotationPresent(Test.class)) {
                        selectMethods.add(String.join("#", testClass.getName(), testMethod.getName()));
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return selectMethods;
    }

    public static void main(String[] args) {
        TestLauncher testLauncher = new TestLauncher();
        testLauncher.runIndividual();
    }
}
