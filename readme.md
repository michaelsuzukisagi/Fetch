#Selenuim page capture
A utility which stores the page as seen by the WebDriver.

## Features
- **Debugging** provides the ability to reload the page to inspect HTML changes.
- **Quick feedback loop** removed the need to recreate the environment and steps.

## Use Maven
Import the project
```
<dependency>
    <groupId>org.alfresco</groupId>
    <artifactId>savepage</artifactId>
    <version>1.0</version>
</dependency>
```
## Usage
```
WebDriver driver = new FireFoxDriver();
SavePageUtil.save(driver, "mytest.html");
```
To see the html output, open with browser:
```
./target/public/mytest.html
```
## Project layout

.
├── /pom.xml                    # Project meta data and build information.
├── /src/                       # Source location.
|   ├── main                    # Main code.
|   ├── test                    # Test code.
├── /target/                    # Project output location.
│   ├── /public/                # The directory which all content is stored to.

