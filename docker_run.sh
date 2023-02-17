set -e

docker stop auth || true
./gradlew dockerBuild
docker run --rm -d \
  --name auth \
  --network host \
  -v $PWD/service/local.conf:/app/local.conf \
  -e SERVICE_OPTS='-Dconfig.file=/app/local.conf' \
  juggernaut0/auth:SNAPSHOT $@
