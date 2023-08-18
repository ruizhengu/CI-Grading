package uk.ac.sheffield.com1003.cafe;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.sheffield.com1003.cafe.Recipe.Size;
import uk.ac.sheffield.com1003.cafe.exceptions.CafeOutOfCapacityException;
import uk.ac.sheffield.com1003.cafe.exceptions.TooManyIngredientsException;
import uk.ac.sheffield.com1003.cafe.ingredients.Coffee;
import uk.ac.sheffield.com1003.cafe.ingredients.Syrup;
import uk.ac.sheffield.com1003.cafe.ingredients.Water;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestCafe extends TestCafeBase {

    @DisplayName("The greeting function is implemented")
    @Test 
    public void cafeGreeting() throws Exception {
        Cafe cafe = new Cafe("Central Perk");
        System.out.println("Test being executed");
        assertEquals(cafe.greeting(), "Welcome to Central Perk");
    }

    @DisplayName("A TooManyIngredientsException is caught")
    @Test
    public void testTooManyIngredients() {
        Recipe latte = new Recipe("Just Water", 1, Size.LARGE, 1);
        assertThrows(TooManyIngredientsException.class, () -> {
            latte.addIngredient(new Water());
            latte.addIngredient(new Coffee());
        });
    }

    @DisplayName("The menu has the correct size")
    @Test
    public void testMenuSize() throws Exception {
        Cafe cafe = new Cafe("Central Perk");

        Recipe espresso = createEspressoRecipe();
        cafe.addRecipe(espresso);

        Recipe americano = new Recipe("Americano", 2, Size.REGULAR, 2);
        americano.addIngredient(new Coffee());
        americano.addIngredient(new Water());
        cafe.addRecipe(americano);

        assertEquals(2, cafe.getMenu().length);
    }

    @DisplayName("The menu is printed correctly")
    @Test
    public void testPrintMenu() throws Exception {
        Cafe cafe = new Cafe("Central Perk");

        Recipe espresso = createEspressoRecipe();
        cafe.addRecipe(espresso);

        Recipe americano = new Recipe("Americano", 2, Size.REGULAR, 2);
        americano.addIngredient(new Coffee());
        americano.addIngredient(new Water());
        cafe.addRecipe(americano);

        cafe.printMenu();
        ArrayList<String> lines = getPrintedLines();
        assertEquals(8, lines.size());
        assertEquals("==========", lines.get(0));
        assertEquals("Welcome to Central Perk", lines.get(1));
        assertEquals("Menu", lines.get(2));
        assertEquals("==========", lines.get(3));
        assertEquals("Espresso - 1.5", lines.get(4));
        assertEquals("Americano - 2.0", lines.get(5));
        assertEquals("==========", lines.get(6));
        assertEquals("Enjoy!", lines.get(7));
    }

    @DisplayName("Placing order and printing pending orders")
    @Test
    public void printPendingOrders() throws Exception {
        Cafe cafe = new Cafe("Central Perk", 2, 1);
        cafe.addRecipe(createEspressoRecipe());
        cafe.placeOrder("Espresso", "Jose", 3);
        cafe.printPendingOrders();
        ArrayList<String> lines = getPrintedLines();
        assertEquals(2, lines.size());
        assertEquals("Pending Orders:", lines.get(0));
        assertEquals("Order: Espresso; For: Jose; Paid: 3.0", lines.get(1));

    }

    @DisplayName("Placing order when cafe is out of capacity")
    @Test
    public void placeOrderOutOfCapacity() {
        Exception thrown = assertThrows(Exception.class, () -> {
            Cafe cafe = new Cafe("Central Perk", 2, 0);
            cafe.addRecipe(createEspressoRecipe());
            cafe.placeOrder("Espresso", "Jose", 3);
        });
        assertEquals("uk.ac.sheffield.com1003.cafe.exceptions.CafeOutOfCapacityException", thrown.getClass().getName());
    }

    @DisplayName("App class exists in the right package")
    @Test
    public void testAppClassExists() {
        try {
            Class.forName("uk.ac.sheffield.com1003.cafe.App");
        } catch (ClassNotFoundException e) {
            fail("App class does not exist");
        }
    }

    @DisplayName("CafeOutOfCapacityException class exists in the right package")
    @Test
    public void testCafeOutOfCapacityExceptionExists() {
        try {
            Class.forName("uk.ac.sheffield.com1003.cafe.exceptions.CafeOutOfCapacityException");
        } catch (ClassNotFoundException e) {
            fail("CafeOutOfCapacityException class does not exist");
        }
    }

    @DisplayName("RecipeNotFoundException class exists in the right package")
    @Test
    public void testRecipeNotFoundExceptionExists() {
        try {
            Class.forName("uk.ac.sheffield.com1003.cafe.exceptions.RecipeNotFoundException");
        } catch (ClassNotFoundException e) {
            fail("RecipeNotFoundException class does not exist");
        }
    }

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

    @DisplayName("App.main include creation of Espresso recipe (task 1)")
    @Test
    public void testTask1Espresso() {
        String FILE_PATH = "src/main/java/uk/ac/sheffield/com1003/cafe/App.java";
        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));
            VoidVisitor<MutableBoolean> checker = new VoidVisitorAdapter<>() {
                @Override
                public void visit(MethodDeclaration md, MutableBoolean result) {
                    super.visit(md, result);
                    result.setValue(md.getNameAsString().equals("main"));
                }
            };
            MutableBoolean result = new MutableBoolean(false);
            checker.visit(cu, result);
            assertTrue(result.getValue(), "App.main does not seem to exist");
        } catch (FileNotFoundException e) {
            fail("App.java does not seem to exist in the right package.");
        }
    }

    @Test
    public void testTask1Espresso2() {
        String FILE_PATH = "src/main/java/uk/ac/sheffield/com1003/cafe/App.java";
        CompilationUnit cu = null;
        try {
            cu = StaticJavaParser.parse(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        VoidVisitor<List<String>> mainChecker = new MainMethodChecker();
        List<String> results = new ArrayList<>();
        mainChecker.visit(cu, results);
        System.out.println(results);
    }
    private static class MainMethodChecker extends VoidVisitorAdapter<List<String>> {
        @Override
        public void visit(MethodDeclaration md, List<String> results) {
            super.visit(md, results);
            if (md.getNameAsString().equals("main")) {
                results.add("Main method exists");
            }
        }

        @Override
        public void visit(MethodCallExpr callExpr, List<String> results) {
            super.visit(callExpr, results);
            System.out.println(callExpr.getNameAsString());

            results.add(callExpr.getNameAsString());

        }

        @Override
        public void visit(ObjectCreationExpr constr, List<String> results) {
            super.visit(constr, results);
            System.out.println(constr.getTypeAsString());

            results.add(constr.getTypeAsString());

        }


    }
}
