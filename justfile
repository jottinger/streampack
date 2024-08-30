ri IMAGE:
  docker container ps -f "name={{IMAGE}}" -q | xargs docker stop && \
  docker images -f "reference=*{{IMAGE}}*" -q | xargs docker rmi -f
