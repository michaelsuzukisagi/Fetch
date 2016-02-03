#Fetch
A utility which stores the page as seen by the WebDriver.

## Features
- **Debugging** provides the ability to reload the page and inspect HTML changes.
- **Quick feedback loop** removes the need to recreate the environment and steps to reach the point of failure. 

## Use Maven
Import the project
```
<dependency>
    <groupId>org.alfresco</groupId>
    <artifactId>fetch</artifactId>
    <version>1.0</version>
</dependency>
```
## Usage
```
WebDriver driver = new FireFoxDriver();
FetchUtil.save(driver, "mytest.html");
```
To see the html output, open with browser:
```
./target/public/mytest.html
```
## Project layout
```
.
├── /pom.xml                    # Project meta data and build information.
├── /src/                       # Source location.
|   ├── main                    # Main code.
|   ├── test                    # Test code.
├── /target/                    # Project output location.
│   ├── /public/                # The output directory of the utility.
│   │   ├── mytest.html         # The HTML that is captured by the utility.
│   │   ├── /content/           # The directory which contains all the retrieved assets(js, css and images).
```
