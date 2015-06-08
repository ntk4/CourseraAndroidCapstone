package org.ntk.mutibo.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.common.base.Objects;

/**
 * The liked user object
 */
@Entity
public class LikedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "localUser")
    private String localUser;

    // @ManyToOne(fetch = FetchType.LAZY, targetEntity = ItemSet.class)
    // @JoinColumn(name = "SetID", nullable = false)
    private long setId;

    @Column(name = "liked")
    private boolean liked;

    @Column(name = "disliked")
    private boolean disliked;

    public LikedUser() {
    }

    public LikedUser(String localUser, long setId, boolean liked, boolean disliked) {
        super();
        this.localUser = localUser;
        this.setId = setId;
        this.liked = liked;
        this.disliked = disliked;
    }

    /**
     * Two Liked Users will generate the same hashcode if they have exactly the same values for their localUser and
     * itemSet.
     * 
     */
    @Override
    public int hashCode() {
        // Google Guava provides great utilities for hashing
        return Objects.hashCode(localUser, setId);
    }

    /**
     * Two Movie Sets are considered equal if they have exactly the same values for their name, url, and duration.
     * 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LikedUser) {
            LikedUser other = (LikedUser) obj;
            // Google Guava provides great utilities for equals too!
            return Objects.equal(localUser, other.localUser) && Objects.equal(setId, other.setId);
        } else {
            return false;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocalUser() {
        return localUser;
    }

    public void setLocalUser(String localUser) {
        this.localUser = localUser;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isDisliked() {
        return disliked;
    }

    public void setDisliked(boolean disliked) {
        this.disliked = disliked;
    }

}
