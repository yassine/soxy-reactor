#!/bin/sh
docker events --filter "label=${NODE_LABEL}" --filter "label=${NAMESPACE_KEY}=${NAMESPACE}" --filter 'type=container' --filter 'event=start' --format='json' | while read event
do
  echo "detected node service startup : $event"
  consul_host=${CONSUL_HOST}
  container_id=`echo $event | jq '.id'`
  container_name=`docker inspect --format='{{.Name}}' ${container_id}`
  ip_address=`docker inspect --format='{{.NetworkSettings.IPAddress}}' ${container_id}`
  port=1080
  service_key=`docker inspect --format="{{ index .Config.Labels \"$SERVICE_KEY_LABEL\" }}" $container_id`
  request="{
  \"ID\": \"$container_name\",
  \"Name\": \"$service_key\",
  \"Tags\": [
    \"$service_key\"
  ],
  \"Address\": \"$ip_address\",
  \"Port\": 1080,
  \"Meta\": {},
  \"EnableTagOverride\": false,
  \"Check\": {
    \"DockerContainerId\": \"$container_id\",
    \"Shell\": \"/bin/sh\",
    \"Args\": [\"/etc/scripts/health-check.sh\"],
    \"Interval\": \"60s\",
    \"Timeout\":\"10s\"
  }
}";
  `echo ${request} | curl -v -H "Content-Type: application/json" -X PUT -d @- http://${CONSUL_HOST}:${CONSUL_PORT}/agent/service/register`
done
