Image Classifier Microservice
--
A microservice for classifying images.

For more information see [my blog](https://fxapps.blogspot.com/2018/05/a-java-microservice-for-image.html).

## Building

First to build this project you will need to make sure you have the following repository in `settings.xml`
~~~
<profile>
      <id>oos-sonatype</id>
      <repositories>
        <repository>
          <id>oos-sonatype</id>
          <name>OOS Sonatype</name>
          <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
          <layout>default</layout>
          <releases>
            <enabled>false</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>daily</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
                <id>oos-sonatype-plugins</id>
                <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
                <releases>
                        <enabled>false</enabled>
                </releases>
                <snapshots>
                        <enabled>true</enabled>
                </snapshots>
        </pluginRepository>
      </pluginRepositories>
</profile>
~~~

Then: `mvn clean install` - it take a while for the first time becuase it download a lot of stuff, including the model bin. After that all the run will be faster :).
If you want to build a docker image make sure you have Docker installed and then run:

~~~
mvn docker:build
~~~
