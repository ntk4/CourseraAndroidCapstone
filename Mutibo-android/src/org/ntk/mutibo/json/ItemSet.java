package org.ntk.mutibo.json;

import java.util.List;

import com.google.common.base.Objects;

/**
 * The movie set object
 */
public class ItemSet {

	private long id;

	private String name;
	
	private List<Item> items;
	
	private int differentItemId;

	private long likes;
	
	private long dislikes;

	private List<LikedUser> usersLiked;
	
	private String explanation;

	private boolean inactive;
	
	private int difficulty;

	public ItemSet() {
	}

	public ItemSet(String name, List<Item> items, int differentItemId, long likes, long dislikes,
			List<LikedUser> usersLiked) {
		super();
		this.name = name;
		this.items = items;
		this.differentItemId = differentItemId;
		this.likes = likes;
		this.dislikes = dislikes;
		this.usersLiked = usersLiked;
	}
	
	   public ItemSet(int id, String name, List<Item> items, int differentItemId, long likes, long dislikes,
	            List<LikedUser> usersLiked) {
	       this(name, items, differentItemId, likes, dislikes, usersLiked);
	       this.id = id;
	    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getLikes() {
		return likes;
	}

	public void setLikes(long likes) {
		this.likes = likes;
	}

	public List<LikedUser> getUsersLiked() {
		return usersLiked;
	}

	public void setUsersLiked(List<LikedUser> usersLiked) {
		this.usersLiked = usersLiked;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public int getDifferentItemId() {
		return differentItemId;
	}

	public void setDifferentItemId(int differentItemId) {
		this.differentItemId = differentItemId;
	}

	public long getDislikes() {
		return dislikes;
	}

	public void setDislikes(long dislikes) {
		this.dislikes = dislikes;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * Two Item Sets will generate the same hashcode if they have exactly the same values for their name, url, and
	 * duration.
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(name);
	}

	/**
	 * Two Item Sets are considered equal if they have exactly the same values for their name, url, and duration.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ItemSet) {
			ItemSet other = (ItemSet) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(name, other.name);
		} else {
			return false;
		}
	}
	

	public boolean isAnswerCorrect(int answer) {
		return answer == getDifferentItemId();
	}

}
