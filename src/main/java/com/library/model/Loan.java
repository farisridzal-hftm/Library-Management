package com.library.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Loan {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Member> member = new SimpleObjectProperty<>();
    private final ObjectProperty<Media> media = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> loanDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> returnDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("Active");
    private final IntegerProperty renewalCount = new SimpleIntegerProperty(0);
    private final IntegerProperty maxRenewals = new SimpleIntegerProperty(2);
    private final StringProperty notes = new SimpleStringProperty();

    public Loan() {}

    public Loan(int id, Member member, Media media, LocalDate loanDate) {
        setId(id);
        setMember(member);
        setMedia(media);
        setLoanDate(loanDate);
        calculateDueDate();
    }

    // ID Property
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // Member Property
    public ObjectProperty<Member> memberProperty() { return member; }
    public Member getMember() { return member.get(); }
    public void setMember(Member member) { this.member.set(member); }

    // Media Property
    public ObjectProperty<Media> mediaProperty() { return media; }
    public Media getMedia() { return media.get(); }
    public void setMedia(Media media) { 
        this.media.set(media);
        calculateDueDate();
    }

    // Loan Date Property
    public ObjectProperty<LocalDate> loanDateProperty() { return loanDate; }
    public LocalDate getLoanDate() { return loanDate.get(); }
    public void setLoanDate(LocalDate loanDate) { 
        this.loanDate.set(loanDate);
        calculateDueDate();
    }

    // Due Date Property
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public LocalDate getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDate dueDate) { this.dueDate.set(dueDate); }

    // Return Date Property
    public ObjectProperty<LocalDate> returnDateProperty() { return returnDate; }
    public LocalDate getReturnDate() { return returnDate.get(); }
    public void setReturnDate(LocalDate returnDate) { 
        this.returnDate.set(returnDate);
        if (returnDate != null) {
            setStatus("Returned");
        }
    }

    // Status Property
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    // Renewal Count Property
    public IntegerProperty renewalCountProperty() { return renewalCount; }
    public int getRenewalCount() { return renewalCount.get(); }
    public void setRenewalCount(int renewalCount) { this.renewalCount.set(renewalCount); }

    // Max Renewals Property
    public IntegerProperty maxRenewalsProperty() { return maxRenewals; }
    public int getMaxRenewals() { return maxRenewals.get(); }
    public void setMaxRenewals(int maxRenewals) { this.maxRenewals.set(maxRenewals); }

    // Notes Property
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }

    public boolean isOverdue() {
        return "Active".equals(getStatus()) && getDueDate() != null && LocalDate.now().isAfter(getDueDate());
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(getDueDate(), LocalDate.now());
    }

    public boolean canRenew() {
        return "Active".equals(getStatus()) && getRenewalCount() < getMaxRenewals() && !isOverdue();
    }

    public boolean renew() {
        if (!canRenew()) return false;
        
        setRenewalCount(getRenewalCount() + 1);
        LocalDate newDueDate = getDueDate().plusDays(getMedia().getLoanDurationDays());
        setDueDate(newDueDate);
        return true;
    }

    public void returnMedia() {
        setReturnDate(LocalDate.now());
        setStatus("Returned");
    }

    private void calculateDueDate() {
        if (getLoanDate() != null && getMedia() != null) {
            setDueDate(getLoanDate().plusDays(getMedia().getLoanDurationDays()));
        }
    }

    public String getMemberName() {
        return getMember() != null ? getMember().getFullName() : "Unknown";
    }

    public String getMediaTitle() {
        return getMedia() != null ? getMedia().getTitle() : "Unknown";
    }

    public double calculateFine() {
        if (!isOverdue()) return 0.0;
        
        long daysOverdue = getDaysOverdue();
        double finePerDay = 0.50;
        double maxFine = 10.0;
        
        return Math.min(daysOverdue * finePerDay, maxFine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return getId() == loan.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Loan #" + getId() + " - " + getMediaTitle() + " by " + getMemberName();
    }
}