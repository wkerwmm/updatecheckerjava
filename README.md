# UpdateChecker

UpdateChecker is a lightweight and reusable utility that checks whether a newer version of a plugin/software is available by fetching version information from a remote source.

## Features
- ✅ **Asynchronous Execution** (Prevents blocking the main thread)
- ✅ **Customizable Callback** (Define how you want to handle updates)
- ✅ **Lightweight and Fast** (Minimal resource usage)
- ✅ **Reusable for Any Project** (Not limited to Bukkit/Spigot)

## Installation
1. Add `UpdateChecker.java` to your project.
2. Ensure you have `OkHttp` library in your dependencies.
3. Implement the update check in your code.

## Usage
```java
UpdateChecker updateChecker = new UpdateChecker(
    "1.0.0",
    "https://example.com/latest-version.txt",
    (current, latest) -> {
        if (!current.equals(latest)) {
            System.out.println("A new update is available: " + latest);
        } else {
            System.out.println("You are using the latest version.");
        }
    }
);
updateChecker.checkForUpdate();
