package com.library.model;

import javafx.beans.property.*;

import java.util.Objects;

public class Author {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty biography = new SimpleStringProperty();
    private final StringProperty nationality = new SimpleStringProperty();

    public Author() {}

    public Author(int id, String firstName, String lastName, String biography, String nationality) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setBiography(biography);
        setNationality(nationality);
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

    // Biography Property
    public StringProperty biographyProperty() { return biography; }
    public String getBiography() { return biography.get(); }
    public void setBiography(String biography) { this.biography.set(biography); }

    // Nationality Property
    public StringProperty nationalityProperty() { return nationality; }
    public String getNationality() { return nationality.get(); }
    public void setNationality(String nationality) { this.nationality.set(nationality); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return getId() == author.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getFullName();
    }
}