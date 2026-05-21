# 🌍 Worldtura Information - Server Side

Travel planning can be overwhelming, with information scattered across multiple websites and
platforms, making it hard to find reliable tips and personalized recommendations. Our project
aims to create an interactive and collaborative web application that centralizes travel information while encouraging community contributions. Users explore destinations through an
interactive world map, they can create and share travel boards, plan itineraries, and discover popular or highly rated places.

## Technologies Used

These are the technologies we used for the project: 

- Java: primary language
- JPA: mapping java objects to database tables, H2 in-memory database during development and testing for data storage
- Gradle: build tool to manage dependencies, compile the code and run the tests
- Spring Boot: framework for building REST APIs
- SonarCloud: code quality and test coverage
- Docker: containerization and ensuring the project runs the same way across local development, CI, and production environments
- Google Cloud App Engine: deployment and hosting

## High-Level Components

### Saved Places Services
The backend manages all operations related to users’ saved places. Places are categorised according to the categories provided by the  [Google Places API](https://developers.google.com/maps/documentation/places/web-service/overview).

The server communicates with the database to:

- Retrieve saved places for authenticated users
- Organise places into their respective categories
- Remove places from the user’s saved list
- Maintain persistent storage of user-specific location data

These services ensure that saved places remain synchronised across the application and accessible from the frontend interface.

### Travel Boards Services
The Travel Boards backend component manages the creation, retrieval, and collaboration features related to trip collections. The server handles travel board data, including board names, locations, optional date ranges, and privacy settings (PRIVATE, FRIENDS, PUBLIC).

The backend is responsible for:

- Creating and updating travel boards
- Managing board membership and permissions
- Handling invite and join flows through generated codes, notifications, and friend invitations
- Persisting places associated with each board
- Processing board deletion, renaming, and member removal actions

Additionally, the server validates access permissions to ensure that only authorised users can view or modify boards according to their privacy settings and membership roles.

### Friend Request Services
The Friend Request backend component manages friend requests and friendships between users.

The server communicates with the database to:

- Send friend requests between users
- Retrieve pending friend requests
- Accept or decline friend requests
- Manage users’ friend lists
- Remove friends from users’ friend lists

These services ensure that only authorised users can manage friendship-related actions.

## Launch & Deployment 
To be able to work on this directory locally, developers should first clone the repository using either HTTPS or SSH:

**HTTPS**: 
```bash
git clone https://github.com/chawlamuskan/sopra-fs26-group-33-server.git
```
**SSH**: 
```bash
git clone git@github.com:chawlamuskan/sopra-fs26-group-33-server.git
```

### Build
Build the project using Gradle: 

```bash
./gradlew build
```
This compiles the code and resolves all dependencies. 

### Run
Start the Spring Boot server locally: 

```bash
./gradlew bootRun
```
You can verify that the server is running by visiting `localhost:8080` in your browser.

### Test
Run the automated test suite using: 

```bash
./gradlew test
```
This executes all unit and integration tests defined in the project. 

### Committing changes
The project followed a branch workflow. Each team member worked on a dedicated branch and pushed changes there first. Once a feature or fix was completed, a pull request (PR) was created to merge the changes into the main branch.
All pull requests were reviewed by at least one other team member before being merged, ensuring code quality and consistency across the project. Deployment needs to be manually triggered.

Typical workflow: 

- Moving to own branch
```bash
git checkout your-branch-name
```
- State changes
```bash
git add .
```
- Commit changes
```bash
git commit -m “Description of changes”
```
- Pushing branch to remote

```bash
git push origin your-branch-name
```


## Roadmaps

This section outlines the top features that future contributors could implement to extend the functionality of the project. These features were planned but could not be completed due to time constraints. 

### Posts
A key planned feature is a Posts system, where users can share their travel experiences in a social format.

Users would be able to:

- Write posts about their trips or experiences
- Associate a post with a specific travel board or place
- Share travel stories with the community
- Comment on other posts
- Save posts to read later

These posts would then be integrated into the platform by:

- Displaying them in the dedicated Posts page
- Displaying them in the Community page together with popular travel boards
- Showing them in map pop-ups linked to countries or specific places

This feature would significantly enhance the social aspect of the application.

### Reviews, Ratings, and Personalised Recommendations
While the application currently uses Google Maps ratings for places, a future improvement would be to implement an internal review and rating system.

This would allow users to:

- Leave detailed reviews for places
- Rate locations within the application itself
- Contribute to a community-driven feedback system

In addition, these ratings could be used to:
- Improve place recommendations
- Provide personalised suggestions based on user preferences and community feedback

This would make the platform more dynamic and tailored to individual users.

### Itinerary mode in the travel boards
Another planned enhancement is an Itinerary Mode within travel boards.
Currently, users can collect places in a board. In itinerary mode, users would be able to:

- Organise selected places by specific travel dates
- Structure their trip in a day-by-day format
- Mark places as visited using checkmarks

This feature would transform travel boards from simple collections into fully structured trip planners.


## Authors and Acknowledgements

### Team members

- [Muskan Chawla](https://github.com/chawlamuskan)
- [Nadia Pandolfo](https://github.com/nadiapan4)
- [Despoina Pantazi](https://github.com/despoinapantazi)
- [Nina Jael Savas](https://github.com/nsavas8)
- [Yalini Sivapathasundaram](https://github.com/yalini-siva)

### Acknowledgements

The authors would like to thank the SOPRA team for their help during the semester, and the group that provided feedback during beta testing.


## License


This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
