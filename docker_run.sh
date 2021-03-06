set -e

./gradlew dockerBuild
docker run -it --rm \
  --name auth \
  --network host \
  -v $PWD/service/local.conf:/app/local.conf \
  -e SERVICE_OPTS='-Dconfig.file=/app/local.conf' \
  auth:SNAPSHOT $@
