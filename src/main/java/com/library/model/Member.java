package com.library.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.Objects;

public class Member {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> birthDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("Active");
    private final IntegerProperty maxLoans = new SimpleIntegerProperty(5);
    private final IntegerProperty currentLoans = new SimpleIntegerProperty(0);
    private final ObjectProperty<LocalDate> memberSince = new SimpleObjectProperty<>();

    public Member() {
        this.memberSince.set(LocalDate.now());
    }

    public Member(int id, String firstName, String lastName, String email, String phone, String address, LocalDate birthDate) {
        this();
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhone(phone);
        setAddress(address);
        setBirthDate(birthDate);
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

    // Address Property
    public StringProperty addressProperty() { return address; }
    public String getAddress() { return address.get(); }
    public void setAddress(String address) { this.address.set(address); }

    // Birth Date Property
    public ObjectProperty<LocalDate> birthDateProperty() { return birthDate; }
    public LocalDate getBirthDate() { return birthDate.get(); }
    public void setBirthDate(LocalDate birthDate) { this.birthDate.set(birthDate); }

    // Status Property
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    // Max Loans Property
    public IntegerProperty maxLoansProperty() { return maxLoans; }
    public int getMaxLoans() { return maxLoans.get(); }
    public void setMaxLoans(int maxLoans) { this.maxLoans.set(maxLoans); }

    // Current Loans Property
    public IntegerProperty currentLoansProperty() { return currentLoans; }
    public int getCurrentLoans() { return currentLoans.get(); }
    public void setCurrentLoans(int currentLoans) { this.currentLoans.set(currentLoans); }

    // Member Since Property
    public ObjectProperty<LocalDate> memberSinceProperty() { return memberSince; }
    public LocalDate getMemberSince() { return memberSince.get(); }
    public void setMemberSince(LocalDate memberSince) { this.memberSince.set(memberSince); }

    public boolean isActive() {
        return "Active".equals(getStatus());
    }

    public boolean canBorrow() {
        return isActive() && getCurrentLoans() < getMaxLoans();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return getId() == member.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getFullName() + " (ID: " + getId() + ")";
    }
}