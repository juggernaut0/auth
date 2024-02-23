set -e

docker network create my-net || true
docker network connect my-net test-db --alias test-db || true
docker stop auth || true
./gradlew dockerBuild
docker run --rm -d \
  --name auth \
  -p 9001:9001 \
  --network my-net \
  -v $PWD/service/local.conf:/app/local.conf \
  -e SERVICE_OPTS='-Dconfig.file=/app/local.conf' \
  -e DB_HOST=test-db \
  juggernaut0/auth:SNAPSHOT $@
