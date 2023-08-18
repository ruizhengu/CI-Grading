package uk.ac.sheffield.com1003.cafe.causality;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static String PACKAGE_NAME = "uk.ac.sheffield.com1003.cafe";
    public static String aspectJPackage = "aspect";
    public static String causalityPackage = "causality";
    public static String replacePackage = "replace";

    /**
     * Get the project path by the current OS
     *
     * @return the abs path of /src/main/java
     */
    public static String getOSPath() {
        String macPath = "/Users/ray/Project/PhD/GTA/com1003_cafe/src/main/java";
        String linuxPath = "src/main/java";
        if (System.getProperty("os.name").startsWith("Mac")) {
            return macPath;
        } else {
            return linuxPath;
        }
    }

    public static boolean filterInstrumentation(MethodDeclaration m) {
        return !m.resolve().getQualifiedName().contains(aspectJPackage) && !m.resolve().getQualifiedName().contains(causalityPackage) && !m.resolve().getQualifiedName().contains(replacePackage);
    }

    public static String getReferencePath() {
        return "/home/ruizhen/Projects/Experiment/com1003_cafe-reference/src/main/java";
    }

    /**
     * Get all the files except the test files in the project.
     *
     * @param dir The path of main/java directory
     * @return All the files in the directory
     */
    public static Set<File> getFiles(File dir) {
        Set<File> files = new HashSet<>();
        getFilesRecursion(dir, files);
        return files;
    }

    public static void getFilesRecursion(File dir, Set<File> files) {
        for (File entry : Objects.requireNonNull(dir.listFiles())) {
            if (entry.isDirectory()) {
                getFilesRecursion(entry, files);
            } else {
                files.add(entry);
            }
        }
    }

    /**
     * Get the last segment from a fully qualified name
     *
     * @param fullyQualifiedName e.g. uk.ac.sheffield.com1003.cafe.ingredients.Water
     * @return e.g. Water
     */
    public static String getLastSegment(String fullyQualifiedName) {
        String[] splits = fullyQualifiedName.split("\\.");
        return splits[splits.length - 1];
    }

    /**
     * Get the last 'length' number of segments from a fully qualified name
     *
     * @param fullyQualifiedName e.g. uk.ac.sheffield.com1003.cafe.ingredients.Water
     * @param length             the length of segments
     * @return e.g. ingredients.Water if index == 2
     */
    public static String getLastSegment(String fullyQualifiedName, int length) {
        String[] splits = fullyQualifiedName.split("\\.");
        StringBuilder output = new StringBuilder(splits[splits.length - length]);
        for (int i = 1; i < length; i++) {
            output.append(".").append(splits[splits.length - length + i]);
        }
        return output.toString();
    }

    /**
     * Get the 'length' number of segments start from 'index'
     *
     * @param fullyQualifiedName e.g. e.g. uk.ac.sheffield.com1003.cafe.ingredients.Water.Type
     * @param index              the index of the segments (from the end of the string)
     * @param length             the length of the segments
     * @return e.g. Water if index == 2, length == 1
     */
    public static String getLastSegment(String fullyQualifiedName, int index, int length) {
        if (length > index) {
            throw new IndexOutOfBoundsException();
        }
        String[] splits = fullyQualifiedName.split("\\.");
        StringBuilder output = new StringBuilder(splits[splits.length - index]);
        for (int i = 1; i < length; i++) {
            output.append(".").append(splits[splits.length - length + i]);
        }
        return output.toString();
    }

    public static boolean matchArguments(NodeList<Expression> arguments, JSONObject constructorParameters) {
        List<String> argumentTypes = new ArrayList<>();
        for (Expression argument : arguments) {
            argumentTypes.add(Util.getLastSegment(argument.calculateResolvedType().describe()));
        }
        return argumentTypes.equals(constructorParameters.getJSONArray(DataDependence.PARAMETER_TYPE_KEY).toList());
    }

    public static boolean notDuplicatedArray(List<String[]> data, String[] targetArray) {
        for (String[] array : data) {
            if (Arrays.equals(array, targetArray)) {
                return false;
            }
        }
        return true;
    }
}
