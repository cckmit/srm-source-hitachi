FROM open-registry.going-link.com/zhenyun/javabase-slim:0.1.0
COPY app.jar /srm-source.jar
RUN curl -o /srm-agent-call-chain.jar https://nexus.going-link.com/repository/zhenyun-release/org/srm/srm-agent-call-chain/0.2.0.RELEASE/srm-agent-call-chain-0.2.0.RELEASE.jar
ENTRYPOINT exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xmx$(($(cat sys/fs/cgroup/memory/memory.limit_in_bytes) * 50 / 100 / 1024 /1024))M $JAVA_OPTS $APM_OPTS -javaagent:/srm-agent-call-chain.jar -jar /srm-source.jar