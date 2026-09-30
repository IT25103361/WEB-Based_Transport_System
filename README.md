# WEB-Based_Transport_System
NEXT GO web based transport system

# Where to add your files
All paths below start from the repository root, which is the folder containing pom.xml, mvnw and src.
Keep each feature inside its own module folders in the existing project. Do not upload a separate IntelliJ project, another src folder inside a module, or a ZIP containing your work.

| Module | Java source | JSP pages | CSS, JavaScript and images |
| --- | --- | --- | --- |
| Ride booking | `src/main/java/com/webtrans/ridebook/` | `src/main/webapp/WEB-INF/jsp/ride/` | Use `src/main/resources/static/ride/` for new external assets. |
| School transport | `src/main/java/com/webtrans/school/` | `src/main/webapp/WEB-INF/jsp/school/` | `src/main/resources/static/school/` |

# Other modules
Agree on one lowercase module name before adding your feature. Replace <module> in the following paths with that name; do not create a folder literally named <module>.

| File type | Location |
| --- | --- |
| Controllers | `src/main/java/com/webtrans/<module>/controller/` |
| Entities / models | `src/main/java/com/webtrans/<module>/model/` |
| Repositories | `src/main/java/com/webtrans/<module>/repository/` |
| Services | `src/main/java/com/webtrans/<module>/service/` |
| Request and response DTOs | `src/main/java/com/webtrans/<module>/dto/` |
| Module exceptions | `src/main/java/com/webtrans/<module>/exception/` |
| Utility classes | `src/main/java/com/webtrans/<module>/util/` |
| JSP pages | `src/main/webapp/WEB-INF/jsp/<module>/` |
| CSS | `src/main/resources/static/<module>/css/` |
| JavaScript | `src/main/resources/static/<module>/js/` |
| Images | `src/main/resources/static/<module>/assets/images/` |
| Icons | `src/main/resources/static/<module>/assets/icons/` |
| Tests | `src/test/java/com/webtrans/<module>/` |

# Branches and shared files
main contains the integrated project. Each member submits their module on their assigned branch, then opens a pull request into main.

| Member / branch | Confirmed module |
| --- | --- |
| `IT25103361` | Ride booking |
| `IT25101086` | School transport |

Other member branches are IT25100281, IT25101543, IT25102355 and IT25103030. Each member should use their assigned branch and the agreed folder name for their feature. Branch names identify members; source folders identify modules.
These files are shared across the application. Coordinate changes to them with the team:
- pom.xml — project dependencies and build settings.
- src/main/java/com/webtrans/WebTransApplication.java — application entry point.
- src/main/resources/application.properties — shared application configuration.
- src/main/webapp/WEB-INF/jsp/ride/home.jsp — home page and links to features.
Use the existing application entry point and Maven project. Do not add a separate pom.xml or another Spring Boot application class for each module.

# Before submitting your module
- Include the Java classes, JSP pages, assets and supporting resources needed by your feature.
- Check that your changes do not remove or overwrite another member's files.
- Add the feature's home-page link in coordination with the team.
- Check that the project builds and that your pages, assets and database operations work.
- Keep SQL passwords, personal connection settings and local-only debug settings out of commits.
- Leave out ZIP backups, target/, compiled .class files and personal IDE files.
Database records are stored in SQL Server, not in the Git repository. Each member needs a working local database connection to run the project.
