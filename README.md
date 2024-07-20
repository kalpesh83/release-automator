# Release Automator
Create beta and prod release changes to respective branches hence reducing the manual effort for release process.

## Usage
Download the `jar` from the package url then execute the below command:
```shell
java -jar file_name.jar args-list
```

Arguments list:
* **remote** - The origin url of the repository
* **branch** - The branch on which the code is to be executed. Values much be one of these: `main`, `release-v*` or `play-release-v*`