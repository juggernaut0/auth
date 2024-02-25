set -e

docker network create my-net || true
docker network connect my-net test-db --alias test-db || true
docker rm -f auth || true
./gradlew jibDockerBuild
docker run -d \
  --name auth \
  -p 9001:9001 \
  --network my-net \
  -v $PWD/service/local.conf:/app/local.conf \
  -e JAVA_TOOL_OPTIONS='-Dconfig.file=/app/local.conf' \
  -e DB_HOST=test-db \
  auth:SNAPSHOT $@
