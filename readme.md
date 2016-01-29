###Selenuim page capture
Utility which stores the page as seen by the webdriver.
Scarping the page should help with debugging and inspecting the page at future date.
Screen shots are effective but do not reveal any code changes in the UI, having
a page scrape should help identify any changes in the HTML or CSS.
### Get the code
Git:
 git clone https://github.com/michaelsuzukisagi/savepage.git

### Use Maven
Import the project
```
<dependency>
    <groupId>org.alfresco</groupId>
    <artifactId>savepage</artifactId>
    <version>1.0</version>
</dependency>
```

###TODO
1. Check that we dont over write files in content folder.
2. Remove hard coded values.
3. Try against other sites
4. imporve on test to include a jetty loader. 
