package org.ntk.mutibo.repository;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * A simple object to represent a movie that's defined by a title, director, year and its optional screenshot
 */
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "author")
    private String author;

    @Column(name = "year")
    private int year;

    @Column(name = "image")
    private String image;

    @Column(name = "itemType")
    private ItemType type;

    @Column(name = "Ext_ID")
    private long externalId;

    @ElementCollection
    @CollectionTable(name = "contributors", joinColumns = @JoinColumn(name = "Contributor_ID"))
    private List<String> contributors;

    @ElementCollection
    @CollectionTable(name = "genres", joinColumns = @JoinColumn(name = "Genre_ID"))
    private List<String> genres;

    public Item() {
    }

    public Item(String name, String author, int year, String image) {
        super();
        this.name = name;
        this.author = author;
        this.year = year;
        this.image = image;
        this.type = ItemType.UNDEFINED;
    }

    public Item(String name, String author, int year, String image, long externalId, ItemType type) {
        this(name, author, year, image);
        this.externalId = externalId;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public long getExternalId() {
        return externalId;
    }

    public void setExternalId(long externalId) {
        this.externalId = externalId;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public void addContributor(String contributor) {
        if (contributors == null)
            contributors = Lists.newArrayList();
        contributors.add(contributor);
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    /**
     * Two Movies will generate the same hashcode if they have exactly the same values for their name, url, and
     * duration.
     * 
     */
    @Override
    public int hashCode() {
        // Google Guava provides great utilities for hashing
        return Objects.hashCode(name, author, year);
    }

    /**
     * Two Movies are considered equal if they have exactly the same values for their name, url, and duration.
     * 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            Item other = (Item) obj;
            // Google Guava provides great utilities for equals too!
            return Objects.equal(name, other.name) && Objects.equal(author, other.author) && year == other.year;
        } else {
            return false;
        }
    }

}
