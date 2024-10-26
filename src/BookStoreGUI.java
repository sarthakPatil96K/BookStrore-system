import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BookStoreGUI extends JFrame {

    // GUI components
    private JList<String> bookList;         // List to display available books
    private JList<String> cartList;         // List to display selected books in the cart
    private DefaultListModel<String> cartModel; // Model for the cart list
    private JLabel totalLabel;              // Label to display total cost
    private JTextField searchField;         // Search field for book search
    private JComboBox<String> categoryCombo; // Dropdown for book categories

    private double totalCost = 0.0;         // To keep track of the total cost

    // Database manager for fetching books
    private DatabaseManager dbManager;

    // Constructor to set up the GUI
    public BookStoreGUI() {
        // Initialize the database manager
        dbManager = new DatabaseManager();

        // Set up the main window
        setTitle("Bookstore Shopping Cart");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(Color.BLACK);

        // Heading panel
        JLabel headingLabel = new JLabel("Welcome to the Online Bookstore", JLabel.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headingLabel.setForeground(Color.WHITE);
        add(headingLabel, BorderLayout.NORTH);

        // Create a panel for search and category
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(Color.BLACK);

        // Search field
        searchField = new JTextField(20);
        searchField.setBackground(Color.BLACK);
        searchField.setForeground(Color.WHITE);
        topPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(Color.BLACK);
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> searchBook());
        topPanel.add(searchButton);

        // Category dropdown
        categoryCombo = new JComboBox<>(new String[]{"All", "Programming", "Algorithms", "Data Structures", "Artificial Intelligence", "Operating Systems"});
        categoryCombo.setBackground(Color.BLACK);
        categoryCombo.setForeground(Color.WHITE);
        categoryCombo.addActionListener(e -> filterBooksByCategory());
        topPanel.add(categoryCombo);

        add(topPanel, BorderLayout.NORTH); // Add search and category panel below heading

        // Create a panel to hold the book list and cart
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.BLACK);

        // Create and add the book list with categories
        bookList = new JList<>(getBooksByCategory("All"));
        bookList.setFont(new Font("Arial", Font.PLAIN, 16)); // Increased font size
        bookList.setForeground(Color.WHITE);
        bookList.setBackground(Color.BLACK);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane bookScrollPane = new JScrollPane(bookList);
        mainPanel.add(bookScrollPane);

        // Create and add the shopping cart list
        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartList.setForeground(Color.WHITE);
        cartList.setBackground(Color.BLACK);
        JScrollPane cartScrollPane = new JScrollPane(cartList);
        mainPanel.add(cartScrollPane);

        // Create a panel for the buttons and total cost
        JPanel controlPanel = new JPanel(new GridLayout(6, 1));
        controlPanel.setBackground(Color.BLACK);

        // Button to add the selected book to the cart
        JButton addButton = new JButton("Add to Cart");
        addButton.setBackground(Color.BLACK);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> {
            int selectedIndex = bookList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedBook = bookList.getSelectedValue();
                String bookName = selectedBook.split(" - ")[0]; // Extract book name
                int stockLeft = getStockLeft(bookName); // Get current stock

                if (stockLeft > 0) {
                    double price = getBookPrice(bookName);
                    cartModel.addElement(selectedBook);
                    totalCost += price;
                    totalLabel.setText("Total: $" + String.format("%.2f", totalCost));
                    dbManager.updateStock(bookName, -1); // Update stock in the database
                    bookList.setListData(getBooksByCategory(categoryCombo.getSelectedItem().toString())); // Refresh the book list
                } else {
                    JOptionPane.showMessageDialog(this, "This book is out of stock!");
                }
            }
        });
        controlPanel.add(addButton);
        // Button to remove selected book from the cart
        // Button to remove selected book from the cart
        JButton removeButton = new JButton("Remove from Cart");
        removeButton.setBackground(Color.BLACK);
        removeButton.setForeground(Color.WHITE);
        removeButton.addActionListener(e -> {
            int selectedIndex = cartList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedItem = cartModel.getElementAt(selectedIndex);

                // Extract book name and price from selectedItem
                String bookName = selectedItem.split(" - ")[0]; // Extract book name
                double price = extractPriceFromCartItem(selectedItem); // Extract the price from the cart item

                // Remove the selected item from the cart
                cartModel.remove(selectedIndex);

                // Update the total cost by subtracting the item's price
                totalCost -= getBookPrice(bookName);
                totalLabel.setText("Total: $" + String.format("%.2f", totalCost));

                // Update stock in the database (increase stock by 1)
                dbManager.updateStock(bookName, 1);

                // Refresh the book list (to show updated stock)
                bookList.setListData(getBooksByCategory(categoryCombo.getSelectedItem().toString()));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to remove.");
            }
        });

        // Add Remove button to the controlPanel
        controlPanel.add(removeButton);



        // Button to view book information
        JButton infoButton = new JButton("View Book Info");
        infoButton.setBackground(Color.BLACK);
        infoButton.setForeground(Color.WHITE);
        infoButton.addActionListener(e -> {
            int selectedIndex = bookList.getSelectedIndex();
            if (selectedIndex != -1) {
                openBookInfoWindow(bookList.getSelectedValue());
            }
        });
        controlPanel.add(infoButton);

        // Button to view book review
        JButton reviewButton = new JButton("Get Book Review");
        reviewButton.setBackground(Color.BLACK);
        reviewButton.setForeground(Color.WHITE);
        reviewButton.addActionListener(e -> {
            int selectedIndex = bookList.getSelectedIndex();
            if (selectedIndex != -1) {
                openBookReviewWindow(bookList.getSelectedValue());
            }
        });
        controlPanel.add(reviewButton);

        // Button to initiate payment process
        JButton payButton = new JButton("Proceed to Payment");
        payButton.setBackground(Color.BLACK);
        payButton.setForeground(Color.WHITE);
        payButton.addActionListener(e -> openPaymentWindow());
        controlPanel.add(payButton);

        // Label to display the total cost
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setForeground(Color.WHITE);
        controlPanel.add(totalLabel);

        // Add components to the main window
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        // Display the window
        setVisible(true);
    }

    // Helper method to extract the price from a cart item
    private double extractPriceFromCartItem(String item) {
        try {
            // Split by " - $" to get the price part
            String[] parts = item.split(" - \\$");
            if (parts.length > 1) {
                return Double.parseDouble(parts[1].trim()); // Parse the price
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing price from item: " + item);
            e.printStackTrace();
        }
        return 0.0; // Default return if error occurs
    }





    // Method to search for a book
    private void searchBook() {
        String searchText = searchField.getText().toLowerCase();
        ArrayList<String> searchResults = new ArrayList<>();

        try (ResultSet rs = dbManager.searchBooks(searchText)) {
            while (rs.next()) {
                String bookName = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                searchResults.add(bookName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No books found!");
        } else {
            bookList.setListData(searchResults.toArray(new String[0]));
        }
    }

    // Method to filter books by selected category
    private void filterBooksByCategory() {
        String selectedCategory = (String) categoryCombo.getSelectedItem();
        bookList.setListData(getBooksByCategory(selectedCategory));
    }

    // Method to get books by category from the database
    private String[] getBooksByCategory(String category) {
        ArrayList<String> filteredBooks = new ArrayList<>();

        try (ResultSet rs = category.equals("All") ? dbManager.getAllBooks() : dbManager.getBooksByCategory(category)) {
            while (rs.next()) {
                String bookName = rs.getString("name");
                filteredBooks.add(bookName); // Only add the book name
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return filteredBooks.toArray(new String[0]);
    }

    // Method to get the price of a book from the database
    private double getBookPrice(String bookName) {
        return dbManager.getBookPrice(bookName); // Call the method that returns the price directly
    }


    // Method to get the stock left for a book
    private int getStockLeft(String bookName) {
        return dbManager.getBookStock(bookName); // Call the updated method
    }


    // Method to open the book info window
    private void openBookInfoWindow(String bookName) {
        JFrame infoWindow = new JFrame("Book Information");
        infoWindow.setSize(300, 200);
        infoWindow.setLayout(new BorderLayout());
        infoWindow.getContentPane().setBackground(Color.BLACK);

        try (ResultSet rs = dbManager.getBookInfo(bookName)) { // Use book name directly
            if (rs.next()) {
                JLabel bookTitle = new JLabel("Title: " + rs.getString("name"), JLabel.CENTER);
                bookTitle.setForeground(Color.WHITE);
                JLabel bookPrice = new JLabel("Price: $" + String.format("%.2f", rs.getDouble("price")), JLabel.CENTER);
                bookPrice.setForeground(Color.WHITE);
                JLabel bookStock = new JLabel("Stock: " + rs.getInt("stock"), JLabel.CENTER);
                bookStock.setForeground(Color.WHITE);
                JLabel bookDescription = new JLabel("<html>Description: " + rs.getString("description") + "</html>", JLabel.CENTER);
                bookDescription.setForeground(Color.WHITE);

                infoWindow.add(bookTitle, BorderLayout.NORTH);
                infoWindow.add(bookPrice, BorderLayout.WEST);
                infoWindow.add(bookStock, BorderLayout.CENTER);
                infoWindow.add(bookDescription, BorderLayout.SOUTH);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Make the info window visible
        infoWindow.setVisible(true);
    }


    // Method to open the book review window
    private void openBookReviewWindow(String bookName) {
        JFrame reviewWindow = new JFrame("Book Review");
        reviewWindow.setSize(300, 200);
        reviewWindow.setLayout(new BorderLayout());
        reviewWindow.getContentPane().setBackground(Color.BLACK);

        // Fetch the review from the database (Assuming the review is stored)
        String review = dbManager.getBookReview(bookName.split(" - ")[0]); // Extract book name

        JLabel reviewLabel = new JLabel("<html>Review: " + review + "</html>", JLabel.CENTER);
        reviewLabel.setForeground(Color.WHITE);
        reviewWindow.add(reviewLabel, BorderLayout.CENTER);

        // Make the review window visible
        reviewWindow.setVisible(true);
    }

    // Method to open the payment window
    private void openPaymentWindow() {
        JFrame paymentWindow = new JFrame("Payment Gateway");
        paymentWindow.setSize(300, 200);
        paymentWindow.setLayout(new GridLayout(3, 2));
        paymentWindow.getContentPane().setBackground(Color.BLACK);

        // Add payment form components
        paymentWindow.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        nameField.setBackground(Color.BLACK);
        nameField.setForeground(Color.WHITE);
        paymentWindow.add(nameField);

        paymentWindow.add(new JLabel("Card Number:"));
        JTextField cardField = new JTextField();
        cardField.setBackground(Color.BLACK);
        cardField.setForeground(Color.WHITE);
        paymentWindow.add(cardField);

        paymentWindow.add(new JLabel("Total Amount:"));
        JLabel totalAmountLabel = new JLabel("$" + String.format("%.2f", totalCost));
        totalAmountLabel.setForeground(Color.WHITE);
        paymentWindow.add(totalAmountLabel);

        // Add "Pay" button
        JButton payButton = new JButton("Pay Now");
        payButton.setBackground(Color.BLACK);
        payButton.setForeground(Color.WHITE);
        payButton.addActionListener(e -> {
            String name = nameField.getText();
            String card = cardField.getText();

            if (!name.isEmpty() && !card.isEmpty()) {
                JOptionPane.showMessageDialog(paymentWindow, "Payment Successful! Thank you for your purchase, " + name + "!");

                // Clear the cart and reset total cost
                cartModel.clear();
                totalCost = 0.0;
                totalLabel.setText("Total: $0.00");

                paymentWindow.dispose();
            } else {
                JOptionPane.showMessageDialog(paymentWindow, "Please fill in all fields.");
            }
        });

        paymentWindow.add(payButton);

        // Make the payment window visible
        paymentWindow.setVisible(true);
    }

    // Main method to launch the application
    public static void main(String[] args) {
        // Run the bookstore GUI
        SwingUtilities.invokeLater(() -> new BookStoreGUI());
    }
}

