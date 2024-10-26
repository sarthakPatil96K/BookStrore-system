import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager() {
        // Establish the connection to the database
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore", "root", "Sarthak@2005");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to get all books, including the stock information
    public ResultSet getAllBooks() {
        String query = "SELECT name, price, description, category, stock FROM books";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to get books by category, including stock information
    public ResultSet getBooksByCategory(String category) {
        String query = "SELECT name, price, description, category, stock FROM books WHERE category = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, category);
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to update the stock of a book
    public void updateStock(String bookName, int quantityChange) {
        String query = "UPDATE books SET stock = stock + ? WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantityChange);
            stmt.setString(2, bookName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to get the stock of a book
    public int getBookStock(String bookName) {
        String query = "SELECT stock FROM books WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, bookName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock"); // Return the stock value directly
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if no stock found or an error occurred
    }

    // Method to get book details by name
    public ResultSet getBookInfo(String bookName) {
        String query = "SELECT * FROM books WHERE name = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, bookName);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to close the connection when done
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to search for books by name
    public ResultSet searchBooks(String searchText) {
        String query = "SELECT * FROM books WHERE name LIKE ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, "%" + searchText + "%");
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to get the price of a book
    // Method to get the price of a book
    public double getBookPrice(String bookName) {
        String query = "SELECT price FROM books WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, bookName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price"); // Return the price directly
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Return 0.0 if no price found or an error occurred
    }

    // Method to get the review of a book
    public String getBookReview(String bookName) {
        String query = "SELECT review FROM books WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, bookName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("review");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No review available.";
    }
}
