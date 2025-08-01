# 🪵 LogFactory Android

**LogFactory** is a lightweight and customizable logging library for Android. Supports Android 5 (API 21) and above, with scoped storage compatibility (Android 10+).

---

## 🚀 Features

- ✅ Write logs to a file.
- ✅ Send logs via email (SMTP).
- ✅ Output logs to Android Logcat
- ✅ Customizable log formatting.
- ✅ Scoped Storage support for Android 10+.
- ✅ Auto-clear log file.
- ✅ Easy integration and configuration.

---

## 📦 Installation

### Step 1: Add JitPack to your root `build.gradle`

<pre><code>allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
</code></pre>

### Step 2: Add the dependency

<pre><code>dependencies {
    implementation 'com.github.sali1290:LogFactory:$logfactory_version'
}
</code></pre>

---

## 🛠️ Usage

### 1. Initialize the logger (e.g., in your `Application` class):

<pre><code>val emailLogger = EmailLogger(
    config = EmailLoggerConfig(
        thresholdType = ThresholdType.Counter,
        logCountThreshold = 3,
        senderEmail = "sender@gmail.com",
        senderPassword = "sender password",
        recipientEmail = "recepient@gmail.com",
        )
    )

val fileLogger = FileLogger(config = FileLoggerConfig())
val logcatLogger = LogcatLogger()
LogFactory.configureLoggers(
    context = this,
    enabledLoggers = arrayOf(fileLogger, emailLogger, logcatLogger)
)
</code></pre>

### 2. Log messages anywhere in your app:

<pre><code>
LogFactory.log(
    logType = LogType.Info, // Specify the type of the log you are using
    tag = "Log tag",
    message = "Log message"
)
</code></pre>

### 3. Optional: Use a custom formatter

<pre><code>class MyCustomFormatter : LogFormatter {
    override fun format(entry: LogEntry): String {
        return "[${entry.level}] ${entry.tag}: ${entry.message}" // Example format
    }
}
</code></pre>

---

## 🔐 Permissions

### Android < 10 (API 29)

Add this to your `AndroidManifest.xml`:

<pre><code>&lt;uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
  android:maxSdkVersion="28"/&gt;
</code></pre>

Request the permission at runtime:

<pre><code>if (Build.VERSION.SDK_INT &lt; Build.VERSION_CODES.Q && 
  ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        REQUEST_CODE
    )
}
</code></pre>

### Android 10+

No special permissions are required. The library uses `MediaStore` and scoped storage APIs.

---

## ⚠️ Security

🔑 **Do NOT hard-code your email password or app-specific password directly in your source code.**
Instead, store sensitive information in a more secure way, for example:
- Encrypted SharedPreferences
- Android Keystore

---

## 📁 Default Log File Location

Default behaviour saves logs in the `Downloads` folder. Visible to users and file managers.

---

## 🧪 Pro Tips

- 💾 Set `clearFileWhenAppLaunched = true` to remove old logs at app launch.
- 🐛 Add throwable to `LogFactory.log` to detect bugs more easily.

---

## 👨‍💻 Contributing

Feel free to open issues or pull requests. Suggestions, improvements, and bug reports are welcome!
