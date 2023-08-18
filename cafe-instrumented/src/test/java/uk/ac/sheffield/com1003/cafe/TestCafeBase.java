package uk.ac.sheffield.com1003.cafe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.sheffield.com1003.cafe.Recipe.Size;
import uk.ac.sheffield.com1003.cafe.exceptions.TooManyIngredientsException;
import uk.ac.sheffield.com1003.cafe.ingredients.Coffee;
import uk.ac.sheffield.com1003.cafe.ingredients.Water;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestCafeBase {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    protected Recipe createEspressoRecipe() throws TooManyIngredientsException {
        Recipe espresso = new Recipe("Espresso", 1.5, Size.SMALL, 2);
        espresso.addIngredient(new Coffee());
        espresso.addIngredient(new Water());
        return espresso;
    }

    protected ArrayList<String> getPrintedLines() {
        Stream<String> lines = outContent.toString().lines();
        ArrayList<String> arrayList = new ArrayList<>();
        lines.forEach(arrayList::add);
        return arrayList;
    }

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

    @DisplayName("Place order and print receipt")
    @Test
    public void printReceipt() throws Exception {
        Cafe cafe = new Cafe("Central Perk", 2, 1);
        cafe.addRecipe(createEspressoRecipe());
        cafe.placeOrder("Espresso", "Jose", 3);
        Order o = cafe.serveOrder();
        o.printReceipt();
        ArrayList<String> lines = getPrintedLines();
        assertEquals(5, lines.size());
        assertTrue(lines.contains("Paid: 3.0"));
        assertTrue(lines.contains("Espresso for Jose"));
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

    @DisplayName("App class exists")
    @Test
    public void testAppClassExists() {
        try {
            Class.forName("uk.ac.sheffield.com1003.cafe.App");
        } catch (ClassNotFoundException e) {
            fail("App class does not exist");
        }
    }
}
