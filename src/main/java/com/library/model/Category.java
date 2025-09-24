package com.library.model;

import javafx.beans.property.*;

import java.util.Objects;

public class Category {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty loanDurationDays = new SimpleIntegerProperty(14);

    public Category() {}

    public Category(int id, String name, String description, int loanDurationDays) {
        setId(id);
        setName(name);
        setDescription(description);
        setLoanDurationDays(loanDurationDays);
    }

    // ID Property
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // Name Property
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // Description Property
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    // Loan Duration Days Property
    public IntegerProperty loanDurationDaysProperty() { return loanDurationDays; }
    public int getLoanDurationDays() { return loanDurationDays.get(); }
    public void setLoanDurationDays(int loanDurationDays) { this.loanDurationDays.set(loanDurationDays); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return getId() == category.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}