echo "REMOVE ALL DOCKER TEST CONTAINERS (Re-cleaning)"
echo "-- Is a duplicate if the deletion is unsuccessful in the test itself --"

docker kill $(docker ps --format "{{.Names}}" | grep "itest") | xargs docker rm

exit 0