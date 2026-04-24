// package ch.uzh.ifi.hase.soprafs26.entity;

// import jakarta.persistence.*;

// import java.io.Serializable;
		

// @Entity
// @Table(name = "places")
// public class Place implements Serializable {
//     @Id
// 	@GeneratedValue
// 	private Long id;
    
//     @Column(nullable = false)
//     private String name;

//     @Column(nullable = false)
//     private Double latitude;

//     @Column(nullable = false)
//     private Double longitude;


//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public String getName() {
//         return name;
//     }

//     public void setName(String name) {
//         this.name = name;
//     }


//     public Double getLatitude() {
//         return latitude;
//     }

//     public void setLatitude(Double latitude) {
//         this.latitude = latitude;
//     }

//     public Double getLongitude() {
//         return longitude;
//     }

//     public void setLongitude(Double longitude) {
//         this.longitude = longitude;
//     }

// }