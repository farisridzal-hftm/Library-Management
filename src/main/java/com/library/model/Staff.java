package com.library.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.Objects;

public class Staff {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty position = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> hireDate = new SimpleObjectProperty<>();
    private final DoubleProperty salary = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty("Active");
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty("Librarian");

    public Staff() {
        this.hireDate.set(LocalDate.now());
    }

    public Staff(int id, String firstName, String lastName, String email, String phone, 
                String position, String department, String username, String role) {
        this();
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhone(phone);
        setPosition(position);
        setDepartment(department);
        setUsername(username);
        setRole(role);
    }

    // ID Property
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // First Name Property
    public StringProperty firstNameProperty() { return firstName; }
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }

    // Last Name Property
    public StringProperty lastNameProperty() { return lastName; }
    public String getLastName() { return lastName.get(); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }

    // Full Name getter
    public String getFullName() {
        String firstName = getFirstName() != null ? getFirstName() : "";
        String lastName = getLastName() != null ? getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    // Email Property
    public StringProperty emailProperty() { return email; }
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }

    // Phone Property
    public StringProperty phoneProperty() { return phone; }
    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }

    // Position Property
    public StringProperty positionProperty() { return position; }
    public String getPosition() { return position.get(); }
    public void setPosition(String position) { this.position.set(position); }

    // Department Property
    public StringProperty departmentProperty() { return department; }
    public String getDepartment() { return department.get(); }
    public void setDepartment(String department) { this.department.set(department); }

    // Hire Date Property
    public ObjectProperty<LocalDate> hireDateProperty() { return hireDate; }
    public LocalDate getHireDate() { return hireDate.get(); }
    public void setHireDate(LocalDate hireDate) { this.hireDate.set(hireDate); }

    // Salary Property
    public DoubleProperty salaryProperty() { return salary; }
    public double getSalary() { return salary.get(); }
    public void setSalary(double salary) { this.salary.set(salary); }

    // Status Property
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    // Username Property
    public StringProperty usernameProperty() { return username; }
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }

    // Role Property
    public StringProperty roleProperty() { return role; }
    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }

    public boolean isActive() {
        return "Active".equals(getStatus());
    }

    public boolean isAdmin() {
        return "Administrator".equals(getRole()) || "Manager".equals(getRole());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff staff = (Staff) o;
        return getId() == staff.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getFullName() + " (" + getPosition() + ")";
    }
}