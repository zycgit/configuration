call mvn clean release:clean release:prepare -P release
call mvn clean deploy -P release
pause