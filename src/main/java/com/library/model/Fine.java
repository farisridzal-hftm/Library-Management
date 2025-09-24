package com.library.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.Objects;

public class Fine {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Member> member = new SimpleObjectProperty<>();
    private final ObjectProperty<Loan> loan = new SimpleObjectProperty<>();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final StringProperty reason = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> paidDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("Outstanding");
    private final StringProperty description = new SimpleStringProperty();

    public Fine() {
        this.issueDate.set(LocalDate.now());
    }

    public Fine(int id, Member member, Loan loan, double amount, String reason) {
        this();
        setId(id);
        setMember(member);
        setLoan(loan);
        setAmount(amount);
        setReason(reason);
    }

    // ID Property
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // Member Property
    public ObjectProperty<Member> memberProperty() { return member; }
    public Member getMember() { return member.get(); }
    public void setMember(Member member) { this.member.set(member); }

    // Loan Property
    public ObjectProperty<Loan> loanProperty() { return loan; }
    public Loan getLoan() { return loan.get(); }
    public void setLoan(Loan loan) { this.loan.set(loan); }

    // Amount Property
    public DoubleProperty amountProperty() { return amount; }
    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }

    // Reason Property
    public StringProperty reasonProperty() { return reason; }
    public String getReason() { return reason.get(); }
    public void setReason(String reason) { this.reason.set(reason); }

    // Issue Date Property
    public ObjectProperty<LocalDate> issueDateProperty() { return issueDate; }
    public LocalDate getIssueDate() { return issueDate.get(); }
    public void setIssueDate(LocalDate issueDate) { this.issueDate.set(issueDate); }

    // Paid Date Property
    public ObjectProperty<LocalDate> paidDateProperty() { return paidDate; }
    public LocalDate getPaidDate() { return paidDate.get(); }
    public void setPaidDate(LocalDate paidDate) { 
        this.paidDate.set(paidDate);
        if (paidDate != null) {
            setStatus("Paid");
        }
    }

    // Status Property
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    // Description Property
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public boolean isPaid() {
        return "Paid".equals(getStatus());
    }

    public boolean isOutstanding() {
        return "Outstanding".equals(getStatus());
    }

    public void markAsPaid() {
        setPaidDate(LocalDate.now());
        setStatus("Paid");
    }

    public void waive() {
        setStatus("Waived");
        setPaidDate(LocalDate.now());
    }

    public String getMemberName() {
        return getMember() != null ? getMember().getFullName() : "Unknown";
    }

    public String getLoanInfo() {
        if (getLoan() != null) {
            return getLoan().getMediaTitle() + " (Loan #" + getLoan().getId() + ")";
        }
        return "No associated loan";
    }

    public String getFormattedAmount() {
        return String.format("€%.2f", getAmount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fine fine = (Fine) o;
        return getId() == fine.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Fine #" + getId() + " - " + getFormattedAmount() + " (" + getStatus() + ")";
    }
}