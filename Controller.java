package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.sql.*;

public class EmployeeController {

    @FXML
    private TextField tfEmployeeID;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfAge;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfDepartment;

    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:employee.db");
        } catch (SQLException e) {
            showError("Database Connection Error", "Unable to connect to the database.");
            return null;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void createEmployeeTable() {
        Connection conn = getConnection();
        if (conn == null) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS employee (" +
                    "emp_id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +
                    "age INTEGER, " +
                    "email TEXT, " +
                    "department TEXT)";
            stmt.executeUpdate(sql);
            showInfo("Success", "Employee table created successfully.");
        } catch (SQLException e) {
            showError("Table Creation Error", "Error creating employee table.");
        }
    }

    @FXML
    private void registerEmployee() {
        String emp_id = tfEmployeeID.getText();
        String name = tfName.getText();
        String ageStr = tfAge.getText();
        String email = tfEmail.getText();
        String department = tfDepartment.getText();

        if (emp_id.isEmpty() || name.isEmpty() || ageStr.isEmpty() || email.isEmpty() || department.isEmpty()) {
            showError("Validation Error", "All fields must be filled.");
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            showError("Validation Error", "Age must be a valid number.");
            return;
        }

        Connection conn = getConnection();
        if (conn == null) {
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO employee (emp_id, name, age, email, department) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setInt(1, Integer.parseInt(emp_id));
            pstmt.setString(2, name);
            pstmt.setInt(3, Integer.parseInt(ageStr));
            pstmt.setString(4, email);
            pstmt.setString(5, department);
            pstmt.executeUpdate();
            showInfo("Success", "Employee registered successfully.");
        } catch (SQLException e) {
            showError("Registration Error", "Error registering employee.");
        }
    }

    @FXML
    private void viewEmployees() {
        Connection conn = getConnection();
        if (conn == null) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM employee");
            StringBuilder result = new StringBuilder("Employee List:\n");

            while (rs.next()) {
                result.append("ID: ").append(rs.getInt("emp_id"))
                      .append(", Name: ").append(rs.getString("name"))
                      .append(", Age: ").append(rs.getInt("age"))
                      .append(", Email: ").append(rs.getString("email"))
                      .append(", Department: ").append(rs.getString("department"))
                      .append("\n");
            }

            showInfo("Employees", result.toString());
        } catch (SQLException e) {
            showError("Query Error", "Error retrieving employee list.");
        }
    }

    @FXML
    private void updateEmployee() {
        String emp_id = tfEmployeeID.getText();
        String name = tfName.getText();
        String ageStr = tfAge.getText();
        String email = tfEmail.getText();
        String department = tfDepartment.getText();

        if (emp_id.isEmpty()) {
            showError("Validation Error", "Employee ID must be provided.");
            return;
        }

        Connection conn = getConnection();
        if (conn == null) {
            return;
        }

        StringBuilder query = new StringBuilder("UPDATE employee SET ");
        boolean hasFieldsToUpdate = false;

        if (!name.isEmpty()) {
            query.append("name = '").append(name).append("', ");
            hasFieldsToUpdate = true;
        }

        if (!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                query.append("age = ").append(age).append(", ");
                hasFieldsToUpdate = true;
            } catch (NumberFormatException ex) {
                showError("Validation Error", "Age must be a valid number.");
                return;
            }
        }

        if (!email.isEmpty()) {
            query.append("email = '").append(email).append("', ");
            hasFieldsToUpdate = true;
        }

        if (!department.isEmpty()) {
            query.append("department = '").append(department).append("', ");
            hasFieldsToUpdate = true;
        }

        if (!hasFieldsToUpdate) {
            showError("Update Error", "No fields to update.");
            return;
        }

        // Remove the last comma and space
        query.delete(query.length() - 2, query.length());
        query.append(" WHERE emp_id = ").append(emp_id);

        try (Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(query.toString());
            if (rowsAffected > 0) {
                showInfo("Success", "Employee updated successfully.");
            } else {
                showError("Update Error", "Employee not found.");
            }
        } catch (SQLException e) {
            showError("Update Error", "Error updating employee.");
        }
    }
}
