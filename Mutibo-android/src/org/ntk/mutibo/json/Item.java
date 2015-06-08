package org.ntk.mutibo.json;

import java.util.List;

import com.google.common.base.Objects;

/**
 * A simple object to represent a movie that's defined by a title, director, year and its optional screenshot
 */
public class Item {

	private long id;

	private String name;
	
	private String author;
	
	private int year;

	private String image;
	
	private ItemType type;
	
	private long externalId;

    private List<String> contributors;

    private List<String> genres;

	public Item() {
	}


	public Item(String name, String author, int year, String image) {
		super();
		this.name = name;
		this.author = author;
		this.year = year;
		this.image = image;
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


//	public Timestamp getCreated() {
//		return created;
//	}
//
//
//	public void setCreated(Timestamp created) {
//		this.created = created;
//	}
//
//
//	public Timestamp getUpdated() {
//		return updated;
//	}
//
//
//	public void setUpdated(Timestamp updated) {
//		this.updated = updated;
//	}


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
