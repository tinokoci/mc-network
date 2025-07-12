# mc-infrastructure
This is all of backend related software created and maintained for the Exemine minecraft server.
Source code was leaked and sold without my permission so I decided to open source it.

### Important Stack Components
- [/-spigot](https://github.com/execets/exe-spigot) - Our modified version of the spigot server
- [MongoDB](https://www.mongodb.com/) - NoSQL document oriented database
- [Redis](https://redis.io/) - Cache and message broker for communication between servers
- [Lombok](https://projectlombok.org/) - Annotation library that removes a lot of boilerplate code
- [Gson](https://github.com/google/gson) - For converting objects to json and vice versa
- [JDA](https://github.com/DV8FromTheWorld/JDA) - Java discord API wrapper
- [Spring](https://spring.io/) - Backend java web framework

### Installation
We use gradle to build the project and manage all dependencies.  
To compile the project, you can use either `build` to compile or `install` to compile and deploy to your local maven repository:

- Linux / macOS
```sh
./gradlew build
```
- Windows
```sh
gradlew.bat build
```
