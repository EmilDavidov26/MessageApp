A real-time Android chat application designed for gamers to connect based on shared gaming interests.

<img src="app/screenshots/ic_game_finder_logo.PNG" alt="logo" width="150">

## Features

### Authentication & Profile
- Firebase email/password authentication
- Profile customization with avatar upload
- Bio/description editing
- Select favorite games from RAWG API
- Real-time online status tracking

### Social
- Friend requests system
- User search by game preferences
- User profiles with game interests
- Online/offline status with last seen

### Communication
- Real-time private messaging
- Group chats with member management
- Message read receipts
- Real-time updates
- Message timestamps

## Tech Stack

### Frontend
- Native Android (Java)
- Material Design Components
- CircleImageView
- ViewPager2
- RecyclerView

### Backend & APIs
- Firebase
  - Authentication
  - Realtime Database
  - Cloud Storage
- RAWG Gaming API
  - Game search
  - Metadata retrieval

## Prerequisites
- Android Studio
- JDK 11+
- Android SDK API 34
- Firebase account
- RAWG API key

## Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/game-connect.git
```

2. Firebase Setup
- Create a new Firebase project
- Add Android app to your project
- Download `google-services.json`
- Place in app directory

3. RAWG API Setup
- Get API key from [RAWG](https://rawg.io/apidocs)
- Add to `GameRepository.java`

4. Enable Firebase Services
- Authentication (Email/Password)
- Realtime Database
- Cloud Storage

5. Build and run
```bash
./gradlew build
```

## Security

- Strong password requirements:
  - 8+ characters
  - Uppercase & lowercase
  - Numbers
  - Special characters
  - Secure Firebase rules
  - Connection state management

## Screenshots

### Login Page  
<img src="app/screenshots/Screenshot_20250210_145334.png" alt="login" width="300">

### Sign-up Page  
<img src="app/screenshots/Screenshot_20250210_145452.png" alt="register" width="300">

### Change Email/Password Page  
<img src="app/screenshots/Screenshot_20250210_145712.png" alt="changeemailorpassword" width="300">

### Users (Main Tab)  
<img src="app/screenshots/Screenshot_20250210_145511.png" alt="users" width="300">

### Filter Users by Games Page  
<img src="app/screenshots/Screenshot_20250210_145722.png" alt="filter" width="300">

### Private Chat  
<img src="app/screenshots/Screenshot_20250210_145700.png" alt="privatechat" width="300">

### Create a Group Page  
<img src="app/screenshots/Screenshot_20250210_145745.png" alt="creategroup" width="300">

### Group Chat Page  
<img src="app/screenshots/Screenshot_20250210_145732.png" alt="groupchat" width="300">


## Acknowledgments

- [Firebase](https://firebase.google.com).
- [RAWG API](https://rawg.io/apidocs).
- [Material Design](https://material.io).
