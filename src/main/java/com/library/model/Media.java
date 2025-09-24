package com.library.model;

import javafx.beans.property.*;

import java.util.Objects;

public class Media {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty isbn = new SimpleStringProperty();
    private final IntegerProperty publishYear = new SimpleIntegerProperty();
    private final StringProperty publisher = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty("Book");
    private final IntegerProperty totalCopies = new SimpleIntegerProperty(1);
    private final IntegerProperty availableCopies = new SimpleIntegerProperty(1);
    private final StringProperty location = new SimpleStringProperty();
    private final ObjectProperty<Author> author = new SimpleObjectProperty<>();
    private final ObjectProperty<Category> category = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty language = new SimpleStringProperty("German");

    public Media() {}

    public Media(int id, String title, String isbn, int publishYear, String publisher, String type, 
                int totalCopies, String location, Author author, Category category) {
        setId(id);
        setTitle(title);
        setIsbn(isbn);
        setPublishYear(publishYear);
        setPublisher(publisher);
        setType(type);
        setTotalCopies(totalCopies);
        setAvailableCopies(totalCopies);
        setLocation(location);
        setAuthor(author);
        setCategory(category);
    }

    // ID Property
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // Title Property
    public StringProperty titleProperty() { return title; }
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    // ISBN Property
    public StringProperty isbnProperty() { return isbn; }
    public String getIsbn() { return isbn.get(); }
    public void setIsbn(String isbn) { this.isbn.set(isbn); }

    // Publish Year Property
    public IntegerProperty publishYearProperty() { return publishYear; }
    public int getPublishYear() { return publishYear.get(); }
    public void setPublishYear(int publishYear) { this.publishYear.set(publishYear); }

    // Publisher Property
    public StringProperty publisherProperty() { return publisher; }
    public String getPublisher() { return publisher.get(); }
    public void setPublisher(String publisher) { this.publisher.set(publisher); }

    // Type Property
    public StringProperty typeProperty() { return type; }
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }

    // Total Copies Property
    public IntegerProperty totalCopiesProperty() { return totalCopies; }
    public int getTotalCopies() { return totalCopies.get(); }
    public void setTotalCopies(int totalCopies) { 
        this.totalCopies.set(totalCopies);
        if (getAvailableCopies() > totalCopies) {
            setAvailableCopies(totalCopies);
        }
    }

    // Available Copies Property
    public IntegerProperty availableCopiesProperty() { return availableCopies; }
    public int getAvailableCopies() { return availableCopies.get(); }
    public void setAvailableCopies(int availableCopies) { this.availableCopies.set(availableCopies); }

    // Location Property
    public StringProperty locationProperty() { return location; }
    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); }

    // Author Property
    public ObjectProperty<Author> authorProperty() { return author; }
    public Author getAuthor() { return author.get(); }
    public void setAuthor(Author author) { this.author.set(author); }

    // Category Property
    public ObjectProperty<Category> categoryProperty() { return category; }
    public Category getCategory() { return category.get(); }
    public void setCategory(Category category) { this.category.set(category); }

    // Description Property
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    // Language Property
    public StringProperty languageProperty() { return language; }
    public String getLanguage() { return language.get(); }
    public void setLanguage(String language) { this.language.set(language); }

    public boolean isAvailable() {
        return getAvailableCopies() > 0;
    }

    public void borrowCopy() {
        if (isAvailable()) {
            setAvailableCopies(getAvailableCopies() - 1);
        }
    }

    public void returnCopy() {
        if (getAvailableCopies() < getTotalCopies()) {
            setAvailableCopies(getAvailableCopies() + 1);
        }
    }

    public String getAuthorName() {
        return getAuthor() != null ? getAuthor().getFullName() : "Unknown";
    }

    public String getCategoryName() {
        return getCategory() != null ? getCategory().getName() : "Uncategorized";
    }

    public int getLoanDurationDays() {
        if (getCategory() != null) {
            return getCategory().getLoanDurationDays();
        }
        return switch (getType()) {
            case "DVD", "CD" -> 7;
            case "Magazine" -> 3;
            default -> 14;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Media media = (Media) o;
        return getId() == media.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getTitle() + " by " + getAuthorName();
    }
}