#Selenuim page capture
A utility which stores the page as seen by the WebDriver.

## Features
- **Debugging**, the ability to reload the page to inspect HTML changes.
- **Quick feedback loop **, eliminates the need to recreate the environment and steps.

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

###TODO
1. Check that we dont over write files in content folder. // Doesnt overwrite files unless test html files
2. Try against other sites
3. imporve on test to include a jetty loader. 
4. Update documentation