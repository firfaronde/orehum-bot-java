./gradlew jar
docker build -t big-bob .
docker stop big-bob
docker rm big-bob
