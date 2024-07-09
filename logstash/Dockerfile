# Use an ARM64 compatible base image for Logstash
FROM docker.elastic.co/logstash/logstash:8.2.3

# Install required plugins
RUN logstash-plugin install logstash-output-opensearch