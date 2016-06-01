# Dont-Stop-Me-Now
Second project of SDIS (Distributed Systems), at MIEIC (Integrated Master's in Informatics and Computer Engineering), Faculty of Engineering of the University of Porto.

Client-server system for the users to register, consult and review street events such as speed cameras, traffic stops, heavy traffic or crashes.

Server:
- Java application providing a HTTPS connection with a REST API.
- Database implemented in PostgreSQL
- Database backup system to improve fault-tolerance and database consistency:
  - Every 10 seconds, the main server backs up the database to a secondary server, which may be in a different computer
  - The secondary server uses a heartbeat mechanism to keep checking if the main server is alive
  - If the main server goes down, the secondary server takes his position as primary, starting to reply to user requests.
  - When the former primary server comes back online, he becomes the secondary server, starting to receive backup data from the new primary server
  - Uses Facebook Graph API to perform user authentication and Google Firebase Cloud Messaging to send event notifications to users (every time a new event is created, all "subscribed" users receive a push notification)
  
Client:
- Android application with a responsive and intuitive design, that uses the server's API to get and create information
- Uses Google Places API for users to input a new event's location
- Allows users to consult the most recent events and subsribe/unsubsribe event notifications (Google Firebase Cloud Messaging)
- Uses the Facebook API to login users and send their current session token to login with the main server.
