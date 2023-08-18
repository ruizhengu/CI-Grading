package uk.ac.sheffield.com1003.cafe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {
    
    private Recipe recipeOrdered;
    private String customerName;
    private double amountPaid;
    private String specialRequest;
    private LocalDateTime orderPlaced;
    private LocalDateTime orderServed;

    /**
     * Constructor that takes as parameters the recipe,
     * the customer name, and the amount paid, setting the special
     * request field to "none". Sets the placing date and time to
     * the current date using {@link LocalDateTime#now()} and
     * the serving date to null.
     * 
     * @param recipe Recipe for the order
     * @param customerName Customer name
     * @param amountPaid Amount paid by customer in pounds
     */
    public Order(Recipe recipe, String customerName, double amountPaid) {
        this(recipe, customerName, amountPaid, "none");
    }

    /**
     * Constructor that takes as parameters the recipe,
     * the customer name, the amount paid, and the special
     * request from the customer. Sets the placing date and time to
     * the current date using {@link LocalDateTime#now()} and
     * the serving date to null.
     * 
     * @param recipe Recipe for the order
     * @param customerName Customer name
     * @param amountPaid Amount paid by customer in pounds
     * @param specialRequest The text of the special request, e.g., "extra shot"
     */
    public Order(Recipe recipe, String customerName, double amountPaid, String specialRequest) {
        this.recipeOrdered = recipe;
        this.customerName = customerName;
        this.amountPaid = amountPaid;
        this.specialRequest = specialRequest;
        this.orderPlaced = LocalDateTime.now();
        this.orderServed = null;
    }

    /**
     * Prints order receipt in the following format:
     * <Placed date and time in format dd-MM-yyyy HH-mm-ss>
     * <Recipe name> for <Customer name>
     * Paid: <Amount paid>
     * Change due: <Amount paid minus recipe price>
     * Note: <Special request>
     * Served: <Served date and time in format dd-MM-yyyy HH-mm-ss if already served; otherwise "Pending">
     * Thank you!
     */
    public void printReceipt() {
        StringBuffer sb = new StringBuffer();
        String lineBreak = System.getProperty("line.separator");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        sb.append(orderPlaced.format(formatter));
        sb.append(lineBreak);
        sb.append(recipeOrdered.getName());
        sb.append(" for ");
        sb.append(customerName);
        sb.append(lineBreak);
        sb.append("Paid: " + amountPaid);
        sb.append(lineBreak);
        sb.append("Change due: ");
        sb.append(amountPaid-recipeOrdered.getPrice());
        sb.append("Note: " + specialRequest);
        sb.append(lineBreak);
        sb.append("Served: ");
        if (orderServed == null) 
            sb.append("Pending");
        else
            sb.append(orderServed.format(formatter));
        sb.append("Thank you!");
        System.out.println(sb);
    }

    /**
     * Updates field {@link Order#orderServed}
     */
    public void serve() {
        this.orderServed = LocalDateTime.now();
    }

    /**
     * @return Returns a string representation of an order in
     * the format "Order: <Recipe name>; For: <Customer name>; Paid: <Amount paid>"
     */
    @Override
    public String toString() {
        return "Order: " + recipeOrdered.getName() + "; For: " + customerName + "; Paid: " + amountPaid;
    }
}