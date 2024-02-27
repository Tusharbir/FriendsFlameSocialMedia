# 🌟 Friends Flame: Your Social Media Android App 🌟

Welcome to **Friends Flame**, the vibrant social media application that lets you connect, share, and express yourself like never before! Built with ❤️ in Kotlin and powered by Firebase, Social Spark is your go-to platform for creating lasting memories with friends and family.

## 🚀 Features

Social Spark comes packed with a plethora of features that make your social media experience seamless and fun:

- 📸 **Posting Images:** Capture or upload your favorite moments and share them with your world.
- 👫 **Adding Friends:** Connect with your friends and expand your social circle.
- 💾 **Saving Posts:** Bookmark those special posts to revisit them anytime.
- ❤️ **Liking Posts:** Show your appreciation for the content you love with a heart.
- 💬 **Commenting on Posts:** Engage in conversations and share your thoughts on various posts.
- 🔑 **Signing in with Google and Facebook:** Quick and easy access using your favorite social accounts.
- 🚑 Crash reporting with Crashlytics

### 📣 Important Notice for Facebook Users

As per the latest guidelines by Meta, only users with a developer account linked to the app can use the Facebook login feature. Ensure your account is correctly set up to enjoy uninterrupted access.

### Prerequisites
    - Android Studio
    - A Firebase account
    - A Facebook Developer account

## 🛠 How to Get Started

Ready to dive into the world of Social Spark? Follow these simple steps to get your app up and running:

### Step 1: Clone the Repository

Start by cloning the app repository to your local machine using:

```bash
git clone https://github.com/yourusername/social-spark.git
```

###Step 2: Set Up Firebase

    2.1 Head over to the Firebase Console.
    2.2 Create a new project and follow the setup instructions.
    2.3 Download the google-services.json file.
    2.4 Place the downloaded file into the app/ directory of your Android Studio project. 
    2.5 Once your project is ready, navigate to the project settings.

![image](https://github.com/Tusharbir/FriendsFlameSocialMedia/assets/109094205/e7905d7e-cbd9-4d68-8745-4855c3cd71f5)


#### Step 3: Add SHA-1 and SHA-256 Fingerprints
To make Google Sign-In work, you'll need to add your app's SHA-1 and SHA-256 fingerprints to your Firebase project:

    1. Generate your SHA-1 and SHA-256 fingerprints using Android Studio or your preferred method.
    2. In your Firebase project settings, add these fingerprints under your app's settings.

#### Step 4: Configure Google Sign-In
    1. In the Firebase Console, enable Google Sign-In method in the Authentication section.
    2. Copy the Web Client id from there
    2. Download the `google-services.json` file and add it to your app's `app` directory.

![image](https://github.com/Tusharbir/FriendsFlameSocialMedia/assets/109094205/5481d7dd-36b3-4621-831a-a83d0a56133a)




#### Step 5: Set Up Facebook Login
Setting up Facebook Login involves a few more steps:

    1. Visit the Facebook Developers site and create a new app.
    2. Add the Facebook Login product to your app.
    3. In the settings, add your Android package name and default class.
    4. Add your Facebook App ID and Client Token to your project's strings resource file.
    5. Configure the Facebook SDK in your project as per the Facebook developer documentation.

### Step 6: Add Both Google WebClient and Facebook Client in the string resources:
    app/src/main/res/values/strings.xml
```kotlin
    <string name="web_client_id"></string>
    <string name="facebook_app_id"></string>
    <string name="fb_login_protocol_scheme"></string>
    <string name="facebook_client_token"></string>
```

⚠️ **Note:** Due to the latest rules by Meta, the developer account created for Facebook can only log in. Make sure your Facebook Developer settings are configured accordingly.
