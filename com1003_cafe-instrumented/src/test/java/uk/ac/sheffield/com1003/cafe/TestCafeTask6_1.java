package uk.ac.sheffield.com1003.cafe;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.sheffield.com1003.cafe.Recipe.Size;
import uk.ac.sheffield.com1003.cafe.exceptions.TooManyIngredientsException;
import uk.ac.sheffield.com1003.cafe.ingredients.Coffee;
import uk.ac.sheffield.com1003.cafe.ingredients.Syrup;
import uk.ac.sheffield.com1003.cafe.ingredients.Water;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCafeTask6_1 extends TestCafeBase {

    @DisplayName("Syrup class exists in the right package")
    @Test
    public void testSyrupExists() {
        try {
            Class.forName("uk.ac.sheffield.com1003.cafe.ingredients.Syrup");
        } catch (ClassNotFoundException e) {
            fail("Syrup class does not exist");
        }
    }

    @DisplayName("Syrup class contains flavour field")
    @Test
    public void testSyrupContainsFlavourField() {
        Syrup syrup = new Syrup();
        Field[] fields = syrup.getClass().getDeclaredFields();

        List<String> fieldNames = new ArrayList<>();
        for (Field field : fields)
            fieldNames.add(field.getName());
        assertTrue(Arrays.asList("flavour").containsAll(fieldNames));
    }

}
