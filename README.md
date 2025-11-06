# 📱 TaskMaster - Modern Task Management App

A beautiful, feature-rich Android task management application built with Kotlin, Firebase, and Material Design 3.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white)

## ✨ Features

### 🔐 Authentication & Security
- **User Registration & Login** with Firebase Authentication
- **Single Sign-On (SSO)** with Google Sign-In
- **Secure Password Management** with encrypted storage
- **Forgot Password** functionality with email reset
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
- **Task Sharing** via email, messaging, or other apps
- **Task Duplication** for quick task creation
- **Task Export** as formatted text

### 🎨 User Interface
- **Modern Material Design 3** with beautiful gradient backgrounds
- **Dark/Light Theme** support with automatic switching
- **Multi-language Support** (English, Afrikaans, Zulu)
- **Responsive Design** for all screen sizes
- **Smooth Animations** and transitions
- **Professional Visual Design** with floating elements

### ⚙️ Settings & Customization
- **Language Switching** with instant updates (English, Afrikaans, isiZulu)
- **Theme Toggle** (Light/Dark mode)
- **Notification Preferences** with enable/disable toggles
- **Privacy Policy & Terms** with WebView integration
- **User Profile** management with photo uploads
- **Account Settings** and preferences

### 🔔 Notifications & Alerts
- **Real-Time Push Notifications** for task events
- **Due Date Reminders** with customizable timing
- **Task Completion Notifications**
- **High Priority Task Alerts**
- **Notification History** with filtering (All, Unread, Important)
- **Unread Badge Counter** on home screen

### 💾 Offline & Sync
- **Full Offline Functionality** using Room database
- **Automatic Synchronization** when online
- **Conflict Resolution** for offline edits
- **Sync Status Indicators** for each task
- **Offline-First Architecture** for fast performance

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
| Home Screen                               | Task Details                                             | Settings                                          |
|-------------------------------------------|----------------------------------------------------------|---------------------------------------------------|
| ![Light Home](screenshots/light_home.jpg) | ![Light Task Details](screenshots/light_task_details.jpg) | ![Light Settings](screenshots/light_settings.jpg) |

### Dark Mode
| Home Screen                             | Task Details                                            | Settings                                       |
|-----------------------------------------|---------------------------------------------------------|------------------------------------------------|
| ![Dark Home](screenshots/dark_home.jpg) | ![Dark Task Details](screenshots/dark_task_details.jpg) | ![Dark Settings](screenshots/dark_settings.jpg) |

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

The app includes comprehensive automated testing for core functionality:

### Unit Tests
- **Task Management**: CRUD operations, data validation, serialization
- **User Authentication**: Firebase integration, error handling
- **Data Models**: Task and Notification class testing
- **Repository Pattern**: Firebase Firestore operations
- **Edge Cases**: Invalid inputs, network failures, data corruption

### Test Coverage
- ✅ Task creation, updating, deletion
- ✅ User authentication flows
- ✅ Data validation and error handling
- ✅ Firebase integration testing
- ✅ UI component testing
- ✅ Notification system testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test classes
./gradlew test --tests TaskTest
./gradlew test --tests TaskRepositoryTest

# Generate test report
./gradlew testDebugUnitTest
```

### GitHub Actions Testing
- **Automated Testing**: Runs on every push/PR
- **Multi-Environment**: Tests on different Android versions
- **Build Verification**: Ensures code compiles and runs
- **Test Reports**: Generated and stored as artifacts

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

## 🤖 AI Tools Usage

This project utilized AI assistance for development and debugging:

**AI Tools Used:**
- **Claude Sonnet 4** for code generation, debugging, and architectural guidance
- **GitHub Copilot** for code completion and suggestions
- **ChatGPT** for initial project planning and documentation

**Specific AI Contributions:**
- Firebase integration setup and configuration
- Material Design 3 implementation and theming
- Unit test creation and debugging
- GitHub Actions CI/CD pipeline configuration
- Code optimization and error resolution
- Documentation and README enhancement

**Code Examples:**
```kotlin
// AI-assisted Firebase repository implementation
class TaskRepository(private val firestore: FirebaseFirestore) {
    suspend fun addTask(task: Task): Result<Task> {
        return try {
            firestore.collection(COLLECTION_TASKS)
                .document(task.id)
                .set(task)
                .await()
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**AI Citation:** All AI-generated code has been reviewed, tested, and integrated following best practices. AI tools were used as development assistants, not as primary code generators.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Team Members

- Sufyaan Cassim | ST10304152
- Mukethwa Susan Mukhoro | ST10400833
- Sijongokuhle Kungawo Jikijela | ST10374043
- Ntokoza Mayisela | ST10270987

## 📺 Demo Video

[🎥 Watch the complete app demonstration video](https://youtu.be/u1rUb_OXvaQ)

- ✅ User registration and login process
- ✅ Single Sign-On (SSO) with Google
- ✅ Password encryption demonstration
- ✅ Settings management and customization
- ✅ Firebase database connection and data storage
- ✅ Task management features (CRUD operations)
- ✅ Offline mode with automatic synchronization
- ✅ Real-time push notifications
- ✅ Multi-language support (English, Afrikaans, isiZulu)
- ✅ Theme switching (Light/Dark mode)
- ✅ Forgot password functionality
- ✅ Professional voice-over explanation

---

## 📝 Release Notes

### Version 2.0.0 - Final POE Release

**🎉 Major New Features:**

#### 🔐 Single Sign-On (SSO) with Google
- One-tap Google Sign-In integration
- Seamless authentication experience
- Automatic account creation for new users

#### 💾 Offline Mode with Automatic Synchronization
- Full offline functionality using Room database
- Automatic sync when connectivity is restored
- Smart conflict resolution
- Sync status indicators

#### 🔔 Real-Time Push Notification System
- Task completion notifications
- Due date reminders
- High priority task alerts
- Notification preferences and filtering
- Unread badge counter

#### 🌍 Multi-Language Support
- Complete UI translation for English, Afrikaans, and isiZulu
- Dynamic language switching
- Over 200+ translated strings
- Localized date/time formats

#### 🔑 Forgot Password Functionality
- Secure password reset via email
- Custom branded email template
- User-friendly validation and error handling

**🐛 Bug Fixes:**
- Fixed app crashes during task operations
- Fixed notification persistence issues
- Fixed language switching updates
- Fixed offline sync conflicts

**🔧 Technical Improvements:**
- Comprehensive code comments and logging
- Lifecycle-aware coroutines
- Enhanced error handling
- Improved architecture patterns

**📚 Documentation:**
- Enhanced README with comprehensive documentation
- Code comments throughout codebase
- Detailed release notes

For complete release notes, see [RELEASE_NOTES.md](RELEASE_NOTES.md)

---

**Built with ❤️ for modern task management**

*This project was developed as part of an academic assessment demonstrating modern Android development practices, Firebase integration, and professional software engineering skills.*