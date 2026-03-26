package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
		

@Entity
@Table(name = "places")
public class Place implements Serializable {
    @Id
	@GeneratedValue
	private Long id;

    // To be done!
}