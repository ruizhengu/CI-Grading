package uk.ac.sheffield.com1003.cafe;

import uk.ac.sheffield.com1003.cafe.Recipe.Size;
import uk.ac.sheffield.com1003.cafe.ingredients.Water;
import uk.ac.sheffield.com1003.cafe.ingredients.Milk.Type;
import uk.ac.sheffield.com1003.cafe.ingredients.Coffee;
import uk.ac.sheffield.com1003.cafe.ingredients.Milk;

public class App {

    public static Coffee cc = new Coffee();

    public static void main(String[] args) throws Exception {
        Cafe cafe = new Cafe("Central Perk");
        
        Recipe espresso = new Recipe("Espresso", 1.5, Size.SMALL, 2);
        espresso.addIngredient(new Coffee());
        espresso.addIngredient(new Water());
        espresso.addIngredient(cc);
        cafe.addRecipe(espresso);

        Recipe soyLatte = new Recipe("Large Soy Latte", 2.5, Size.LARGE, 3);
        soyLatte.addIngredient(new Coffee());
        soyLatte.addIngredient(new Water());
        soyLatte.addIngredient(new Milk(100, Type.SOY));
        cafe.addRecipe(soyLatte);

        cafe.removeRecipe(soyLatte.getName());

        cafe.printMenu();
    }
}
