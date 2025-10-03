# 📱 TaskMaster - Modern Task Management App

A beautiful, feature-rich Android task management application built with Kotlin, Firebase, and Material Design 3.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)

## ✨ Features

### 🔐 Authentication & Security
- **User Registration & Login** with Firebase Authentication
- **Secure Password Management** with encrypted storage
- **Profile Management** with photo uploads
- **Password Change** functionality with re-authentication

### 📋 Task Management
- **Create, Edit, Delete Tasks** with full CRUD operations
- **Priority System** (High, Medium, Low) with color coding
- **Due Dates & Times** with intuitive date/time pickers
- **Task Descriptions** and detailed information
- **Reminder System** with customizable notifications
- **Smart Filtering** (All, Pending, Completed, Today)
- **Task Status Management** (Complete/Pending toggle)

### 🎨 User Interface
- **Modern Material Design 3** with beautiful gradient backgrounds
- **Dark/Light Theme** support with automatic switching
- **Multi-language Support** (English, Afrikaans, Zulu)
- **Responsive Design** for all screen sizes
- **Smooth Animations** and transitions
- **Professional Visual Design** with floating elements

### 🔔 Notifications
- **Real-time Notifications** based on task events
- **Smart Filtering** (All, Unread, Important)
- **Mark as Read** functionality
- **Bulk Operations** (Clear All notifications)
- **Persistent Notification State** across app sessions

### ⚙️ Settings & Customization
- **Language Switching** with instant updates
- **Theme Toggle** (Light/Dark mode)
- **Privacy Policy & Terms** with WebView integration
- **User Profile** management with photo uploads
- **Account Settings** and preferences

## 🛠️ Technical Stack

- **Language:** Kotlin
- **UI Framework:** Android Views with Material Design 3
- **Backend:** Firebase (Authentication, Firestore, Analytics)
- **Architecture:** MVVM with Repository Pattern
- **Dependencies:**
  - Firebase SDK (Auth, Firestore, Analytics)
  - Material Design Components 3
  - Gson for JSON serialization
  - Kotlin Coroutines for async operations
  - Navigation Component
  - SharedPreferences for local storage

## 📱 Screenshots

### Light Mode
| Home Screen | Task Details | Settings |
|-------------|--------------|----------|
| ![Light Home](screenshots/light_home.png) | ![Light Task Details](screenshots/light_task_details.png) | ![Light Settings](screenshots/light_settings.png) |

### Dark Mode
| Home Screen | Task Details | Settings |
|-------------|--------------|----------|
| ![Dark Home](screenshots/dark_home.png) | ![Dark Task Details](screenshots/dark_task_details.png) | ![Dark Settings](screenshots/dark_settings.png) |

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Firebase project setup
- Java 8 or higher

### Installation
1. Clone the repository
```bash
git clone https://github.com/Sufyaan-Cassim/TaskMaster-Android-App.git
cd TaskMaster-Android-App
```

2. Open in Android Studio
3. Add your Firebase configuration file (`google-services.json`) to the `app/` directory
4. Sync project with Gradle files
5. Build and run the project

## 🔧 Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Authentication and Firestore Database
3. Download `google-services.json` from Project Settings
4. Place it in the `app/` directory
5. Configure Firestore security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read all tasks
    match /tasks/{document} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

## 🧪 Testing

The app includes comprehensive testing for core functionality:
- Task management operations (CRUD)
- User authentication flows
- Data validation and error handling
- UI component testing
- Firebase integration testing

Run tests with:
```bash
./gradlew test
```

## 📊 GitHub Actions

This project uses GitHub Actions for:
- Automated testing on every push
- Code quality checks
- Build verification
- Deployment automation

## 🏗️ Project Structure

```
app/
├── src/main/java/za/co/rosebankcollege/st10304152/taskmaster/
│   ├── data/                    # Data models and repositories
│   │   ├── Task.kt
│   │   ├── Notification.kt
│   │   └── TaskRepository.kt
│   ├── ui/                      # UI fragments and activities
│   │   ├── HomeFragment.kt
│   │   ├── SettingsFragment.kt
│   │   ├── NotificationsFragment.kt
│   │   └── ...
│   └── adapter/                 # RecyclerView adapters
│       ├── TaskAdapter.kt
│       └── NotificationAdapter.kt
├── src/main/res/               # Resources
│   ├── layout/                 # XML layouts
│   ├── drawable/               # Icons and backgrounds
│   ├── values/                 # Strings, colors, themes
│   └── navigation/             # Navigation graphs
└── google-services.json        # Firebase configuration
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Make your changes
4. Add tests if applicable
5. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
6. Push to the branch (`git push origin feature/AmazingFeature`)
7. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Team Mebers

- Sufyaan Cassim | ST10304152
- Mukethwa Susan Mukhoro | ST10400833
- Sijongokuhle Kungawo Jikijela | ST10374043
- Ntokoza Mayisela | ST10270987

## 📺 Demo Video

[🎥 Watch the complete app demonstration video](https://youtube.com/watch?v=YOUR_VIDEO_ID)

*The video showcases all features including authentication, task management, notifications, settings, and theme switching.*

## 🙏 Acknowledgments

- Firebase team for excellent backend services
- Material Design team for beautiful UI components
- Android team for the amazing platform
- Open source community for inspiration and tools
- All contributors and testers

## 📈 Future Enhancements

- [ ] Task categories and tags
- [ ] Team collaboration features
- [ ] Advanced analytics and reporting
- [ ] Widget support for home screen
- [ ] Offline mode with sync
- [ ] Voice notes for tasks
- [ ] Calendar integration

---

**Built with ❤️ for modern task management**

*This project was developed as part of an academic assessment demonstrating modern Android development practices, Firebase integration, and professional software engineering skills.*
